import { Link } from 'react-router-dom';
import Icon from './Icon.jsx';

export default function CampusCta() {
  return (
    <section className="campus-cta" id="join" aria-labelledby="cta-title">
      <div className="container campus-cta__inner">
        <div>
          <p className="eyebrow eyebrow--line eyebrow--light">Join the movement</p>
          <h2 id="cta-title">Be part of a stronger,<br />more connected community.</h2>
        </div>
        <div className="campus-cta__action">
          <p>Create your account today and start buying, selling and supporting local.</p>
          <Link className="button button--orange" to="/register">Get Started <Icon name="arrowRight" size={15} /></Link>
        </div>
      </div>
    </section>
  );
}
