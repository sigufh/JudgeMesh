import client from './client';
import type { Contest, RankEntry } from '../types';

export const fetchContests = () => client.get<Contest[]>('/api/contest/list');

export const registerContest = (id: string | number) =>
  client.post<Contest>(`/api/contest/${id}/register`);

export const fetchContestRank = (id: string | number) =>
  client.get<RankEntry[]>(`/api/contest/${id}/rank`);

export const fetchGlobalRank = () => client.get<RankEntry[]>('/api/rank/global');
