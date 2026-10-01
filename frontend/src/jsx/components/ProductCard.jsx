import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getProductSpecificationSummary } from '../../js/data/categories.js';
import { formatCondition, formatPrice } from '../../js/utils/formatters.js';
import { getCategoryFallback, getProductImage } from '../../js/utils/productImages.js';
import Icon from './Icon.jsx';
import SellerIdentity from './SellerIdentity.jsx';

export default function ProductCard({ product, featured = false }) {
  const productImage = getProductImage(product);
  const [imageSource, setImageSource] = useState(productImage.src);
  const soldOut = product.status === 'SOLD_OUT' || product.quantity <= 0;
  const service = product.category === 'SERVICE';
  const condition = service ? 'Service' : formatCondition(product.condition);
  const specifications = getProductSpecificationSummary(product);
  const detailPath = `/products/${product.id}`;

  useEffect(() => setImageSource(productImage.src), [productImage.src]);

  return (
    <article className="product-card">
      <Link className="product-card__media" to={detailPath} aria-label={`View ${product.title}`}>
        <img src={imageSource} alt={productImage.alt} loading="lazy" decoding="async" onError={() => setImageSource(getCategoryFallback(product.category))} />
        {(featured || soldOut) && <span className={`product-card__badge${soldOut ? ' product-card__badge--sold' : ''}`}>{soldOut ? 'Sold out' : 'Featured'}</span>}
        <span className="product-card__open" aria-hidden="true"><Icon name="arrowRight" size={14} /></span>
      </Link>
      <div className="product-card__body">
        <div className="product-card__title-row"><h3><Link to={detailPath}>{product.title}</Link></h3><strong>{formatPrice(product.price, product.currency)}</strong></div>
        {specifications.length > 0 && <p className="product-card__specifications">{specifications.join(' · ')}</p>}
        <SellerIdentity seller={product.seller} />
        <p className="product-card__meta">{condition}<span>•</span>{service ? 'Local booking' : 'Campus pickup'}</p>
      </div>
    </article>
  );
}
