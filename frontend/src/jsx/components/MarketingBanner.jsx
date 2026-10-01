import { Link } from 'react-router-dom';
import Icon from './Icon.jsx';

export default function MarketingBanner({ campaign, eyebrow, title, copy, imageSrc, imageAlt, to, cta }) {
  const titleId = `${campaign}-campaign-title`;

  return (
    <section className={`marketing-banner marketing-banner--${campaign}`} aria-labelledby={titleId}>
      <div className="container">
        <div className="marketing-banner__panel">
          <div className="marketing-banner__content">
            <p className="eyebrow eyebrow--light">{eyebrow}</p>
            <h2 id={titleId}>{title}</h2>
            <p>{copy}</p>
            <Link className="button button--orange" to={to}>{cta} <Icon name="arrowRight" size={15} /></Link>
          </div>
          <figure className="marketing-banner__media">
            <img src={imageSrc} alt={imageAlt} loading="lazy" />
          </figure>
        </div>
      </div>
    </section>
  );
}
