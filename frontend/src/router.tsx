import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import RootLayout from './layouts/RootLayout';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import ProblemList from './pages/ProblemList';
import ProblemDetail from './pages/ProblemDetail';
import Submits from './pages/Submits';
import SubmitDetail from './pages/SubmitDetail';
import Contests from './pages/Contests';
import ContestDetail from './pages/ContestDetail';
import Profile from './pages/Profile';
import NotFound from './pages/NotFound';
import AppErrorBoundary from './components/AppErrorBoundary';

const router = createBrowserRouter([
  {
    path: '/',
    Component: RootLayout,
    errorElement: <AppErrorBoundary />,
    children: [
      { index: true, Component: Home },
      { path: 'login', Component: Login },
      { path: 'register', Component: Register },
      { path: 'problems', Component: ProblemList },
      { path: 'problems/:id', Component: ProblemDetail },
      { path: 'submits', Component: Submits },
      { path: 'submits/:id', Component: SubmitDetail },
      { path: 'contests', Component: Contests },
      { path: 'contests/:id', Component: ContestDetail },
      { path: 'profile', Component: Profile },
      { path: '*', Component: NotFound },
    ],
  },
]);

export default function AppRouter() {
  return <RouterProvider router={router} />;
}
