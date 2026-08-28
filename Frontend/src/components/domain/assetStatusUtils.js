export const todayValue = () => new Date().toISOString().slice(0, 10);

export const initialStatusDetails = () => ({
  statusReason: '',
  statusDescription: '',
  statusEffectiveDate: todayValue(),
  maintenancePriority: 'MEDIUM',
  maintenanceStartDate: todayValue(),
  maintenanceDueDate: '',
  maintenanceVendorId: '',
  maintenanceAssignedToEmployeeId: '',
  disposalDate: todayValue(),
  disposalMethod: 'OTHER',
  disposalProceeds: '',
  statusNotes: '',
});
