import { useState } from 'react';
import { Button } from '../common/Button';
import { Icon } from '../common/Icon';
import { Modal } from '../common/Modal';

export function AssignmentEditModal({ assignment, submitting = false, actionError, onClose, onSubmit }) {
  const [form, setForm] = useState({
    expectedReturnDate: assignment?.expectedReturnDate || '',
    handoverNotes: assignment?.handoverNotes || '',
  });
  if (!assignment) return null;

  const submit = (event) => {
    event.preventDefault();
    onSubmit({ expectedReturnDate: form.expectedReturnDate || null, handoverNotes: form.handoverNotes || '' });
  };

  return (
    <Modal open onClose={onClose} title="Edit assignment" description="Update the planned closing date or assignment notes without changing its history.">
      <form className="form-grid" onSubmit={submit}>
        <div className="transfer-context">
          <div><span>Asset</span><strong>{assignment.assetName || assignment.assetTag || '—'}</strong></div>
          <div><span>Employee</span><strong>{assignment.employeeName || '—'}</strong></div>
          <div><span>Status</span><strong>{String(assignment.status || '').replace(/_/g, ' ') || '—'}</strong></div>
        </div>
        <div className="form-fields">
          <label>
            Closing date <span className="optional-label">(optional)</span>
            <input type="date" value={form.expectedReturnDate} onChange={(event) => setForm((current) => ({ ...current, expectedReturnDate: event.target.value }))} disabled={submitting} />
          </label>
          <label>
            Assignment notes <span className="optional-label">(optional)</span>
            <textarea value={form.handoverNotes} onChange={(event) => setForm((current) => ({ ...current, handoverNotes: event.target.value }))} placeholder="Add or update assignment notes" rows="3" disabled={submitting} />
          </label>
        </div>
        {actionError && <div className="inline-error"><Icon name="warning" size={16} />{actionError.message}</div>}
        <div className="modal-actions"><Button variant="outline" onClick={onClose} disabled={submitting}>Cancel</Button><Button type="submit" disabled={submitting}>{submitting ? 'Saving...' : 'Save changes'}</Button></div>
      </form>
    </Modal>
  );
}
