import { useState } from 'react';
import { Button } from '../common/Button';
import { Icon } from '../common/Icon';
import { Modal } from '../common/Modal';
import { StatusBadge } from '../common/StatusBadge';
import { AssetStatusFields } from './AssetStatusFields';
import { initialStatusDetails } from './assetStatusUtils';

export function AssetStatusChangeModal({ asset, status, vendors = [], employees = [], submitting = false, error, onClose, onSubmit }) {
  const [form, setForm] = useState(initialStatusDetails);
  if (!asset || !status) return null;
  const update = (key, value) => setForm((current) => ({ ...current, [key]: value }));
  const submit = (event) => {
    event.preventDefault();
    onSubmit(form);
  };

  return (
    <Modal open onClose={onClose} className="status-change-modal" title="Change asset status" description={`Update ${asset.assetTag || asset.name} and any related operational record.`}>
      <div className="status-change-summary"><span>New status</span><StatusBadge status={status} /></div>
      <form className="form-grid" onSubmit={submit}>
        <div className="form-fields two">
          <AssetStatusFields status={status} form={form} update={update} vendors={vendors} employees={employees} disabled={submitting} />
        </div>
        {error && <div className="inline-error"><Icon name="warning" size={16} />{error.message}</div>}
        <div className="modal-actions"><Button variant="outline" onClick={onClose} disabled={submitting}>Cancel</Button><Button type="submit" disabled={submitting}>{submitting ? 'Updating...' : 'Confirm status'}</Button></div>
      </form>
    </Modal>
  );
}
