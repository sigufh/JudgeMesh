import { NavLink, Outlet, Link, useNavigate } from 'react-router-dom';
import { currentUser, logout } from '../api/auth';

const navItems = [
  { to: '/', label: 'Dashboard', end: true },
  { to: '/problems', label: 'Problems' },
  { to: '/contests', label: 'Contests' },
  { to: '/submits', label: 'Submits' },
  { to: '/profile', label: 'Profile' },
  { to: '/login', label: 'Login' },
];

export default function RootLayout() {
  const navigate = useNavigate();
  const user = currentUser();

  return (
    <div className="shell">
      <header className="topbar">
        <Link className="brand" to="/">
          <strong>JudgeMesh</strong>
          <span>distributed online judge</span>
        </Link>
        <nav className="nav">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="user-strip">
          {user ? (
            <>
              <div className="avatar">{user.username.slice(0, 1).toUpperCase()}</div>
              <span>{user.username}</span>
              <button
                className="ghost-button"
                type="button"
                onClick={() => {
                  logout();
                  navigate('/login');
                }}
              >
                Logout
              </button>
            </>
          ) : (
            <Link className="button" to="/login">
              Sign in
            </Link>
          )}
        </div>
      </header>
      <main className="main">
        <Outlet />
      </main>
    </div>
  );
}
