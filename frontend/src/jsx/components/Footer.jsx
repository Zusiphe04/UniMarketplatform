import { Link } from 'react-router-dom';
import Brand from './Brand.jsx';

const LINK_GROUPS = {
  Marketplace: [
    { label: 'Browse', to: '/marketplace' },
    { label: 'Categories', to: '/marketplace#categories' },
    { label: 'My cart', to: '/cart' },
  ],
  Community: [
    { label: 'Our community', to: '/community' },
    { label: 'About us', to: '/about' },
    { label: 'Get started', to: '/register' },
  ],
  Vendors: [
    { label: 'Become a Vendor', to: '/register?type=VENDOR' },
    { label: 'Vendor Dashboard', to: '/seller' },
    { label: 'Application Status', to: '/vendor-status' },
  ],
  Account: [
    { label: 'Sign in', to: '/sign-in' },
    { label: 'Buyer dashboard', to: '/buyer' },
    { label: 'Privacy', to: '/about#privacy' },
  ],
};

export default function Footer() {
  return (
    <footer className="site-footer">
      <div className="container site-footer__top">
        <div className="footer-brand"><Brand /><p>People supporting people.</p></div>
        <div className="footer-groups">
          {Object.entries(LINK_GROUPS).map(([heading, links]) => (
            <nav aria-label={heading} key={heading}>
              <h2>{heading}</h2>
              {links.map((link) => <Link to={link.to} key={link.label}>{link.label}</Link>)}
            </nav>
          ))}
        </div>
        <div className="footer-impact"><p>Local people.<br /><em>Real impact.</em></p></div>
      </div>
      <div className="container site-footer__bottom">
        <p>© {new Date().getFullYear()} Community Store. All rights reserved.</p>
        <nav aria-label="Legal"><Link to="/about#privacy">Privacy</Link><Link to="/about#terms">Terms</Link><Link to="/about#contact">Contact</Link></nav>
      </div>
    </footer>
  );
}
