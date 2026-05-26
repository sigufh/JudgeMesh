import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { login, persistSession } from '../api/auth';

export default function Login() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('student@judgemesh.local');
  const [password, setPassword] = useState('Student@12345');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setBusy(true);
    setError('');
    try {
      const { data } = await login({ email, password });
      persistSession(data);
      navigate('/');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed');
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="grid two">
      <div className="page-head">
        <h1>Sign in</h1>
        <p className="muted">Use the seeded local users or your registered account.</p>
        <div className="notice">
          Demo student: student@judgemesh.local / Student@12345
        </div>
      </div>

      <form className="panel pad stack" onSubmit={submit}>
        <label className="field">
          <span>Email</span>
          <input
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            autoComplete="email"
          />
        </label>
        <label className="field">
          <span>Password</span>
          <input
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            autoComplete="current-password"
          />
        </label>
        {error && <div className="notice error">{error}</div>}
        <button className="button" type="submit" disabled={busy}>
          {busy ? 'Signing in...' : 'Sign in'}
        </button>
        <span className="muted">
          No account yet? <Link to="/register">Create one</Link>.
        </span>
      </form>
    </section>
  );
}
