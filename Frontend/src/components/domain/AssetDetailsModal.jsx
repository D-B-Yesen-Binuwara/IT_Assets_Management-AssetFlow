import { Modal } from '../common/Modal';
import { StatusBadge } from '../common/StatusBadge';

const value = (item) => item === null || item === undefined || item === '' ? '—' : item;
const date = (item) => item ? new Date(`${item}T00:00:00`).toLocaleDateString() : '—';
const amount = (item, currency) => item === null || item === undefined || item === ''
  ? '—'
  : `${currency || 'USD'} ${Number(item).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;

function DetailSection({ title, children }) {
  return (
    <section className="asset-detail-section">
      <h3>{title}</h3>
      <div className="asset-detail-grid">{children}</div>
    </section>
  );
}

function Detail({ label, children }) {
  return <div><span>{label}</span><strong>{children}</strong></div>;
}

export function AssetDetailsModal({ asset, vendor, onClose }) {
  if (!asset) return null;

  return (
    <Modal
      open
      onClose={onClose}
      className="asset-detail-modal"
      title={asset.name || 'Asset details'}
      description={`Full record for ${asset.assetTag || 'this asset'}.`}
    >
      <div className="asset-detail-summary">
        <div>
          <span>Asset tag</span>
          <strong>{value(asset.assetTag)}</strong>
        </div>
        <StatusBadge status={asset.status} />
      </div>

      <div className="asset-detail-sections">
        <DetailSection title="Identity">
          <Detail label="Asset Name">{value(asset.name)}</Detail>
          <Detail label="Category">{value(asset.category)}</Detail>
          <Detail label="Brand">{value(asset.brand)}</Detail>
          <Detail label="Model No">{value(asset.modelNo)}</Detail>
          <Detail label="Serial number">{value(asset.serialNumber)}</Detail>
          <Detail label="Condition">{value(asset.condition || asset.assetCondition)}</Detail>
        </DetailSection>

        <DetailSection title="Assignment and location">
          <Detail label="Status"><StatusBadge status={asset.status} /></Detail>
          <Detail label="Assigned to">{value(asset.assignedTo)}</Detail>
          <Detail label="Department">{value(asset.department)}</Detail>
          <Detail label="Location">{value(asset.location)}</Detail>
          <Detail label="Retirement date">{date(asset.retirementDate)}</Detail>
        </DetailSection>

        <DetailSection title="Vendor and purchase">
          <Detail label="Vendor">{value(vendor?.name || asset.vendor)}</Detail>
          <Detail label="Vendor location">{value(vendor?.address)}</Detail>
          <Detail label="Vendor contact no">{value(vendor?.phone)}</Detail>
          <Detail label="Vendor email">{value(vendor?.email)}</Detail>
          <Detail label="Purchase date">{date(asset.purchaseDate)}</Detail>
          <Detail label="Price">{amount(asset.purchaseCost, asset.currency)}</Detail>
          <Detail label="Warranty period">{asset.warrantyPeriodMonths === null || asset.warrantyPeriodMonths === undefined ? '—' : `${asset.warrantyPeriodMonths} months`}</Detail>
        </DetailSection>

        {(asset.notes || asset.disposalNotes) && (
          <DetailSection title="Notes">
            {asset.notes && <Detail label="Asset notes">{asset.notes}</Detail>}
            {asset.disposalNotes && <Detail label="Disposal notes">{asset.disposalNotes}</Detail>}
          </DetailSection>
        )}
      </div>
    </Modal>
  );
}
