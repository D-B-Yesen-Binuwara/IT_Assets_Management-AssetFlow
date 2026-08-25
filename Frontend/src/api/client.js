const configuredBaseUrl = (import.meta.env.VITE_API_BASE_URL || '/api').trim();
const csrfCookieName = (import.meta.env.VITE_CSRF_COOKIE_NAME || 'XSRF-TOKEN').trim();
const stateChangingMethods = new Set(['POST', 'PUT', 'PATCH', 'DELETE']);
const authExpiredEvent = 'assetflow:auth-expired';

let csrfToken = null;
let csrfRequest = null;

export class ApiError extends Error {
  constructor(message, { status = 0, details = null } = {}) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.details = details;
  }
}

export class ApiUnavailableError extends ApiError {
  constructor(message = 'The backend API is unavailable.') {
    super(message);
    this.name = 'ApiUnavailableError';
  }
}

function apiUrl(path) {
  const base = configuredBaseUrl.replace(/\/+$/, '');
  const suffix = path.startsWith('/') ? path : `/${path}`;
  return `${base}${suffix}`;
}

function readCookie(name) {
  if (typeof document === 'undefined') return null;

  const prefix = `${name}=`;
  const match = document.cookie
    .split(';')
    .map((value) => value.trim())
    .find((value) => value.startsWith(prefix));

  if (!match) return null;

  try {
    return decodeURIComponent(match.slice(prefix.length));
  } catch {
    return match.slice(prefix.length);
  }
}

async function parseResponse(response) {
  const text = await response.text();
  if (!text) return null;

  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

async function fetchCsrfToken() {
  let response;
  try {
    response = await fetch(apiUrl('/auth/csrf'), {
      method: 'GET',
      credentials: 'include',
      headers: { Accept: 'application/json' },
    });
  } catch {
    throw new ApiUnavailableError(
      'Unable to reach the backend. Confirm that the API is running and the frontend URL is configured.',
    );
  }
  const payload = await parseResponse(response);

  if (!response.ok) {
    throw new ApiError(payload?.message || 'Could not initialize CSRF protection.', {
      status: response.status,
      details: payload,
    });
  }

  const token = readCookie(csrfCookieName) || payload?.token;
  if (!token) throw new ApiError('The backend did not provide a CSRF token.', { status: 500 });
  return token;
}

async function ensureCsrfToken() {
  csrfToken = readCookie(csrfCookieName) || csrfToken;
  if (csrfToken) return csrfToken;

  if (!csrfRequest) {
    csrfRequest = fetchCsrfToken().finally(() => {
      csrfRequest = null;
    });
  }

  csrfToken = await csrfRequest;
  return csrfToken;
}

function notifyAuthenticationExpired() {
  if (typeof window !== 'undefined') window.dispatchEvent(new Event(authExpiredEvent));
}

export function onAuthenticationExpired(listener) {
  if (typeof window === 'undefined') return () => {};
  window.addEventListener(authExpiredEvent, listener);
  return () => window.removeEventListener(authExpiredEvent, listener);
}

export async function request(path, options = {}) {
  const method = (options.method || 'GET').toUpperCase();
  const { skipCsrf = false, skipUnauthorized = false, ...fetchOptions } = options;
  const headers = new Headers(fetchOptions.headers || {});
  headers.set('Accept', 'application/json');

  if (fetchOptions.body !== undefined && !(fetchOptions.body instanceof FormData)) {
    if (!headers.has('Content-Type')) headers.set('Content-Type', 'application/json');
  }

  if (stateChangingMethods.has(method) && !skipCsrf) {
    headers.set('X-XSRF-TOKEN', await ensureCsrfToken());
  }

  let response;
  try {
    response = await fetch(apiUrl(path), {
      ...fetchOptions,
      method,
      credentials: 'include',
      headers,
    });
  } catch {
    throw new ApiUnavailableError(
      'Unable to reach the backend. Confirm that the API is running and the frontend URL is configured.',
    );
  }

  const payload = await parseResponse(response);

  if (!response.ok) {
    if (response.status === 401 && !skipUnauthorized) notifyAuthenticationExpired();

    throw new ApiError(
      payload?.message || payload?.error || `Request failed with status ${response.status}.`,
      { status: response.status, details: payload },
    );
  }

  if (response.status === 204) return null;
  return payload;
}

export { apiUrl, csrfCookieName };
