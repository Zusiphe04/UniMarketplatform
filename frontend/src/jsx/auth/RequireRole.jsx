import { Navigate, useLocation } from 'react-router-dom';
import { getDefaultRoute, hasAnyRole } from '../../js/auth/roles.js';
import { useAuth } from './AuthContext.jsx';

export default function RequireRole({ roles = [], excludedRoles = [], children }) {
  const auth = useAuth();
  const location = useLocation();

  if (auth.status === 'restoring') {
    return (
      <main className="route-loading" aria-live="polite">
        <span className="route-loading__mark" />
        <p>Restoring your secure session…</p>
      </main>
    );
  }

  if (!auth.isAuthenticated) {
    return <Navigate to="/sign-in" replace state={{ from: location.pathname }} />;
  }

  if (excludedRoles.length > 0 && hasAnyRole(auth.account, excludedRoles)) {
    return <Navigate to={getDefaultRoute(auth.account)} replace />;
  }

  if (!hasAnyRole(auth.account, roles)) {
    return <Navigate to={getDefaultRoute(auth.account)} replace />;
  }

  return children;
}
