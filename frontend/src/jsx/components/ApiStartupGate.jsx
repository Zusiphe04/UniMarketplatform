import { useEffect, useMemo, useState } from 'react';
import { API_BASE_URL } from '../../js/api/client.js';
import './ApiStartupGate.css';

const HEALTH_PATH = '/actuator/health';
const MINIMUM_SPLASH_MS = 900;
const DEFAULT_MAX_WAIT_MS = 90000;

function configuredWaitMs() {
  const value = Number(import.meta.env.VITE_STARTUP_MAX_WAIT_MS);
  return Number.isFinite(value) && value >= 10000 ? value : DEFAULT_MAX_WAIT_MS;
}

function delay(milliseconds) {
  return new Promise((resolve) => window.setTimeout(resolve, milliseconds));
}

export default function ApiStartupGate({ children }) {
  const healthUrl = useMemo(() => `${API_BASE_URL}${HEALTH_PATH}`, []);
  const [run, setRun] = useState(0);
  const [phase, setPhase] = useState('waking');
  const [progress, setProgress] = useState(0);
  const [attempt, setAttempt] = useState(1);
  const [open, setOpen] = useState(true);

  useEffect(() => {
    if (!open) return undefined;
    const timer = window.setInterval(() => {
      setProgress((current) => {
        if (phase === 'ready') return Math.min(100, current + 5);
        if (phase === 'unavailable') return current;
        if (current < 70) return current + 2;
        if (current < 90) return current + 1;
        if (current < 95) return current + 0.25;
        return 95;
      });
    }, phase === 'ready' ? 45 : 140);
    return () => window.clearInterval(timer);
  }, [open, phase]);

  useEffect(() => {
    if (phase !== 'ready' || progress < 100) return undefined;
    const timer = window.setTimeout(() => setOpen(false), 420);
    return () => window.clearTimeout(timer);
  }, [phase, progress]);

  useEffect(() => {
    let active = true;
    let retryTimer = 0;
    const startedAt = Date.now();
    const maxWaitMs = configuredWaitMs();

    setPhase('waking');
    setProgress(0);
    setAttempt(1);

    const probe = async (number) => {
      if (!active) return;
      setAttempt(number);
      const controller = new AbortController();
      const timeout = window.setTimeout(() => controller.abort(), 10000);
      try {
        const response = await fetch(healthUrl, {
          headers: { Accept: 'application/json' },
          credentials: 'omit',
          cache: 'no-store',
          signal: controller.signal,
        });
        const payload = response.ok ? await response.json() : null;
        if (!response.ok || payload?.status !== 'UP') throw new Error('API is not ready');
        const remainingMinimum = Math.max(0, MINIMUM_SPLASH_MS - (Date.now() - startedAt));
        if (remainingMinimum) await delay(remainingMinimum);
        if (active) setPhase('ready');
      } catch {
        if (!active) return;
        if (Date.now() - startedAt >= maxWaitMs) {
          setPhase('unavailable');
          return;
        }
        const wait = Math.min(5000, 700 * (2 ** Math.min(number - 1, 3)));
        retryTimer = window.setTimeout(() => probe(number + 1), wait);
      } finally {
        window.clearTimeout(timeout);
      }
    };

    probe(1);
    return () => {
      active = false;
      window.clearTimeout(retryTimer);
    };
  }, [healthUrl, run]);

  if (!open) return children;

  const roundedProgress = Math.floor(progress);
  const retry = () => { setRun((current) => current + 1); setPhase('waking'); };
  return <div className={`startup-splash startup-splash--${phase}`} role="status" aria-live="polite" aria-label="Community Store is starting">
    <div className="startup-splash__glow" aria-hidden="true" />
    <div className="startup-splash__content">
      <div className="startup-splash__mark" aria-hidden="true"><span /><span /><span /><span /></div>
      <p className="startup-splash__eyebrow">Community Store</p>
      <h1>{phase === 'unavailable' ? 'The store is taking longer than expected.' : phase === 'ready' ? 'Your community is ready.' : 'Opening your community store…'}</h1>
      <p className="startup-splash__message">{phase === 'unavailable' ? 'The API may still be waking up or its URL may be unavailable.' : phase === 'ready' ? 'Products, accounts and community services are connected.' : `Connecting securely to the marketplace API${attempt > 1 ? ` · attempt ${attempt}` : ''}`}</p>
      <div className="startup-splash__progress" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow={roundedProgress}>
        <span style={{ width: `${roundedProgress}%` }} />
      </div>
      <strong className="startup-splash__count">{roundedProgress}<small>%</small></strong>
      {phase === 'unavailable' && <button className="startup-splash__retry" type="button" onClick={retry}>Try connecting again</button>}
    </div>
  </div>;
}
