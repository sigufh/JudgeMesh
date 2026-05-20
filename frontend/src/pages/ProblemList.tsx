import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { fetchProblems } from '../api/problems';
import type { Problem } from '../types';

const difficulties = ['', 'EASY', 'MEDIUM', 'HARD'];

export default function ProblemList() {
  const [problems, setProblems] = useState<Problem[]>([]);
  const [q, setQ] = useState('');
  const [difficulty, setDifficulty] = useState('');
  const [loaded, setLoaded] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    const timer = window.setTimeout(() => {
      fetchProblems({ q, difficulty: difficulty || undefined })
        .then(({ data }) => {
          if (!cancelled) setProblems(data);
        })
        .catch((err: unknown) => {
          if (!cancelled) setError(err instanceof Error ? err.message : 'Failed to load problems');
        })
        .finally(() => {
          if (!cancelled) setLoaded(true);
        });
    }, 180);
    return () => {
      cancelled = true;
      window.clearTimeout(timer);
    };
  }, [q, difficulty]);

  const tags = useMemo(() => Array.from(new Set(problems.flatMap((problem) => problem.tags))).slice(0, 10), [problems]);

  return (
    <div className="stack">
      <section className="page-head">
        <h1>Problem set</h1>
        <p className="muted">Search, inspect limits, and open the submit panel for any published task.</p>
      </section>

      <section className="panel pad">
        <div className="toolbar">
          <label className="field" style={{ minWidth: 260, flex: 1 }}>
            <span>Search</span>
            <input
              value={q}
              onChange={(event) => {
                setLoaded(false);
                setError('');
                setQ(event.target.value);
              }}
              placeholder="title, statement, keyword"
            />
          </label>
          <label className="field" style={{ width: 180 }}>
            <span>Difficulty</span>
            <select
              value={difficulty}
              onChange={(event) => {
                setLoaded(false);
                setError('');
                setDifficulty(event.target.value);
              }}
            >
              {difficulties.map((item) => (
                <option key={item || 'ALL'} value={item}>
                  {item || 'ALL'}
                </option>
              ))}
            </select>
          </label>
        </div>
        {tags.length > 0 && (
          <div className="tag-row" style={{ marginTop: 12 }}>
            {tags.map((tag) => (
              <button
                className="tag"
                key={tag}
                type="button"
                onClick={() => {
                  setLoaded(false);
                  setError('');
                  setQ(tag);
                }}
              >
                {tag}
              </button>
            ))}
          </div>
        )}
      </section>

      {error && <div className="notice error">{error}</div>}

      <section className="panel">
        <table className="table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Title</th>
              <th>Difficulty</th>
              <th>Limits</th>
              <th>Acceptance</th>
            </tr>
          </thead>
          <tbody>
            {!loaded && (
              <tr>
                <td colSpan={5} className="muted">
                  Loading problem index...
                </td>
              </tr>
            )}
            {loaded &&
              problems.map((problem) => (
                <tr key={problem.id}>
                  <td>{problem.id}</td>
                  <td>
                    <Link to={`/problems/${problem.id}`}>{problem.title}</Link>
                    <div className="tag-row" style={{ marginTop: 6 }}>
                      {problem.tags.map((tag) => (
                        <span className="tag" key={tag}>
                          {tag}
                        </span>
                      ))}
                    </div>
                  </td>
                  <td>
                    <span className="status">{problem.difficulty}</span>
                  </td>
                  <td>
                    {problem.timeLimitMs} ms / {problem.memoryLimitMb} MB
                  </td>
                  <td>
                    {problem.totalSubmit ? `${problem.totalAc ?? 0}/${problem.totalSubmit}` : 'fresh'}
                  </td>
                </tr>
              ))}
            {loaded && problems.length === 0 && (
              <tr>
                <td colSpan={5} className="muted">
                  No problems matched the current filter.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </section>
    </div>
  );
}
