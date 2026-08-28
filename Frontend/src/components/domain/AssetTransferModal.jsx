import { useState } from 'react';
import { Button } from '../common/Button';
import { Icon } from '../common/Icon';
import { Modal } from '../common/Modal';
import { employeeLabel, locationLabel, recordId } from '../../utils/collections';

const today = () => new Date().toISOString().slice(0, 10);

const initialForm = (assignment) => ({
  newEmployeeId: '',
  newLocationId: recordId(assignment, 'locationId'),
  reason: '',
  transferredAt: today(),
  expectedReturnDate: assignment?.expectedReturnDate || '',
  notes: '',
});

const assetLabel = (assignment) =>
  assignment?.assetName ||
  assignment?.asset?.name ||
  assignment?.assetTag ||
  recordId(assignment, 'assetId') ||
  '-';

const currentEmployee = (assignment) =>
  assignment?.employeeName ||
  assignment?.employee?.name ||
  [assignment?.employee?.firstName, assignment?.employee?.lastName].filter(Boolean).join(' ') ||
  recordId(assignment, 'employeeId') ||
  '-';

const currentLocation = (assignment) =>
  assignment?.locationName ||
  assignment?.location?.name ||
  recordId(assignment, 'locationId') ||
  'Not specified';

export function AssetTransferModal({
  open,
  assignment,
  employees = [],
  locations = [],
  lookupStatus = 'idle',
  lookupError,
  actionStatus = 'idle',
  actionError,
  onClose,
  onSubmit,
}) {
  const [form, setForm] = useState(initialForm(assignment));

  if (!assignment) return null;

  const currentEmployeeId = recordId(assignment, 'employeeId');
  const availableEmployees = employees.filter((employee) => recordId(employee, 'id') !== currentEmployeeId);
  const isBusy = lookupStatus === 'loading' || actionStatus === 'loading';

  const updateField = (key, value) => setForm((current) => ({ ...current, [key]: value }));

  const submit = async (event) => {
    event.preventDefault();
    await onSubmit({
      ...form,
      newLocationId: form.newLocationId || null,
      expectedReturnDate: form.expectedReturnDate || null,
    });
  };

  return (
    <Modal
      open={open}
      onClose={onClose}
      title="Transfer asset"
      description="Move this asset to another employee and preserve the complete transfer history."
    >
      <form className="form-grid" onSubmit={submit}>
        <div className="transfer-context">
          <div>
            <span>Asset</span>
            <strong>{assetLabel(assignment)}</strong>
          </div>
          <div>
            <span>Current employee</span>
            <strong>{currentEmployee(assignment)}</strong>
          </div>
          <div>
            <span>Current location</span>
            <strong>{currentLocation(assignment)}</strong>
          </div>
        </div>

        {lookupError && (
          <div className="inline-error">
            <Icon name="warning" size={16} />
            {lookupError.message}
          </div>
        )}

        <div className="form-fields">
          <label>
            New employee
            <select
              value={form.newEmployeeId}
              onChange={(event) => updateField('newEmployeeId', event.target.value)}
              required
              disabled={isBusy || availableEmployees.length === 0}
            >
              <option value="">{lookupStatus === 'loading' ? 'Loading employees...' : 'Select employee'}</option>
              {availableEmployees.map((employee) => {
                const id = recordId(employee, 'id');
                return (
                  <option key={id} value={id}>
                    {employeeLabel(employee)}
                  </option>
                );
              })}
            </select>
          </label>

          <label>
            <span className="field-label">New branch <span className="optional-label">(optional)</span></span>
            <select
              value={form.newLocationId}
              onChange={(event) => updateField('newLocationId', event.target.value)}
              disabled={isBusy}
            >
              <option value="">Keep current location</option>
              {locations.map((location) => {
                const id = recordId(location, 'id');
                return (
                  <option key={id} value={id}>
                    {locationLabel(location)}
                  </option>
                );
              })}
            </select>
          </label>

          <label>
            Transfer reason
            <input
              value={form.reason}
              onChange={(event) => updateField('reason', event.target.value)}
              placeholder="e.g. Employee reassignment"
              required
              disabled={isBusy}
            />
          </label>

          <label>
            Transfer date
            <input
              type="date"
              value={form.transferredAt}
              onChange={(event) => updateField('transferredAt', event.target.value)}
              required
              disabled={isBusy}
            />
          </label>

          <label>
            <span className="field-label">New closing date <span className="optional-label">(optional)</span></span>
            <input
              type="date"
              value={form.expectedReturnDate}
              onChange={(event) => updateField('expectedReturnDate', event.target.value)}
              min={form.transferredAt || undefined}
              disabled={isBusy}
            />
          </label>

          <label>
            <span className="field-label">Notes <span className="optional-label">(optional)</span></span>
            <textarea
              value={form.notes}
              onChange={(event) => updateField('notes', event.target.value)}
              placeholder="Add handover or approval notes"
              rows="3"
              disabled={isBusy}
            />
          </label>
        </div>

        {actionError && (
          <div className="inline-error">
            <Icon name="warning" size={16} />
            {actionError.message}
          </div>
        )}

        <div className="modal-actions">
          <Button variant="outline" onClick={onClose} disabled={actionStatus === 'loading'}>
            Cancel
          </Button>
          <Button type="submit" icon="transfer" disabled={isBusy || availableEmployees.length === 0}>
            {actionStatus === 'loading' ? 'Submitting...' : 'Submit transfer'}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
