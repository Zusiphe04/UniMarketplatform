import { Link, useLocation } from 'react-router-dom';
import { getPrimaryRole, getPersonaLabels } from '../../js/auth/roles.js';
import { useAuth } from '../auth/AuthContext.jsx';
import Icon from './Icon.jsx';

const ROLE_LINKS = {
  BUYER: [['Home', '/', 'home'], ['Browse', '/marketplace', 'search'], ['My orders', '/buyer', 'bag'], ['Cart', '/cart', 'store'], ['Community', '/community', 'users'], ['Vendor application', '/vendor-status', 'store']],
  SELLER: [['Seller dashboard', '/seller', 'grid'], ['Vendor status', '/vendor-status', 'shield'], ['My listings', '/seller#product-editor', 'store'], ['Customer orders', '/seller#seller-orders', 'bag']],
  MODERATOR: [['Home', '/', 'home'], ['Browse', '/marketplace', 'search'], ['Community', '/community', 'users'], ['Moderation', '/moderation', 'shield']],
  ADMIN: [['Admin overview', '/admin', 'grid'], ['Accounts', '/admin/accounts', 'users'], ['Moderation', '/moderation', 'shield']],
};

export default function AppSidebar({ role: requestedRole }) {
  const auth = useAuth();
  const location = useLocation();
  const role = requestedRole || getPrimaryRole(auth.account) || 'BUYER';
  const displayName = auth.account?.displayName || auth.account?.fullName || 'UniMarket member';
  const personas = getPersonaLabels(auth.account);
  const links = [...(ROLE_LINKS[role] || []), ['Notifications', '/notifications', 'bell'], ['Settings', '/settings', 'settings']];
  const initial = displayName.trim().charAt(0).toUpperCase() || 'U';

  return <aside className="app-sidebar" aria-label="Account navigation">
    <div className="app-sidebar__profile"><span>{initial}</span><div><strong>{displayName}</strong><small>{personas.includes('STUDENT') && role === 'BUYER' ? 'Student' : role === 'SELLER' ? 'Verified vendor' : role.toLocaleLowerCase()}</small></div></div>
    <nav>{links.map(([label, to, icon]) => { const [pathname, hash] = to.split('#'); const active = location.pathname === pathname && (!hash || location.hash === `#${hash}`); return <Link className={active ? 'active' : ''} to={to} key={`${role}-${label}`}><Icon name={icon} size={18} /><span>{label}</span>{label === 'Notifications' && <i aria-hidden="true" />}</Link>; })}</nav>
    <div className="app-sidebar__impact"><strong>{role === 'SELLER' ? 'Grow local.' : role === 'ADMIN' ? 'Govern safely.' : 'Support local.'}<br />Strengthen campuses.</strong><p>Trusted people and useful opportunities, all in one community.</p></div>
  </aside>;
}
