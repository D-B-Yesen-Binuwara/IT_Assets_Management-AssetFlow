import { request } from '../api/client';

const collection = (path) => ({
  list: (params = {}) => {
    const query = new URLSearchParams(
      Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== ''),
    ).toString();
    return request(`${path}${query ? `?${query}` : ''}`);
  },
  get: (id) => request(`${path}/${id}`),
  create: (payload) => request(path, { method: 'POST', body: JSON.stringify(payload) }),
  update: (id, payload) => request(`${path}/${id}`, { method: 'PATCH', body: JSON.stringify(payload) }),
  remove: (id) => request(`${path}/${id}`, { method: 'DELETE' }),
});

const assetCollection = collection('/assets');

export const assetService = {
  ...assetCollection,
  transfer: (assetId, payload) =>
    request(`/assets/${assetId}/transfers`, {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  transferHistory: (assetId) => request(`/assets/${assetId}/transfers`),
  lifecycle: (assetId) => request(`/assets/${assetId}/lifecycle`),
  valuations: (assetId) => request(`/assets/${assetId}/valuations`),
  disposal: (assetId) => request(`/assets/${assetId}/disposal`),
  changeStatus: (assetId, payload) => request(`/assets/${assetId}/status`, {
    method: 'POST',
    body: JSON.stringify(payload),
  }),
};
export const employeeService = collection('/employees');
export const departmentService = collection('/departments');
export const categoryService = collection('/categories');
export const assignmentService = collection('/assignments');
export const maintenanceService = collection('/maintenance');
export const warrantyService = collection('/warranty');
warrantyService.claims = (params = {}) => {
  const query = new URLSearchParams(Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== '')).toString();
  return request(`/warranty/claims${query ? `?${query}` : ''}`);
};
warrantyService.createClaim = (payload) => request('/warranty/claims', { method: 'POST', body: JSON.stringify(payload) });
warrantyService.updateClaim = (id, payload) => request(`/warranty/claims/${id}`, { method: 'PATCH', body: JSON.stringify(payload) });
export const vendorService = collection('/vendors');
export const licenseService = collection('/licenses');
licenseService.assignments = () => request('/licenses/assignments');
licenseService.assign = (payload) => request('/licenses/assignments', { method: 'POST', body: JSON.stringify(payload) });
licenseService.revoke = (id) => request(`/licenses/assignments/${id}/revoke`, { method: 'PATCH' });
export const procurementService = collection('/procurement');
procurementService.invoices = () => request('/invoices');
procurementService.contracts = (params = {}) => {
  const query = new URLSearchParams(Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== '')).toString();
  return request(`/vendor-contracts${query ? `?${query}` : ''}`);
};
export const locationService = collection('/locations');
export const notificationService = collection('/notifications');
export const userService = collection('/users');
notificationService.markRead = (id) => request(`/notifications/${id}/read`, { method: 'PATCH' });
notificationService.preferences = () => request('/notifications/preferences');
notificationService.savePreference = (payload) => request('/notifications/preferences', {
  method: 'POST',
  body: JSON.stringify(payload),
});

export const dashboardService = {
  summary: () => request('/dashboard/summary'),
  activity: () => request('/dashboard/activity'),
};

export const reportService = {
  summary: (params = {}) => {
    const query = new URLSearchParams(
      Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== ''),
    ).toString();
    return request(`/reports/summary${query ? `?${query}` : ''}`);
  },
};

export const settingsService = {
  get: () => request('/settings'),
  update: (payload) => request('/settings', { method: 'PATCH', body: JSON.stringify(payload) }),
  system: () => request('/settings/system'),
  saveSystem: (payload) => request('/settings/system', { method: 'POST', body: JSON.stringify(payload) }),
  templates: () => request('/settings/email-templates'),
  createTemplate: (payload) => request('/settings/email-templates', { method: 'POST', body: JSON.stringify(payload) }),
  updateTemplate: (id, payload) => request(`/settings/email-templates/${id}`, { method: 'PATCH', body: JSON.stringify(payload) }),
};
