const configuredBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim();

export class ApiUnavailableError extends Error {
  constructor(message = 'The backend API is not configured yet.') {
    super(message);
    this.name = 'ApiUnavailableError';
  }
}

export async function request(path, options = {}) {
  if (!configuredBaseUrl) {
    throw new ApiUnavailableError();
  }

  const response = await fetch(`${configuredBaseUrl.replace(/\/$/, '')}${path}`, {
    ...options,
    headers: {
      Accept: 'application/json',
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...options.headers,
    },
  });

  if (!response.ok) {
    throw new Error(`Request failed with status ${response.status}.`);
  }

  if (response.status === 204) return null;
  return response.json();
}

