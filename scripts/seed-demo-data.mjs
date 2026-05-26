#!/usr/bin/env node

const terminalStatuses = new Set(['AC', 'WA', 'TLE', 'MLE', 'RE', 'CE', 'SE']);

const defaults = {
  baseUrl: process.env.BASE_URL ?? 'http://127.0.0.1:8080',
  total: numberEnv('TOTAL', 18),
  contestUsers: numberEnv('CONTEST_USERS', 6),
  password: process.env.PASSWORD ?? 'Seed@12345',
  userPrefix: process.env.USER_PREFIX ?? `seed-${Date.now()}`,
  pollIntervalMs: numberEnv('POLL_INTERVAL_MS', 500),
  timeoutMs: numberEnv('TIMEOUT_MS', 45_000),
  includeSlow: process.env.INCLUDE_SLOW === 'true',
};

main().catch((error) => {
  console.error(`seed failed: ${error.message}`);
  process.exit(1);
});

async function main() {
  console.log('== JudgeMesh demo seed ==');
  console.log(`baseUrl=${defaults.baseUrl}`);
  console.log(`users=${defaults.total} contestUsers=${defaults.contestUsers}`);

  const seedProblem = await ensureSeedProblem();
  console.log(`seed problem=${seedProblem.id} ${seedProblem.title}`);

  const contest = await firstContest();
  if (contest) {
    console.log(`contest=${contest.id} ${contest.title}`);
  } else {
    console.log('contest=none');
  }

  const users = await ensureUsers(defaults.total);
  console.log(`users ready=${users.length}`);

  const acceptedContestUsers = contest
    ? await registerContestUsers(contest.id, users.slice(0, Math.min(defaults.contestUsers, users.length)))
    : [];
  if (acceptedContestUsers.length > 0) {
    console.log(`contest registrations=${acceptedContestUsers.length}`);
  }

  const submissions = await submitSeedTraffic(users, seedProblem.id, contest);
  console.log(`submitted=${submissions.length}`);

  const results = await Promise.all(submissions.map(pollUntilTerminal));
  printSummary(results, users, contest, seedProblem);
}

async function ensureSeedProblem() {
  const problems = unwrap(await request('/api/problem/list?includeDraft=true&size=500'));
  const existing = Array.isArray(problems)
    ? problems.find((problem) => problem.title === 'Seed A+B Stability')
    : null;
  if (existing) {
    return existing;
  }

  return unwrap(await request('/api/problem', {
    method: 'POST',
    body: JSON.stringify({
      title: 'Seed A+B Stability',
      description: '## Seed A+B Stability\n\nRead two integers and print their sum.\n\nThis problem is created by `scripts/seed-demo-data.mjs` to generate deterministic submissions.',
      timeLimitMs: 1000,
      memoryLimitMb: 256,
      difficulty: 'EASY',
      setterId: 1001,
      published: true,
      tags: ['seed', 'demo', 'math'],
      testCases: [
        { caseIndex: 1, input: '1 2\n', expectedOutput: '3\n', score: 50 },
        { caseIndex: 2, input: '20 22\n', expectedOutput: '42\n', score: 50 },
      ],
    }),
  }));
}

async function firstContest() {
  try {
    const contests = unwrap(await request('/api/contest/list'));
    return Array.isArray(contests) && contests.length > 0 ? contests[0] : null;
  } catch {
    return null;
  }
}

async function ensureUsers(total) {
  const users = [];
  for (let index = 1; index <= total; index += 1) {
    const username = `${defaults.userPrefix}-${index}`.slice(0, 60);
    const email = `${username}@judgemesh.local`;
    const body = { username, email, password: defaults.password, role: 'STUDENT' };
    try {
      const registered = unwrap(await request('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify(body),
      }));
      users.push({ email, token: registered.token ?? registered.accessToken, username });
    } catch (error) {
      if (!String(error.message).includes('409')) {
        throw error;
      }
      const loggedIn = unwrap(await request('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password: defaults.password }),
      }));
      users.push({ email, token: loggedIn.token ?? loggedIn.accessToken, username });
    }
  }
  return users;
}

async function registerContestUsers(contestId, users) {
  const accepted = [];
  for (const user of users) {
    try {
      await request(`/api/contest/${contestId}/register`, {
        method: 'POST',
        token: user.token,
      });
      accepted.push(user);
    } catch (error) {
      if (!String(error.message).includes('already') && !String(error.message).includes('409')) {
        console.warn(`contest register skipped for ${user.email}: ${error.message}`);
      }
      accepted.push(user);
    }
  }
  return accepted;
}

