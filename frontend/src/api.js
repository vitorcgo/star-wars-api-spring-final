const DEFAULT_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

function cleanBaseUrl(value) {
  return String(value || '').trim().replace(/\/$/, '');
}

export function getDefaultBaseUrl() {
  return cleanBaseUrl(DEFAULT_BASE_URL);
}

export function buildUrl(baseUrl, path, params = {}) {
  const url = new URL(`${cleanBaseUrl(baseUrl)}${path}`);
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && String(value).trim() !== '') {
      url.searchParams.set(key, value);
    }
  });
  return url.toString();
}

export async function apiRequest(baseUrl, path, options = {}) {
  const {
    method = 'GET',
    params = {},
    body,
    apiKey = '',
    idempotencyKey = '',
    version = '',
    headers = {}
  } = options;

  const requestHeaders = {
    Accept: 'application/json',
    ...headers
  };

  if (body !== undefined && body !== null) {
    requestHeaders['Content-Type'] = 'application/json';
  }

  if (apiKey) {
    requestHeaders['X-API-Key'] = apiKey;
  }

  if (idempotencyKey) {
    requestHeaders['X-Idempotency-Key'] = idempotencyKey;
  }

  if (version) {
    requestHeaders['X-API-Version'] = String(version);
  }

  const response = await fetch(buildUrl(baseUrl, path, params), {
    method,
    headers: requestHeaders,
    body: body !== undefined && body !== null ? JSON.stringify(body) : undefined
  });

  const contentType = response.headers.get('content-type') || '';
  const payload = contentType.includes('application/json')
    ? await response.json()
    : await response.text();

  if (!response.ok) {
    const error = new Error(
      payload?.mensagem || payload?.message || `HTTP ${response.status}`
    );
    error.status = response.status;
    error.payload = payload;
    throw error;
  }

  return payload;
}

export function unwrapCollection(payload) {
  if (!payload || typeof payload !== 'object') {
    return { items: [], page: null, raw: payload };
  }

  if (Array.isArray(payload.films)) {
    return {
      items: payload.films,
      page: {
        number: payload.currentPage ?? 0,
        totalPages: payload.totalPages ?? 1,
        totalElements: payload.totalElements ?? payload.films.length
      },
      raw: payload
    };
  }

  if (payload._embedded && typeof payload._embedded === 'object') {
    const key = Object.keys(payload._embedded)[0];
    const items = Array.isArray(payload._embedded[key]) ? payload._embedded[key] : [];
    const page = payload.page
      ? {
          number: payload.page.number ?? 0,
          totalPages: payload.page.totalPages ?? 1,
          totalElements: payload.page.totalElements ?? items.length
        }
      : null;
    return { items, page, raw: payload };
  }

  return { items: [], page: null, raw: payload };
}

export function isJsonLike(value) {
  return value && typeof value === 'object';
}
