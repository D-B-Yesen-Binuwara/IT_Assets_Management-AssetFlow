import { recordId, employeeLabel } from '../../utils/collections';

export function AssetStatusFields({ status, form, update, vendors = [], employees = [], disabled = false, showReason = true }) {
  const maintenance = status === 'UNDER_MAINTENANCE';
  const disposed = status === 'DISPOSED';

  if (!showReason) return null;

  return (
    <div className="status-workflow-fields">
      <label className="form-field-wide">
        <span className="field-label">{maintenance ? 'Issue / reason' : 'Reason'} <span className="optional-label">(optional)</span></span>
        <input value={form.statusReason} onChange={(event) => update('statusReason', event.target.value)} placeholder={maintenance ? 'Describe the maintenance issue' : 'Explain why the status is changing'} disabled={disabled} />
      </label>

      {maintenance && (
        <>
          <label className="form-field-wide">
            <span className="field-label">Description <span className="optional-label">(optional)</span></span>
            <textarea value={form.statusDescription} onChange={(event) => update('statusDescription', event.target.value)} placeholder="Add technical details or symptoms" rows="3" disabled={disabled} />
          </label>
          <label>
            Priority
            <select value={form.maintenancePriority} onChange={(event) => update('maintenancePriority', event.target.value)} disabled={disabled}>
              {['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'].map((priority) => <option key={priority} value={priority}>{priority.replace('_', ' ')}</option>)}
            </select>
          </label>
          <label>
            Start date
            <input type="date" value={form.maintenanceStartDate} onChange={(event) => update('maintenanceStartDate', event.target.value)} required disabled={disabled} />
          </label>
          <label>
            <span className="field-label">Due date <span className="optional-label">(optional)</span></span>
            <input type="date" value={form.maintenanceDueDate} min={form.maintenanceStartDate || undefined} onChange={(event) => update('maintenanceDueDate', event.target.value)} disabled={disabled} />
          </label>
          <label>
            <span className="field-label">Vendor <span className="optional-label">(optional)</span></span>
            <select value={form.maintenanceVendorId} onChange={(event) => update('maintenanceVendorId', event.target.value)} disabled={disabled}>
              <option value="">No vendor selected</option>
              {vendors.filter((vendor) => String(vendor.status || 'ACTIVE').toUpperCase() === 'ACTIVE').map((vendor) => <option key={recordId(vendor, 'id')} value={recordId(vendor, 'id')}>{vendor.name}</option>)}
            </select>
          </label>
          <label className="form-field-wide">
            <span className="field-label">Assigned employee <span className="optional-label">(optional)</span></span>
            <select value={form.maintenanceAssignedToEmployeeId} onChange={(event) => update('maintenanceAssignedToEmployeeId', event.target.value)} disabled={disabled}>
              <option value="">Unassigned</option>
              {employees.filter((employee) => String(employee.status || '').toUpperCase() === 'ACTIVE').map((employee) => <option key={recordId(employee, 'id')} value={recordId(employee, 'id')}>{employeeLabel(employee)} ({employee.employeeNumber})</option>)}
            </select>
          </label>
        </>
      )}

      {disposed && (
        <>
          <label>
            Disposal date
            <input type="date" value={form.disposalDate} onChange={(event) => update('disposalDate', event.target.value)} required disabled={disabled} />
          </label>
          <label>
            Disposal method
            <select value={form.disposalMethod} onChange={(event) => update('disposalMethod', event.target.value)} disabled={disabled}>
              {['RECYCLED', 'SOLD', 'DONATED', 'DESTROYED', 'OTHER'].map((method) => <option key={method} value={method}>{method.replace('_', ' ')}</option>)}
            </select>
          </label>
          <label>
            <span className="field-label">Proceeds (Rs) <span className="optional-label">(optional)</span></span>
            <input type="number" min="0" step="0.01" value={form.disposalProceeds} onChange={(event) => update('disposalProceeds', event.target.value)} placeholder="0.00" disabled={disabled} />
          </label>
          <label className="form-field-wide">
            <span className="field-label">Disposal notes <span className="optional-label">(optional)</span></span>
            <textarea value={form.statusNotes} onChange={(event) => update('statusNotes', event.target.value)} placeholder="Add disposal details" rows="3" disabled={disabled} />
          </label>
        </>
      )}

      {!maintenance && !disposed && (
        <label>
          Effective date
          <input type="date" value={form.statusEffectiveDate} onChange={(event) => update('statusEffectiveDate', event.target.value)} required disabled={disabled} />
        </label>
      )}
    </div>
  );
}
