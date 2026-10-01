import { Link } from 'react-router-dom';
import { CATEGORIES } from '../../js/data/categories.js';
import Icon from './Icon.jsx';

export default function CategorySection({ activeCategory, onSelect, showAll = false }) {
  const categories = showAll ? CATEGORIES : CATEGORIES.filter((category) => category.featured);

  return (
    <section className="market-section categories-section" id="categories" aria-labelledby="categories-title">
      <div className="container">
        <div className="reference-heading">
          <div>
            <p className="eyebrow eyebrow--line">Shop by category</p>
            <h2 id="categories-title">{showAll ? 'All categories' : 'Popular categories'}</h2>
          </div>
          <Link to={showAll ? '/marketplace' : '/marketplace#categories'}>
            {showAll ? 'Show all listings' : 'View all categories'} <Icon name="arrowRight" size={14} />
          </Link>
        </div>

        <div className="category-grid">
          {categories.map((category) => (
            <button
              className={`category-card${activeCategory === category.value ? ' category-card--active' : ''}`}
              type="button"
              key={category.value}
              aria-pressed={activeCategory === category.value}
              onClick={() => onSelect(activeCategory === category.value ? '' : category.value)}
            >
              <span className="category-card__image"><img src={category.image} alt="" loading="lazy" /></span>
              <span className="category-card__footer">
                <span><strong>{category.label === 'Books' ? 'Textbooks' : category.label}</strong><small>{category.featureCopy}</small></span>
                <span className="category-card__arrow"><Icon name="arrowRight" size={12} /></span>
              </span>
            </button>
          ))}
        </div>
      </div>
    </section>
  );
}
