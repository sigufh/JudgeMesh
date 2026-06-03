import { startTransition, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { fetchProblem } from '../api/problems';
import { createSubmit } from '../api/submits';
import { useLiveTopic } from '../hooks/useLiveTopic';
import type { Problem, Submit } from '../types';

const starterCode: Record<string, string> = {
  CPP: '#include <iostream>\nusing namespace std;\n\nint main() {\n    long long a, b;\n    cin >> a >> b;\n    cout << a + b << "\\n";\n    return 0;\n}\n',
  C: '#include <stdio.h>\n\nint main() {\n    long long a, b;\n    scanf("%lld%lld", &a, &b);\n    printf("%lld\\n", a + b);\n    return 0;\n}\n',
  JAVA: 'import java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        long a = sc.nextLong();\n        long b = sc.nextLong();\n        System.out.println(a + b);\n    }\n}\n',
  PYTHON: 'a, b = map(int, input().split())\nprint(a + b)\n',
};

const terminalStatuses = new Set(['AC', 'WA', 'TLE', 'MLE', 'RE', 'CE', 'SE']);

export default function ProblemDetail() {
  const { id } = useParams<{ id: string }>();
  const [problem, setProblem] = useState<Problem | null>(null);
  const [language, setLanguage] = useState('PYTHON');
  const [code, setCode] = useState(starterCode.PYTHON);
  const [submit, setSubmit] = useState<Submit | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const safeTags = Array.isArray(problem?.tags) ? problem.tags : [];
  const submitLiveState = useLiveTopic<Submit>(
    submit ? `/topic/submission/${submit.id}` : null,
    (nextSubmit) => {
      startTransition(() => {
        setSubmit(nextSubmit);
        if (terminalStatuses.has(nextSubmit.status)) {
          setBusy(false);
        }
      });
    },
    Boolean(submit),
  );

  useEffect(() => {
    if (!id) return;
    let cancelled = false;
    fetchProblem(id)
      .then(({ data }) => {
        if (!cancelled) setProblem(data);
      })
      .catch((err: unknown) => {
        if (!cancelled) setError(err instanceof Error ? err.message : 'Failed to load problem');
      });
    return () => {
      cancelled = true;
    };
  }, [id]);

  function changeLanguage(next: string) {
    setLanguage(next);
    setCode(starterCode[next] ?? '');
  }

  async function submitCode() {
    if (!id) return;
    setBusy(true);
    setError('');
    try {
      const { data } = await createSubmit({ problemId: Number(id), language, code });
      setSubmit(data);
      if (terminalStatuses.has(data.status)) {
        setBusy(false);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Submit failed');
      setBusy(false);
    }
  }

  return (
    <div className="stack">
      <section className="page-head">
        <Link to="/problems" className="muted">
          Back to problem set
        </Link>
        <h1>{problem?.title ?? `Problem #${id}`}</h1>
        {problem && (
          <p className="muted">
            {problem.timeLimitMs} ms / {problem.memoryLimitMb} MB / {problem.difficulty}
          </p>
        )}
      </section>

      {error && <div className="notice error">{error}</div>}

      <section className="grid two">
        <article className="panel pad">
          <div className="tag-row" style={{ marginBottom: 12 }}>
            {safeTags.map((tag) => (
              <span className="tag" key={tag}>
                {tag}
              </span>
            ))}
            {problem?.status && <span className={`status ${problem.status}`}>{problem.status}</span>}
          </div>
          <div className="problem-body">{problem?.description ?? 'Loading statement...'}</div>
        </article>

        <aside className="panel pad stack">
          <h2>Submit</h2>
          <label className="field">
            <span>Language</span>
            <select value={language} onChange={(event) => changeLanguage(event.target.value)}>
              <option value="CPP">C++17</option>
              <option value="C">C11</option>
              <option value="JAVA">Java 17</option>
              <option value="PYTHON">Python 3</option>
            </select>
          </label>
          <label className="field">
            <span>Source</span>
            <textarea value={code} onChange={(event) => setCode(event.target.value)} spellCheck={false} />
          </label>
          <button className="button" type="button" onClick={submitCode} disabled={busy}>
            {busy ? 'Waiting for live result...' : 'Submit to queue'}
          </button>
          {submit && (
            <div className="notice">
              Submission #{submit.id} status{' '}
              <span className={`status ${submit.status}`}>{submit.status}</span>
              {submit.score != null && <> / score {submit.score}</>}
              {submit.judgedByWorker && <> / {submit.judgedByWorker}</>}.{' '}
              <Link to={`/submits/${submit.id}`}>Track it</Link>.
              <div className="muted">live channel {submitLiveState === 'open' ? 'connected' : 'reconnecting'}</div>
              {submit.judgeMessage && <div className="muted">{submit.judgeMessage}</div>}
            </div>
          )}
        </aside>
      </section>
    </div>
  );
}
