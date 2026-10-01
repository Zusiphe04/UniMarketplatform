import { Link } from 'react-router-dom';
import Icon from './Icon.jsx';

const BENEFITS = [
  { icon: 'user', title: 'Share Resources', text: 'Keep useful goods moving.' },
  { icon: 'store', title: 'Create Opportunities', text: 'Grow skills and local trade.' },
  { icon: 'spark', title: 'Build a Sustainable Future', text: 'Make community impact.' },
];

export default function PromoSection() {
  return (
    <section className="community-section" id="community" aria-labelledby="community-title">
      <div className="container community-panel">
        <div className="community-panel__image">
          <img src="/images/hero/middle-hero.png" alt="Student carrying books through a green university campus" loading="lazy" />
        </div>
        <div className="community-panel__content">
          <p className="eyebrow eyebrow--line">Community first</p>
          <h2 id="community-title">More than a marketplace.</h2>
          <p>
            We're a community-driven platform that connects people, ideas and resources—helping students, local businesses and residents support each other.
          </p>
          <div className="community-benefits">
            {BENEFITS.map((benefit) => (
              <article key={benefit.title}>
                <span><Icon name={benefit.icon} size={18} /></span>
                <h3>{benefit.title}</h3>
                <p>{benefit.text}</p>
              </article>
            ))}
          </div>
          <Link className="inline-arrow-link" to="/community">Explore community events <Icon name="arrowRight" size={14} /></Link>
        </div>
      </div>
    </section>
  );
}
