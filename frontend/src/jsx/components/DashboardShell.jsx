import { Link } from 'react-router-dom';
import { ROLE_DESTINATIONS, ROLE_LABELS, getPersonaLabels } from '../../js/auth/roles.js';
import { useAuth } from '../auth/AuthContext.jsx';
import AppSidebar from './AppSidebar.jsx';
import Icon from './Icon.jsx';

const ROLE_DESCRIPTIONS = {
  BUYER: 'Your purchases, saved activity and campus marketplace in one place.',
  SELLER: 'Manage products and fulfil customer orders without the clutter.',
  MODERATOR: 'Review community reports and protect trusted marketplace activity.',
  ADMIN: 'Essential platform health, access governance and approval work.',
};

export default function DashboardShell({ role, eyebrow, children }) {
  const auth = useAuth();
  const personas = getPersonaLabels(auth.account);
  const availableRoles = (auth.account?.roles || []).filter((item) => ROLE_DESTINATIONS[item] && item !== role);
  const displayName = auth.account?.displayName || auth.account?.fullName || 'UniMarket member';
  const student = role === 'BUYER' && personas.includes('STUDENT');
  const workspaceName = student ? 'Student dashboard' : role === 'SELLER' ? 'Vendor dashboard' : `${ROLE_LABELS[role]} dashboard`;

  return <main className="dashboard-page" id="main-content"><div className="container portal-shell dashboard-layout"><AppSidebar role={role} /><section className="dashboard-workspace"><header className="dashboard-workspace__header"><div><p className="eyebrow eyebrow--line">{eyebrow || workspaceName}</p><h1>Welcome back, {displayName.split(' ')[0]}.</h1><p>{ROLE_DESCRIPTIONS[role]}</p></div><div className="identity-tags identity-tags--light">{personas.slice(0, 2).map((item) => <span className="identity-tags__persona" key={item}><Icon name="shield" size={12} /> {item.replaceAll('_', ' ')}</span>)}{availableRoles.slice(0, 1).map((item) => <Link to={ROLE_DESTINATIONS[item]} key={item}>Switch to {ROLE_LABELS[item]}</Link>)}</div></header><div className="dashboard-content">{children}</div></section></div></main>;
}
