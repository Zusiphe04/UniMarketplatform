import { Link, useLocation } from 'react-router-dom';
import { ROLE_DESTINATIONS } from '../../js/auth/roles.js';
import { useAuth } from '../auth/AuthContext.jsx';
import { useCart } from '../cart/CartContext.jsx';
import Icon from './Icon.jsx';

const DASHBOARD_PATHS = [...Object.values(ROLE_DESTINATIONS), '/settings', '/vendor-status', '/orders', '/admin/accounts', '/sign-in', '/register'];

function matchesPath(pathname, prefix) {
  return pathname === prefix || pathname.startsWith(`${prefix}/`);
}

/**
 * App-style bottom navigation, shown on phones and small tablets only (see mobile.css).
 * The fourth tab adapts to the account: buyers get their cart, other members get the inbox,
 * and guests get the vendor sign-up entry point.
 */
export default function MobileTabBar() {
  const auth = useAuth();
  const cart = useCart();
  const { pathname, search } = useLocation();
  const cartCount = cart.itemCount > 99 ? '99+' : String(cart.itemCount);

  const contextualTab = cart.canPurchase
    ? { key: 'cart', label: 'Cart', icon: 'bag', to: '/cart', active: matchesPath(pathname, '/cart'), badge: cart.itemCount > 0 ? cartCount : null, ariaLabel: `Cart with ${cart.itemCount} ${cart.itemCount === 1 ? 'item' : 'items'}` }
    : auth.isAuthenticated
      ? { key: 'inbox', label: 'Inbox', icon: 'bell', to: '/notifications', active: matchesPath(pathname, '/notifications') }
      : { key: 'sell', label: 'Sell', icon: 'store', to: '/register?type=VENDOR', active: pathname === '/register' && new URLSearchParams(search).get('type') === 'VENDOR' };

  const accountActive = !contextualTab.active && DASHBOARD_PATHS.some((path) => matchesPath(pathname, path));

  const tabs = [
    { key: 'home', label: 'Home', icon: 'home', to: '/', active: pathname === '/' },
    { key: 'shop', label: 'Shop', icon: 'grid', to: '/marketplace', active: matchesPath(pathname, '/marketplace') || matchesPath(pathname, '/products') },
    { key: 'community', label: 'Community', icon: 'users', to: '/community', active: matchesPath(pathname, '/community') },
    contextualTab,
    { key: 'account', label: auth.isAuthenticated ? 'Account' : 'Sign in', icon: 'user', to: auth.isAuthenticated ? auth.defaultRoute : '/sign-in', active: accountActive },
  ];

  return (
    <nav className="mobile-tabbar" aria-label="Quick navigation">
      <ul>
        {tabs.map((tab) => (
          <li key={tab.key}>
            <Link
              className={`mobile-tabbar__item${tab.active ? ' mobile-tabbar__item--active' : ''}`}
              to={tab.to}
              aria-current={tab.active ? 'page' : undefined}
              aria-label={tab.ariaLabel}
            >
              <span className="mobile-tabbar__icon">
                <Icon name={tab.icon} size={22} strokeWidth={tab.active ? 2.2 : 1.8} />
                {tab.badge && <span className="mobile-tabbar__badge" aria-hidden="true">{tab.badge}</span>}
              </span>
              <span className="mobile-tabbar__label">{tab.label}</span>
            </Link>
          </li>
        ))}
      </ul>
    </nav>
  );
}
