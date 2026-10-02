export const API_CONFIG = {
  BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8888/api',
  REFRESH_INTERVAL: 5000,
  REQUEST_TIMEOUT: 10000,
  TOKEN_KEY: 'notification_token',
  USER_KEY: 'notification_user',
  ENDPOINTS: {
    AUTH_LOGIN: '/auth/login',
    AUTH_SIGNUP: '/auth/signup',
    STATS: '/jobs/stats',
    JOBS: '/jobs',
    JOB_DETAILS: (id) => `/jobs/${id}`,
    JOB_ATTEMPTS: (id) => `/jobs/${id}/attempts`,
    JOB_RETRY: (id) => `/jobs/${id}/retry`,
    JOB_DELETE: (id) => `/jobs/${id}`,
    DEAD_LETTER_QUEUE: '/jobs/dead-letter',
    WORKERS: '/workers',
    WORKER_STATS: '/workers/stats',
    ANALYTICS: '/analytics',
    TIMELINE: '/jobs/analytics/timeline',
    PROCESSING_TIME: '/jobs/analytics/processing-time',
    ADMIN_STATS: '/admin/stats',
    ADMIN_QUEUE_CLEAR: '/admin/queue/clear',
    ADMIN_DLQ_PURGE: '/admin/queue/dead-letter/purge',
    ADMIN_RETRY_ALL: '/admin/jobs/retry-all',
  }
};

export async function apiCall(endpoint, options = {}) {
  const token = localStorage.getItem(API_CONFIG.TOKEN_KEY);
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };
  
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  
  const url = `${API_CONFIG.BASE_URL}${endpoint}`;
  
  try {
    const response = await fetch(url, {
      ...options,
      headers,
    });
    
    if (!response.ok) {
      if (response.status === 401) {
        localStorage.removeItem(API_CONFIG.TOKEN_KEY);
        if (typeof window !== 'undefined') {
          window.dispatchEvent(new CustomEvent('auth:unauthorized'));
        }
      }
      let errorMsg = `API Error: ${response.status} ${response.statusText}`;
      try {
        const errJson = await response.json();
        if (errJson.message) errorMsg = errJson.message;
        else if (errJson.error) errorMsg = errJson.error;
      } catch {
        try {
          const errText = await response.text();
          if (errText) errorMsg = errText;
        } catch {
          // ignore
        }
      }
      throw new Error(errorMsg);
    }
    
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      return await response.json();
    }
    return await response.text();
  } catch (error) {
    console.error('API Call failed:', error);
    throw error;
  }
}

export function apiGet(endpoint) {
  return apiCall(endpoint, { method: 'GET' });
}

export function apiPost(endpoint, data) {
  return apiCall(endpoint, {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export function apiPut(endpoint, data) {
  return apiCall(endpoint, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

export function apiDelete(endpoint) {
  return apiCall(endpoint, { method: 'DELETE' });
}

export default API_CONFIG;

