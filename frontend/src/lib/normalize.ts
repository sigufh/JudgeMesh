import type { DispatcherStatus, DispatcherWorker } from '../types';

export function asArray<T>(value: unknown): T[] {
  if (Array.isArray(value)) {
    return value as T[];
  }
  if (value && typeof value === 'object') {
    const source = value as { items?: unknown; data?: unknown; list?: unknown };
    if (Array.isArray(source.items)) return source.items as T[];
    if (Array.isArray(source.data)) return source.data as T[];
    if (Array.isArray(source.list)) return source.list as T[];
  }
  return [];
}

export function asStringArray(value: unknown): string[] {
  return asArray<string>(value).filter((item): item is string => typeof item === 'string');
}

export function asNumberArray(value: unknown): number[] {
  return asArray<number>(value).filter((item): item is number => typeof item === 'number');
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
      currentLeader: typeof leader.currentLeader === 'string'
        ? leader.currentLeader
        : typeof leader.leader === 'string'
          ? leader.leader
          : null,
      self: typeof leader.self === 'string' ? leader.self : '-',
      isLeader: Boolean(leader.isLeader),
      mode: typeof leader.mode === 'string' ? leader.mode : 'unknown',
      leaseId: typeof leader.leaseId === 'number' ? leader.leaseId : 0,
      leaderKey: typeof leader.leaderKey === 'string' ? leader.leaderKey : '',
      lastChangedAt: typeof leader.lastChangedAt === 'string' ? leader.lastChangedAt : new Date(0).toISOString(),
    },
    workers: {
      configured: asStringArray(workers.configured),
      availableCount: typeof workers.availableCount === 'number' ? workers.availableCount : 0,
      totalCount: typeof workers.totalCount === 'number' ? workers.totalCount : 0,
      workers: asArray<Record<string, unknown>>(workers.workers).map((worker) => ({
        url: typeof worker.url === 'string' ? worker.url : '-',
        inflight: typeof worker.inflight === 'number' ? worker.inflight : 0,
        maxConcurrency: typeof worker.maxConcurrency === 'number' ? worker.maxConcurrency : undefined,
        available: Boolean(worker.available),
        state: typeof worker.state === 'string' ? worker.state : 'DOWN',
        blacklistedUntil: typeof worker.blacklistedUntil === 'string' ? worker.blacklistedUntil : null,
        lastError: typeof worker.lastError === 'string' ? worker.lastError : null,
      }) satisfies DispatcherWorker),
    },
  };
}
