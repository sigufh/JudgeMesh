import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';

export const TOKEN_KEY = 'judgemesh.token';
export const USER_KEY = 'judgemesh.user';

const baseURL = import.meta.env.VITE_API_BASE ?? '';

export const client = axios.create({
  baseURL,
  timeout: 10_000,
});

client.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.set('Authorization', `Bearer ${token}`);
  }
  return config;
});

client.interceptors.response.use(
  (response) => {
    const payload = response.data as { code?: string; data?: unknown };
    if (payload && typeof payload === 'object' && 'code' in payload && 'data' in payload) {
      response.data = payload.data;
    }
    return response;
  },
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
      // Avoid redirect loop if we're already on /login
      if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  },
);

export default client;
