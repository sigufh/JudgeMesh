import type { DispatcherStatus, DispatcherWorker } from '../types';

export function asArray<T>(value: unknown): T[] {
  return Array.isArray(value) ? (value as T[]) : [];
}

export function normalizeDispatcherStatus(value: unknown): DispatcherStatus | null {
  if (!value || typeof value !== 'object') {
    return null;
  }

  const source = value as {
    leader?: Record<string, unknown>;
    workers?: Record<string, unknown>;
  };

  const leader = source.leader ?? {};
  const workers = source.workers ?? {};

  return {
    leader: {
      leader: typeof leader.leader === 'string' ? leader.leader : null,
      self: typeof leader.self === 'string' ? leader.self : '-',
      isLeader: Boolean(leader.isLeader),
      mode: typeof leader.mode === 'string' ? leader.mode : 'unknown',
      leaseId: typeof leader.leaseId === 'number' ? leader.leaseId : 0,
      leaderKey: typeof leader.leaderKey === 'string' ? leader.leaderKey : '',
      lastChangedAt: typeof leader.lastChangedAt === 'string' ? leader.lastChangedAt : new Date(0).toISOString(),
    },
    workers: {
      configured: asArray<string>(workers.configured),
      availableCount: typeof workers.availableCount === 'number' ? workers.availableCount : 0,
      totalCount: typeof workers.totalCount === 'number' ? workers.totalCount : 0,
      workers: asArray<DispatcherWorker>(workers.workers),
    },
  };
}
