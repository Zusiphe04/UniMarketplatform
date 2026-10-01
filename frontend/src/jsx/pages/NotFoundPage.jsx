import { Link } from 'react-router-dom';

export default function NotFoundPage() {
  return <main className="route-error"><p className="eyebrow eyebrow--line">404</p><h1>This page is outside the marketplace.</h1><p>Return home or open your role dashboard from the navigation.</p><Link className="button button--dark" to="/">Return home</Link></main>;
}
