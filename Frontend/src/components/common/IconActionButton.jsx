import { Icon } from './Icon';

export function IconActionButton({ icon, label, onClick, tone = 'neutral', disabled = false }) {
  return (
    <button
      type="button"
      className={`table-icon-action ${tone}`}
      aria-label={label}
      title={label}
      onClick={onClick}
      disabled={disabled}
    >
      <Icon name={icon} size={16} />
    </button>
  );
}
