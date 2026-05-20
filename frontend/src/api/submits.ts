import client from './client';
import type { Submit } from '../types';

export const fetchMySubmits = () => client.get<Submit[]>('/api/submit/mine');

export const fetchSubmit = (id: string | number) =>
  client.get<Submit>(`/api/submit/${id}`);

export const createSubmit = (body: {
  problemId: number;
  contestId?: number;
  language: string;
  code: string;
}) => client.post<Submit>('/api/submit', body);
