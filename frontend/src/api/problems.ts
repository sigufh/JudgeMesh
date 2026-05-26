import client from './client';
import type { Problem } from '../types';

export const fetchProblems = (params?: { q?: string; tag?: string; difficulty?: string }) =>
  client.get<Problem[]>('/api/problem/list', { params });

export const fetchProblem = (id: string | number) =>
  client.get<Problem>(`/api/problem/${id}`);
