import { useCallback, useEffect, useRef, useState } from 'react';
import { Link, NavLink, useLocation, useNavigate } from 'react-router-dom';
import { ROLE_LABELS, getPrimaryRole } from '../../js/auth/roles.js';
import { CATEGORIES } from '../../js/data/categories.js';
import { useAuth } from '../auth/AuthContext.jsx';
import { useCart } from '../cart/CartContext.jsx';
import Brand from './Brand.jsx';
import Icon from './Icon.jsx';
import SearchBox from './SearchBox.jsx';

const NAVIGATION = [
  { label: 'Marketplace', to: '/marketplace' },
  { label: 'Community', to: '/community' },
  { label: 'Vendors', to: '/register?type=VENDOR' },
  { label: 'About', to: '/about' },
];

const DESKTOP_QUERY = '(min-width: 901px)';
const CONDENSE_AFTER = 140;
const FOCUSABLE = 'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])';

function DrawerLink({ to, icon, children, end = false, badge = null, onNavigate }) {
  return (
    <NavLink className="mobile-drawer__link" end={end} to={to} onClick={onNavigate}>
      <span className="mobile-drawer__link-icon"><Icon name={icon} size={18} /></span>
      <span className="mobile-drawer__link-label">{children}</span>
      {badge}
      <Icon className="mobile-drawer__chevron" name="chevronRight" size={16} />
    </NavLink>
  );
}

