import Icon from './Icon.jsx';

const FEATURES = [
  {
    icon: 'user',
    title: 'Verified Community',
    text: 'Students, staff, vendors and residents.',
  },
  {
    icon: 'store',
    title: 'Local Commerce',
    text: 'Support local vendors and small businesses.',
  },
  {
    icon: 'spark',
    title: 'Sustainable Impact',
    text: 'Give goods a longer life. Build a greener community.',
  },
];

export default function TrustBar() {
  return (
    <section className="trust-bar" aria-label="Community Store commitments">
      <div className="container trust-bar__grid">
        {FEATURES.map((feature) => (
          <article className="trust-item" key={feature.title}>
            <span className="trust-item__icon"><Icon name={feature.icon} size={21} /></span>
            <div><h2>{feature.title}</h2><p>{feature.text}</p></div>
          </article>
        ))}
      </div>
    </section>
  );
}
