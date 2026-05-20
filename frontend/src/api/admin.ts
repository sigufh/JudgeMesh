import client from './client';
import type { DispatcherStatus } from '../types';

export const fetchDispatcherStatus = () =>
  client.get<DispatcherStatus>('/api/admin/dispatcher/status');

export const relinquishDispatcherLeader = () =>
  client.post<DispatcherStatus>('/api/admin/dispatcher/leader/relinquish');

export const reacquireDispatcherLeader = () =>
  client.post<DispatcherStatus>('/api/admin/dispatcher/leader/reacquire');
