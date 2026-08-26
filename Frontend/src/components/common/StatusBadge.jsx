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

export function StatusBadge({ status }) { return <span className={`status-badge ${tone(status)}`}><span />{status || 'Unknown'}</span>; }
