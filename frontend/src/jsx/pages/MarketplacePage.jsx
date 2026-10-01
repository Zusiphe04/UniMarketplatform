import { useEffect, useState } from 'react';
import { useLocation, useSearchParams } from 'react-router-dom';
import { CATEGORIES, CATEGORY_BY_VALUE } from '../../js/data/categories.js';
import { useProducts } from '../../js/hooks/useProducts.js';
import { useAuth } from '../auth/AuthContext.jsx';
import AppSidebar from '../components/AppSidebar.jsx';
import CategorySection from '../components/CategorySection.jsx';
import ProductSection from '../components/ProductSection.jsx';
import SearchBox from '../components/SearchBox.jsx';

const PAGE_SIZE = 12;
function readPage(value) { const parsed = Number.parseInt(value || '1', 10); return Number.isFinite(parsed) && parsed > 0 ? parsed - 1 : 0; }

export default function MarketplacePage() {
  const auth = useAuth();
  const location = useLocation();
  const [searchParams, setSearchParams] = useSearchParams();
  const query = searchParams.get('query')?.trim() || '';
  const requestedCategory = searchParams.get('category') || '';
  const category = CATEGORIES.some((item) => item.value === requestedCategory) ? requestedCategory : '';
  const page = readPage(searchParams.get('page'));
  const [searchValue, setSearchValue] = useState(query);
  const catalogue = useProducts({ page, size: PAGE_SIZE, query, category });

  useEffect(() => setSearchValue(query), [query]);
  useEffect(() => { if (location.hash) window.requestAnimationFrame(() => document.getElementById(location.hash.slice(1))?.scrollIntoView({ behavior: 'smooth' })); }, [location.hash, query, category, page]);

  const updateParams = (update) => { const next = new URLSearchParams(searchParams); update(next); setSearchParams(next); };
  const submitSearch = (value) => { const normalized = value.trim(); updateParams((next) => { if (normalized) next.set('query', normalized); else next.delete('query'); next.delete('page'); }); window.requestAnimationFrame(() => document.getElementById('listings')?.scrollIntoView({ behavior: 'smooth' })); };
  const selectCategory = (value) => { const nextCategory = value === category ? '' : value; updateParams((next) => { if (nextCategory) next.set('category', nextCategory); else next.delete('category'); next.delete('page'); }); window.requestAnimationFrame(() => document.getElementById('listings')?.scrollIntoView({ behavior: 'smooth' })); };
  const clearFilters = () => { setSearchValue(''); setSearchParams({}); };
  const setPage = (nextPage) => { updateParams((next) => { if (nextPage > 0) next.set('page', String(nextPage + 1)); else next.delete('page'); }); window.requestAnimationFrame(() => document.getElementById('listings')?.scrollIntoView({ behavior: 'smooth' })); };

  const categoryLabel = CATEGORY_BY_VALUE[category]?.label;
  const title = categoryLabel ? `${categoryLabel} listings` : query ? 'Search results' : 'All listings';
  const resultSummary = catalogue.isSuccess ? `${catalogue.data.totalElements} published ${catalogue.data.totalElements === 1 ? 'listing' : 'listings'}${query ? ` matching “${query}”` : ''}.` : '';
  const pagination = catalogue.isSuccess && catalogue.data.totalPages > 1 ? <nav className="catalogue-pagination" aria-label="Marketplace pages"><button type="button" disabled={page === 0} onClick={() => setPage(page - 1)}>Previous</button><span>Page <strong>{page + 1}</strong> of <strong>{catalogue.data.totalPages}</strong></span><button type="button" disabled={page + 1 >= catalogue.data.totalPages} onClick={() => setPage(page + 1)}>Next</button></nav> : null;

  const content = <>
    <section className="marketplace-intro" aria-label="Marketplace"><div className="container marketplace-intro__grid">
      <div className="marketplace-intro__copy"><p className="eyebrow eyebrow--line">Verified campus vendors</p><p>Shop products, services and everyday essentials from people in your campus community.</p><SearchBox id="marketplace-search" value={searchValue} onChange={setSearchValue} onSubmit={submitSearch} /></div>
      <figure className="marketplace-intro__art"><img src="/images/vendors.png" alt="A local campus vendor serving customers at an outdoor market" /></figure>
    </div></section>
    <CategorySection activeCategory={category} onSelect={selectCategory} showAll />
    <ProductSection activeCategory={category} error={catalogue.error} footer={pagination} isLoading={catalogue.isLoading} onClearFilters={clearFilters} onRetry={catalogue.retry} products={catalogue.data.content} query={query} resultSummary={resultSummary} showViewAll={false} title={title} />
  </>;
  return <main className="marketplace-page" id="main-content">{auth.isAuthenticated ? <div className="container portal-shell portal-shell--page"><AppSidebar /><div className="portal-main">{content}</div></div> : content}</main>;
}
