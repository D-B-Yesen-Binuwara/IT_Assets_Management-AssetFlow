import { useMemo, useState } from 'react';
import { Button } from '../common/Button';
import { Icon } from '../common/Icon';
import { Modal } from '../common/Modal';
import { employeeLabel, recordId } from '../../utils/collections';

const statuses = [
  { value: 'ACTIVE', label: 'Active' },
  { value: 'RETURNED', label: 'Returned' },
  { value: 'TRANSFERRED', label: 'Transferred' },
  { value: 'CANCELLED', label: 'Cancelled' },
];

export function AssignmentEditModal({ assignment, employees = [], submitting = false, actionError, onClose, onSubmit }) {
  const [form, setForm] = useState({
    assignedDate: assignment?.assignedDate || '',
    expectedReturnDate: assignment?.expectedReturnDate || '',
    status: assignment?.status || 'ACTIVE',
    handoverNotes: assignment?.handoverNotes || '',
    closingReason: assignment?.closingReason || '',
  });
  const employee = useMemo(
    () => employees.find((item) => recordId(item, 'id') === recordId(assignment, 'employeeId')),
    [employees, assignment],
  );
  if (!assignment) return null;

  const update = (key, value) => setForm((current) => ({ ...current, [key]: value }));
  const submit = (event) => {
    event.preventDefault();
    onSubmit({
      assignedDate: form.assignedDate || null,
      expectedReturnDate: form.expectedReturnDate || null,
      status: form.status,
      handoverNotes: form.handoverNotes || '',
      closingReason: form.closingReason || '',
    });
  };

  return (
    <Modal open onClose={onClose} className="assignment-edit-modal" title="Edit assignment" description="Review the verified assignment details and update dates, status, and notes.">
      <form className="form-grid" onSubmit={submit}>
        <div className="form-fields two">
          <label>Category<input value={assignment.category || ''} readOnly /></label>
          <label>Asset tag<input value={assignment.assetTag || ''} readOnly /></label>
          <label>Assets Name<input value={assignment.assetName || ''} readOnly /></label>
          <label>Employee<input value={employeeLabel(employee) || assignment.employeeName || ''} readOnly /></label>
        </div>
        <div className="form-fields two employee-details">
          <label>Department<input value={employee?.department || ''} readOnly /></label>
          <label>Branch<input value={employee?.branch || ''} readOnly /></label>
          <label>Email<input value={employee?.email || ''} readOnly /></label>
          <label>Contact No<input value={employee?.phone || ''} readOnly /></label>
        </div>
        <div className="form-fields two">
          <label>Assignment date<input type="date" value={form.assignedDate} onChange={(event) => update('assignedDate', event.target.value)} required disabled={submitting} /></label>
          <label><span className="field-label">Closing date <span className="optional-label">(optional)</span></span><input type="date" value={form.expectedReturnDate} onChange={(event) => update('expectedReturnDate', event.target.value)} min={form.assignedDate || undefined} disabled={submitting} /></label>
          <label>
            Status
            <select value={form.status} onChange={(event) => update('status', event.target.value)} disabled={submitting}>
              {statuses.map((status) => <option key={status.value} value={status.value}>{status.label}</option>)}
            </select>
          </label>
          <label><span className="field-label">Closing reason <span className="optional-label">(optional)</span></span><input value={form.closingReason} onChange={(event) => update('closingReason', event.target.value)} placeholder="Enter a closure reason" disabled={submitting} /></label>
          <label className="form-field-wide"><span className="field-label">Notes <span className="optional-label">(optional)</span></span><textarea value={form.handoverNotes} onChange={(event) => update('handoverNotes', event.target.value)} placeholder="Add or update assignment notes" rows="3" disabled={submitting} /></label>
        </div>
        {actionError && <div className="inline-error"><Icon name="warning" size={16} />{actionError.message}</div>}
        <div className="modal-actions"><Button variant="outline" onClick={onClose} disabled={submitting}>Cancel</Button><Button type="submit" disabled={submitting}>{submitting ? 'Saving...' : 'Save changes'}</Button></div>
      </form>
    </Modal>
  );
}
