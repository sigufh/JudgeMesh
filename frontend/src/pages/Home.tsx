import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { fetchContests, fetchGlobalRank } from '../api/contests';
import { fetchProblems } from '../api/problems';
import { fetchMySubmits } from '../api/submits';
import type { Contest, Problem, RankEntry, Submit } from '../types';

export default function Home() {
  const [problems, setProblems] = useState<Problem[]>([]);
  const [contests, setContests] = useState<Contest[]>([]);
  const [submits, setSubmits] = useState<Submit[]>([]);
  const [rank, setRank] = useState<RankEntry[]>([]);

  useEffect(() => {
    let cancelled = false;
    Promise.allSettled([fetchProblems(), fetchContests(), fetchMySubmits(), fetchGlobalRank()]).then((results) => {
      if (cancelled) return;
      if (results[0].status === 'fulfilled') setProblems(results[0].value.data);
      if (results[1].status === 'fulfilled') setContests(results[1].value.data);
      if (results[2].status === 'fulfilled') setSubmits(results[2].value.data);
      if (results[3].status === 'fulfilled') setRank(results[3].value.data);
    });
    return () => {
      cancelled = true;
    };
  }, []);

  const accepted = submits.filter((submit) => submit.status === 'AC').length;

  return (
    <div className="stack">
      <section className="page-head">
        <h1>Contest operations desk</h1>
        <p className="muted">
          Submit code, watch judge status, and keep the contest board in one focused workspace.
        </p>
      </section>

      <section className="grid three">
        <div className="metric">
          <span className="muted">Published problems</span>
          <strong>{problems.length}</strong>
          <Link to="/problems">Browse set</Link>
        </div>
        <div className="metric">
          <span className="muted">Recent submissions</span>
          <strong>{submits.length}</strong>
          <Link to="/submits">Open queue</Link>
        </div>
        <div className="metric">
          <span className="muted">Accepted locally</span>
          <strong>{accepted}</strong>
          <span className="muted">from this browser session</span>
        </div>
      </section>

      <section className="grid two">
        <div className="panel">
          <div className="panel-header">
            <h2>Active contests</h2>
            <Link className="ghost-button" to="/contests">
              View all
            </Link>
          </div>
          <table className="table">
            <thead>
              <tr>
                <th>Contest</th>
                <th>Problems</th>
                <th>State</th>
              </tr>
            </thead>
            <tbody>
              {contests.slice(0, 5).map((contest) => (
                <tr key={contest.id}>
                  <td>
                    <Link to={`/contests/${contest.id}`}>{contest.title}</Link>
                    <div className="muted">{formatWindow(contest.startTime, contest.endTime)}</div>
                  </td>
                  <td>{contest.problemIds.length}</td>
                  <td>
                    <span className={`status ${contest.frozen ? 'PENDING' : 'AC'}`}>
                      {contest.frozen ? 'Frozen' : 'Live'}
                    </span>
                  </td>
                </tr>
              ))}
              {contests.length === 0 && (
                <tr>
                  <td colSpan={3} className="muted">
                    No contest data loaded.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <div className="panel">
          <div className="panel-header">
            <h2>Global rank</h2>
            <Link className="ghost-button" to="/contests">
              Rankings
            </Link>
          </div>
          <table className="table">
            <thead>
              <tr>
                <th>#</th>
                <th>User</th>
                <th>Solved</th>
              </tr>
            </thead>
            <tbody>
              {rank.slice(0, 6).map((entry) => (
                <tr key={entry.userId}>
                  <td>{entry.rank}</td>
                  <td>{entry.username}</td>
                  <td>{entry.solved}</td>
                </tr>
              ))}
              {rank.length === 0 && (
                <tr>
                  <td colSpan={3} className="muted">
                    Rank updates appear after accepted submissions.
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

function formatWindow(start: string, end: string) {
  return `${new Date(start).toLocaleString()} to ${new Date(end).toLocaleTimeString()}`;
}
