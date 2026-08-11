import { Icon } from './Icon';

export function StatCard({ label, icon, tone = 'indigo', helper }) {
  return (
    <div className={`stat-card stat-${tone}`}>
      <div className="stat-icon">
        <Icon name={icon} size={20} />
      </div>

      <div>
        <p>{label}</p>
        <strong>&mdash;</strong>
        {helper && <small>{helper}</small>}
      </div>
    </div>
  );
}
