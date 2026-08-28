import { Modal } from '../common/Modal';
import { StatusBadge } from '../common/StatusBadge';

const value = (item) => item === null || item === undefined || item === '' ? '—' : item;
const date = (item) => item ? new Date(`${item}T00:00:00`).toLocaleDateString() : '—';
const dateTime = (item) => item ? new Date(item).toLocaleString() : '—';
const money = (item) => item === null || item === undefined || item === '' ? '—' : `Rs ${Number(item).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;

function Detail({ label, children }) {
  return <div><span>{label}</span><strong>{children}</strong></div>;
}

export function MaintenanceDetailsModal({ ticket, onClose }) {
  if (!ticket) return null;
  return (
    <Modal open onClose={onClose} className="asset-detail-modal" title={ticket.ticketNumber || 'Maintenance ticket'} description={`Maintenance record for ${ticket.assetTag || ticket.assetName || 'this asset'}.`}>
      <div className="asset-detail-summary"><div><span>Ticket</span><strong>{value(ticket.ticketNumber)}</strong></div><StatusBadge status={ticket.status} /></div>
      <div className="asset-detail-sections">
        <section className="asset-detail-section"><h3>Maintenance details</h3><div className="asset-detail-grid">
          <Detail label="Asset tag">{value(ticket.assetTag)}</Detail><Detail label="Asset Name">{value(ticket.assetName)}</Detail><Detail label="Priority"><StatusBadge status={ticket.priority} /></Detail>
          <Detail label="Issue">{value(ticket.issue)}</Detail><Detail label="Description">{value(ticket.description)}</Detail><Detail label="Status"><StatusBadge status={ticket.status} /></Detail>
          <Detail label="Start date">{date(ticket.startDate)}</Detail><Detail label="Due date">{date(ticket.dueDate)}</Detail><Detail label="Opened">{dateTime(ticket.openedAt)}</Detail>
          <Detail label="Started">{dateTime(ticket.startedAt)}</Detail><Detail label="Completed">{dateTime(ticket.completedAt)}</Detail><Detail label="Cost">{money(ticket.cost)}</Detail>
        </div></section>
        <section className="asset-detail-section"><h3>Responsibility and outcome</h3><div className="asset-detail-grid">
          <Detail label="Vendor">{value(ticket.vendor)}</Detail><Detail label="Requested by">{value(ticket.requestedByEmployee)}</Detail><Detail label="Assigned employee">{value(ticket.assignedToEmployee)}</Detail><Detail label="Resolution">{value(ticket.resolution)}</Detail>
        </div></section>
      </div>
    </Modal>
  );
}
