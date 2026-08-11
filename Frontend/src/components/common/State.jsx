import { Button } from './Button';
import { Icon } from './Icon';

export function EmptyState({
  icon = 'assets',
  title = 'No records yet',
  description = 'Records will appear here once the backend is connected.',
  action,
}) {
  return (
    <div className="state-panel">
      <div className="state-icon">
        <Icon name={icon} size={26} />
      </div>
      <h3>{title}</h3>
      <p>{description}</p>
      {action}
    </div>
  );
}

export function LoadingState({ label = 'Loading data' }) {
  return (
    <div className="state-panel compact">
      <div className="spinner" aria-hidden="true" />
      <p>{label}...</p>
    </div>
  );
}

export function ErrorState({ error, onRetry }) {
  return (
    <div className="state-panel compact error-state">
      <div className="state-icon">
        <Icon name="warning" size={22} />
      </div>
      <h3>Data is unavailable</h3>
      <p>{error?.message || 'The request could not be completed.'}</p>
      {onRetry && (
        <Button variant="outline" icon="refresh" onClick={onRetry}>
          Try again
        </Button>
      )}
    </div>
  );
}

export function ResourceState({ status, error, onRetry, children, empty = false, emptyProps }) {
  if (status === 'loading') return <LoadingState />;
  if (status === 'error') return <ErrorState error={error} onRetry={onRetry} />;
  if (empty || !children) return <EmptyState {...emptyProps} />;

  return children;
}
