import { useState } from 'react';
import { Button } from '../components/common/Button';
import { Card } from '../components/common/Card';
import { Icon } from '../components/common/Icon';
import { BackendForm, Modal } from '../components/common/Modal';
import { PageHeader } from '../components/common/PageHeader';
import { StatCard } from '../components/common/StatCard';
import { Table } from '../components/common/Table';
import { AssetTransferModal } from '../components/domain/AssetTransferModal';
import { useAction, useResource } from '../hooks/useResource';
import { resourceConfigs } from '../constants/resourceConfigs';
import { assetService, employeeService, locationService } from '../services/resources';
import { collectionItems, recordId } from '../utils/collections';

const config = resourceConfigs.assignments;

export function AssignmentsPage() {
  const resource = useResource(() => config.service.list(), 'list');
  const createAction = useAction(config.service.create);
  const transferAction = useAction(assetService.transfer);
  const [assignmentModalOpen, setAssignmentModalOpen] = useState(false);
  const [transferAssignment, setTransferAssignment] = useState(null);
  const [lookup, setLookup] = useState({ status: 'idle', employees: [], locations: [], error: null });
  const rows = collectionItems(resource.data);

  const submitAssignment = async (payload) => {
    try {
      await createAction.execute(payload);
      setAssignmentModalOpen(false);
      resource.reload();
    } catch {
      // The form displays the backend error inside the modal.
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

  return (
    <div>
      <PageHeader
        title={config.title}
        description={config.description}
        breadcrumbs={[{ label: config.title }]}
        actions={
          <>
            <Button variant="outline" icon="download">Export</Button>
            <Button icon="plus" onClick={() => setAssignmentModalOpen(true)}>
              {config.actionLabel}
            </Button>
          </>
        }
      />

      <div className="stats-grid">
        {config.stats.map((stat) => <StatCard key={stat.label} {...stat} />)}
      </div>

      <Card className="table-wrap">
        <Table
          columns={config.columns}
          rows={rows}
          status={resource.status}
          error={resource.error}
          onRetry={resource.reload}
          emptyTitle="No assignments yet"
          emptyDescription="Assignments will appear here when the backend returns them."
          searchPlaceholder="Search assignments..."
          rowActions={(row) =>
            String(row.status).toUpperCase() === 'ACTIVE' ? (
              <Button variant="outline" size="sm" icon="transfer" onClick={() => openTransfer(row)}>
                Transfer
              </Button>
            ) : (
              <span className="table-action-muted">Closed</span>
            )
          }
        />
      </Card>

      <Modal
        open={assignmentModalOpen}
        onClose={() => setAssignmentModalOpen(false)}
        title={config.actionLabel}
        description="Create an initial asset assignment through the backend API."
      >
        <BackendForm
          fields={config.fields}
          onClose={() => setAssignmentModalOpen(false)}
          onSubmit={submitAssignment}
          submitting={createAction.status === 'loading'}
        />
        {createAction.status === 'error' && (
          <div className="inline-error">
            <Icon name="warning" size={16} />
            {createAction.error?.message}
          </div>
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
    </div>
  );
}
