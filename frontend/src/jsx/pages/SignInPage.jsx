import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { getDefaultRoute } from '../../js/auth/roles.js';
import { useAuth } from '../auth/AuthContext.jsx';
import Icon from '../components/Icon.jsx';

export default function SignInPage() {
  const auth = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (auth.isAuthenticated) navigate(getDefaultRoute(auth.account), { replace: true });
  }, [auth.isAuthenticated, auth.account, navigate]);

  const updateField = (event) => setForm((current) => ({ ...current, [event.target.name]: event.target.value }));

  const submit = async (event) => {
    event.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      const account = await auth.login(form);
      const requested = location.state?.from;
      navigate(requested && requested !== '/sign-in' ? requested : getDefaultRoute(account), { replace: true });
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="auth-page">
      <section className="auth-panel">
        <div className="auth-panel__form">
          <div className="auth-heading">
            <p className="eyebrow eyebrow--line">Welcome back</p>
            <h1>Sign in to your community.</h1>
            <p>Access the workspace connected to your verified roles and continue where you left off.</p>
          </div>

          <form className="platform-form" onSubmit={submit}>
            {error && <div className="form-alert form-alert--error" role="alert">{error}</div>}
            <label>Email address<input required autoComplete="email" name="email" type="email" value={form.email} onChange={updateField} /></label>
            <label>Password<input required autoComplete="current-password" minLength="12" name="password" type="password" value={form.password} onChange={updateField} /></label>
            <button className="button button--orange form-submit" type="submit" disabled={submitting}>
              {submitting ? 'Signing in…' : 'Sign In'} {!submitting && <Icon name="arrowRight" size={16} />}
            </button>
          </form>

          <p className="auth-switch">New to Community Store? <Link to="/register">Create your account</Link></p>
        </div>

        <div className="auth-panel__visual">
          <img src="/images/hero/home-hero.png" alt="Student entering a modern campus building" />
          <div><span>One account.</span><h2>Every role in the right place.</h2><p>Buyers, sellers, moderators and administrators each receive a focused workspace.</p></div>
        </div>
      </section>
    </main>
  );
}
