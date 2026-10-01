import { useEffect, useMemo } from 'react';
import { useLocation, useSearchParams } from 'react-router-dom';
import { useProducts } from '../../js/hooks/useProducts.js';
import CampusCta from '../components/CampusCta.jsx';
import CategorySection from '../components/CategorySection.jsx';
import Hero from '../components/Hero.jsx';
import HowItWorks from '../components/HowItWorks.jsx';
import MarketingBanner from '../components/MarketingBanner.jsx';
import ProductRail, { CatalogueRail } from '../components/ProductRail.jsx';
import ProductSection from '../components/ProductSection.jsx';
import PromoSection from '../components/PromoSection.jsx';
import Testimonials from '../components/Testimonials.jsx';
import TrustBar from '../components/TrustBar.jsx';

const CATALOGUE_SIZE = 24;
const CATEGORY_RAILS = [
  { category: 'TECH', eyebrow: 'Devices & gadgets', title: 'Tech deals near you' },
  { category: 'BOOKS', eyebrow: 'Study smarter', title: 'Textbooks & study guides' },
  { category: 'ROOM_AND_HOME', eyebrow: 'Res & room', title: 'Room & home essentials' },
  { category: 'CLOTHING', eyebrow: 'Campus style', title: 'Clothing & sneakers' },
];
const SERVICE_RAIL = { category: 'SERVICE', eyebrow: 'Book a local', title: 'Services from the community' };
const FEATURED_TITLES = ['macbook air m1', 'campus hybrid bicycle', 'foldable lap desk', 'nike air force sneakers'];

function selectHomepageListings(products, filtered) {
  if (filtered) return products.slice(0, 4);
  const preferred = FEATURED_TITLES.map((title) => products.find((product) => product.title.toLocaleLowerCase('en-ZA') === title)).filter(Boolean);
  const preferredIds = new Set(preferred.map((product) => product.id));
  return [...preferred, ...products.filter((product) => !preferredIds.has(product.id))].slice(0, 4);
}

export default function HomePage() {
  const location = useLocation();
  const [searchParams, setSearchParams] = useSearchParams();
  const query = searchParams.get('query') || '';
  const activeCategory = searchParams.get('category') || '';
  const catalogue = useProducts({ page: 0, size: CATALOGUE_SIZE, query, category: activeCategory });

  useEffect(() => {
    if (!location.hash) return;
    window.requestAnimationFrame(() => document.getElementById(location.hash.slice(1))?.scrollIntoView({ behavior: 'smooth' }));
  }, [location.hash, query, activeCategory]);

  const displayedProducts = useMemo(
    () => selectHomepageListings(catalogue.data.content, Boolean(query || activeCategory)),
    [catalogue.data.content, query, activeCategory],
  );
  const viewAllParams = new URLSearchParams();
  if (query.trim()) viewAllParams.set('query', query.trim());
  if (activeCategory) viewAllParams.set('category', activeCategory);
  const viewAllTo = `/marketplace${viewAllParams.size ? `?${viewAllParams}` : ''}#listings`;

  const selectCategory = (category) => {
    const next = new URLSearchParams(searchParams);
    if (category) next.set('category', category); else next.delete('category');
    setSearchParams(next, { replace: true });
    window.requestAnimationFrame(() => document.getElementById('listings')?.scrollIntoView({ behavior: 'smooth' }));
  };

  const clearFilters = () => setSearchParams({}, { replace: true });
  const filtered = Boolean(query || activeCategory);
  // Trending rail shows catalogue items that are not already featured in the listings grid.
  const trendingProducts = useMemo(() => {
    const featuredIds = new Set(displayedProducts.map((product) => product.id));
    return catalogue.data.content.filter((product) => !featuredIds.has(product.id)).slice(0, 16);
  }, [catalogue.data.content, displayedProducts]);
  const showTrending = !filtered && !catalogue.isError && (catalogue.isLoading || trendingProducts.length > 0);

  return <main id="main-content">
    <Hero />
    <TrustBar />
    <MarketingBanner
      campaign="macbook"
      eyebrow="Campus tech"
      title="A MacBook for your next big idea."
      copy="Explore local MacBook listings for coursework, creative projects and campus life."
      imageSrc="/images/Woman gazing at laptop, flowing green banner.png"
      imageAlt="Student studying beside a MacBook laptop"
      to="/marketplace?query=MacBook&category=TECH#listings"
      cta="Browse MacBooks"
    />
    <CategorySection activeCategory={activeCategory} onSelect={selectCategory} />
    {showTrending && <ProductRail
      eyebrow="Trending on campus"
      isLoading={catalogue.isLoading}
      products={trendingProducts}
      seeAllTo="/marketplace#listings"
      title="Popular right now"
    />}
    <ProductSection
      activeCategory={activeCategory}
      error={catalogue.error}
      featureFirst
      isLoading={catalogue.isLoading}
      onClearFilters={clearFilters}
      onRetry={catalogue.retry}
      products={displayedProducts}
      query={query}
      viewAllTo={viewAllTo}
    />
    {CATEGORY_RAILS.map((rail, index) => (
      <CatalogueRail
        category={rail.category}
        eyebrow={rail.eyebrow}
        key={rail.category}
        seeAllTo={`/marketplace?category=${rail.category}#listings`}
        title={rail.title}
        tone={index % 2 === 0 ? 'cream' : 'light'}
      />
    ))}
    <MarketingBanner
      campaign="kota"
      eyebrow="Local flavour"
      title="A campus favourite, made local."
      copy="Discover freshly prepared kota listings from sellers in your community."
      imageSrc="/images/South African Kota on cream platter.png"
      imageAlt="Fresh South African kota topped with chips, wors and egg"
      to="/marketplace?query=Kota&category=OTHER#listings"
      cta="Find a kota"
    />
    <CatalogueRail
      category={SERVICE_RAIL.category}
      eyebrow={SERVICE_RAIL.eyebrow}
      seeAllTo={`/marketplace?category=${SERVICE_RAIL.category}#listings`}
      title={SERVICE_RAIL.title}
      tone="cream"
    />
    <PromoSection />
    <HowItWorks />
    <Testimonials />
    <CampusCta />
  </main>;
}
