import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ApiError, apiRequest } from '../../js/api/client.js';
import { useAuth } from '../auth/AuthContext.jsx';
import DashboardShell from '../components/DashboardShell.jsx';
import Icon from '../components/Icon.jsx';

export default function VendorStatusPage() {
  const auth = useAuth();
  const isSeller = auth.hasRole('SELLER');
  const [profile, setProfile] = useState(null);
  const [status, setStatus] = useState('loading');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [form, setForm] = useState({ businessName: '', description: '', websiteUrl: '' });

  const load = useCallback(async (signal) => {
    setStatus('loading'); setError('');
    try { setProfile(await apiRequest('/api/v1/account/vendor-profile', signal ? { signal } : {})); setStatus('ready'); }
    catch (requestError) {
      if (requestError.name === 'AbortError') return;
      if (requestError instanceof ApiError && requestError.status === 404) setStatus('missing');
      else { setError(requestError.message); setStatus('error'); }
    }
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    load(controller.signal);
    return () => controller.abort();
  }, [load]);

  const submitApplication = async (event) => {
    event.preventDefault(); setSubmitting(true); setError('');
    try {
      const created = await apiRequest('/api/v1/account/vendor-profile', {
        method: 'POST',
        body: { vendorType: 'INDIVIDUAL', businessName: form.businessName, description: form.description, registrationNumber: null, websiteUrl: form.websiteUrl || null },
      });
      setProfile(created); setStatus('ready');
    } catch (requestError) { setError(requestError.message); }
    finally { setSubmitting(false); }
  };

  return <DashboardShell role={isSeller ? 'SELLER' : 'BUYER'} eyebrow="Vendor onboarding">
    {error && <div className="form-alert form-alert--error" role="alert">{error} {status === 'error' && <button type="button" onClick={() => load()}>Retry</button>}</div>}
    <section className="vendor-status-card" aria-busy={status === 'loading'}>
      {status === 'loading' && <p>Loading your persisted vendor profile…</p>}
      {status === 'missing' && isSeller && <>
        <span className="vendor-status-card__icon"><Icon name="store" size={25} /></span>
        <h2>You are a verified vendor</h2>
        <p>Your account already has seller access. You cannot submit another vendor application.</p>
        <Link className="button button--dark" to="/seller">Open seller workspace</Link>
      </>}
      {status === 'missing' && !isSeller && <>
        <span className="vendor-status-card__icon"><Icon name="store" size={25} /></span>
        <h2>Become a verified vendor</h2>
        <p>Submit your local business for administrator review. Approval adds the SELLER role to your account and unlocks listing tools.</p>
        <form className="platform-form vendor-application-form" onSubmit={submitApplication}>
          <label>Business name<input required maxLength="160" value={form.businessName} onChange={(event) => setForm((current) => ({ ...current, businessName: event.target.value }))} /></label>
          <label>Business description<textarea required maxLength="1000" rows="4" value={form.description} onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))} /></label>
          <label>Website URL <span>(optional)</span><input maxLength="500" type="url" value={form.websiteUrl} onChange={(event) => setForm((current) => ({ ...current, websiteUrl: event.target.value }))} /></label>
          <button className="button button--orange form-submit" disabled={submitting} type="submit">{submitting ? 'Submitting…' : 'Submit Vendor Application'} <Icon name="arrowRight" size={14} /></button>
        </form>
      </>}
      {profile && <>
        <span className={`vendor-status-card__status vendor-status-card__status--${profile.verificationStatus?.toLowerCase()}`}>{profile.verificationStatus}</span>
        <h2>{profile.businessName}</h2><p>{profile.description}</p>
        <dl><div><dt>Vendor type</dt><dd>{profile.vendorType?.replaceAll('_',' ')}</dd></div><div><dt>Submitted</dt><dd>{new Date(profile.submittedAt).toLocaleDateString('en-ZA')}</dd></div><div><dt>Review note</dt><dd>{profile.reviewNote || 'Awaiting administrator review'}</dd></div></dl>
        {profile.verificationStatus === 'VERIFIED' ? (isSeller ? <><p className="vendor-review-note">You are a verified vendor. Your seller workspace is active.</p><Link className="button button--dark" to="/seller">Open seller workspace</Link></> : <p className="vendor-review-note">Your vendor application is approved. Sign in again to activate your seller workspace.</p>) : <p className="vendor-review-note">An administrator must approve this application. Sign in again after approval to receive a JWT containing SELLER.</p>}
      </>}
    </section>
  </DashboardShell>;
}
