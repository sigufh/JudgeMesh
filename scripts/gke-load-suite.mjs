#!/usr/bin/env node

const terminalStatuses = new Set(['AC', 'WA', 'TLE', 'MLE', 'RE', 'CE', 'SE']);
const defaultReadPaths = [
  '/api/problem/list',
  '/api/contest/list',
  '/api/rank/global',
  '/api/admin/dispatcher/status',
];

const defaults = {
  baseUrl: process.env.BASE_URL ?? 'http://127.0.0.1:8080',
  frontendUrl: process.env.FRONTEND_URL ?? '',
  mode: process.env.MODE ?? 'full',
  total: numberEnv('TOTAL', 40),
  concurrency: numberEnv('CONCURRENCY', 20),
  users: numberEnv('USERS', 0),
  readTotal: numberEnv('READ_TOTAL', 120),
  readConcurrency: numberEnv('READ_CONCURRENCY', 30),
  readPaths: process.env.READ_PATHS ?? defaultReadPaths.join(','),
  problemId: process.env.PROBLEM_ID ? Number(process.env.PROBLEM_ID) : 0,
  language: process.env.LANGUAGE ?? 'PYTHON',
  password: process.env.PASSWORD ?? 'Load@12345',
  userPrefix: process.env.USER_PREFIX ?? `load-${Date.now()}`,
  pollIntervalMs: numberEnv('POLL_INTERVAL_MS', 500),
  timeoutMs: numberEnv('TIMEOUT_MS', 60_000),
  observeEveryMs: numberEnv('OBSERVE_EVERY_MS', 5_000),
  statusPath: process.env.STATUS_PATH ?? '/api/admin/dispatcher/status',
};

const args = parseArgs(process.argv.slice(2), defaults);

if (args.help) {
  printHelp();
  process.exit(0);
}

main().catch((error) => {
  console.error(`\nload suite failed: ${error.stack ?? error.message}`);
  process.exit(1);
});

async function main() {
  const startedAt = Date.now();
  const users = args.users > 0 ? args.users : args.total;
  const readPaths = splitCsv(args.readPaths);
  const problemId = args.problemId || await firstProblemId();
  const code = sourceFor(args.language);

  console.log('== JudgeMesh GKE load suite ==');
  console.log(`mode=${args.mode}`);
  console.log(`baseUrl=${args.baseUrl}`);
  console.log(`frontendUrl=${args.frontendUrl || '(disabled)'}`);
  console.log(`problemId=${problemId} language=${args.language}`);
  console.log(`submit total=${args.total} concurrency=${args.concurrency} users=${users}`);
  console.log(`read total=${args.readTotal} concurrency=${args.readConcurrency}`);

  await printDispatcherStatus('before');

  if (args.frontendUrl) {
    await probeFrontendProxy();
  }

  if (args.mode === 'read' || args.mode === 'full') {
    await runReadPhase(readPaths);
  }

  if (args.mode === 'submit' || args.mode === 'full') {
    await runSubmitPhase(users, problemId, code);
  }

  await printDispatcherStatus('after');
  console.log(`\nall done in ${Date.now() - startedAt} ms`);
}

async function runReadPhase(readPaths) {
  console.log('\n== read phase ==');
  const startedAt = Date.now();
  const results = await mapLimit(
    Array.from({ length: args.readTotal }, (_, index) => index),
    args.readConcurrency,
    async (index) => {
      const path = readPaths[index % readPaths.length];
      const t0 = Date.now();
      try {
        await request(path);
        return {
          index,
          kind: path,
          ok: true,
          latencyMs: Date.now() - t0,
        };
      } catch (error) {
        return {
          index,
          kind: path,
          ok: false,
          latencyMs: Date.now() - t0,
          error: error.message,
        };
      }
    },
  );

  printPhaseSummary('read', results, Date.now() - startedAt);
}

