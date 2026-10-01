import Icon from './Icon.jsx';

const STEPS = [
  {
    number: '01',
    icon: 'user',
    title: 'Create an account',
    text: 'Join as a student, vendor or resident.',
  },
  {
    number: '02',
    icon: 'search',
    title: 'List or browse',
    text: 'Buy, sell or offer services with ease.',
  },
  {
    number: '03',
    icon: 'store',
    title: 'Connect & grow',
    text: 'Build trust and support your community.',
  },
];

export default function HowItWorks() {
  return (
    <section className="how-section" id="about" aria-labelledby="how-title">
      <div className="container">
        <div className="how-section__heading">
          <div>
            <p className="eyebrow eyebrow--line eyebrow--light">How it works</p>
            <h2 id="how-title">Simple. Secure. Community driven.</h2>
          </div>
        </div>
        <div className="steps-grid">
          {STEPS.map((step) => (
            <article className="step-card" key={step.number}>
              <span className="step-card__icon"><Icon name={step.icon} size={18} /></span>
              <div><small>{step.number}</small><h3>{step.title}</h3><p>{step.text}</p></div>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
