import { Link } from 'react-router-dom';

export default function Brand({ light = false }) {
  return (
    <Link className={`brand${light ? ' brand--light' : ''}`} to="/" aria-label="Community Store home">
      <img src="/images/brand/unimarket-logo.png" alt="" width="34" height="34" />
      <span className="brand__copy">
        <strong>Community Store</strong>
        <small>People · Ideas · Goods · Opportunities</small>
      </span>
    </Link>
  );
}