export default function Header() {
  const auth = useAuth();
  const cart = useCart();
  const location = useLocation();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);
  const [searchOpen, setSearchOpen] = useState(false);
  const [searchValue, setSearchValue] = useState('');
  const [condensed, setCondensed] = useState(false);
  const toggleRef = useRef(null);
  const closeRef = useRef(null);
  const drawerRef = useRef(null);
  const wasOpenRef = useRef(false);

  const closeMenu = useCallback(() => setMenuOpen(false), []);

  useEffect(() => {
    const closeOnEscape = (event) => {
      if (event.key === 'Escape') {
        setMenuOpen(false);
        setSearchOpen(false);
      }
    };
    window.addEventListener('keydown', closeOnEscape);
    return () => window.removeEventListener('keydown', closeOnEscape);
  }, []);

  useEffect(() => {
    setMenuOpen(false);
    setSearchOpen(false);
    setCondensed(false);
    if (location.pathname === '/marketplace') {
      setSearchValue(new URLSearchParams(location.search).get('query') || '');
    }
  }, [location.pathname, location.search, location.hash]);

  // The drawer only exists on small screens; close it if the viewport grows past the breakpoint.
  useEffect(() => {
    const media = window.matchMedia(DESKTOP_QUERY);
    const closeOnDesktop = (event) => { if (event.matches) setMenuOpen(false); };
    media.addEventListener('change', closeOnDesktop);
    return () => media.removeEventListener('change', closeOnDesktop);
  }, []);

  // App-style header: hide the brand row while scrolling down, reveal it on scroll up.
  useEffect(() => {
    if (window.matchMedia?.('(prefers-reduced-motion: reduce)').matches) {
      setCondensed(false);
      return undefined;
    }
    let lastY = window.scrollY;
    let frame = 0;
    const onScroll = () => {
      if (frame) return;
      frame = window.requestAnimationFrame(() => {
        frame = 0;
        const currentY = window.scrollY;
        if (Math.abs(currentY - lastY) < 8) return;
        const typing = document.activeElement?.matches?.('.site-header input');
        setCondensed(!typing && currentY > CONDENSE_AFTER && currentY > lastY);
        lastY = currentY;
      });
    };
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => {
      window.removeEventListener('scroll', onScroll);
      if (frame) window.cancelAnimationFrame(frame);
    };
  }, []);

  // Lock page scroll and manage focus while the drawer is open.
  useEffect(() => {
    if (menuOpen) {
      wasOpenRef.current = true;
      document.body.classList.add('has-open-drawer');
      closeRef.current?.focus();
      return () => document.body.classList.remove('has-open-drawer');
    }
    if (wasOpenRef.current) {
      wasOpenRef.current = false;
      if (drawerRef.current?.contains(document.activeElement)) toggleRef.current?.focus();
    }
    return undefined;
  }, [menuOpen]);

  const trapFocus = (event) => {
    if (event.key !== 'Tab' || !drawerRef.current) return;
    const focusable = [...drawerRef.current.querySelectorAll(FOCUSABLE)];
    if (focusable.length === 0) return;
    const first = focusable[0];
    const last = focusable[focusable.length - 1];
    if (event.shiftKey && document.activeElement === first) {
      event.preventDefault();
      last.focus();
    } else if (!event.shiftKey && document.activeElement === last) {
      event.preventDefault();
      first.focus();
    }
  };

  const submitSearch = (value) => {
    const normalized = value.trim();
    navigate(normalized ? `/marketplace?query=${encodeURIComponent(normalized)}#listings` : '/marketplace#listings');
    setSearchValue(normalized);
    setSearchOpen(false);
    setMenuOpen(false);
    document.activeElement?.blur?.();
  };

  const signOut = async () => {
    try {
      await auth.logout();
    } finally {
      // AuthContext clears local credentials even when the server cannot be reached.
      // Always close the modal drawer and return to a public route as well.
      setMenuOpen(false);
      navigate('/');
    }
  };

  const primaryRole = getPrimaryRole(auth.account);
  const displayName = auth.account?.displayName || auth.account?.fullName || 'UniMarket member';
  const hasSellerRole = auth.hasRole('SELLER');
  const hasAdminRole = auth.hasRole('ADMIN');
  const isSeller = primaryRole === 'SELLER';
  const isAdmin = primaryRole === 'ADMIN';
  const buyerNavigation = !hasSellerRole && !hasAdminRole;
  const vendorDestination = isSeller ? '/seller' : auth.isAuthenticated ? '/vendor-status' : '/register?type=VENDOR';
  const navigationItems = isAdmin
    ? [{ label: 'Admin dashboard', to: '/admin' }, { label: 'Accounts', to: '/admin/accounts' }]
    : isSeller
      ? [{ label: 'Seller dashboard', to: '/seller' }]
      : buyerNavigation
        ? NAVIGATION.map((item) => ({ ...item, to: item.label === 'Vendors' ? vendorDestination : item.to }))
        : [{ label: `${ROLE_LABELS[primaryRole] || 'Account'} dashboard`, to: auth.defaultRoute }];
  const navigation = navigationItems.map((item) => (
    <NavLink key={item.label} to={item.to} onClick={closeMenu}>{item.label}</NavLink>
  ));
  const displayedCartCount = cart.itemCount > 99 ? '99+' : String(cart.itemCount);
  const cartBadge = cart.itemCount > 0 ? <span className="header-cart-badge" aria-hidden="true">{displayedCartCount}</span> : null;

  return <>
    <header className={`site-header${condensed ? ' site-header--condensed' : ''}`} id="top">
      <div className="container site-header__inner">
        <Brand />
        <nav className="desktop-nav" aria-label="Primary navigation">{navigation}</nav>

        <div className="header-actions">
          {buyerNavigation && <button className="header-search-toggle" type="button" aria-label="Search marketplace" aria-expanded={searchOpen} aria-controls="header-search-panel" onClick={() => setSearchOpen((open) => !open)}>
            <Icon name="search" size={17} />
          </button>}
          {auth.isAuthenticated && <Link className="header-icon-link" to="/notifications" aria-label="Inbox notifications" title="Inbox">
            <Icon name="bell" size={20} />
          </Link>}
          {cart.canPurchase && <Link className="header-cart-link" to="/cart" aria-label={`Cart with ${cart.itemCount} ${cart.itemCount === 1 ? 'item' : 'items'}`} title="Open cart">
            <Icon name="bag" size={20} />
            {cartBadge}
          </Link>}
          {!auth.isAuthenticated && <Link className="header-icon-link" to="/sign-in" aria-label="Sign in" title="Sign in">
            <Icon name="user" size={20} />
          </Link>}
          <span className="header-divider" aria-hidden="true" />
          {auth.isAuthenticated ? <>
            <Link className="header-utility-link" to="/notifications">Inbox</Link>
            <Link className="header-utility-link" to="/settings">Settings</Link>
            <Link className="header-sign-in" to={auth.defaultRoute}>{auth.account.displayName || 'Dashboard'}</Link>
            <button className="button button--dark button--header" type="button" onClick={signOut}>Sign Out <Icon name="arrowRight" size={14} /></button>
          </> : <>
            <Link className="header-sign-in" to="/sign-in">Sign In</Link>
            <Link className="button button--dark button--header" to="/register">Get Started <Icon name="arrowRight" size={14} /></Link>
          </>}
        </div>
        <button className="menu-toggle" ref={toggleRef} type="button" aria-label={menuOpen ? 'Close menu' : 'Open menu'} aria-expanded={menuOpen} aria-controls="mobile-menu" onClick={() => setMenuOpen((open) => !open)}>
          <Icon name="menu" size={22} />
        </button>
      </div>

      {buyerNavigation && <div className="header-mobile-search">
        <div className="container"><SearchBox id="header-mobile-search" value={searchValue} onChange={setSearchValue} onSubmit={submitSearch} /></div>
      </div>}

      {buyerNavigation && searchOpen && <div className="header-search-panel header-search-panel--open" id="header-search-panel">
        <div className="container"><SearchBox id="header-search" value={searchValue} onChange={setSearchValue} onSubmit={submitSearch} /></div>
      </div>}
    </header>

    <div className={`mobile-drawer${menuOpen ? ' mobile-drawer--open' : ''}`} id="mobile-menu" inert={!menuOpen}>
      <div className="mobile-drawer__backdrop" aria-hidden="true" onClick={closeMenu} />
      <aside className="mobile-drawer__panel" ref={drawerRef} role="dialog" aria-modal="true" aria-label="Site menu" onKeyDown={trapFocus}>
        <div className="mobile-drawer__head">
          <Brand />
          <button className="mobile-drawer__close" ref={closeRef} type="button" aria-label="Close menu" onClick={closeMenu}>
            <Icon name="close" size={20} />
          </button>
        </div>

        <div className="mobile-drawer__scroll">
          <section className="mobile-drawer__profile" aria-label="Your account">
            <span className="mobile-drawer__avatar" aria-hidden="true">
              {auth.isAuthenticated ? displayName.trim().charAt(0).toUpperCase() : <Icon name="user" size={20} />}
            </span>
            {auth.isAuthenticated ? <div>
              <strong>Hi, {displayName}</strong>
              <small>{ROLE_LABELS[primaryRole] || 'Member'} account</small>
            </div> : <div>
              <strong>Welcome to Community Store</strong>
              <small>Sign in to buy, sell and join events.</small>
            </div>}
            {!auth.isAuthenticated && <div className="mobile-drawer__profile-actions">
              <Link className="button button--dark" to="/sign-in" onClick={closeMenu}>Sign In</Link>
              <Link className="button button--orange" to="/register" onClick={closeMenu}>Create account</Link>
            </div>}
          </section>

          <nav className="mobile-drawer__nav" aria-label="Mobile navigation">
            {buyerNavigation && <>
              <p className="mobile-drawer__label">Shop</p>
              <DrawerLink end icon="home" to="/" onNavigate={closeMenu}>Home</DrawerLink>
              <DrawerLink icon="grid" to="/marketplace" onNavigate={closeMenu}>Marketplace</DrawerLink>
              {cart.canPurchase && <DrawerLink icon="bag" to="/cart" onNavigate={closeMenu} badge={cart.itemCount > 0 ? <span className="mobile-drawer__badge">{displayedCartCount}</span> : null}>My cart</DrawerLink>}

              <p className="mobile-drawer__label">Shop by category</p>
              <div className="mobile-drawer__categories">
                {CATEGORIES.map((category) => (
                  <Link key={category.value} to={`/marketplace?category=${category.value}#listings`} onClick={closeMenu}>
                    <img src={category.image} alt="" loading="lazy" />
                    <span>{category.label}</span>
                  </Link>
                ))}
              </div>

              <p className="mobile-drawer__label">Community</p>
              <DrawerLink icon="users" to="/community" onNavigate={closeMenu}>Community &amp; events</DrawerLink>
              <DrawerLink icon="spark" to="/about" onNavigate={closeMenu}>About us</DrawerLink>
            </>}

            {isSeller && <>
              <p className="mobile-drawer__label">Vendor</p>
              <DrawerLink icon="grid" to="/seller" onNavigate={closeMenu}>Seller dashboard</DrawerLink>
              <DrawerLink icon="shield" to="/vendor-status" onNavigate={closeMenu}>Verified vendor status</DrawerLink>
              <DrawerLink icon="store" to="/seller#product-editor" onNavigate={closeMenu}>My listings</DrawerLink>
              <DrawerLink icon="bag" to="/seller#seller-orders" onNavigate={closeMenu}>Customer orders</DrawerLink>
            </>}

            {isAdmin && <>
              <p className="mobile-drawer__label">Administration</p>
              <DrawerLink icon="grid" to="/admin" onNavigate={closeMenu}>Admin dashboard</DrawerLink>
              <DrawerLink icon="users" to="/admin/accounts" onNavigate={closeMenu}>Accounts</DrawerLink>
              <DrawerLink icon="shield" to="/moderation" onNavigate={closeMenu}>Moderation</DrawerLink>
            </>}

            {buyerNavigation && <>
              <p className="mobile-drawer__label">Vendors</p>
              <DrawerLink icon="store" to={auth.isAuthenticated ? '/vendor-status' : '/register?type=VENDOR'} onNavigate={closeMenu}>Become a vendor</DrawerLink>
              {auth.isAuthenticated && auth.hasRole('BUYER') && <DrawerLink icon="shield" to="/vendor-status" onNavigate={closeMenu}>Application status</DrawerLink>}
            </>}

            {auth.isAuthenticated && <>
              <p className="mobile-drawer__label">Account</p>
              <DrawerLink icon="user" to={auth.defaultRoute} onNavigate={closeMenu}>My dashboard</DrawerLink>
              <DrawerLink icon="bell" to="/notifications" onNavigate={closeMenu}>Inbox</DrawerLink>
              <DrawerLink icon="settings" to="/settings" onNavigate={closeMenu}>Settings</DrawerLink>
            </>}
          </nav>
        </div>

        {auth.isAuthenticated && <div className="mobile-drawer__footer">
          <button className="mobile-drawer__signout" type="button" onClick={signOut}><Icon name="logout" size={18} /> Sign out</button>
        </div>}
      </aside>
    </div>
  </>;
}
