import { useEffect, useRef } from 'react';
import { useLocation } from 'react-router-dom';

const MAX_HASH_ATTEMPTS = 20;

function prefersReducedMotion() {
  return window.matchMedia?.('(prefers-reduced-motion: reduce)').matches ?? false;
}

function scrollToHash(hash) {
  let id;
  try {
    id = decodeURIComponent(hash.slice(1));
  } catch {
    id = hash.slice(1);
  }

  const target = id ? document.getElementById(id) : null;
  if (!target) return false;

  const headerHeight = document.querySelector('.site-header')?.getBoundingClientRect().height ?? 72;
  const top = Math.max(0, target.getBoundingClientRect().top + window.scrollY - headerHeight - 12);
  window.scrollTo({
    top,
    left: 0,
    behavior: prefersReducedMotion() ? 'auto' : 'smooth',
  });
  return true;
}

export default function RouteScrollManager() {
  const { pathname, hash } = useLocation();
  const previousPathname = useRef(null);

  useEffect(() => {
    const pathChanged = previousPathname.current !== pathname;
    previousPathname.current = pathname;
    const timers = [];

    if (!hash) {
      if (pathChanged) window.scrollTo({ top: 0, left: 0, behavior: 'auto' });
      return undefined;
    }

    let attempts = 0;
    const findTarget = () => {
      if (scrollToHash(hash) || attempts >= MAX_HASH_ATTEMPTS) return;
      attempts += 1;
      timers.push(window.setTimeout(findTarget, 50));
    };

    findTarget();
    return () => timers.forEach(window.clearTimeout);
  }, [pathname, hash]);

  return null;
}
