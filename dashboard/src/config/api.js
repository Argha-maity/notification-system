export const API_CONFIG = {
  BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8888/api',
  REFRESH_INTERVAL: 5000,
  REQUEST_TIMEOUT: 10000,
  TOKEN_KEY: 'notification_token',
  ENDPOINTS: {
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
    TIMELINE: '/analytics/timeline',
    PROCESSING_TIME: '/analytics/processing-time',
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
      timeout: API_CONFIG.REQUEST_TIMEOUT,
      ...options,
      headers,
    });
    
    if (!response.ok) {
      if (response.status === 401) {
        localStorage.removeItem(API_CONFIG.TOKEN_KEY);
        window.location.href = '/login';
      }
      throw new Error(`API Error: ${response.statusText}`);
    }
    
    return await response.json();
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
