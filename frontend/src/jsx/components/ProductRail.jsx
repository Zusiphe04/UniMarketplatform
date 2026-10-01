import { useCallback, useEffect, useId, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { useProducts } from '../../js/hooks/useProducts.js';
import Icon from './Icon.jsx';
import ProductCard from './ProductCard.jsx';

const SKELETON_COUNT = 5;

function prefersReducedMotion() {
  return typeof window !== 'undefined' && window.matchMedia?.('(prefers-reduced-motion: reduce)').matches;
}

function RailSkeleton() {
  return (
    <li className="product-rail__item" aria-hidden="true">
      <div className="product-card product-card--skeleton">
        <div className="skeleton skeleton--image" />
        <div className="product-card__body">
          <div className="skeleton skeleton--title" />
          <div className="skeleton skeleton--meta" />
        </div>
      </div>
    </li>
  );
}

/**
 * Horizontally scrolling product carousel. Touch devices swipe natively with scroll-snap;
 * pointer devices also get previous/next buttons.
 */
export default function ProductRail({
  eyebrow,
  title,
  products = [],
  isLoading = false,
  seeAllTo,
  seeAllLabel = 'See all',
  tone = 'light',
}) {
  const headingId = useId();
  const trackRef = useRef(null);
  const [edges, setEdges] = useState({ start: true, end: false });

  const updateEdges = useCallback(() => {
    const track = trackRef.current;
    if (!track) return;
    const maxScroll = track.scrollWidth - track.clientWidth;
    setEdges({ start: track.scrollLeft <= 4, end: track.scrollLeft >= maxScroll - 4 });
  }, []);

  useEffect(() => {
    const track = trackRef.current;
    if (!track) return undefined;
    updateEdges();
    track.addEventListener('scroll', updateEdges, { passive: true });
    const observer = typeof ResizeObserver === 'function' ? new ResizeObserver(updateEdges) : null;
    observer?.observe(track);
    return () => {
      track.removeEventListener('scroll', updateEdges);
      observer?.disconnect();
    };
  }, [updateEdges, products.length, isLoading]);

  const scrollByPage = (direction) => {
    const track = trackRef.current;
    if (!track) return;
    track.scrollBy({ left: direction * track.clientWidth * 0.85, behavior: prefersReducedMotion() ? 'auto' : 'smooth' });
  };

  return (
    <section className={`product-rail product-rail--${tone}`} aria-labelledby={headingId}>
      <div className="container product-rail__heading">
        <div>
          {eyebrow && <p className="eyebrow eyebrow--line">{eyebrow}</p>}
          <h2 id={headingId}>{title}</h2>
        </div>
        <div className="product-rail__actions">
          {seeAllTo && <Link className="product-rail__see-all" to={seeAllTo}>{seeAllLabel} <Icon name="arrowRight" size={14} /></Link>}
          <button className="product-rail__control" type="button" aria-label={`Scroll ${title} back`} disabled={edges.start} onClick={() => scrollByPage(-1)}>
            <Icon name="chevronLeft" size={18} />
          </button>
          <button className="product-rail__control" type="button" aria-label={`Scroll ${title} forward`} disabled={edges.end} onClick={() => scrollByPage(1)}>
            <Icon name="chevronRight" size={18} />
          </button>
        </div>
      </div>

      <ul className="product-rail__track" ref={trackRef} aria-busy={isLoading || undefined} aria-label={title}>
        {isLoading
          ? Array.from({ length: SKELETON_COUNT }, (_, index) => <RailSkeleton key={index} />)
          : products.map((product) => (
            <li className="product-rail__item" key={product.id}><ProductCard product={product} /></li>
          ))}
      </ul>
    </section>
  );
}

function LoadedCatalogueRail({ category, excludeId, size, ...railProps }) {
  const catalogue = useProducts({ page: 0, size, query: '', category });
  const products = catalogue.data.content.filter((product) => product.id !== excludeId);

  // A rail is a discovery aid, so it disappears quietly when there is nothing useful to show.
  if (catalogue.isError || (catalogue.isSuccess && products.length === 0)) return null;
  return <ProductRail {...railProps} isLoading={catalogue.isLoading} products={products} />;
}

/**
 * Rail backed by the live catalogue API. It only requests products once it is close to the
 * viewport, so a page with several rails does not fire every request on first paint.
 */
export function CatalogueRail({ category = '', excludeId = null, size = 12, ...railProps }) {
  const placeholderRef = useRef(null);
  const [visible, setVisible] = useState(() => typeof IntersectionObserver !== 'function');

  useEffect(() => {
    if (visible || !placeholderRef.current) return undefined;
    const observer = new IntersectionObserver((entries) => {
      if (entries.some((entry) => entry.isIntersecting)) {
        setVisible(true);
        observer.disconnect();
      }
    }, { rootMargin: '400px 0px' });
    observer.observe(placeholderRef.current);
    return () => observer.disconnect();
  }, [visible]);

  if (!visible) {
    return <div ref={placeholderRef}><ProductRail {...railProps} isLoading /></div>;
  }
  return <LoadedCatalogueRail category={category} excludeId={excludeId} size={size} {...railProps} />;
}
