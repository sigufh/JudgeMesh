#!/usr/bin/env node

const terminalStatuses = new Set(['AC', 'WA', 'TLE', 'MLE', 'RE', 'CE', 'SE']);

const defaults = {
  baseUrl: process.env.BASE_URL ?? 'http://127.0.0.1:8080',
  dispatcherUrl: process.env.DISPATCHER_URL ?? 'http://127.0.0.1:8084',
  total: numberEnv('TOTAL', 40),
  concurrency: numberEnv('CONCURRENCY', 20),
  users: numberEnv('USERS', 0),
  problemId: process.env.PROBLEM_ID ? Number(process.env.PROBLEM_ID) : 0,
  language: process.env.LANGUAGE ?? 'PYTHON',
  password: process.env.PASSWORD ?? 'Load@12345',
  userPrefix: process.env.USER_PREFIX ?? `load-${Date.now()}`,
  pollIntervalMs: numberEnv('POLL_INTERVAL_MS', 500),
  timeoutMs: numberEnv('TIMEOUT_MS', 60_000),
};

const args = parseArgs(process.argv.slice(2), defaults);

if (args.help) {
  printHelp();
  process.exit(0);
}

main().catch((error) => {
  console.error(`\nload demo failed: ${error.message}`);
  process.exit(1);
});

async function main() {
  const startedAt = Date.now();
  const users = args.users > 0 ? args.users : args.total;
  const problemId = args.problemId || await firstProblemId();
  const code = sourceFor(args.language);

  console.log('== JudgeMesh distributed load demo ==');
  console.log(`baseUrl=${args.baseUrl}`);
  console.log(`dispatcherUrl=${args.dispatcherUrl}`);
  console.log(`total=${args.total} concurrency=${args.concurrency} users=${users}`);
  console.log(`problemId=${problemId} language=${args.language}`);

  await printDispatcherStatus('before');

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
  const progress = setInterval(() => printProgress(accepted), 2_000);
  const results = await mapLimit(
    accepted,
    args.concurrency,
    pollUntilTerminal,
  );
  clearInterval(progress);
  printProgress(results);

  console.log('\n== summary ==');
  printSummary(results, rejected, Date.now() - startedAt);
  await printDispatcherStatus('after');

  const unfinished = results.filter((item) => !terminalStatuses.has(item.status ?? ''));
  if (rejected.length > 0 || unfinished.length > 0) {
    process.exitCode = 1;
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
  return { ...current, status: current.status ?? 'TIMEOUT', error: current.error ?? 'poll timeout' };
}

async function request(pathOrUrl, options = {}) {
  const url = pathOrUrl.startsWith('http') ? pathOrUrl : `${args.baseUrl}${pathOrUrl}`;
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
    const message = payload?.message ?? text;
    throw new Error(`${response.status} ${message}`);
  }
  return payload;
}

async function printDispatcherStatus(label) {
  try {
    const status = await request(`${args.dispatcherUrl}/api/admin/dispatcher/status`);
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

function printSummary(results, rejected, elapsedMs) {
  const statusCounts = countBy([...results, ...rejected], (item) => item.status ?? 'UNKNOWN');
  const workerCounts = countBy(results, (item) => item.worker ?? 'unassigned');
  const durations = results
    .filter((item) => item.startedAt && item.finishedAt)
    .map((item) => item.finishedAt - item.startedAt)
    .sort((a, b) => a - b);
  console.log(`elapsedMs=${elapsedMs}`);
  console.log(`statusCounts=${JSON.stringify(statusCounts)}`);
  console.log(`workerCounts=${JSON.stringify(workerCounts)}`);
  if (durations.length > 0) {
    console.log(`latencyMs p50=${percentile(durations, 50)} p95=${percentile(durations, 95)} max=${durations.at(-1)}`);
  }
  const failures = [...rejected, ...results.filter((item) => item.status !== 'AC')];
  if (failures.length > 0) {
    printSamples('non-AC samples', failures);
  }
}

function printSamples(title, items) {
  console.log(`${title}:`);
  for (const item of items.slice(0, 5)) {
    console.log(JSON.stringify({
      index: item.index,
      submitId: item.submitId,
      status: item.status,
      worker: item.worker,
      error: item.error,
      message: item.message,
    }));
  }
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
    if (['total', 'concurrency', 'users', 'problemId', 'pollIntervalMs', 'timeoutMs'].includes(key)) {
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
  node scripts/demo-distributed-load.mjs [options]

Options:
  --base-url URL            Gateway URL, default http://127.0.0.1:8080
  --dispatcher-url URL      Dispatcher URL, default http://127.0.0.1:8084
  --total N                 Number of submissions, default 40
  --concurrency N           Concurrent submission/poll workers, default 20
  --users N                 Load users, default equals --total to avoid per-user rate limits
  --problem-id N            Problem id, default first item from /api/problem/list
  --language NAME           PYTHON, CPP, C, or JAVA, default PYTHON
  --user-prefix PREFIX      Prefix for generated load users
  --timeout-ms N            Poll timeout per submission, default 60000

Examples:
  node scripts/demo-distributed-load.mjs --total 60 --concurrency 30
  TOTAL=100 CONCURRENCY=50 LANGUAGE=CPP node scripts/demo-distributed-load.mjs
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

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function mapLimit(items, limit, fn) {
  const results = new Array(items.length);
  let next = 0;
  const workers = Array.from({ length: Math.min(limit, items.length) }, async () => {
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
