import { Icon } from './Icon';

export function IconActionButton({ icon, label, onClick, tone = 'neutral', disabled = false, className = '' }) {
  return (
    <button
      type="button"
      className={`table-icon-action ${tone} ${className}`}
      aria-label={label}
      title={label}
      onClick={onClick}
      disabled={disabled}
    >
      <Icon name={icon} size={16} />
    </button>
  );
}