async function submitSeedTraffic(users, seedProblemId, contest) {
  const submissions = [];
  const contestCutoff = contest ? Math.min(defaults.contestUsers, users.length) : 0;

  for (let index = 0; index < users.length; index += 1) {
    const user = users[index];
    const scenario = scenarioFor(index, defaults.includeSlow);
    const useContest = contest && index < contestCutoff && Array.isArray(contest.problemIds) && contest.problemIds.length > 0;
    const problemId = useContest ? contest.problemIds[0] : seedProblemId;
    const payload = {
      problemId,
      contestId: useContest ? contest.id : undefined,
      language: scenario.language,
      code: scenario.source,
    };
    const created = unwrap(await request('/api/submit', {
      method: 'POST',
      token: user.token,
      body: JSON.stringify(payload),
    }));
    submissions.push({
      email: user.email,
      username: user.username,
      contestId: payload.contestId,
      expected: scenario.expected,
      submitId: created.id,
      token: user.token,
      startedAt: Date.now(),
    });
  }
  return submissions;
}

function scenarioFor(index, includeSlow) {
  const plan = includeSlow
    ? ['AC', 'WA', 'RE', 'TLE', 'AC', 'AC']
    : ['AC', 'WA', 'RE', 'AC', 'AC', 'WA'];
  const expected = plan[index % plan.length];
  switch (expected) {
    case 'WA':
      return {
        expected,
        language: 'PYTHON',
        source: 'a, b = map(int, input().split())\nprint(a - b)\n',
      };
    case 'RE':
      return {
        expected,
        language: 'PYTHON',
        source: 'raise RuntimeError("seeded runtime error")\n',
      };
    case 'TLE':
      return {
        expected,
        language: 'PYTHON',
        source: 'while True:\n    pass\n',
      };
    default:
      return {
        expected: 'AC',
        language: 'PYTHON',
        source: 'a, b = map(int, input().split())\nprint(a + b)\n',
      };
  }
}

async function pollUntilTerminal(item) {
  const deadline = Date.now() + defaults.timeoutMs;
  let current = { ...item, status: 'PENDING' };
  while (Date.now() < deadline) {
    try {
      const data = unwrap(await request(`/api/submit/${item.submitId}`, {
        token: item.token,
      }));
      current = {
        ...current,
        status: data.status,
        worker: data.judgedByWorker,
        finishedAt: terminalStatuses.has(data.status) ? Date.now() : undefined,
      };
      if (terminalStatuses.has(data.status)) {
        return current;
      }
    } catch (error) {
      current = { ...current, error: error.message };
    }
    await sleep(defaults.pollIntervalMs);
  }
  return { ...current, status: 'TIMEOUT', error: current.error ?? 'poll timeout' };
}

function printSummary(results, users, contest, seedProblem) {
  console.log('\n== seed summary ==');
  console.log(`seedUsers=${users.length}`);
  console.log(`seedProblemId=${seedProblem.id}`);
  console.log(`contestId=${contest?.id ?? 'none'}`);
  console.log(`statusCounts=${JSON.stringify(countBy(results, (result) => result.status))}`);
  console.log(`workerCounts=${JSON.stringify(countBy(results, (result) => result.worker ?? 'unassigned'))}`);
  const mismatches = results.filter((result) => result.expected !== result.status);
  if (mismatches.length > 0) {
    console.log('mismatches:');
    for (const row of mismatches.slice(0, 8)) {
      console.log(JSON.stringify({
        email: row.email,
        submitId: row.submitId,
        expected: row.expected,
        actual: row.status,
        worker: row.worker,
        error: row.error,
      }));
    }
  }
  console.log('\nseed users:');
  for (const user of users.slice(0, 8)) {
    console.log(`- ${user.email} / ${defaults.password}`);
  }
}

async function request(pathOrUrl, options = {}) {
  const url = pathOrUrl.startsWith('http') ? pathOrUrl : `${defaults.baseUrl}${pathOrUrl}`;
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

function unwrap(payload) {
  if (payload && typeof payload === 'object' && 'data' in payload) {
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

function countBy(items, keyOf) {
  return items.reduce((accumulator, item) => {
    const key = keyOf(item);
    accumulator[key] = (accumulator[key] ?? 0) + 1;
    return accumulator;
  }, {});
}

function numberEnv(name, fallback) {
  const raw = process.env[name];
  return raw ? Number(raw) : fallback;
}

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}
