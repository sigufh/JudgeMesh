import { startTransition, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  fetchDispatcherStatus,
  reacquireDispatcherLeader,
  relinquishDispatcherLeader,
} from '../api/admin';
import { fetchContests, fetchGlobalRank } from '../api/contests';
import { fetchProblems } from '../api/problems';
import { fetchMySubmits } from '../api/submits';
import { useLiveTopic } from '../hooks/useLiveTopic';
import type { Contest, DispatcherStatus, Problem, RankEntry, Submit } from '../types';

export default function Home() {
  const [problems, setProblems] = useState<Problem[]>([]);
  const [contests, setContests] = useState<Contest[]>([]);
  const [submits, setSubmits] = useState<Submit[]>([]);
  const [rank, setRank] = useState<RankEntry[]>([]);
  const [dispatcher, setDispatcher] = useState<DispatcherStatus | null>(null);
  const [statusError, setStatusError] = useState('');
  const [controlBusy, setControlBusy] = useState(false);

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

  useEffect(() => {
    let cancelled = false;

    async function loadDispatcherStatus() {
      try {
        const { data } = await fetchDispatcherStatus();
        if (cancelled) return;
        startTransition(() => {
          setDispatcher(data);
          setStatusError('');
        });
      } catch (err: unknown) {
        if (!cancelled) {
          setStatusError(err instanceof Error ? err.message : 'Failed to load dispatcher status');
        }
      }
    }

    void loadDispatcherStatus();
    const timer = window.setInterval(() => {
      void loadDispatcherStatus();
    }, 5000);

    return () => {
      cancelled = true;
      window.clearInterval(timer);
    };
  }, []);

  const globalLiveState = useLiveTopic<RankEntry[]>('/topic/rank/global', (entries) => {
    startTransition(() => {
      setRank(entries);
    });
  });

  const accepted = submits.filter((submit) => submit.status === 'AC').length;
  const activeWorkers = dispatcher?.workers.workers.filter((worker) => worker.available).length ?? 0;

  async function runLeaderAction(action: 'relinquish' | 'reacquire') {
    setControlBusy(true);
    setStatusError('');
    try {
      const response = action === 'relinquish'
        ? await relinquishDispatcherLeader()
        : await reacquireDispatcherLeader();
      setDispatcher(response.data);
    } catch (err: unknown) {
      setStatusError(err instanceof Error ? err.message : 'Dispatcher control failed');
    } finally {
      setControlBusy(false);
    }
  }

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
        <div className="metric">
          <span className="muted">Healthy workers</span>
          <strong>{dispatcher ? `${activeWorkers}/${dispatcher.workers.totalCount}` : '-'}</strong>
          <span className="muted">dispatcher live mode {dispatcher?.leader.mode ?? 'loading'}</span>
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
            <div className="tag-row">
              <span className={`status ${globalLiveState === 'open' ? 'AC' : 'PENDING'}`}>
                {globalLiveState === 'open' ? 'Live feed' : 'Reconnect'}
              </span>
              <Link className="ghost-button" to="/contests">
                Rankings
              </Link>
            </div>
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

      <section className="panel pad stack">
        <div className="panel-header" style={{ padding: 0, borderBottom: 'none' }}>
          <div>
            <h2>Cluster control</h2>
            <p className="muted">Track current leader election mode and worker pool health from the web console.</p>
          </div>
          <div className="tag-row">
            <button
              className="ghost-button"
              type="button"
              onClick={() => {
                void runLeaderAction('relinquish');
              }}
              disabled={controlBusy}
            >
              Relinquish leader
            </button>
            <button
              className="ghost-button"
              type="button"
              onClick={() => {
                void runLeaderAction('reacquire');
              }}
              disabled={controlBusy}
            >
              Reacquire
            </button>
          </div>
        </div>
        {statusError && <div className="notice error">{statusError}</div>}
        <div className="grid two">
          <div className="metric">
            <span className="muted">Leader</span>
            <strong>{dispatcher?.leader.leader ?? 'none'}</strong>
            <span className="muted">
              self {dispatcher?.leader.self ?? '-'} / {dispatcher?.leader.isLeader ? 'active' : 'standby'}
            </span>
          </div>
          <div className="metric">
            <span className="muted">Lease state</span>
            <strong>{dispatcher?.leader.mode ?? 'loading'}</strong>
            <span className="muted">lease #{dispatcher?.leader.leaseId ?? 0}</span>
          </div>
        </div>
        <table className="table">
          <thead>
            <tr>
              <th>Worker</th>
              <th>State</th>
              <th>Inflight</th>
              <th>Last error</th>
            </tr>
          </thead>
          <tbody>
            {dispatcher?.workers.workers.map((worker) => (
              <tr key={worker.url}>
                <td>{worker.url}</td>
                <td>
                  <span className={`status ${worker.available ? 'AC' : 'PENDING'}`}>{worker.state}</span>
                  {worker.blacklistedUntil && (
                    <div className="muted">until {new Date(worker.blacklistedUntil).toLocaleTimeString()}</div>
                  )}
                </td>
                <td>{worker.inflight}</td>
                <td className="muted">{worker.lastError ?? '-'}</td>
              </tr>
            ))}
            {!dispatcher?.workers.workers.length && (
              <tr>
                <td colSpan={4} className="muted">
                  No worker registry data yet.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </section>
    </div>
  );
}

function formatWindow(start: string, end: string) {
  return `${new Date(start).toLocaleString()} to ${new Date(end).toLocaleTimeString()}`;
}
