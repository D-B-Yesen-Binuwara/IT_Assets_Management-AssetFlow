import { Icon } from './Icon';

export function StatCard({ label, icon, tone = 'indigo', helper, value = '\u2014' }) {
  return (
    <div className={`stat-card stat-${tone}`}>
      <div className="stat-icon">
        <Icon name={icon} size={20} />
      </div>

      <div>
        <p>{label}</p>
        <strong>{value}</strong>
        {helper && <small>{helper}</small>}
      </div>
    </div>
  );
}
