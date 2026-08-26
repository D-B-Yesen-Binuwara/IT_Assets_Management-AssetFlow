import { Link } from 'react-router-dom';

export function PageHeader({ title, description, breadcrumbs = [], actions, className = '' }) {
  return (
    <div className={`page-header ${className}`}>
      <div>
        <div className="breadcrumbs">
          <Link to="/">AssetFlow</Link>

          {breadcrumbs.map((crumb) => (
            <span key={crumb.label}>
              / {crumb.to ? <Link to={crumb.to}>{crumb.label}</Link> : crumb.label}
            </span>
          ))}
        </div>

        <h1>{title}</h1>
        {description && <p>{description}</p>}
      </div>

      {actions && <div className="page-actions">{actions}</div>}
    </div>
  );
}
