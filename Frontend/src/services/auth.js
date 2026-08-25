import { request } from '../api/client';

export const authService = {
  login: (payload) => request('/auth/login', { method: 'POST', body: JSON.stringify(payload) }),
  bootstrap: (payload) => request('/auth/bootstrap', { method: 'POST', body: JSON.stringify(payload) }),
  me: () => request('/auth/me', { skipUnauthorized: true }),
  logout: () => request('/auth/logout', { method: 'POST' }),
};
