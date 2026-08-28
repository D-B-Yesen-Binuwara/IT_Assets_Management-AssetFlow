import { Modal } from '../common/Modal';
import { StatusBadge } from '../common/StatusBadge';

const empty = (value) => value === null || value === undefined || value === '';

function displayValue(record, key) {
  const value = record?.[key];
  if (empty(value)) return '—';
  if (typeof value === 'boolean') return value ? 'Yes' : 'No';
  if (Array.isArray(value)) return value.join(', ');
  if (/At$/.test(key)) return new Date(value).toLocaleString();
  if (/Date$/.test(key)) return new Date(`${value}T00:00:00`).toLocaleDateString();
  if (typeof value === 'object') return JSON.stringify(value);
  return String(value).replaceAll('_', ' ');
}

export function RecordDetailsModal({ record, title, description, fields = [], onClose }) {
  if (!record) return null;
  return (
    <Modal open onClose={onClose} className="asset-detail-modal" title={title || 'Record details'} description={description || 'Full record details.'}>
      <div className="asset-detail-sections record-detail-sections">
        <section className="asset-detail-section">
          <h3>Details</h3>
          <div className="asset-detail-grid">
            {fields.map(([key, label]) => (
              <div key={key}>
                <span>{label}</span>
                <strong>{key === 'status' ? <StatusBadge status={record[key]} /> : displayValue(record, key)}</strong>
              </div>
            ))}
          </div>
        </section>
      </div>
    </Modal>
  );
}
