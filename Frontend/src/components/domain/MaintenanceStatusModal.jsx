import { useState } from 'react';
import { Button } from '../common/Button';
import { Icon } from '../common/Icon';
import { Modal } from '../common/Modal';
import { StatusBadge } from '../common/StatusBadge';

const today = () => new Date().toISOString().slice(0, 10);

export function MaintenanceStatusModal({ ticket, status, submitting = false, error, onClose, onSubmit }) {
  const [form, setForm] = useState(() => ({ startDate: ticket?.startDate || today(), dueDate: ticket?.dueDate || '', resolution: ticket?.resolution || '' }));
  if (!ticket || !status) return null;
  const terminal = status === 'COMPLETED' || status === 'CANCELLED';
  const submit = (event) => {
    event.preventDefault();
    onSubmit({ status, startDate: form.startDate, dueDate: form.dueDate || null, resolution: form.resolution || null });
  };
  return (
    <Modal open onClose={onClose} className="status-change-modal" title="Change maintenance status" description={`Update ${ticket.ticketNumber} and keep the asset status synchronized.`}>
      <div className="status-change-summary"><span>New status</span><StatusBadge status={status} /></div>
      <form className="form-grid" onSubmit={submit}>
        <div className="form-fields two">
          <label>Start date<input type="date" value={form.startDate} max={form.dueDate || undefined} onChange={(event) => setForm((current) => ({ ...current, startDate: event.target.value }))} required disabled={submitting} /></label>
          <label><span className="field-label">Due date <span className="optional-label">(optional)</span></span><input type="date" value={form.dueDate} min={form.startDate || undefined} onChange={(event) => setForm((current) => ({ ...current, dueDate: event.target.value }))} disabled={submitting} /></label>
          {terminal && <label className="form-field-wide"><span className="field-label">{status === 'CANCELLED' ? 'Cancellation reason' : 'Resolution'} <span className="optional-label">(optional)</span></span><textarea rows="3" value={form.resolution} onChange={(event) => setForm((current) => ({ ...current, resolution: event.target.value }))} placeholder="Add the outcome or reason" disabled={submitting} /></label>}
        </div>
        {error && <div className="inline-error"><Icon name="warning" size={16} />{error.message}</div>}
        <div className="modal-actions"><Button variant="outline" onClick={onClose} disabled={submitting}>Cancel</Button><Button type="submit" disabled={submitting}>{submitting ? 'Updating...' : 'Confirm status'}</Button></div>
      </form>
    </Modal>
  );
}
