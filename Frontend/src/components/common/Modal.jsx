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

    // The form passes entered values to the service layer without creating fake results.
    onSubmit(Object.fromEntries(new FormData(event.currentTarget)));
  };

  return (
    <form className="form-grid" onSubmit={handleSubmit}>
      <div className="form-fields">
        {fields.map((field) => (
          <label key={field.name}>
            {field.label}
            <input
              name={field.name}
              type={field.type || 'text'}
              placeholder={field.placeholder || 'Enter value'}
              required={field.required !== false}
            />
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
