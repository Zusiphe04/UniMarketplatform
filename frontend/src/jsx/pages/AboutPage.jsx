import { Link } from 'react-router-dom';
import Icon from '../components/Icon.jsx';

const VALUES = [
  { icon: 'user', title: 'People first', text: 'Trust begins with clear identities, respectful participation and role-aware access.' },
  { icon: 'store', title: 'Local opportunity', text: 'Every listing, service and vendor relationship keeps value moving through the community.' },
  { icon: 'spark', title: 'Longer product lives', text: 'Affordable reuse reduces waste while making student and household essentials more accessible.' },
];

const PARTICIPANTS = [
  { name: 'Buyers', detail: 'Students, faculty and residents discover products, maintain persistent carts and follow orders.' },
  { name: 'Sellers', detail: 'Verified vendors create listings, publish supplied imagery and serve the community.' },
  { name: 'Moderators', detail: 'Trusted reviewers investigate reports and support safe, accountable trade.' },
  { name: 'Administrators', detail: 'Platform stewards manage accounts, roles and vendor verification.' },
];

export default function AboutPage() {
  return <main className="about-page">
    <section className="about-hero">
      <div className="container about-hero__grid">
        <div><p className="eyebrow eyebrow--line">Our purpose</p><h1>Built for the people who make a community <em>work.</em></h1><p>Community Store connects people, useful goods, practical skills and local opportunity in one trusted, role-aware marketplace.</p><div><Link className="button button--dark" to="/register">Join the community <Icon name="arrowRight" size={15} /></Link><Link className="round-link" to="/marketplace#listings">Explore marketplace <span><Icon name="arrowRight" size={14} /></span></Link></div></div>
        <figure><img src="/images/hero/middle-hero.png" alt="Student walking through a green university campus" /></figure>
      </div>
    </section>

    <section className="about-mission"><div className="container"><div className="reference-heading"><div><p className="eyebrow eyebrow--line">What guides us</p><h2>Modern commerce. Human values.</h2></div><p>Designed around real local needs—not anonymous transactions.</p></div><div className="about-values">{VALUES.map((value, index) => <article key={value.title}><span>0{index + 1}</span><div><Icon name={value.icon} size={20} /></div><h3>{value.title}</h3><p>{value.text}</p></article>)}</div></div></section>

    <section className="about-roles"><div className="container about-roles__grid"><div><p className="eyebrow eyebrow--line eyebrow--light">A multi-role platform</p><h2>Different responsibilities.<br /><em>One connected system.</em></h2><p>Backend permissions and verified personas shape what each member can see and do. Multi-role accounts can move between the workspaces they hold.</p></div><div>{PARTICIPANTS.map((participant) => <article key={participant.name}><span><Icon name="arrowRight" size={14} /></span><div><h3>{participant.name}</h3><p>{participant.detail}</p></div></article>)}</div></div></section>

    <section className="about-data"><div className="container about-data__grid"><figure className="about-data__media"><img src="/images/hero/about-student-exchange.webp" alt="Two university students exchanging a laptop on campus" loading="lazy" decoding="async" /></figure><div className="about-data__copy"><p className="eyebrow eyebrow--line">Accountable by design</p><h2>Real actions become reliable records.</h2><p>Registration, sessions, profile changes, vendor approvals, listings, carts, orders, notifications, reports and engagement are handled securely by the platform.</p></div></div></section>

    <section className="about-policies" id="privacy"><div className="container"><article><h2>Privacy</h2><p>Refresh credentials remain in secure HttpOnly cookies, access tokens stay in application memory, and sensitive payment credentials are never collected by this academic marketplace.</p></article><article id="terms"><h2>Responsible participation</h2><p>Members agree to provide accurate listing information, respect role boundaries, report suspicious activity and complete exchanges in good faith.</p></article><article id="contact"><h2>Contact & support</h2><p>Use your role dashboard and notification centre for platform activity. Administrators and moderators handle verification and safety workflows.</p></article></div></section>
  </main>;
}
