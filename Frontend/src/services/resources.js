import { request } from '../api/client';

const collection = (path) => ({
  list: (params = {}) => {
    const query = new URLSearchParams(params).toString();
    return request(`${path}${query ? `?${query}` : ''}`);
  },
  get: (id) => request(`${path}/${id}`),
  create: (payload) => request(path, { method: 'POST', body: JSON.stringify(payload) }),
  update: (id, payload) => request(`${path}/${id}`, { method: 'PATCH', body: JSON.stringify(payload) }),
  remove: (id) => request(`${path}/${id}`, { method: 'DELETE' }),
});

export const assetService = collection('/assets');
export const employeeService = collection('/employees');
export const assignmentService = collection('/assignments');
export const maintenanceService = collection('/maintenance');
export const warrantyService = collection('/warranty');
export const vendorService = collection('/vendors');
export const licenseService = collection('/licenses');
export const procurementService = collection('/procurement');
export const locationService = collection('/locations');
export const notificationService = collection('/notifications');

export const dashboardService = {
  summary: () => request('/dashboard/summary'),
  activity: () => request('/dashboard/activity'),
};

export const reportService = {
  summary: (params = {}) => {
    const query = new URLSearchParams(params).toString();
    return request(`/reports/summary${query ? `?${query}` : ''}`);
  },
};

export const settingsService = {
  get: () => request('/settings'),
  update: (payload) => request('/settings', { method: 'PATCH', body: JSON.stringify(payload) }),
};

