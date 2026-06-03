import { startTransition, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { fetchMySubmits } from '../api/submits';
import { useLiveTopic } from '../hooks/useLiveTopic';
import { asArray } from '../lib/normalize';
import type { Submit } from '../types';

export default function Submits() {
  const [submits, setSubmits] = useState<Submit[]>([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const safeSubmits = Array.isArray(submits) ? submits : [];

  useEffect(() => {
    void loadSubmits();
  }, []);

  async function loadSubmits() {
    try {
      const { data } = await fetchMySubmits();
      startTransition(() => {
        setSubmits(asArray<Submit>(data));
        setError('');
        setLoading(false);
      });
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to load submissions');
      setLoading(false);
    }
  }

  function updateSubmit(nextSubmit: Submit) {
    setSubmits((current) => (Array.isArray(current) ? current : []).map((item) => (item.id === nextSubmit.id ? nextSubmit : item)));
  }

  return (
    <div className="stack">
      <section className="page-head">
        <h1>Submission queue</h1>
        <p className="muted">Track state transitions from PENDING to the final judge result.</p>
      </section>
      {error && <div className="notice error">{error}</div>}
      <section className="panel">
        <div className="panel-header">
          <h2>Live submissions</h2>
          <button
            className="ghost-button"
            type="button"
            onClick={() => {
              void loadSubmits();
            }}
          >
            Refresh list
          </button>
        </div>
        <table className="table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Problem</th>
              <th>Language</th>
              <th>Status</th>
              <th>Runtime</th>
              <th>Worker</th>
              <th>Feed</th>
            </tr>
          </thead>
          <tbody>
            {safeSubmits.map((submit) => (
              <LiveSubmitRow key={submit.id} submit={submit} onUpdate={updateSubmit} />
            ))}
            {!loading && safeSubmits.length === 0 && (
              <tr>
                <td colSpan={7} className="muted">
                  No submissions yet. Open a problem and submit starter code.
                </td>
              </tr>
            )}
            {loading && (
              <tr>
                <td colSpan={7} className="muted">
                  Loading submissions...
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </section>
    </div>
  );
}

function LiveSubmitRow({
  submit,
  onUpdate,
}: {
  submit: Submit;
  onUpdate: (submit: Submit) => void;
}) {
  const terminal = isTerminal(submit.status);
  const liveState = useLiveTopic<Submit>(
    terminal ? null : `/topic/submission/${submit.id}`,
    onUpdate,
    !terminal,
  );

  return (
    <tr>
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
      <td>
        <Link to={`/submits/${submit.id}`}>
          <span className={`status ${terminal || liveState === 'open' ? 'AC' : 'PENDING'}`}>
            {terminal ? 'Done' : liveState === 'open' ? 'Live' : 'Track'}
          </span>
        </Link>
      </td>
    </tr>
  );
}

function isTerminal(status: string) {
  return ['AC', 'WA', 'TLE', 'MLE', 'RE', 'CE', 'SE'].includes(status);
}
