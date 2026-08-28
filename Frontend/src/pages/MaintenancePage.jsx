import { useState } from 'react';
import { Button } from '../components/common/Button';
import { Card } from '../components/common/Card';
import { Icon } from '../components/common/Icon';
import { IconActionButton } from '../components/common/IconActionButton';
import { Modal } from '../components/common/Modal';
import { PageHeader } from '../components/common/PageHeader';
import { StatCard } from '../components/common/StatCard';
import { StatusBadge } from '../components/common/StatusBadge';
import { StatusPicker } from '../components/common/StatusPicker';
import { Table } from '../components/common/Table';
import { MaintenanceDetailsModal } from '../components/domain/MaintenanceDetailsModal';
import { MaintenanceForm } from '../components/domain/MaintenanceForm';
import { MaintenanceStatusModal } from '../components/domain/MaintenanceStatusModal';
import { resourceConfigs } from '../constants/resourceConfigs';
import { useResource } from '../hooks/useResource';
import { assetService, employeeService, maintenanceService, vendorService } from '../services/resources';
import { collectionItems, recordId } from '../utils/collections';

const config = resourceConfigs.maintenance;
const statusOptions = ['OPEN', 'IN_PROGRESS', 'ON_HOLD', 'COMPLETED', 'CANCELLED'].map((value) => ({ value, label: value.replace('_', ' ') }));
const emptyLookups = { status: 'idle', assets: [], vendors: [], employees: [], error: null };
const displayDate = (item) => item ? new Date(`${item}T00:00:00`).toLocaleDateString() : '—';

