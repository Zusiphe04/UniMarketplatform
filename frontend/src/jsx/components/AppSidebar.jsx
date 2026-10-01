import { Link, useLocation } from 'react-router-dom';
import { getPrimaryRole, getPersonaLabels } from '../../js/auth/roles.js';
import { useAuth } from '../auth/AuthContext.jsx';
import Icon from './Icon.jsx';

const BASE_LINKS = [['Home', '/', 'home'], ['Browse', '/marketplace', 'search']];
const ROLE_LINKS = {
  BUYER: [['My orders', '/buyer', 'bag'], ['Cart', '/cart', 'store']],
  SELLER: [['My listings', '/seller#new-listing', 'store'], ['Orders', '/seller#seller-orders', 'bag']],
  MODERATOR: [['Moderation', '/moderation', 'shield']],
  ADMIN: [['Admin overview', '/admin', 'grid'], ['Accounts', '/admin/accounts', 'users'], ['Moderation', '/moderation', 'shield']],
};

export default function AppSidebar({ role: requestedRole }) {
  const auth = useAuth();
  const location = useLocation();
  const role = requestedRole || getPrimaryRole(auth.account) || 'BUYER';
  const displayName = auth.account?.displayName || auth.account?.fullName || 'UniMarket member';
  const personas = getPersonaLabels(auth.account);
  const roleLinks = ROLE_LINKS[role] || [];
  const vendorLink = role === 'BUYER' ? [['Vendors', '/vendor-status', 'store']] : [];
  const links = [...BASE_LINKS, ...roleLinks, ['Community', '/community', 'users'], ...vendorLink, ['Notifications', '/notifications', 'bell'], ['Settings', '/settings', 'settings']];
  const initial = displayName.trim().charAt(0).toUpperCase() || 'U';

  return <aside className="app-sidebar" aria-label="Account navigation">
    <div className="app-sidebar__profile"><span>{initial}</span><div><strong>{displayName}</strong><small>{personas.includes('STUDENT') ? 'Student' : role === 'SELLER' ? 'Verified vendor' : role.toLocaleLowerCase()}</small></div></div>
    <nav>{links.map(([label, to, icon]) => { const [pathname, hash] = to.split('#'); const active = location.pathname === pathname && (!hash || location.hash === `#${hash}` || location.pathname !== '/seller'); return <Link className={active ? 'active' : ''} to={to} key={`${role}-${label}`}><Icon name={icon} size={18} /><span>{label}</span>{label === 'Notifications' && <i aria-hidden="true" />}</Link>; })}</nav>
    <div className="app-sidebar__impact"><strong>{role === 'SELLER' ? 'Grow local.' : 'Support local.'}<br />Strengthen campuses.</strong><p>Trusted people and useful opportunities, all in one community.</p><Link to="/about"><Icon name="arrowRight" size={16} /> Learn more</Link></div>
  </aside>;
}
