import { Link } from 'react-router-dom';
import Icon from './Icon.jsx';

const STATS = [
  { value: '10+', label: 'Community Members' },
  { value: '10+', label: 'Verified Vendors' },
  { value: '10+', label: 'Products & Services' },
];

export default function Hero() {
  return (
    <section className="hero" aria-labelledby="hero-title">
      <div className="container hero__grid">
        <div className="hero__content">
          <p className="eyebrow eyebrow--line">Local. People. Real. Opportunities.</p>
          <h1 id="hero-title">Buy. Sell. Support<br />a stronger<br /><em>community.</em></h1>
          <p className="hero__lead">
            A trusted marketplace for students, faculty, local vendors and residents.
          </p>
          <div className="hero__actions">
            <Link className="button button--dark" to="/marketplace#listings">Explore Marketplace <Icon name="arrowRight" size={16} /></Link>
            <Link className="round-link" to="/community">Join Community <span><Icon name="arrowRight" size={15} /></span></Link>
          </div>
          <dl className="hero__stats">
            {STATS.map((stat) => (
              <div key={stat.label}>
                <dt>{stat.value}</dt>
                <dd>{stat.label}</dd>
              </div>
            ))}
          </dl>
        </div>

        <figure className="hero__visual">
          <img src="/images/hero/home-hero.png" alt="Student walking through a modern university campus" />
          <figcaption>
            <p>A trusted marketplace</p>
            <span>Where people and opportunity meet.</span>
            <Link to="/marketplace#listings" aria-label="Explore the marketplace"><Icon name="arrowRight" size={16} /></Link>
          </figcaption>
        </figure>
      </div>
    </section>
  );
}
