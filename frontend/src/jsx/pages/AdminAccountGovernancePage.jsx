import { useCallback, useEffect, useState } from 'react';
import { apiRequest } from '../../js/api/client.js';
import DashboardShell from '../components/DashboardShell.jsx';

const ROLES = ['BUYER', 'MODERATOR', 'ADMIN'];
const STATUSES = ['PENDING_EMAIL', 'ACTIVE', 'LOCKED', 'SUSPENDED', 'CLOSED'];
const PERSONAS = ['STUDENT', 'FACULTY', 'RESIDENT'];

function label(value) { return String(value || '').replaceAll('_', ' ').toLocaleLowerCase('en-ZA').replace(/^./, (letter) => letter.toUpperCase()); }

export default function AdminAccountGovernancePage() {
  const [accounts, setAccounts] = useState([]);
  const [status, setStatus] = useState('loading');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [working, setWorking] = useState('');
  const [drafts, setDrafts] = useState({});

  const load = useCallback(async (signal) => {
    setStatus('loading'); setError('');
    try { setAccounts(await apiRequest('/api/v1/admin/accounts', signal ? { signal } : {})); setStatus('ready'); }
    catch (requestError) { if (requestError.name !== 'AbortError') { setError(requestError.message); setStatus('error'); } }
  }, []);

  useEffect(() => { const controller = new AbortController(); load(controller.signal); return () => controller.abort(); }, [load]);
  const changeDraft = (id, field, value) => setDrafts((current) => ({ ...current, [id]: { status: 'ACTIVE', role: 'MODERATOR', reason: '', ...current[id], [field]: value } }));
  const run = async (key, request, message) => {
    setWorking(key); setError(''); setSuccess('');
    try { await request(); setSuccess(message); await load(); }
    catch (requestError) { setError(requestError.message); }
    finally { setWorking(''); }
  };

  return <DashboardShell role="ADMIN" eyebrow="Platform governance">
    <div className="workflow-page admin-governance">
      <div className="workflow-heading"><div><p className="eyebrow eyebrow--line">Account governance</p><h2>Status, roles and personas</h2><p>Apply verified platform-access controls. Role changes require a new sign-in before a member’s access token reflects them.</p></div><button className="button button--dark" disabled={status === 'loading'} type="button" onClick={() => load()}>Refresh accounts</button></div>
      {error && <div className="form-alert form-alert--error" role="alert">{error} <button type="button" onClick={() => load()}>Retry</button></div>}
      {success && <div className="form-alert form-alert--success" role="status">{success}</div>}
      {status === 'loading' ? <section className="workflow-panel"><div className="panel-empty"><p>Loading governed accounts…</p></div></section> : <section className="governance-list">{accounts.map((account) => { const draft = { status: account.status || 'ACTIVE', role: 'MODERATOR', reason: '', ...drafts[account.id] }; return <article className="workflow-panel" key={account.id}><div className="governance-account"><div><p className="eyebrow eyebrow--line">{account.email}</p><h3>{account.displayName || account.fullName || 'Unnamed member'}</h3><p>{label(account.status)} · {account.emailVerified ? 'Email verified' : 'Email pending'}</p></div><div className="governance-roles">{(account.roles || []).map((role) => <span key={role}>{label(role)} <button disabled={working === `${account.id}-role-${role}`} type="button" title={`Revoke ${role}`} onClick={() => run(`${account.id}-role-${role}`, () => apiRequest(`/api/v1/admin/accounts/${account.id}/roles/${role}`, { method: 'DELETE' }), `${role} was revoked.`)}>×</button></span>)}</div></div><div className="governance-controls"><form className="platform-form governance-control" onSubmit={(event) => { event.preventDefault(); run(`${account.id}-status`, () => apiRequest(`/api/v1/admin/accounts/${account.id}/status`, { method: 'PATCH', body: { status: draft.status, reason: draft.reason } }), 'Account status updated.'); }}><strong>Account status</strong><select value={draft.status} onChange={(event) => changeDraft(account.id, 'status', event.target.value)}>{STATUSES.map((value) => <option key={value} value={value}>{label(value)}</option>)}</select><input required maxLength="500" placeholder="Reason for this status change" value={draft.reason} onChange={(event) => changeDraft(account.id, 'reason', event.target.value)} /><button className="button button--dark" disabled={working === `${account.id}-status`}>{working === `${account.id}-status` ? 'Saving…' : 'Apply status'}</button></form><div className="governance-control"><strong>Grant role</strong><select value={draft.role} onChange={(event) => changeDraft(account.id, 'role', event.target.value)}>{ROLES.map((role) => <option key={role} value={role}>{label(role)}</option>)}</select><button className="button button--dark" disabled={working === `${account.id}-grant`} type="button" onClick={() => run(`${account.id}-grant`, () => apiRequest(`/api/v1/admin/accounts/${account.id}/roles`, { method: 'POST', body: { role: draft.role } }), `${draft.role} role request completed.`)}>Grant role</button></div><div className="governance-control"><strong>Verify declared persona</strong><div className="governance-personas">{PERSONAS.map((persona) => <button disabled={working === `${account.id}-persona-${persona}`} key={persona} type="button" onClick={() => run(`${account.id}-persona-${persona}`, () => apiRequest(`/api/v1/admin/accounts/${account.id}/personas/${persona}/verify`, { method: 'POST' }), `${label(persona)} persona verified.`)}>{label(persona)}</button>)}</div><small>Only an existing declared persona can be verified. Persona assignments are not included in the admin account list.</small></div></div></article>; })}</section>}
    </div>
  </DashboardShell>;
}
