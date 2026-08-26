import { useEffect, useRef, useState } from 'react';
import { createPortal } from 'react-dom';
import { Icon } from './Icon';
import { StatusBadge } from './StatusBadge';

export function StatusPicker({ value, options, onChange, label = 'Change status', disabled = false }) {
  const [open, setOpen] = useState(false);
  const [position, setPosition] = useState(null);
  const triggerRef = useRef(null);
  const menuRef = useRef(null);

  const close = () => setOpen(false);
  const toggle = () => {
    if (disabled) return;
    if (open) return close();
    const rect = triggerRef.current?.getBoundingClientRect();
    if (rect) setPosition({ top: rect.bottom + 6, left: rect.left });
    setOpen(true);
  };

  useEffect(() => {
    if (!open) return undefined;
    const onPointerDown = (event) => {
      if (!triggerRef.current?.contains(event.target) && !menuRef.current?.contains(event.target)) close();
    };
    const onViewportChange = () => close();
    document.addEventListener('mousedown', onPointerDown);
    window.addEventListener('resize', onViewportChange);
    window.addEventListener('scroll', onViewportChange, true);
    return () => {
      document.removeEventListener('mousedown', onPointerDown);
      window.removeEventListener('resize', onViewportChange);
      window.removeEventListener('scroll', onViewportChange, true);
    };
  }, [open]);

  const choose = (nextValue) => {
    close();
    if (nextValue !== value) onChange(nextValue);
  };

  return (
    <span className="status-picker">
      <button
        ref={triggerRef}
        type="button"
        className="status-picker-trigger"
        aria-label={label}
        aria-haspopup="menu"
        aria-expanded={open}
        disabled={disabled}
        onClick={toggle}
      >
        <StatusBadge status={value} />
        <Icon name="chevronDown" size={13} className="status-picker-chevron" />
      </button>
      {open && position && createPortal(
        <div ref={menuRef} className="status-picker-menu" role="menu" style={position}>
          <p>Status</p>
          {options.map((option) => (
            <button
              key={option.value}
              type="button"
              role="menuitemradio"
              aria-checked={option.value === value}
              className={option.value === value ? 'selected' : ''}
              onClick={() => choose(option.value)}
            >
              <StatusBadge status={option.value} />
            </button>
          ))}
        </div>,
        document.body,
      )}
    </span>
  );
}
