import { useState } from 'react';
import { Button } from '../components/common/Button';
import { Card } from '../components/common/Card';
import { Icon } from '../components/common/Icon';
import { IconActionButton } from '../components/common/IconActionButton';
import { Modal } from '../components/common/Modal';
import { PageHeader } from '../components/common/PageHeader';
import { StatCard } from '../components/common/StatCard';
import { StatusPicker } from '../components/common/StatusPicker';
import { Table } from '../components/common/Table';
import { EmployeeDetailsModal } from '../components/domain/EmployeeDetailsModal';
import { EmployeeForm } from '../components/domain/EmployeeForm';
import { resourceConfigs } from '../constants/resourceConfigs';
import { useResource } from '../hooks/useResource';
import { departmentService, employeeService, locationService } from '../services/resources';
import { collectionItems, recordId } from '../utils/collections';

const config = resourceConfigs.employees;
const editableStatuses = [
  { value: 'ACTIVE', label: 'Active' },
  { value: 'ON_LEAVE', label: 'Suspended' },
  { value: 'INACTIVE', label: 'Inactive' },
  { value: 'TERMINATED', label: 'Left' },
];

export function EmployeesPage() {
  const resource = useResource(() => employeeService.list(), 'employees');
  const [formState, setFormState] = useState({ open: false, employee: null });
  const [lookup, setLookup] = useState({ status: 'idle', branches: [], departments: [], error: null });
  const [detailsEmployee, setDetailsEmployee] = useState(null);
  const [deleteEmployee, setDeleteEmployee] = useState(null);
  const [mutation, setMutation] = useState({ status: 'idle', error: null });
  const [statusAction, setStatusAction] = useState({ status: 'idle', error: null });
  const rows = collectionItems(resource.data);
  const stats = config.stats.map((stat) => ({ ...stat, value: resource.status === 'success' ? stat.getValue(rows) : '—' }));

  const openForm = async (employee = null) => {
    setMutation({ status: 'idle', error: null });
    setFormState({ open: true, employee });
    setLookup({ status: 'loading', branches: [], departments: [], error: null });
    try {
      const [branchData, departmentData] = await Promise.all([locationService.list(), departmentService.list()]);
      setLookup({ status: 'success', branches: collectionItems(branchData), departments: collectionItems(departmentData), error: null });
    } catch (error) {
      setLookup({ status: 'error', branches: [], departments: [], error });
    }
  };
  const closeForm = () => { if (mutation.status !== 'loading') setFormState({ open: false, employee: null }); };
  const submit = async (payload) => {
    setMutation({ status: 'loading', error: null });
    try {
      if (formState.employee) await employeeService.update(recordId(formState.employee, 'id'), payload);
      else await employeeService.create(payload);
      setMutation({ status: 'success', error: null });
      setFormState({ open: false, employee: null });
      resource.reload();
    } catch (error) {
      setMutation({ status: 'error', error });
    }
  };
  const remove = async () => {
    if (!deleteEmployee) return;
    setMutation({ status: 'loading', error: null });
    try {
      await employeeService.remove(recordId(deleteEmployee, 'id'));
      setMutation({ status: 'success', error: null });
      setDeleteEmployee(null);
      resource.reload();
    } catch (error) {
      setMutation({ status: 'error', error });
    }
  };

  const changeStatus = async (employee, status) => {
    setStatusAction({ status: 'loading', error: null });
    try {
      await employeeService.update(recordId(employee, 'id'), { status });
      setStatusAction({ status: 'success', error: null });
      resource.reload();
    } catch (error) {
      setStatusAction({ status: 'error', error });
    }
  };

  const columns = config.columns.map((column) => {
    if (column.key === 'employeeNumber') {
      return {
        ...column,
        render: (row) => (
          <span className="asset-tag-cell">
            <IconActionButton icon="info" className="asset-info-action" label={`View details for ${row.name || row.employeeNumber || 'employee'}`} onClick={() => setDetailsEmployee(row)} />
            <span>{row.employeeNumber || '—'}</span>
          </span>
        ),
      };
    }
    if (column.key === 'status') {
      return {
        ...column,
        render: (row) => (
          <StatusPicker
            value={String(row.status || '').toUpperCase()}
            options={editableStatuses}
            label={`Change status for ${row.name || row.employeeNumber || 'employee'}`}
            disabled={statusAction.status === 'loading'}
            onChange={(status) => changeStatus(row, status)}
          />
        ),
      };
    }
    return column;
  });

  return (
    <div>
      <PageHeader title={config.title} description={config.description} breadcrumbs={[{ label: config.title }]} actions={<><Button variant="outline" icon="download">Export</Button><Button icon="plus" onClick={() => openForm()}>Add employee</Button></>} />
      <div className="stats-grid">{stats.map((stat) => <StatCard key={stat.label} {...stat} />)}</div>
      <Card className="table-wrap">
        <Table columns={columns} rows={rows} status={resource.status} error={resource.error} onRetry={resource.reload} emptyTitle="No employees yet" emptyDescription="Add an employee to start managing assignments." searchPlaceholder="Search employees..." rowActionsLabel="Action" rowActions={(row) => <div className="table-action-group"><IconActionButton icon="edit" label={`Edit ${row.name || row.employeeNumber || 'employee'}`} tone="edit" onClick={() => openForm(row)} /><IconActionButton icon="trash" label={`Delete ${row.name || row.employeeNumber || 'employee'}`} tone="danger" onClick={() => { setMutation({ status: 'idle', error: null }); setDeleteEmployee(row); }} /></div>} />
        {statusAction.status === 'error' && <div className="inline-error table-inline-error"><Icon name="warning" size={16} />{statusAction.error?.message}</div>}
      </Card>
      <EmployeeDetailsModal employee={detailsEmployee} onClose={() => setDetailsEmployee(null)} />
      <Modal open={formState.open} onClose={closeForm} title={formState.employee ? 'Edit employee' : 'Add employee'} description={formState.employee ? 'Update the employee record, branch, and employment status.' : 'Branch is required. Department and other contact details can be added later.'}>
        {lookup.status === 'loading' && <div className="assignment-form-loading"><div className="spinner" aria-hidden="true" /><p>Loading branches and departments...</p></div>}
        {lookup.status === 'error' && <div className="inline-error"><Icon name="warning" size={16} />{lookup.error?.message}</div>}
        {lookup.status === 'success' && <EmployeeForm key={recordId(formState.employee, 'id') || 'new'} employee={formState.employee} branches={lookup.branches} departments={lookup.departments} submitting={mutation.status === 'loading'} actionError={mutation.status === 'error' ? mutation.error : null} onClose={closeForm} onSubmit={submit} />}
      </Modal>
      <Modal open={Boolean(deleteEmployee)} onClose={() => mutation.status !== 'loading' && setDeleteEmployee(null)} title="Deactivate employee" description="This keeps the employee history but prevents future assignments.">
        <div className="confirm-copy">Are you sure you want to deactivate <strong>{deleteEmployee?.name || deleteEmployee?.employeeNumber}</strong>?</div>
        {mutation.status === 'error' && <div className="inline-error"><Icon name="warning" size={16} />{mutation.error?.message}</div>}
        <div className="modal-actions"><Button variant="outline" onClick={() => setDeleteEmployee(null)} disabled={mutation.status === 'loading'}>Cancel</Button><Button variant="danger" onClick={remove} disabled={mutation.status === 'loading'}>{mutation.status === 'loading' ? 'Deactivating...' : 'Deactivate'}</Button></div>
      </Modal>
    </div>
  );
}
