import { useEffect } from 'react';
import { Button } from './Button';
import { Icon } from './Icon';

export function Modal({ open, title, description, children, onClose }) {
  useEffect(() => {
    if (!open) return undefined;

    // The modal listens for Escape only while it is visible.
    const handler = (event) => event.key === 'Escape' && onClose();

    document.addEventListener('keydown', handler);
    return () => document.removeEventListener('keydown', handler);
  }, [open, onClose]);

  if (!open) return null;

  return (
    <div
      className="modal-backdrop"
      onMouseDown={(event) => event.target === event.currentTarget && onClose()}
    >
      <div className="modal" role="dialog" aria-modal="true">
        <div className="modal-header">
          <div>
            <h2>{title}</h2>
            {description && <p>{description}</p>}
          </div>

          <button className="icon-button" onClick={onClose} aria-label="Close">
            <Icon name="x" />
          </button>
        </div>

        {children}
      </div>
    </div>
  );
}

export function BackendForm({ fields, onClose, onSubmit, submitting = false }) {
  const handleSubmit = (event) => {
    event.preventDefault();

    const values = Object.fromEntries(new FormData(event.currentTarget));
    const payload = Object.fromEntries(
      fields.map((field) => {
        const rawValue = values[field.name];
        if (field.parse) return [field.name, field.parse(rawValue)];
        if (field.type === 'number') return [field.name, rawValue === '' ? null : Number(rawValue)];
        return [field.name, rawValue === '' ? null : rawValue];
      }),
    );

    onSubmit(payload);
  };

  return (
    <form className="form-grid" onSubmit={handleSubmit}>
      <div className="form-fields">
        {fields.map((field) => (
          <label key={field.name}>
            {field.label}
            {field.type === 'select' ? (
              <select name={field.name} defaultValue={field.defaultValue || ''} required={field.required !== false}>
                <option value="">{field.placeholder || 'Select an option'}</option>
                {field.options?.map((option) => (
                  <option key={option.value} value={option.value}>{option.label}</option>
                ))}
              </select>
            ) : field.type === 'textarea' ? (
              <textarea
                name={field.name}
                placeholder={field.placeholder || 'Enter value'}
                required={field.required !== false}
                rows={field.rows || 3}
              />
            ) : (
              <input
                name={field.name}
                type={field.type || 'text'}
                placeholder={field.placeholder || 'Enter value'}
                required={field.required !== false}
                min={field.min}
                step={field.step}
              />
            )}
          </label>
        ))}
      </div>

      <div className="modal-actions">
        <Button variant="outline" onClick={onClose}>
          Cancel
        </Button>
        <Button type="submit" disabled={submitting}>
          {submitting ? 'Submitting...' : 'Submit request'}
        </Button>
      </div>
    </form>
  );
}
