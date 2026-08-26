import { useState } from 'react';
import { Button } from '../common/Button';
import { Icon } from '../common/Icon';
import { Modal } from '../common/Modal';

const assetLabel = (assignment) => assignment?.assetName || assignment?.assetTag || 'this asset';

export function AssignmentCloseModal({ assignment, submitting = false, actionError, onClose, onSubmit }) {
  const [reason, setReason] = useState('');
  if (!assignment) return null;

  const submit = (event) => {
    event.preventDefault();
    onSubmit({ closingReason: reason || '' });
  };

  return (
    <Modal open onClose={onClose} title="Close assignment" description="Close this assignment now. It can be closed before its planned closing date.">
      <form className="form-grid" onSubmit={submit}>
        <div className="transfer-context">
          <div><span>Asset</span><strong>{assetLabel(assignment)}</strong></div>
          <div><span>Assigned to</span><strong>{assignment.employeeName || '—'}</strong></div>
          <div><span>Planned closing</span><strong>{assignment.expectedReturnDate || 'Not set'}</strong></div>
        </div>
        <div className="form-fields">
          <label>
            <span className="field-label">Closing reason <span className="optional-label">(optional)</span></span>
            <textarea value={reason} onChange={(event) => setReason(event.target.value)} placeholder="Add an early-return or closure reason" rows="3" disabled={submitting} />
          </label>
        </div>
        {actionError && <div className="inline-error"><Icon name="warning" size={16} />{actionError.message}</div>}
        <div className="modal-actions"><Button variant="outline" onClick={onClose} disabled={submitting}>Cancel</Button><Button type="submit" disabled={submitting}>{submitting ? 'Closing...' : 'Close assignment'}</Button></div>
      </form>
    </Modal>
  );
}
