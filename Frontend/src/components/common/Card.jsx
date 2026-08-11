export function Card({ children, className = '' }) {
  return <section className={`card ${className}`}>{children}</section>;
}

export function CardHeader({ title, description, action }) {
  return (
    <div className="card-header">
      <div>
        <h2 className="card-title">{title}</h2>
        {description && <p className="card-description">{description}</p>}
      </div>
      {action}
    </div>
  );
}
