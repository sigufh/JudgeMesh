import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <section className="panel pad stack">
      <h1>404</h1>
      <p className="muted">The requested JudgeMesh route is not registered.</p>
      <Link className="button" to="/">
        Back to dashboard
      </Link>
    </section>
  );
}
