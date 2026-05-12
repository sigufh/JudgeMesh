import client, { TOKEN_KEY, USER_KEY } from './client';
import type { LoginResponse, User } from '../types';

export const login = (body: { email: string; password: string }) =>
  client.post<LoginResponse>('/api/auth/login', body);

export const register = (body: { username: string; email: string; password: string; role?: string }) =>
  client.post<LoginResponse>('/api/auth/register', body);

export const me = () => client.get<User>('/api/user/me');

export function persistSession(session: LoginResponse) {
  localStorage.setItem(TOKEN_KEY, session.token);
  localStorage.setItem(USER_KEY, JSON.stringify(session.user));
}

export function currentUser(): User | null {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as User;
  } catch {
    return null;
  }
}

export function logout() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}
