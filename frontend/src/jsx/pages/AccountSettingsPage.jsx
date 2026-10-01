import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getPrimaryRole } from '../../js/auth/roles.js';
import { apiRequest } from '../../js/api/client.js';
import { useAuth } from '../auth/AuthContext.jsx';
import DashboardShell from '../components/DashboardShell.jsx';

const DATE_FORMATTER = new Intl.DateTimeFormat('en-ZA', { dateStyle: 'medium', timeStyle: 'short' });

function formatDate(value) {
  const date = value ? new Date(value) : null;
  return date && !Number.isNaN(date.getTime()) ? DATE_FORMATTER.format(date) : 'Time unavailable';
}

export default function AccountSettingsPage() {
  const auth = useAuth();
  const role = getPrimaryRole(auth.account) || 'BUYER';
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [sessions, setSessions] = useState([]);
  const [status, setStatus] = useState('loading');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [working, setWorking] = useState('');
  const [passwords, setPasswords] = useState({ currentPassword: '', newPassword: '' });

  const load = useCallback(async (signal) => {
    setStatus('loading'); setError('');
    try {
      const [savedProfile, activeSessions] = await Promise.all([
        apiRequest('/api/v1/account/profile', signal ? { signal } : {}),
        apiRequest('/api/v1/account/sessions', signal ? { signal } : {}),
      ]);
      setProfile(savedProfile); setSessions(activeSessions || []); setStatus('ready');
    } catch (requestError) {
      if (requestError.name !== 'AbortError') { setError(requestError.message); setStatus('error'); }
    }
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    load(controller.signal);
    return () => controller.abort();
  }, [load]);

  const saveProfile = async (event) => {
    event.preventDefault(); setWorking('profile'); setError(''); setSuccess('');
    try {
      const updated = await apiRequest('/api/v1/account/profile', { method: 'PUT', body: { displayName: profile.displayName || null, phoneNumber: profile.phoneNumber || '', bio: profile.bio || null } });
      setProfile(updated); await auth.reconcileAccount(); setSuccess('Profile details saved.');
    } catch (requestError) { setError(requestError.message); }
    finally { setWorking(''); }
  };

  const changePassword = async (event) => {
    event.preventDefault(); setWorking('password'); setError(''); setSuccess('');
    try {
      await apiRequest('/api/v1/auth/change-password', { method: 'POST', body: passwords });
      await auth.logout(); navigate('/sign-in', { replace: true, state: { passwordChanged: true } });
    } catch (requestError) { setError(requestError.message); }
    finally { setWorking(''); }
  };

  const revokeSession = async (sessionId) => {
    setWorking(sessionId); setError(''); setSuccess('');
    try {
      await apiRequest(`/api/v1/account/sessions/${sessionId}`, { method: 'DELETE' });
      setSessions((current) => current.filter((item) => item.sessionId !== sessionId)); setSuccess('Session revoked.');
    } catch (requestError) { setError(requestError.message); }
    finally { setWorking(''); }
  };

  const logoutAll = async () => {
    setWorking('logout-all'); setError(''); setSuccess('');
    try {
      await apiRequest('/api/v1/auth/logout-all', { method: 'POST' });
      await auth.logout(); navigate('/sign-in', { replace: true });
    } catch (requestError) { setError(requestError.message); }
    finally { setWorking(''); }
  };

  return <DashboardShell role={role} eyebrow="Your account">
    <div className="workflow-page">
      <div className="workflow-heading"><div><p className="eyebrow eyebrow--line">Personal settings</p><h2>Profile and security</h2><p>Keep your public identity, credentials, and active refresh sessions under your control.</p></div></div>
      {error && <div className="form-alert form-alert--error" role="alert">{error} <button type="button" onClick={() => load()}>Retry</button></div>}
      {success && <div className="form-alert form-alert--success" role="status">{success}</div>}
      {status === 'loading' || !profile ? <section className="workflow-panel"><div className="panel-empty"><p>Loading account settings…</p></div></section> : <div className="settings-grid">
        <section className="workflow-panel"><div className="workflow-panel__heading"><div><p className="eyebrow eyebrow--line">Public profile</p><h3>How members see you</h3></div></div><form className="platform-form" onSubmit={saveProfile}><label>Display name<input required maxLength="120" value={profile.displayName || ''} onChange={(event) => setProfile((current) => ({ ...current, displayName: event.target.value }))} /></label><label>Phone number <span>Optional, international format</span><input placeholder="+27123456789" value={profile.phoneNumber || ''} onChange={(event) => setProfile((current) => ({ ...current, phoneNumber: event.target.value }))} /></label><label>Bio <span>Optional</span><textarea maxLength="1000" rows="4" value={profile.bio || ''} onChange={(event) => setProfile((current) => ({ ...current, bio: event.target.value }))} /></label><button className="button button--orange" disabled={working === 'profile'}>{working === 'profile' ? 'Saving…' : 'Save profile'}</button></form></section>
        <section className="workflow-panel"><div className="workflow-panel__heading"><div><p className="eyebrow eyebrow--line">Credential security</p><h3>Change password</h3></div></div><form className="platform-form" onSubmit={changePassword}><label>Current password<input required type="password" value={passwords.currentPassword} onChange={(event) => setPasswords((current) => ({ ...current, currentPassword: event.target.value }))} /></label><label>New password <span>12–128 characters</span><input required minLength="12" maxLength="128" type="password" value={passwords.newPassword} onChange={(event) => setPasswords((current) => ({ ...current, newPassword: event.target.value }))} /></label><button className="button button--dark" disabled={working === 'password'}>{working === 'password' ? 'Updating…' : 'Update password'}</button><small className="form-field-note">Changing your password ends all refresh sessions and returns you to sign in.</small></form></section>
      </div>}
      {status === 'ready' && <section className="workflow-panel settings-sessions"><div className="workflow-panel__heading"><div><p className="eyebrow eyebrow--line">Session control</p><h3>Signed-in devices</h3></div><button className="button button--dark" disabled={working === 'logout-all'} type="button" onClick={logoutAll}>{working === 'logout-all' ? 'Signing out…' : 'Sign out everywhere'}</button></div>{sessions.length ? <div className="session-list">{sessions.map((item) => <article key={item.sessionId}><div><strong>{item.userAgent || 'Unknown device'}</strong><small>Issued {formatDate(item.issuedAt)} · Last used {formatDate(item.lastUsedAt)} · Expires {formatDate(item.expiresAt)}</small></div><button className="button button--dark" disabled={working === item.sessionId} type="button" onClick={() => revokeSession(item.sessionId)}>{working === item.sessionId ? 'Revoking…' : 'Revoke'}</button></article>)}</div> : <div className="panel-empty"><p>No active refresh sessions are currently recorded.</p></div>}</section>}
    </div>
  </DashboardShell>;
}
