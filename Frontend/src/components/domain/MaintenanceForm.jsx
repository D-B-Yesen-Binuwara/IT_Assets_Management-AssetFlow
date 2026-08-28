import { useState } from 'react';
import { Button } from '../common/Button';
import { Icon } from '../common/Icon';
import { employeeLabel, recordId } from '../../utils/collections';

const today = () => new Date().toISOString().slice(0, 10);
const statuses = ['OPEN', 'IN_PROGRESS', 'ON_HOLD', 'COMPLETED', 'CANCELLED'];
const priorities = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

const initialValues = (ticket) => ({
  assetId: recordId(ticket, 'assetId'),
  ticketNumber: ticket?.ticketNumber || '',
  issue: ticket?.issue || '',
  description: ticket?.description || '',
  priority: ticket?.priority || 'MEDIUM',
  status: ticket?.status || 'OPEN',
  startDate: ticket?.startDate || today(),
  dueDate: ticket?.dueDate || '',
  vendorId: recordId(ticket, 'vendorId'),
  requestedByEmployeeId: recordId(ticket, 'requestedByEmployeeId'),
  assignedToEmployeeId: recordId(ticket, 'assignedToEmployeeId'),
  cost: ticket?.cost ?? '',
  resolution: ticket?.resolution || '',
});

export function MaintenanceForm({ ticket, assets = [], vendors = [], employees = [], submitting = false, error, onClose, onSubmit }) {
  const [form, setForm] = useState(() => initialValues(ticket));
  const editing = Boolean(recordId(ticket, 'id'));
  const update = (key, value) => setForm((current) => ({ ...current, [key]: value }));
  const submit = (event) => {
    event.preventDefault();
    onSubmit({
      ...form,
      ticketNumber: form.ticketNumber || null,
      description: form.description || null,
      dueDate: form.dueDate || null,
      vendorId: form.vendorId || null,
      requestedByEmployeeId: form.requestedByEmployeeId || null,
      assignedToEmployeeId: form.assignedToEmployeeId || null,
      cost: form.cost === '' ? null : Number(form.cost),
      resolution: form.resolution || null,
    });
  };

  return (
    <form className="form-grid" onSubmit={submit}>
      <div className="form-fields two">
        <label className="form-field-wide">
          Asset
          <select value={form.assetId} onChange={(event) => update('assetId', event.target.value)} required disabled={submitting || editing}>
            <option value="">Select asset</option>
            {assets.map((asset) => <option key={recordId(asset, 'id')} value={recordId(asset, 'id')}>{asset.assetTag} — {asset.name}</option>)}
          </select>
        </label>
        <label>
          <span className="field-label">Ticket number <span className="optional-label">(optional)</span></span>
          <input value={form.ticketNumber} onChange={(event) => update('ticketNumber', event.target.value)} placeholder="Generated automatically if empty" disabled={submitting} />
        </label>
        <label>
          Priority
          <select value={form.priority} onChange={(event) => update('priority', event.target.value)} disabled={submitting}>
            {priorities.map((priority) => <option key={priority} value={priority}>{priority}</option>)}
          </select>
        </label>
        <label className="form-field-wide">
          Issue
          <input value={form.issue} onChange={(event) => update('issue', event.target.value)} placeholder="Describe the maintenance issue" required disabled={submitting} />
        </label>
        <label className="form-field-wide">
          <span className="field-label">Description <span className="optional-label">(optional)</span></span>
          <textarea value={form.description} onChange={(event) => update('description', event.target.value)} placeholder="Add symptoms, diagnostics, or work required" rows="3" disabled={submitting} />
        </label>
        <label>
          Status
          <select value={form.status} onChange={(event) => update('status', event.target.value)} disabled={submitting}>
            {statuses.map((status) => <option key={status} value={status}>{status.replace('_', ' ')}</option>)}
          </select>
        </label>
        <label>
          Start date
          <input type="date" value={form.startDate} max={form.dueDate || undefined} onChange={(event) => update('startDate', event.target.value)} required disabled={submitting} />
        </label>
        <label>
          <span className="field-label">Due date <span className="optional-label">(optional)</span></span>
          <input type="date" value={form.dueDate} min={form.startDate || undefined} onChange={(event) => update('dueDate', event.target.value)} disabled={submitting} />
        </label>
        <label>
          <span className="field-label">Vendor <span className="optional-label">(optional)</span></span>
          <select value={form.vendorId} onChange={(event) => update('vendorId', event.target.value)} disabled={submitting}>
            <option value="">No vendor selected</option>
            {vendors.filter((vendor) => String(vendor.status || 'ACTIVE').toUpperCase() === 'ACTIVE' || recordId(vendor, 'id') === form.vendorId).map((vendor) => <option key={recordId(vendor, 'id')} value={recordId(vendor, 'id')}>{vendor.name}</option>)}
          </select>
        </label>
        <label>
          <span className="field-label">Requested by <span className="optional-label">(optional)</span></span>
          <select value={form.requestedByEmployeeId} onChange={(event) => update('requestedByEmployeeId', event.target.value)} disabled={submitting}>
            <option value="">No employee selected</option>
            {employees.map((employee) => <option key={recordId(employee, 'id')} value={recordId(employee, 'id')}>{employeeLabel(employee)} ({employee.employeeNumber})</option>)}
          </select>
        </label>
        <label>
          <span className="field-label">Assigned employee <span className="optional-label">(optional)</span></span>
          <select value={form.assignedToEmployeeId} onChange={(event) => update('assignedToEmployeeId', event.target.value)} disabled={submitting}>
            <option value="">Unassigned</option>
            {employees.filter((employee) => String(employee.status || '').toUpperCase() === 'ACTIVE' || recordId(employee, 'id') === form.assignedToEmployeeId).map((employee) => <option key={recordId(employee, 'id')} value={recordId(employee, 'id')}>{employeeLabel(employee)} ({employee.employeeNumber})</option>)}
          </select>
        </label>
        <label>
          <span className="field-label">Cost (Rs) <span className="optional-label">(optional)</span></span>
          <input type="number" min="0" step="0.01" value={form.cost} onChange={(event) => update('cost', event.target.value)} placeholder="0.00" disabled={submitting} />
        </label>
        <label className="form-field-wide">
          <span className="field-label">Resolution <span className="optional-label">(optional)</span></span>
          <textarea value={form.resolution} onChange={(event) => update('resolution', event.target.value)} placeholder="Record the outcome or cancellation reason" rows="3" disabled={submitting} />
        </label>
      </div>
      {error && <div className="inline-error"><Icon name="warning" size={16} />{error.message}</div>}
      <div className="modal-actions"><Button variant="outline" onClick={onClose} disabled={submitting}>Cancel</Button><Button type="submit" disabled={submitting}>{submitting ? 'Saving...' : editing ? 'Save changes' : 'Create ticket'}</Button></div>
    </form>
  );
}
