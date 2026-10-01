import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { ApiError, apiRequest } from '../../js/api/client.js';
import { useAuth } from '../auth/AuthContext.jsx';

const CartContext = createContext(null);

function emptyCart(currency = 'ZAR') {
  return { items: [], itemCount: 0, estimatedTotal: 0, currency };
}

function hasRole(account, role) {
  return Array.isArray(account?.roles) && account.roles.includes(role);
}

function createIdempotencyKey() {
  if (globalThis.crypto?.randomUUID) return globalThis.crypto.randomUUID();
  return `unimarket-checkout-${Date.now()}-${Math.random().toString(36).slice(2)}`;
}

function checkoutSignature(details, cart) {
  const items = (cart?.items || [])
    .map((item) => ({ productId: String(item.productId), quantity: Number(item.quantity) }))
    .sort((left, right) => left.productId.localeCompare(right.productId));
  return JSON.stringify({
    fulfillmentMethod: details.fulfillmentMethod,
    deliveryAddress: details.deliveryAddress?.trim() || null,
    items,
  });
}

export function CartProvider({ children }) {
  const auth = useAuth();
  const canPurchase = auth.isAuthenticated && hasRole(auth.account, 'BUYER') && !hasRole(auth.account, 'ADMIN');
  const roleKey = Array.isArray(auth.account?.roles) ? [...auth.account.roles].sort().join(',') : '';
  const identityKey = canPurchase ? `${auth.account?.id || 'buyer'}:${roleKey}` : '';
  const [cartState, setCartState] = useState(null);
  const [status, setStatus] = useState('idle');
  const [loadError, setLoadError] = useState('');
  const [isMutating, setIsMutating] = useState(false);
  const cartRef = useRef(null);
  const eligibleRef = useRef(canPurchase);
  const generationRef = useRef(0);
  const queueRef = useRef(Promise.resolve());
  const pendingMutationsRef = useRef(0);
  const checkoutAttemptRef = useRef(null);

  eligibleRef.current = canPurchase;

  const commitCart = useCallback((nextCart) => {
    cartRef.current = nextCart;
    setCartState(nextCart);
  }, []);

  useEffect(() => {
    const generation = ++generationRef.current;
    const controller = new AbortController();
    queueRef.current = Promise.resolve();
    pendingMutationsRef.current = 0;
    checkoutAttemptRef.current = null;
    setIsMutating(false);
    setLoadError('');

    if (!canPurchase) {
      commitCart(null);
      setStatus('idle');
      return () => controller.abort();
    }

    setStatus('loading');
    const initialLoad = apiRequest('/api/v1/cart', { signal: controller.signal })
      .then((response) => {
        if (generation !== generationRef.current) return response;
        commitCart(response);
        setStatus('ready');
        return response;
      })
      .catch((error) => {
        if (error.name === 'AbortError' || generation !== generationRef.current) return null;
        setLoadError(error.message);
        setStatus('error');
        return null;
      });
    queueRef.current = initialLoad.catch(() => null);

    return () => controller.abort();
  }, [canPurchase, commitCart, identityKey]);

  const enqueue = useCallback((work, { mutating = false } = {}) => {
    if (!eligibleRef.current) {
      return Promise.reject(new ApiError('Administrator accounts cannot use buyer commerce.', 403));
    }

    const generation = generationRef.current;
    if (mutating) {
      pendingMutationsRef.current += 1;
      setIsMutating(true);
    }

    const task = queueRef.current
      .catch(() => null)
      .then(() => {
        if (!eligibleRef.current || generation !== generationRef.current) {
          throw new ApiError('Your account is no longer eligible to use buyer commerce.', 403);
        }
        return work(generation);
      });

    queueRef.current = task.catch(() => null);
    return task.finally(() => {
      if (!mutating || generation !== generationRef.current) return;
      pendingMutationsRef.current = Math.max(0, pendingMutationsRef.current - 1);
      setIsMutating(pendingMutationsRef.current > 0);
    });
  }, []);

  const refresh = useCallback(() => {
    setLoadError('');
    setStatus('loading');
    return enqueue(async (generation) => {
      try {
        const response = await apiRequest('/api/v1/cart');
        if (generation === generationRef.current) {
          commitCart(response);
          setStatus('ready');
        }
        return response;
      } catch (error) {
        if (generation === generationRef.current) {
          setLoadError(error.message);
          setStatus('error');
        }
        throw error;
      }
    });
  }, [commitCart, enqueue]);

  const addItem = useCallback((productId, quantity) => enqueue(async (generation) => {
    const response = await apiRequest('/api/v1/cart/items', {
      method: 'POST',
      body: { productId, quantity },
    });
    if (generation === generationRef.current) commitCart(response);
    return response;
  }, { mutating: true }), [commitCart, enqueue]);

  const updateQuantity = useCallback((productId, quantity) => enqueue(async (generation) => {
    const response = await apiRequest(`/api/v1/cart/items/${productId}`, {
      method: 'PUT',
      body: { quantity },
    });
    if (generation === generationRef.current) commitCart(response);
    return response;
  }, { mutating: true }), [commitCart, enqueue]);

  const removeItem = useCallback((productId) => enqueue(async (generation) => {
    const response = await apiRequest(`/api/v1/cart/items/${productId}`, { method: 'DELETE' });
    if (generation === generationRef.current) commitCart(response);
    return response;
  }, { mutating: true }), [commitCart, enqueue]);

  const checkout = useCallback((details) => enqueue(async (generation) => {
    const signature = checkoutSignature(details, cartRef.current);
    if (checkoutAttemptRef.current?.signature !== signature) {
      checkoutAttemptRef.current = { signature, key: createIdempotencyKey() };
    }
    const order = await apiRequest('/api/v1/checkout', {
      method: 'POST',
      headers: { 'Idempotency-Key': checkoutAttemptRef.current.key },
      body: details,
    });
    checkoutAttemptRef.current = null;
    if (generation === generationRef.current) {
      commitCart(emptyCart(order.currency || cartRef.current?.currency || 'ZAR'));
    }
    return order;
  }, { mutating: true }), [commitCart, enqueue]);

  const value = useMemo(() => ({
    cart: canPurchase ? cartState : null,
    itemCount: canPurchase ? Number(cartState?.itemCount) || 0 : 0,
    status: canPurchase ? status : 'idle',
    loadError: canPurchase ? loadError : '',
    isMutating: canPurchase && isMutating,
    canPurchase,
    refresh,
    addItem,
    updateQuantity,
    removeItem,
    checkout,
  }), [addItem, canPurchase, cartState, checkout, isMutating, loadError, refresh, removeItem, status, updateQuantity]);

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCart() {
  const context = useContext(CartContext);
  if (!context) throw new Error('useCart must be used inside CartProvider.');
  return context;
}