async function runSubmitPhase(users, problemId, code) {
  console.log('\n== preparing users ==');
  const identities = await mapLimit(
    Array.from({ length: users }, (_, i) => i + 1),
    Math.min(args.concurrency, users),
    ensureUser,
  );
  console.log(`users ready: ${identities.length}`);

  console.log('\n== submitting burst ==');
  const submitted = await mapLimit(
    Array.from({ length: args.total }, (_, i) => i),
    args.concurrency,
    async (i) => submitOne(i, identities[i % identities.length], problemId, code),
  );

  const accepted = submitted.filter((item) => item.submitId);
  const rejected = submitted.filter((item) => !item.submitId);
  console.log(`accepted=${accepted.length} rejected=${rejected.length}`);
  if (rejected.length > 0) {
    printSamples('submit errors', rejected);
  }

  console.log('\n== polling judge results ==');
  const snapshots = [];
  const progress = setInterval(() => printProgress(accepted), 2_000);
  const observer = setInterval(async () => {
    try {
      const status = await request(args.statusPath);
      const normalized = normalizeDispatcherSnapshot(status);
      snapshots.push({
        at: new Date().toISOString(),
        leader: normalized.leader,
        availableWorkers: normalized.availableWorkers,
        totalWorkers: normalized.totalWorkers,
      });
    } catch (error) {
      snapshots.push({
        at: new Date().toISOString(),
        error: error.message,
      });
    }
  }, args.observeEveryMs);

  const results = await mapLimit(accepted, args.concurrency, pollUntilTerminal);
  clearInterval(progress);
  clearInterval(observer);
  printProgress(results);

  console.log('\n== submit summary ==');
  printSubmitSummary(results, rejected, snapshots);
}

async function probeFrontendProxy() {
  console.log('\n== frontend proxy check ==');
  const target = `${trimTrailingSlash(args.frontendUrl)}/api/problem/list`;
  const t0 = Date.now();
  try {
    const payload = await request(target);
    const problems = unwrap(payload);
    const count = Array.isArray(problems) ? problems.length : 0;
    console.log(`frontend proxy ok latencyMs=${Date.now() - t0} problems=${count}`);
  } catch (error) {
    console.log(`frontend proxy failed latencyMs=${Date.now() - t0} error=${error.message}`);
  }
}

async function firstProblemId() {
  const response = await request('/api/problem/list');
  const problems = unwrap(response);
  if (!Array.isArray(problems) || problems.length === 0) {
    throw new Error('no published problems found');
  }
  return problems[0].id;
}

async function ensureUser(index) {
  const username = `${args.userPrefix}-${index}`.slice(0, 60);
  const email = `${username}@judgemesh.local`;
  const body = { username, email, password: args.password, role: 'STUDENT' };
  try {
    const registered = unwrap(await request('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify(body),
    }));
    return { email, token: registered.token ?? registered.accessToken };
  } catch (error) {
    if (!String(error.message).includes('409')) {
      throw error;
    }
    const loggedIn = unwrap(await request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password: args.password }),
    }));
    return { email, token: loggedIn.token ?? loggedIn.accessToken };
  }
}

async function submitOne(index, identity, problemId, code) {
  const startedAt = Date.now();
  try {
    const data = unwrap(await request('/api/submit', {
      method: 'POST',
      token: identity.token,
      body: JSON.stringify({
        problemId,
        language: args.language,
        code,
      }),
    }));
    return {
      index,
      email: identity.email,
      submitId: data.id,
      status: data.status,
      startedAt,
      submittedAt: Date.now(),
      token: identity.token,
    };
  } catch (error) {
    return {
      index,
      email: identity.email,
      status: 'SUBMIT_FAILED',
      error: error.message,
      startedAt,
      finishedAt: Date.now(),
    };
  }
}

async function pollUntilTerminal(item) {
  const deadline = Date.now() + args.timeoutMs;
  let current = item;
  while (Date.now() < deadline) {
    if (terminalStatuses.has(current.status ?? '')) {
      return current;
    }
    await sleep(args.pollIntervalMs);
    try {
      const data = unwrap(await request(`/api/submit/${item.submitId}`, {
        token: item.token,
      }));
      current = {
        ...current,
        status: data.status,
        score: data.score,
        worker: data.judgedByWorker,
        message: data.judgeMessage,
        finishedAt: terminalStatuses.has(data.status) ? Date.now() : undefined,
      };
    } catch (error) {
      current = { ...current, error: error.message };
    }
  }
  return {
    ...current,
    status: current.status ?? 'TIMEOUT',
    error: current.error ?? 'poll timeout',
  };
}

