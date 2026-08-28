import { useState } from 'react';
import { Button } from '../components/common/Button';
import { Card } from '../components/common/Card';
import { Icon } from '../components/common/Icon';
import { IconActionButton } from '../components/common/IconActionButton';
import { Modal } from '../components/common/Modal';
import { PageHeader } from '../components/common/PageHeader';
import { StatCard } from '../components/common/StatCard';
import { Table } from '../components/common/Table';
import { AssignmentForm } from '../components/domain/AssignmentForm';
import { AssignmentCloseModal } from '../components/domain/AssignmentCloseModal';
import { AssignmentEditModal } from '../components/domain/AssignmentEditModal';
import { AssetTransferModal } from '../components/domain/AssetTransferModal';
import { useAction, useResource } from '../hooks/useResource';
import { resourceConfigs } from '../constants/resourceConfigs';
import { assetService, categoryService, employeeService, locationService } from '../services/resources';
import { collectionItems, recordId } from '../utils/collections';

const config = resourceConfigs.assignments;

export function AssignmentsPage() {
  const resource = useResource(() => config.service.list(), 'list');
  const createAction = useAction(config.service.create);
  const transferAction = useAction(assetService.transfer);
  const closeAction = useAction(config.service.update);
  const editAction = useAction(config.service.update);
  const [assignmentModalOpen, setAssignmentModalOpen] = useState(false);
  const [transferAssignment, setTransferAssignment] = useState(null);
  const [closingAssignment, setClosingAssignment] = useState(null);
  const [editingAssignment, setEditingAssignment] = useState(null);
  const [editLookup, setEditLookup] = useState({ status: 'idle', employees: [], error: null });
  const [assignmentLookup, setAssignmentLookup] = useState({
    status: 'idle',
    categories: [],
    assets: [],
    employees: [],
    error: null,
  });
  const [lookup, setLookup] = useState({ status: 'idle', employees: [], locations: [], error: null });
  const rows = collectionItems(resource.data);
  const stats = config.stats.map((stat) => ({
    ...stat,
    value: resource.status === 'success' && stat.getValue ? stat.getValue(rows) : '\u2014',
  }));

  const openAssignment = async () => {
    setAssignmentModalOpen(true);
    setAssignmentLookup({ status: 'loading', categories: [], assets: [], employees: [], error: null });

    try {
      const [categoryData, assetData, employeeData] = await Promise.all([
        categoryService.list(),
        assetService.list(),
        employeeService.list({ status: 'ACTIVE' }),
      ]);
      setAssignmentLookup({
        status: 'success',
        categories: collectionItems(categoryData),
        assets: collectionItems(assetData),
        employees: collectionItems(employeeData),
        error: null,
      });
    } catch (error) {
      setAssignmentLookup({ status: 'error', categories: [], assets: [], employees: [], error });
    }
  };

  const closeNewAssignment = () => {
    if (createAction.status !== 'loading') setAssignmentModalOpen(false);
  };

  const submitAssignment = async (payload) => {
    try {
      await createAction.execute(payload);
      setAssignmentModalOpen(false);
      resource.reload();
    } catch {
      // The form displays request errors inside the modal.
    }
  };

  const openTransfer = async (assignment) => {
    setTransferAssignment(assignment);
    setLookup({ status: 'loading', employees: [], locations: [], error: null });

    try {
      const [employeeData, locationData] = await Promise.all([
        employeeService.list({ status: 'ACTIVE' }),
        locationService.list(),
      ]);
      setLookup({
        status: 'success',
        employees: collectionItems(employeeData),
        locations: collectionItems(locationData),
        error: null,
      });
    } catch (error) {
      setLookup({ status: 'error', employees: [], locations: [], error });
    }
  };

  const closeTransfer = () => {
    if (transferAction.status !== 'loading') setTransferAssignment(null);
  };

  const submitTransfer = async (payload) => {
    const assetId = recordId(transferAssignment, 'assetId');
    if (!assetId) throw new Error('This assignment record does not include an asset identifier.');

    await transferAction.execute(assetId, payload);
    setTransferAssignment(null);
    resource.reload();
  };

  const openEdit = async (assignment) => {
    setEditingAssignment(assignment);
    setEditLookup({ status: 'loading', employees: [], error: null });
    try {
      const employeeData = await employeeService.list();
      setEditLookup({ status: 'success', employees: collectionItems(employeeData), error: null });
    } catch (error) {
      setEditLookup({ status: 'error', employees: [], error });
    }
  };

  const submitClose = async (payload) => {
    try {
      await closeAction.execute(recordId(closingAssignment, 'id'), { status: 'RETURNED', ...payload });
      setClosingAssignment(null);
      resource.reload();
    } catch {
      // The close form shows the request error inside its modal.
    }
  };

  const submitEdit = async (payload) => {
    try {
      await editAction.execute(recordId(editingAssignment, 'id'), payload);
      setEditingAssignment(null);
      resource.reload();
    } catch {
      // The edit form shows the request error inside its modal.
    }
  };

  return (
    <div>
      <PageHeader
        title={config.title}
        className="assignment-page-header"
        description={config.description}
        breadcrumbs={[{ label: config.title }]}
        actions={
          <>
            <Button variant="outline" icon="download">Export</Button>
            <Button icon="plus" onClick={openAssignment}>
              {config.actionLabel}
            </Button>
          </>
        }
      />

      <div className="stats-grid">
        {stats.map((stat) => <StatCard key={stat.label} {...stat} />)}
      </div>

      <Card className="table-wrap">
        <Table
          columns={config.columns}
          rows={rows}
          status={resource.status}
          error={resource.error}
          onRetry={resource.reload}
          emptyTitle="No assignments yet"
          emptyDescription="Assignments will appear here after the first assignment."
          searchPlaceholder="Search assignments..."
          rowActionsLabel="Action"
          rowActions={(row) => {
            const active = String(row.status).toUpperCase() === 'ACTIVE';
            return (
              <div className="table-action-group">
                {active && <IconActionButton icon="check" label="Close assignment" tone="success" onClick={() => setClosingAssignment(row)} />}
                {active && <IconActionButton icon="transfer" label="Transfer asset" tone="transfer" onClick={() => openTransfer(row)} />}
                <IconActionButton icon="edit" label="Edit assignment" tone="edit" onClick={() => openEdit(row)} />
              </div>
            );
          }}
        />
      </Card>

      <Modal
        open={assignmentModalOpen}
        onClose={closeNewAssignment}
        title={config.actionLabel}
        description="Choose a category, then select a verified available asset by asset tag or model number."
      >
        {assignmentLookup.status === 'loading' && (
          <div className="assignment-form-loading">
            <div className="spinner" aria-hidden="true" />
            <p>Loading categories, available assets, and employees...</p>
          </div>
        )}
        {assignmentLookup.status === 'error' && (
          <div className="inline-error">
            <Icon name="warning" size={16} />
            {assignmentLookup.error?.message}
          </div>
        )}
        {assignmentLookup.status === 'success' && (
          <AssignmentForm
            categories={assignmentLookup.categories}
            assets={assignmentLookup.assets}
            employees={assignmentLookup.employees}
            submitting={createAction.status === 'loading'}
            actionError={createAction.status === 'error' ? createAction.error : null}
            onClose={closeNewAssignment}
            onSubmit={submitAssignment}
          />
        )}
      </Modal>

      {transferAssignment && (
        <AssetTransferModal
          key={recordId(transferAssignment, 'id') || recordId(transferAssignment, 'assetId')}
          open
          assignment={transferAssignment}
          employees={lookup.employees}
          locations={lookup.locations}
          lookupStatus={lookup.status}
          lookupError={lookup.error}
          actionStatus={transferAction.status}
          actionError={transferAction.error}
          onClose={closeTransfer}
          onSubmit={submitTransfer}
        />
      )}

      {closingAssignment && (
        <AssignmentCloseModal
          assignment={closingAssignment}
          submitting={closeAction.status === 'loading'}
          actionError={closeAction.status === 'error' ? closeAction.error : null}
          onClose={() => closeAction.status !== 'loading' && setClosingAssignment(null)}
          onSubmit={submitClose}
        />
      )}

      {editingAssignment && (
        <AssignmentEditModal
          assignment={editingAssignment}
          employees={editLookup.employees}
          submitting={editAction.status === 'loading'}
          actionError={editAction.status === 'error' ? editAction.error : editLookup.status === 'error' ? editLookup.error : null}
          onClose={() => editAction.status !== 'loading' && setEditingAssignment(null)}
          onSubmit={submitEdit}
        />
      )}
    </div>
  );
}
