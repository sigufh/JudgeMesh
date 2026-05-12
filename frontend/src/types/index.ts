// Mirrors services/api/src/main/java/com/judgemesh/api/dto/*.java

export type UserRole = 'STUDENT' | 'SETTER' | 'ADMIN';

export interface User {
  id: number;
  username: string;
  email: string;
  nickname?: string;
  avatarUrl?: string;
  balance?: number;
  totalAc?: number;
  totalSubmit?: number;
  /** STUDENT / SETTER / ADMIN */
  role: UserRole | string;
  roles?: Array<UserRole | string>;
}

export type ProblemDifficulty = 'EASY' | 'MEDIUM' | 'HARD';
export type ProblemStatus = 'DRAFT' | 'PUBLISHED' | 'OFFLINE';

export interface Problem {
  id: number;
  title: string;
  description: string;
  difficulty: ProblemDifficulty | string;
  tags: string[];
  timeLimitMs: number;
  memoryLimitMb: number;
  setterId?: number;
  published?: boolean;
  totalSubmit?: number;
  totalAc?: number;
  /** DRAFT / PUBLISHED / OFFLINE */
  status: ProblemStatus | string;
}

export type SubmitStatus =
  | 'PENDING'
  | 'JUDGING'
  | 'AC'
  | 'WA'
  | 'TLE'
  | 'MLE'
  | 'RE'
  | 'CE'
  | 'SE';

export interface Submit {
  id: number;
  userId: number;
  problemId: number;
  contestId?: number;
  language: string;
  /** PENDING / JUDGING / AC / WA / TLE / MLE / RE / CE / SE */
  status: SubmitStatus | string;
  score?: number;
  timeUsedMs?: number;
  memoryUsedKb?: number;
  judgeMessage?: string;
  judgedByWorker?: string;
  submittedAt?: string;
  judgedAt?: string;
  /** ISO8601 timestamp from java.time.Instant */
  createdAt: string;
}

export interface Contest {
  id: number;
  title: string;
  description?: string;
  startTime: string;
  endTime: string;
  freezeBeforeMin: number;
  problemIds: number[];
  frozen?: boolean;
  registered?: boolean;
}

export interface RankEntry {
  rank: number;
  userId: number;
  username: string;
  solved: number;
  penaltyMinutes: number;
  score: number;
}

export interface ApiResponse<T> {
  code: string;
  message: string;
  data: T;
  timestamp?: string;
}

export interface LoginResponse {
  token: string;
  accessToken?: string;
  tokenType?: string;
  expiresIn?: number;
  user: User;
}