async function request(pathOrUrl, options = {}) {
  const url = pathOrUrl.startsWith('http') ? pathOrUrl : `${trimTrailingSlash(args.baseUrl)}${pathOrUrl}`;
  const headers = { 'content-type': 'application/json', ...(options.headers ?? {}) };
  if (options.token) {
    headers.authorization = `Bearer ${options.token}`;
  }
  const response = await fetch(url, {
    method: options.method ?? 'GET',
    headers,
    body: options.body,
  });
  const text = await response.text();
  const payload = text ? safeJson(text) : null;
  if (!response.ok) {
    const message = payload?.message ?? payload?.detail ?? text;
    throw new Error(`${response.status} ${message}`);
  }
  return payload;
}

async function printDispatcherStatus(label) {
  try {
    const status = await request(args.statusPath);
    console.log(`\n== dispatcher status (${label}) ==`);
    console.log(JSON.stringify(status, null, 2));
  } catch (error) {
    console.log(`\n== dispatcher status (${label}) unavailable: ${error.message} ==`);
  }
}

function printProgress(items) {
  const counts = countBy(items, (item) => item.status ?? 'PENDING');
  const done = items.filter((item) => terminalStatuses.has(item.status ?? '')).length;
  console.log(`progress terminal=${done}/${items.length} statuses=${JSON.stringify(counts)}`);
}

function printPhaseSummary(name, results, elapsedMs) {
  const ok = results.filter((item) => item.ok);
  const fail = results.filter((item) => !item.ok);
  const latencies = ok.map((item) => item.latencyMs).sort((a, b) => a - b);
  console.log(`phase=${name} elapsedMs=${elapsedMs} ok=${ok.length} fail=${fail.length}`);
  if (latencies.length > 0) {
    console.log(
      `phase=${name} latencyMs p50=${percentile(latencies, 50)} p95=${percentile(latencies, 95)} p99=${percentile(latencies, 99)} max=${latencies.at(-1)}`,
    );
  }
  const byPath = countBy(results, (item) => `${item.kind}:${item.ok ? 'ok' : 'fail'}`);
  console.log(`phase=${name} byPath=${JSON.stringify(byPath)}`);
  if (fail.length > 0) {
    printSamples(`${name} samples`, fail);
  }
}

function printSubmitSummary(results, rejected, snapshots) {
  const statusCounts = countBy([...results, ...rejected], (item) => item.status ?? 'UNKNOWN');
  const workerCounts = countBy(results, (item) => item.worker ?? 'unassigned');
  const durations = results
    .filter((item) => item.startedAt && item.finishedAt)
    .map((item) => item.finishedAt - item.startedAt)
    .sort((a, b) => a - b);

  console.log(`statusCounts=${JSON.stringify(statusCounts)}`);
  console.log(`workerCounts=${JSON.stringify(workerCounts)}`);
  if (durations.length > 0) {
    console.log(
      `latencyMs p50=${percentile(durations, 50)} p95=${percentile(durations, 95)} p99=${percentile(durations, 99)} max=${durations.at(-1)}`,
    );
  }
  if (snapshots.length > 0) {
    console.log('dispatcherSnapshots=');
    console.log(JSON.stringify(snapshots, null, 2));
  }
  const failures = [...rejected, ...results.filter((item) => item.status !== 'AC')];
  if (failures.length > 0) {
    printSamples('non-AC samples', failures);
  }
}

function printSamples(title, items) {
  console.log(`${title}:`);
  for (const item of items.slice(0, 5)) {
    console.log(
      JSON.stringify({
        index: item.index,
        kind: item.kind,
        submitId: item.submitId,
        status: item.status,
        worker: item.worker,
        latencyMs: item.latencyMs,
        error: item.error,
        message: item.message,
      }),
    );
  }
}

function normalizeDispatcherSnapshot(status) {
  const payload = unwrap(status);
  const leader = payload?.leader?.leader ?? payload?.leader?.self ?? null;
  const availableWorkers = Number(payload?.workers?.availableCount ?? 0);
  const totalWorkers = Number(payload?.workers?.totalCount ?? 0);
  return { leader, availableWorkers, totalWorkers };
}

function sourceFor(language) {
  switch (language.toUpperCase()) {
    case 'CPP':
    case 'C++':
      return '#include <iostream>\nusing namespace std;\n\nint main() {\n    long long a, b;\n    cin >> a >> b;\n    cout << a + b << "\\n";\n    return 0;\n}\n';
    case 'C':
      return '#include <stdio.h>\n\nint main() {\n    long long a, b;\n    scanf("%lld%lld", &a, &b);\n    printf("%lld\\n", a + b);\n    return 0;\n}\n';
    case 'JAVA':
      return 'import java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        long a = sc.nextLong();\n        long b = sc.nextLong();\n        System.out.println(a + b);\n    }\n}\n';
    case 'PYTHON':
    default:
      return 'a,b=map(int,input().split())\nprint(a+b)\n';
  }
}

