import { Link, useLocation } from 'react-router-dom';
import { getPrimaryRole } from '../../js/auth/roles.js';
import { useAuth } from '../auth/AuthContext.jsx';
import { useCart } from '../cart/CartContext.jsx';
import Icon from './Icon.jsx';

function matchesPath(pathname, prefix) {
  return pathname === prefix || pathname.startsWith(`${prefix}/`);
}

function tab(key, label, icon, to, active, extras = {}) {
  return { key, label, icon, to, active, ...extras };
}

export default function MobileTabBar() {
  const auth = useAuth();
  const cart = useCart();
  const { pathname, search, hash } = useLocation();
  const role = getPrimaryRole(auth.account);
  const cartCount = cart.itemCount > 99 ? '99+' : String(cart.itemCount);

  let tabs;
  if (role === 'ADMIN') {
    tabs = [
      tab('dashboard', 'Dashboard', 'grid', '/admin', pathname === '/admin'),
      tab('accounts', 'Accounts', 'users', '/admin/accounts', matchesPath(pathname, '/admin/accounts')),
      tab('moderation', 'Moderate', 'shield', '/moderation', matchesPath(pathname, '/moderation')),
      tab('inbox', 'Inbox', 'bell', '/notifications', matchesPath(pathname, '/notifications')),
      tab('settings', 'Settings', 'settings', '/settings', matchesPath(pathname, '/settings')),
    ];
  } else if (role === 'MODERATOR') {
    tabs = [
      tab('home', 'Home', 'home', '/', pathname === '/'),
      tab('shop', 'Shop', 'store', '/marketplace', matchesPath(pathname, '/marketplace') || matchesPath(pathname, '/products')),
      tab('moderation', 'Moderate', 'shield', '/moderation', matchesPath(pathname, '/moderation')),
      tab('inbox', 'Inbox', 'bell', '/notifications', matchesPath(pathname, '/notifications')),
      tab('settings', 'Settings', 'settings', '/settings', matchesPath(pathname, '/settings')),
    ];
  } else if (role === 'SELLER') {
    tabs = [
      tab('dashboard', 'Dashboard', 'grid', '/seller', pathname === '/seller' && !hash),
      tab('listings', 'Listings', 'store', '/seller#product-editor', pathname === '/seller' && hash === '#product-editor'),
      tab('orders', 'Orders', 'bag', '/seller#seller-orders', pathname === '/seller' && hash === '#seller-orders'),
      tab('inbox', 'Inbox', 'bell', '/notifications', matchesPath(pathname, '/notifications')),
      tab('settings', 'Settings', 'settings', '/settings', matchesPath(pathname, '/settings')),
    ];
  } else if (auth.isAuthenticated) {
    tabs = [
      tab('home', 'Home', 'home', '/', pathname === '/'),
      tab('shop', 'Shop', 'store', '/marketplace', matchesPath(pathname, '/marketplace') || matchesPath(pathname, '/products')),
      tab('cart', 'Cart', 'bag', '/cart', matchesPath(pathname, '/cart'), {
        badge: cart.itemCount > 0 ? cartCount : null,
        ariaLabel: `Cart with ${cart.itemCount} ${cart.itemCount === 1 ? 'item' : 'items'}`,
      }),
      tab('orders', 'Orders', 'grid', '/buyer', matchesPath(pathname, '/buyer') || matchesPath(pathname, '/orders')),
      tab('account', 'Account', 'user', '/settings', matchesPath(pathname, '/settings') || matchesPath(pathname, '/vendor-status')),
    ];
  } else {
    tabs = [
      tab('home', 'Home', 'home', '/', pathname === '/'),
      tab('shop', 'Shop', 'store', '/marketplace', matchesPath(pathname, '/marketplace') || matchesPath(pathname, '/products')),
      tab('community', 'Community', 'users', '/community', matchesPath(pathname, '/community')),
      tab('sell', 'Sell', 'bag', '/register?type=VENDOR', pathname === '/register' && new URLSearchParams(search).get('type') === 'VENDOR'),
      tab('signin', 'Sign in', 'user', '/sign-in', pathname === '/sign-in'),
    ];
  }

  return (
    <nav className="mobile-tabbar" aria-label="Primary mobile navigation">
      <ul>{tabs.map((item) => <li key={item.key}>
        <Link className={`mobile-tabbar__item${item.active ? ' mobile-tabbar__item--active' : ''}`} to={item.to} aria-current={item.active ? 'page' : undefined} aria-label={item.ariaLabel}>
          <span className="mobile-tabbar__icon"><Icon name={item.icon} size={21} strokeWidth={item.active ? 2.3 : 1.9} />{item.badge && <span className="mobile-tabbar__badge" aria-hidden="true">{item.badge}</span>}</span>
          <span className="mobile-tabbar__label">{item.label}</span>
        </Link>
      </li>)}</ul>
    </nav>
  );
}
