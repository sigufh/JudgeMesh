import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { persistSession, register } from '../api/auth';

export default function Register() {
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('STUDENT');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setBusy(true);
    setError('');
    try {
      const { data } = await register({ username, email, password, role });
      persistSession(data);
      navigate('/');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Registration failed');
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="grid two">
      <div className="page-head">
        <h1>Create account</h1>
        <p className="muted">Student accounts can submit immediately. Setter accounts can create and update problems.</p>
      </div>

      <form className="panel pad stack" onSubmit={submit}>
        <label className="field">
          <span>Username</span>
          <input value={username} onChange={(event) => setUsername(event.target.value)} autoComplete="username" />
        </label>
        <label className="field">
          <span>Email</span>
          <input type="email" value={email} onChange={(event) => setEmail(event.target.value)} autoComplete="email" />
        </label>
        <label className="field">
          <span>Password</span>
          <input
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            autoComplete="new-password"
          />
        </label>
        <label className="field">
          <span>Role</span>
          <select value={role} onChange={(event) => setRole(event.target.value)}>
            <option value="STUDENT">Student</option>
            <option value="SETTER">Setter</option>
          </select>
        </label>
        {error && <div className="notice error">{error}</div>}
        <button className="button" type="submit" disabled={busy}>
          {busy ? 'Creating...' : 'Create account'}
        </button>
        <span className="muted">
          Already registered? <Link to="/login">Sign in</Link>.
        </span>
      </form>
    </section>
  );
}
