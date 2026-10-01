import { Link } from 'react-router-dom';
import Icon from './Icon.jsx';
import ProductCard from './ProductCard.jsx';

function ProductSkeleton() {
  return (
    <div className="product-card product-card--skeleton" aria-hidden="true">
      <div className="skeleton skeleton--image" />
      <div className="product-card__body">
        <div className="skeleton skeleton--title" />
        <div className="skeleton skeleton--meta" />
      </div>
    </div>
  );
}

export default function ProductSection({
  products,
  isLoading,
  error,
  onRetry,
  query,
  activeCategory,
  onClearFilters,
  title = 'Latest listings',
  resultSummary = '',
  showViewAll = true,
  viewAllTo = '/marketplace#listings',
  featureFirst = false,
  footer = null,
}) {
  const hasFilters = Boolean(query || activeCategory);

  return (
    <section className="market-section listings-section" id="listings" aria-labelledby="listings-title">
      <div className="container">
        <div className="reference-heading">
          <div>
            <p className="eyebrow eyebrow--line">Fresh in the marketplace</p>
            <h2 id="listings-title">{title}</h2>
            {resultSummary && <p className="catalogue-summary" role="status">{resultSummary}</p>}
          </div>
          {showViewAll && <Link to={viewAllTo}>View all listings <Icon name="arrowRight" size={14} /></Link>}
        </div>

        {isLoading ? (
          <div className="product-grid" aria-busy="true" aria-label="Loading marketplace listings">
            {Array.from({ length: 4 }, (_, index) => <ProductSkeleton key={index} />)}
          </div>
        ) : error ? (
          <div className="empty-state" role="alert">
            <Icon name="search" size={24} />
            <h3>The live catalogue could not be loaded</h3>
            <p>{error.message || 'Check that the UniMarket API is running, then try again.'}</p>
            {onRetry && <button className="button button--dark" type="button" onClick={onRetry}>Retry catalogue</button>}
          </div>
        ) : products.length > 0 ? <>
          <div className="product-grid">
            {products.map((product, index) => <ProductCard featured={featureFirst && index === 0} key={product.id} product={product} />)}
          </div>
          {footer}
        </> : (
          <div className="empty-state">
            <Icon name="search" size={24} />
            <h3>{hasFilters ? 'No listings matched your search' : 'No published listings yet'}</h3>
            <p>{hasFilters ? 'Try another phrase or clear the selected category.' : 'Published seller listings will appear here as soon as they are available.'}</p>
            {hasFilters && <button className="button button--dark" type="button" onClick={onClearFilters}>Clear filters</button>}
          </div>
        )}
      </div>
    </section>
  );
}
