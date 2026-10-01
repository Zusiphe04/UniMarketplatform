import Icon from './Icon.jsx';

export default function SellerIdentity({ seller }) {
  const displayName = seller?.displayName || 'UniMarket seller';
  return <p className="seller-identity"><Icon name="user" size={14} /><span>Listed by <strong>{displayName}</strong></span></p>;
}