function parseArgs(argv, base) {
  const parsed = { ...base };
  for (let i = 0; i < argv.length; i++) {
    const arg = argv[i];
    if (arg === '--help' || arg === '-h') {
      parsed.help = true;
      continue;
    }
    if (!arg.startsWith('--')) {
      throw new Error(`unknown argument: ${arg}`);
    }
    const [rawKey, inlineValue] = arg.slice(2).split('=', 2);
    const key = rawKey.replace(/-([a-z])/g, (_, c) => c.toUpperCase());
    const value = inlineValue ?? argv[++i];
    if (value == null) {
      throw new Error(`missing value for --${rawKey}`);
    }
    if (
      [
        'total',
        'concurrency',
        'users',
        'problemId',
        'pollIntervalMs',
        'timeoutMs',
        'readTotal',
        'readConcurrency',
        'observeEveryMs',
      ].includes(key)
    ) {
      parsed[key] = Number(value);
    } else if (key in parsed) {
      parsed[key] = value;
    } else {
      throw new Error(`unknown option: --${rawKey}`);
    }
  }
  return parsed;
}

function printHelp() {
  console.log(`Usage:
  node scripts/gke-load-suite.mjs [options]

Modes:
  --mode full              Read phase + submit phase
  --mode read              Read endpoints only
  --mode submit            Submit pipeline only

Options:
  --base-url URL           Gateway URL, default http://127.0.0.1:8080
  --frontend-url URL       Optional frontend URL, used to verify /api proxy
  --status-path PATH       Dispatcher status path, default /api/admin/dispatcher/status
  --read-paths CSV         Read endpoints, default ${defaultReadPaths.join(',')}
  --read-total N           Number of read requests, default 120
  --read-concurrency N     Read concurrency, default 30
  --total N                Number of submissions, default 40
  --concurrency N          Concurrent submit/poll workers, default 20
  --users N                Load users, default equals --total
  --problem-id N           Problem id, default first item from /api/problem/list
  --language NAME          PYTHON, CPP, C, or JAVA, default PYTHON
  --user-prefix PREFIX     Prefix for generated load users
  --timeout-ms N           Poll timeout per submission, default 60000
  --observe-every-ms N     Dispatcher snapshot interval, default 5000

Examples:
  node scripts/gke-load-suite.mjs --base-url http://34.80.3.160 --mode full
  node scripts/gke-load-suite.mjs --base-url http://34.80.3.160 --frontend-url http://35.236.151.33 --mode read
  TOTAL=80 CONCURRENCY=30 node scripts/gke-load-suite.mjs --base-url http://34.80.3.160 --mode submit
`);
}

function unwrap(payload) {
  if (payload && typeof payload === 'object' && Object.prototype.hasOwnProperty.call(payload, 'data')) {
    return payload.data;
  }
  return payload;
}

function safeJson(text) {
  try {
    return JSON.parse(text);
  } catch {
    return { message: text };
  }
}

function countBy(items, keyFn) {
  return items.reduce((acc, item) => {
    const key = keyFn(item);
    acc[key] = (acc[key] ?? 0) + 1;
    return acc;
  }, {});
}

function percentile(sorted, p) {
  if (sorted.length === 0) return 0;
  const index = Math.min(sorted.length - 1, Math.ceil((p / 100) * sorted.length) - 1);
  return sorted[index];
}

function splitCsv(value) {
  return String(value)
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean);
}

function trimTrailingSlash(value) {
  return value.endsWith('/') ? value.slice(0, -1) : value;
}

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function mapLimit(items, limit, fn) {
  const results = new Array(items.length);
  let next = 0;
  const workers = Array.from({ length: Math.max(1, Math.min(limit, items.length || 1)) }, async () => {
    while (next < items.length) {
      const index = next++;
      results[index] = await fn(items[index], index);
    }
  });
  await Promise.all(workers);
  return results;
}

function numberEnv(name, fallback) {
  const value = process.env[name];
  return value == null || value === '' ? fallback : Number(value);
}
