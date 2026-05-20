import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { currentUser, me } from '../api/auth';
import type { User } from '../types';

export default function Profile() {
  const [user, setUser] = useState<User | null>(currentUser());
  const [error, setError] = useState('');

  useEffect(() => {
    me()
      .then(({ data }) => setUser(data))
      .catch((err: unknown) => setError(err instanceof Error ? err.message : 'Sign in to load profile'));
  }, []);

  if (!user) {
    return (
      <section className="panel pad stack">
        <h1>Profile</h1>
        <div className="notice">No active browser session. <Link to="/login">Sign in</Link>.</div>
      </section>
    );
  }

  return (
    <div className="stack">
      <section className="page-head">
        <h1>{user.username}</h1>
        <p className="muted">{user.email}</p>
      </section>
      {error && <div className="notice">{error}</div>}
      <section className="grid three">
        <div className="metric">
          <span className="muted">Roles</span>
          <strong>{(user.roles ?? [user.role]).join(', ')}</strong>
        </div>
        <div className="metric">
          <span className="muted">Balance</span>
          <strong>{user.balance ?? 0}</strong>
        </div>
        <div className="metric">
          <span className="muted">Accepted / submits</span>
          <strong>
            {user.totalAc ?? 0}/{user.totalSubmit ?? 0}
          </strong>
        </div>
      </section>
    </div>
  );
}
