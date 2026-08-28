import { Icon } from './Icon';
import { Link } from 'react-router-dom';

export function StatCard({ label, icon, tone = 'indigo', helper, value = '\u2014', to }) {
  const content = (
    <>
      <div className="stat-icon">
        <Icon name={icon} size={20} />
      </div>

      <div>
        <p>{label}</p>
        <strong>{value}</strong>
        {helper && <small>{helper}</small>}
      </div>
      {to && <span className="stat-card-arrow" aria-hidden="true"><Icon name="chevronRight" size={15} /></span>}
    </>
  );
  return to
    ? <Link className={`stat-card stat-card-link stat-${tone}`} to={to} aria-label={`Open ${label}`}>{content}</Link>
    : <div className={`stat-card stat-${tone}`}>{content}</div>;
}
