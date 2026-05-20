import { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { fetchContestRank, fetchContests, registerContest } from '../api/contests';
import type { Contest, RankEntry } from '../types';

export default function ContestDetail() {
  const { id } = useParams<{ id: string }>();
  const [contests, setContests] = useState<Contest[]>([]);
  const [rank, setRank] = useState<RankEntry[]>([]);
  const [error, setError] = useState('');

  const contest = useMemo(() => contests.find((item) => String(item.id) === id), [contests, id]);

  useEffect(() => {
    if (!id) return;
    let cancelled = false;
    Promise.allSettled([fetchContests(), fetchContestRank(id)]).then((results) => {
      if (cancelled) return;
      if (results[0].status === 'fulfilled') setContests(results[0].value.data);
      if (results[1].status === 'fulfilled') setRank(results[1].value.data);
      if (results[0].status === 'rejected') setError('Failed to load contest');
    });
    return () => {
      cancelled = true;
    };
  }, [id]);

  async function join() {
    if (!id) return;
    try {
      const { data } = await registerContest(id);
      setContests((current) => current.map((item) => (item.id === data.id ? data : item)));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Registration failed');
    }
  }

  return (
    <div className="stack">
      <section className="page-head">
        <Link to="/contests" className="muted">
          Back to contests
        </Link>
        <h1>{contest?.title ?? `Contest #${id}`}</h1>
        {contest && (
          <p className="muted">
            {new Date(contest.startTime).toLocaleString()} to {new Date(contest.endTime).toLocaleString()}
          </p>
        )}
      </section>
      {error && <div className="notice error">{error}</div>}

      <section className="grid two">
        <div className="panel pad stack">
          <h2>Contest control</h2>
          <p>{contest?.description ?? 'Loading contest metadata...'}</p>
          <div className="tag-row">
            <span className={`status ${contest?.frozen ? 'PENDING' : 'AC'}`}>
              {contest?.frozen ? 'Frozen board' : 'Live board'}
            </span>
            {contest?.registered && <span className="status AC">Registered</span>}
          </div>
          {!contest?.registered && (
            <button className="button" type="button" onClick={join}>
              Register for contest
            </button>
          )}
          <h3>Problems</h3>
          <div className="tag-row">
            {contest?.problemIds.map((problemId) => (
              <Link className="ghost-button" key={problemId} to={`/problems/${problemId}`}>
                Problem #{problemId}
              </Link>
            ))}
          </div>
        </div>

        <div className="panel">
          <div className="panel-header">
            <h2>Contest rank</h2>
          </div>
          <table className="table">
            <thead>
              <tr>
                <th>#</th>
                <th>User</th>
                <th>Solved</th>
                <th>Penalty</th>
              </tr>
            </thead>
            <tbody>
              {rank.map((entry) => (
                <tr key={entry.userId}>
                  <td>{entry.rank}</td>
                  <td>{entry.username}</td>
                  <td>{entry.solved}</td>
                  <td>{entry.penaltyMinutes}</td>
                </tr>
              ))}
              {rank.length === 0 && (
                <tr>
                  <td colSpan={4} className="muted">
                    No accepted contest submissions yet.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}
