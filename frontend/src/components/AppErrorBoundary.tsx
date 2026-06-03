import { Link, isRouteErrorResponse, useRouteError } from 'react-router-dom';

export default function AppErrorBoundary() {
  const error = useRouteError();
  const message = isRouteErrorResponse(error)
    ? `${error.status} ${error.statusText}`
    : error instanceof Error
      ? error.message
      : 'Unexpected application error';

  return (
    <section className="panel pad stack">
      <h1>Application Error</h1>
      <p className="muted">{message}</p>
      <Link className="button" to="/">
        Back to dashboard
      </Link>
    </section>
  );
}