export function MaintenancePage() {
  const resource = useResource(() => maintenanceService.list(), 'maintenance');
  const [formState, setFormState] = useState({ open: false, ticket: null });
  const [lookups, setLookups] = useState(emptyLookups);
  const [details, setDetails] = useState(null);
  const [deleteTicket, setDeleteTicket] = useState(null);
  const [statusModal, setStatusModal] = useState({ ticket: null, status: null });
  const [mutation, setMutation] = useState({ status: 'idle', error: null });
  const rows = collectionItems(resource.data);
  const stats = config.stats.map((stat) => ({
    ...stat,
    value: resource.status !== 'success'
      ? '—'
      : stat.label === 'Total cost'
        ? `Rs ${rows.reduce((sum, row) => sum + Number(row.cost || 0), 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
        : stat.getValue(rows),
  }));

  const columns = [
    {
      key: 'ticketNumber', label: 'Ticket', value: (row) => row.ticketNumber || '', render: (row) => (
        <span className="asset-tag-cell"><IconActionButton icon="info" className="asset-info-action" label={`View ${row.ticketNumber || 'maintenance ticket'}`} onClick={() => openDetails(row)} /><span>{row.ticketNumber || '—'}</span></span>
      ),
    },
    { key: 'assetTag', label: 'Asset tag', value: (row) => row.assetTag || '' },
    { key: 'assetName', label: 'Asset Name', value: (row) => row.assetName || '' },
    { key: 'issue', label: 'Issue', value: (row) => row.issue || '' },
    { key: 'priority', label: 'Priority', value: (row) => row.priority || '', render: (row) => <StatusBadge status={row.priority} /> },
    { key: 'startDate', label: 'Start date', value: (row) => row.startDate || '', render: (row) => displayDate(row.startDate) },
    { key: 'dueDate', label: 'Due date', value: (row) => row.dueDate || '', render: (row) => displayDate(row.dueDate) },
    {
      key: 'status', label: 'Status', value: (row) => row.status || '', render: (row) => (
        <StatusPicker value={row.status} options={statusOptions} label={`Change status for ${row.ticketNumber}`} disabled={mutation.status === 'loading'} onChange={(status) => openStatusChange(row, status)} />
      ),
    },
  ];

  async function loadLookups() {
    setLookups({ ...emptyLookups, status: 'loading' });
    try {
      const [assetData, vendorData, employeeData] = await Promise.all([assetService.list(), vendorService.list(), employeeService.list()]);
      setLookups({ status: 'success', assets: collectionItems(assetData), vendors: collectionItems(vendorData), employees: collectionItems(employeeData), error: null });
    } catch (error) {
      setLookups({ ...emptyLookups, status: 'error', error });
    }
  }

  async function openForm(ticket = null) {
    setMutation({ status: 'idle', error: null });
    setFormState({ open: true, ticket });
    await loadLookups();
  }

  async function openDetails(ticket) {
    setDetails(ticket);
    try {
      setDetails(await maintenanceService.get(recordId(ticket, 'id')));
    } catch {
      // The list row already contains enough data to keep the details view useful.
    }
  }

  async function saveTicket(payload) {
    setMutation({ status: 'loading', error: null });
    try {
      if (formState.ticket) await maintenanceService.update(recordId(formState.ticket, 'id'), payload);
      else await maintenanceService.create(payload);
      setMutation({ status: 'success', error: null });
      setFormState({ open: false, ticket: null });
      resource.reload();
    } catch (error) {
      setMutation({ status: 'error', error });
    }
  }

  async function changeStatus(payload) {
    if (!statusModal.ticket) return;
    setMutation({ status: 'loading', error: null });
    try {
      await maintenanceService.update(recordId(statusModal.ticket, 'id'), payload);
      setMutation({ status: 'success', error: null });
      setStatusModal({ ticket: null, status: null });
      resource.reload();
    } catch (error) {
      setMutation({ status: 'error', error });
    }
  }

  function openStatusChange(ticket, status) {
    setMutation({ status: 'idle', error: null });
    setStatusModal({ ticket, status });
  }

  async function removeTicket() {
    if (!deleteTicket) return;
    setMutation({ status: 'loading', error: null });
    try {
      await maintenanceService.remove(recordId(deleteTicket, 'id'));
      setMutation({ status: 'success', error: null });
      setDeleteTicket(null);
      resource.reload();
    } catch (error) {
      setMutation({ status: 'error', error });
    }
  }

  const closeForm = () => mutation.status !== 'loading' && setFormState({ open: false, ticket: null });

  return (
    <div>
      <PageHeader title={config.title} description={config.description} breadcrumbs={[{ label: config.title }]} actions={<><Button variant="outline" icon="download">Export</Button><Button icon="plus" onClick={() => openForm()}>Add ticket</Button></>} />
      <div className="stats-grid">{stats.map((stat) => <StatCard key={stat.label} {...stat} />)}</div>
      <Card className="table-wrap">
        <Table columns={columns} rows={rows} status={resource.status} error={resource.error} onRetry={resource.reload} emptyTitle="No maintenance tickets yet" emptyDescription="Create a ticket to start tracking service work." searchPlaceholder="Search maintenance..." rowActionsLabel="Action" rowActions={(row) => <div className="table-action-group"><IconActionButton icon="edit" label={`Edit ${row.ticketNumber}`} tone="edit" onClick={() => openForm(row)} /><IconActionButton icon="trash" label={`Delete ${row.ticketNumber}`} tone="danger" onClick={() => { setMutation({ status: 'idle', error: null }); setDeleteTicket(row); }} /></div>} />
      </Card>

      <Modal open={formState.open} onClose={closeForm} className="asset-modal" title={formState.ticket ? 'Edit maintenance ticket' : 'Add maintenance ticket'} description={formState.ticket ? 'Update the issue, dates, responsibility, status, and resolution.' : 'Record the maintenance issue and planned service period.'}>
        {lookups.status === 'loading' && <div className="assignment-form-loading"><div className="spinner" aria-hidden="true" /><p>Loading assets, vendors, and employees...</p></div>}
        {lookups.status === 'error' && <div className="inline-error"><Icon name="warning" size={16} />{lookups.error?.message}</div>}
        {lookups.status === 'success' && <MaintenanceForm key={recordId(formState.ticket, 'id') || 'new'} ticket={formState.ticket} assets={lookups.assets} vendors={lookups.vendors} employees={lookups.employees} submitting={mutation.status === 'loading'} error={mutation.status === 'error' ? mutation.error : null} onClose={closeForm} onSubmit={saveTicket} />}
      </Modal>

      <MaintenanceDetailsModal ticket={details} onClose={() => setDetails(null)} />
      <MaintenanceStatusModal key={`${recordId(statusModal.ticket, 'id')}-${statusModal.status}`} ticket={statusModal.ticket} status={statusModal.status} submitting={mutation.status === 'loading'} error={mutation.status === 'error' ? mutation.error : null} onClose={() => mutation.status !== 'loading' && setStatusModal({ ticket: null, status: null })} onSubmit={changeStatus} />

      <Modal open={Boolean(deleteTicket)} onClose={() => mutation.status !== 'loading' && setDeleteTicket(null)} title="Delete maintenance ticket" description="This permanently removes the ticket. The deletion itself remains recorded in the audit log.">
        <div className="confirm-copy">Delete <strong>{deleteTicket?.ticketNumber}</strong>?</div>
        {mutation.status === 'error' && <div className="inline-error"><Icon name="warning" size={16} />{mutation.error?.message}</div>}
        <div className="modal-actions"><Button variant="outline" onClick={() => setDeleteTicket(null)} disabled={mutation.status === 'loading'}>Cancel</Button><Button variant="danger" onClick={removeTicket} disabled={mutation.status === 'loading'}>{mutation.status === 'loading' ? 'Deleting...' : 'Delete ticket'}</Button></div>
      </Modal>
    </div>
  );
}
