const tone = (status = '') => {
  const value = status.toLowerCase();
  if (value.includes('active') || value.includes('available') || value.includes('completed') || value.includes('approved')) return 'success';
  if (value.includes('pending') || value.includes('progress') || value.includes('expir')) return 'warning';
  if (value.includes('retired') || value.includes('cancel') || value.includes('reject') || value.includes('overdue')) return 'danger';
  return 'neutral';
};

export function StatusBadge({ status }) { return <span className={`status-badge ${tone(status)}`}><span />{status || 'Unknown'}</span>; }

