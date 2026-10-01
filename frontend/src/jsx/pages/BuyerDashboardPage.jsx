import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { apiRequest } from '../../js/api/client.js';
import { formatPrice } from '../../js/utils/formatters.js';
import { useCart } from '../cart/CartContext.jsx';
import DashboardShell from '../components/DashboardShell.jsx';
import Icon from '../components/Icon.jsx';

function isPayable(order) {
  return ['PENDING_PAYMENT', 'PAYMENT_FAILED'].includes(order.status);
}

export default function BuyerDashboardPage() {
  const cartState = useCart();
  const [data, setData] = useState({ orders: [], unread: 0, engagement: null });
  const [status, setStatus] = useState('loading');
  const [error, setError] = useState('');

  const loadDashboard = useCallback(async (signal) => {
    setStatus('loading');
    setError('');
    const options = signal ? { signal } : {};
    try {
      const [orders, unread, engagement] = await Promise.all([
        apiRequest('/api/v1/orders', options),
        apiRequest('/api/v1/notifications/unread-count', options),
        apiRequest('/api/v1/engagement/me', options),
      ]);
      setData({ orders, unread: unread.unreadCount, engagement });
      setStatus('ready');
    } catch (requestError) {
      if (requestError.name === 'AbortError') return;
      setError(requestError.message);
      setStatus('error');
    }
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    loadDashboard(controller.signal);
    return () => controller.abort();
  }, [loadDashboard]);

  const retry = () => {
    loadDashboard();
    if (cartState.status === 'error') cartState.refresh().catch(() => {});
  };

  const markNotificationsRead = async () => {
    try {
      await apiRequest('/api/v1/notifications/read-all', { method: 'PATCH' });
      setData((current) => ({ ...current, unread: 0 }));
    } catch (requestError) {
      setError(requestError.message);
    }
  };

  const loading = status === 'loading';
  const cartLoading = ['idle', 'loading'].includes(cartState.status);
  const displayedError = error || cartState.loadError;
  const cart = cartState.cart;

  return <DashboardShell role="BUYER">
    {displayedError && <div className="form-alert form-alert--error" role="alert">{displayedError} <button type="button" onClick={retry}>Retry</button></div>}
    <section className="dashboard-metrics" aria-busy={loading || cartLoading}>
      <article><span><Icon name="bag" size={19} /></span><div><strong>{cartLoading ? '—' : cartState.itemCount}</strong><small>Items in cart</small></div></article>
      <article><span><Icon name="store" size={19} /></span><div><strong>{loading ? '—' : data.orders.length}</strong><small>Database orders</small></div></article>
      <article><span><Icon name="spark" size={19} /></span><div><strong>{loading ? '—' : data.engagement?.buyerPoints ?? 0}</strong><small>Buyer points</small></div></article>
      <article><span><Icon name="user" size={19} /></span><div><strong>{loading ? '—' : data.unread}</strong><small>Unread notices</small></div>{!loading && data.unread > 0 && <button type="button" onClick={markNotificationsRead}>Mark read</button>}</article>
    </section>
    <div className="dashboard-grid">
      <section className="dashboard-panel"><div className="dashboard-panel__heading"><div><p className="eyebrow eyebrow--line">Persistent cart</p><h2>Your current basket</h2></div><Link to="/cart">Manage cart <Icon name="arrowRight" size={13} /></Link></div>
        {cartLoading ? <div className="panel-empty"><p>Loading your persisted cart…</p></div> : cart?.items?.length ? <div className="compact-list">{cart.items.slice(0, 4).map((item) => <article key={item.id}><img src={item.primaryImageUrl || '/images/brand/unimarket-logo.png'} alt="" /><div><strong>{item.title}</strong><small>{item.quantity} × {formatPrice(item.unitPrice, item.currency)}</small></div><span>{formatPrice(item.subtotal, item.currency)}</span></article>)}</div> : <div className="panel-empty"><p>Your database-backed cart is empty.</p><Link className="button button--dark" to="/marketplace">Browse listings</Link></div>}
      </section>
      <section className="dashboard-panel"><div className="dashboard-panel__heading"><div><p className="eyebrow eyebrow--line">Order activity</p><h2>Recent orders</h2></div></div>
        {loading ? <div className="panel-empty"><p>Loading your order activity…</p></div> : data.orders.length ? <div className="compact-list compact-list--orders">{data.orders.slice(0, 5).map((order) => <article key={order.id}><div><strong><Link to={`/orders/${order.id}`}>{order.reference}</Link></strong><small>{order.itemCount} items · {order.status?.replaceAll('_', ' ')}</small>{isPayable(order) && <Link className="order-payment-link" to={`/orders/${order.id}/payment`}>Complete payment <Icon name="arrowRight" size={12} /></Link>}</div><span>{formatPrice(order.totalAmount, order.currency)}</span></article>)}</div> : <div className="panel-empty"><p>Completed checkout activity will appear here.</p></div>}
      </section>
    </div>
    <section className="dashboard-actions"><Link to="/marketplace"><Icon name="search" /><span><strong>Discover products</strong></span></Link><Link to="/cart"><Icon name="bag" /><span><strong>Open persistent cart</strong></span></Link><Link to="/vendor-status"><Icon name="store" /><span><strong>Vendor application</strong></span></Link></section>
  </DashboardShell>;
}
