import { useMemo, useState } from 'react';
import { Button } from '../common/Button';
import { Icon } from '../common/Icon';
import { recordId } from '../../utils/collections';

const employeeStatuses = [
  { value: 'ACTIVE', label: 'Active' },
  { value: 'ON_LEAVE', label: 'Suspended' },
  { value: 'INACTIVE', label: 'Inactive' },
  { value: 'TERMINATED', label: 'Left' },
];

const initialForm = (employee) => ({
  employeeNumber: employee?.employeeNumber || '',
  firstName: employee?.firstName || '',
  lastName: employee?.lastName || '',
  email: employee?.email || '',
  branchId: recordId(employee, 'branchId'),
  departmentId: recordId(employee, 'departmentId'),
  jobTitle: employee?.jobTitle || '',
  phone: employee?.phone || '',
  address: employee?.address || '',
  hireDate: employee?.hireDate || '',
  terminationDate: employee?.terminationDate || '',
  status: employee?.status || 'ACTIVE',
});

export function EmployeeForm({ employee, branches = [], departments = [], submitting = false, actionError, onClose, onSubmit }) {
  const [form, setForm] = useState(() => initialForm(employee));
  const isEditing = Boolean(recordId(employee, 'id'));
  const availableBranches = useMemo(
    () => branches.filter((branch) => String(branch.locationType || branch.type || '').toUpperCase() === 'BRANCH' || recordId(branch, 'id') === form.branchId),
    [branches, form.branchId],
  );
  const update = (key, value) => setForm((current) => ({ ...current, [key]: value }));

  const submit = (event) => {
    event.preventDefault();
    onSubmit({
      ...form,
      branchId: form.branchId,
      departmentId: form.departmentId || null,
      department: form.departmentId ? null : '',
      jobTitle: form.jobTitle || null,
      phone: form.phone || null,
      address: form.address || null,
      hireDate: form.hireDate || null,
      terminationDate: form.terminationDate || null,
    });
  };

  return (
    <form className="form-grid employee-form" onSubmit={submit}>
      <div className="form-fields two">
        <label>Employee ID<input value={form.employeeNumber} onChange={(event) => update('employeeNumber', event.target.value)} placeholder="e.g. EMP-0001" required disabled={submitting} /></label>
        <label>Email<input type="email" value={form.email} onChange={(event) => update('email', event.target.value)} placeholder="name@company.com" required disabled={submitting} /></label>
        <label>First name<input value={form.firstName} onChange={(event) => update('firstName', event.target.value)} placeholder="Enter first name" required disabled={submitting} /></label>
        <label>Last name<input value={form.lastName} onChange={(event) => update('lastName', event.target.value)} placeholder="Enter last name" required disabled={submitting} /></label>
        <label>
          Branch
          <select value={form.branchId} onChange={(event) => update('branchId', event.target.value)} required disabled={submitting || availableBranches.length === 0}>
            <option value="">{availableBranches.length ? 'Select branch' : 'No branches available'}</option>
            {availableBranches.map((branch) => <option key={recordId(branch, 'id')} value={recordId(branch, 'id')}>{branch.name}{branch.code ? ` (${branch.code})` : ''}</option>)}
          </select>
        </label>
        <label>
          <span className="field-label">Department <span className="optional-label">(optional)</span></span>
          <select value={form.departmentId} onChange={(event) => update('departmentId', event.target.value)} disabled={submitting}>
            <option value="">No department selected</option>
            {departments.map((department) => <option key={recordId(department, 'id')} value={recordId(department, 'id')}>{department.name}{department.code ? ` (${department.code})` : ''}</option>)}
          </select>
        </label>
        <label><span className="field-label">Job title <span className="optional-label">(optional)</span></span><input value={form.jobTitle} onChange={(event) => update('jobTitle', event.target.value)} placeholder="Enter job title" disabled={submitting} /></label>
        <label><span className="field-label">Phone <span className="optional-label">(optional)</span></span><input value={form.phone} onChange={(event) => update('phone', event.target.value)} placeholder="Enter contact number" disabled={submitting} /></label>
        <label><span className="field-label">Hire date <span className="optional-label">(optional)</span></span><input type="date" value={form.hireDate} onChange={(event) => update('hireDate', event.target.value)} disabled={submitting} /></label>
        <label><span className="field-label">Left date <span className="optional-label">(optional)</span></span><input type="date" value={form.terminationDate} onChange={(event) => update('terminationDate', event.target.value)} min={form.hireDate || undefined} disabled={submitting} /></label>
        {isEditing && (
          <label>
            Status
            <select value={form.status} onChange={(event) => update('status', event.target.value)} disabled={submitting}>
              {employeeStatuses.map((status) => <option key={status.value} value={status.value}>{status.label}</option>)}
            </select>
          </label>
        )}
        <label className="form-field-wide"><span className="field-label">Address <span className="optional-label">(optional)</span></span><textarea value={form.address} onChange={(event) => update('address', event.target.value)} placeholder="Enter address" rows="3" disabled={submitting} /></label>
      </div>
      {actionError && <div className="inline-error"><Icon name="warning" size={16} />{actionError.message}</div>}
      <div className="modal-actions"><Button variant="outline" onClick={onClose} disabled={submitting}>Cancel</Button><Button type="submit" disabled={submitting || availableBranches.length === 0}>{submitting ? 'Saving...' : isEditing ? 'Save changes' : 'Add employee'}</Button></div>
    </form>
  );
}
