import { startTransition, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { fetchSubmit } from '../api/submits';
import { useLiveTopic } from '../hooks/useLiveTopic';
import type { Submit } from '../types';

const terminalStatuses = new Set(['AC', 'WA', 'TLE', 'MLE', 'RE', 'CE', 'SE']);

export default function SubmitDetail() {
  const { id } = useParams<{ id: string }>();
  const [submit, setSubmit] = useState<Submit | null>(null);
  const [error, setError] = useState('');

  const liveState = useLiveTopic<Submit>(
    submit ? `/topic/submission/${submit.id}` : null,
    (nextSubmit) => {
      startTransition(() => {
        setSubmit(nextSubmit);
      });
    },
    Boolean(submit),
  );

  useEffect(() => {
    if (!id) return;
    let cancelled = false;
    fetchSubmit(id)
      .then(({ data }) => {
        if (!cancelled) setSubmit(data);
      })
      .catch((err: unknown) => {
        if (!cancelled) setError(err instanceof Error ? err.message : 'Failed to load submission');
      });
    return () => {
      cancelled = true;
    };
  }, [id]);

  const terminal = submit ? terminalStatuses.has(submit.status) : false;

  return (
    <section className="panel pad stack">
      <Link className="muted" to="/submits">
        Back to submissions
      </Link>
      <h1>Submission #{id}</h1>
      {error && <div className="notice error">{error}</div>}
      {submit ? (
        <>
          <div>
            Status: <span className={`status ${submit.status}`}>{submit.status}</span>
          </div>
          <div>Worker: {submit.judgedByWorker ?? '-'}</div>
          <div>Message: {submit.judgeMessage ?? '-'}</div>
          <div>Live: {liveState}</div>
          <div>Final: {terminal ? 'yes' : 'no'}</div>
        </>
      ) : (
        <div className="muted">Loading submission...</div>
      )}
    </section>
  );
}
