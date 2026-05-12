import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { fetchContests, fetchGlobalRank, registerContest } from '../api/contests';
import type { Contest, RankEntry } from '../types';

export default function Contests() {
  const [contests, setContests] = useState<Contest[]>([]);
  const [rank, setRank] = useState<RankEntry[]>([]);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    Promise.allSettled([fetchContests(), fetchGlobalRank()]).then((results) => {
      if (cancelled) return;
      if (results[0].status === 'fulfilled') setContests(results[0].value.data);
      if (results[1].status === 'fulfilled') setRank(results[1].value.data);
      if (results[0].status === 'rejected') setError('Failed to load contests');
    });
    return () => {
      cancelled = true;
    };
  }, []);

  async function join(contest: Contest) {
    setError('');
    try {
      const { data } = await registerContest(contest.id);
      setContests((current) => current.map((item) => (item.id === data.id ? data : item)));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Registration failed');
    }
  }

  return (
    <div className="stack">
      <section className="page-head">
        <h1>Contests and ranks</h1>
        <p className="muted">Register for a live contest, open its problem list, and monitor rank movement.</p>
      </section>
      {error && <div className="notice error">{error}</div>}
      <section className="grid two">
        <div className="panel">
          <div className="panel-header">
            <h2>Contest list</h2>
          </div>
          <table className="table">
            <thead>
              <tr>
                <th>Contest</th>
                <th>Window</th>
                <th>Problems</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {contests.map((contest) => (
                <tr key={contest.id}>
                  <td>
                    <Link to={`/contests/${contest.id}`}>{contest.title}</Link>
                    <div className="muted">{contest.description}</div>
                  </td>
                  <td>
                    {new Date(contest.startTime).toLocaleString()}
                    <div className="muted">freeze {contest.freezeBeforeMin} min before end</div>
                  </td>
                  <td>{contest.problemIds.length}</td>
                  <td>
                    {contest.registered ? (
                      <span className="status AC">Registered</span>
                    ) : (
                      <button className="ghost-button" type="button" onClick={() => join(contest)}>
                        Register
                      </button>
                    )}
                  </td>
                </tr>
              ))}
              {contests.length === 0 && (
                <tr>
                  <td colSpan={4} className="muted">
                    No contests loaded.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <div className="panel">
          <div className="panel-header">
            <h2>Global board</h2>
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
                    Accepted submissions will populate this table.
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
