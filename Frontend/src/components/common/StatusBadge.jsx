const tone = (status = '') => {
  const value = String(status).toLowerCase().trim().replace(/[-\s]+/g, '_');
  const tones = {
    active: 'info',
    assigned: 'indigo',
    available: 'success',
    approved: 'success',
    completed: 'success',
    received: 'success',
    paid: 'success',
    returned: 'slate',
    transferred: 'violet',
    in_transit: 'cyan',
    in_progress: 'blue',
    open: 'amber',
    pending: 'amber',
    expiring: 'orange',
    under_maintenance: 'orange',
    retired: 'orange',
    disposed: 'rose',
    expired: 'rose',
    overdue: 'rose',
    rejected: 'rose',
    cancelled: 'danger',
    lost: 'danger',
  };

  return tones[value] || 'neutral';
};

const display = (status) => {
  const value = String(status || '').toUpperCase();
  const labels = {
    UNDER_MAINTENANCE: 'Maintenance',
    IN_TRANSIT: 'In Transit',
    ON_LEAVE: 'Suspended',
    TERMINATED: 'Left',
  };
  return labels[value] || String(status || 'Unknown').replace(/_/g, ' ');
};

export function StatusBadge({ status }) { return <span className={`status-badge ${tone(status)}`}><span />{display(status)}</span>; }
