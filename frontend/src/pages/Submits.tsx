import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { fetchMySubmits } from '../api/submits';
import type { Submit } from '../types';

export default function Submits() {
  const [submits, setSubmits] = useState<Submit[]>([]);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    async function loadSubmits() {
      try {
        const { data } = await fetchMySubmits();
        if (!cancelled) {
          setSubmits(data);
          setError('');
        }
      } catch (err: unknown) {
        if (!cancelled) setError(err instanceof Error ? err.message : 'Failed to load submissions');
      }
    }

    void loadSubmits();
    const timer = window.setInterval(loadSubmits, 1500);
    return () => {
      cancelled = true;
      window.clearInterval(timer);
    };
  }, []);

  return (
    <div className="stack">
      <section className="page-head">
        <h1>Submission queue</h1>
        <p className="muted">Track state transitions from PENDING to the final judge result.</p>
      </section>
      {error && <div className="notice error">{error}</div>}
      <section className="panel">
        <table className="table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Problem</th>
              <th>Language</th>
              <th>Status</th>
              <th>Runtime</th>
              <th>Worker</th>
            </tr>
          </thead>
          <tbody>
            {submits.map((submit) => (
              <tr key={submit.id}>
                <td>{submit.id}</td>
                <td>
                  <Link to={`/problems/${submit.problemId}`}>#{submit.problemId}</Link>
                </td>
                <td>{submit.language}</td>
                <td>
                  <span className={`status ${submit.status}`}>{submit.status}</span>
                  {submit.judgeMessage && <div className="muted">{submit.judgeMessage}</div>}
                </td>
                <td>{submit.timeUsedMs == null ? '-' : `${submit.timeUsedMs} ms`}</td>
                <td>{submit.judgedByWorker ?? '-'}</td>
              </tr>
            ))}
            {submits.length === 0 && (
              <tr>
                <td colSpan={6} className="muted">
                  No submissions yet. Open a problem and submit starter code.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </section>
    </div>
  );
}
