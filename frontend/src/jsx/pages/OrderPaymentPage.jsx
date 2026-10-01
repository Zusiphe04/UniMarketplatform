import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { apiRequest } from '../../js/api/client.js';
import { formatPrice } from '../../js/utils/formatters.js';
import Icon from '../components/Icon.jsx';
import SimulatedPaymentPanel from '../components/SimulatedPaymentPanel.jsx';

function readableStatus(value) {
  return value?.replaceAll('_', ' ').toLowerCase() || 'unknown';
}

export default function OrderPaymentPage() {
  const { orderId } = useParams();
  const [state, setState] = useState({ status: 'loading', order: null, error: '' });

  useEffect(() => {
    const controller = new AbortController();
    setState({ status: 'loading', order: null, error: '' });
    apiRequest(`/api/v1/orders/${orderId}`, { signal: controller.signal })
      .then((order) => setState({ status: 'ready', order, error: '' }))
      .catch((error) => {
        if (error.name !== 'AbortError') setState({ status: 'error', order: null, error: error.message });
      });
    return () => controller.abort();
  }, [orderId]);

  if (state.status === 'loading') return <main className="route-loading"><span className="route-loading__mark" /><p>Loading your order…</p></main>;
  if (state.status === 'error') return <main className="route-error"><h1>Order unavailable</h1><p>{state.error}</p><Link className="button button--dark" to="/buyer">Return to buyer dashboard</Link></main>;

  const { order } = state;
  const payable = ['PENDING_PAYMENT', 'PAYMENT_FAILED'].includes(order.status);
  return <main className="cart-page payment-page" id="main-content"><div className="container payment-page__container">
    <div className="page-title-row"><div><p className="eyebrow eyebrow--line">Order payment</p><h1>{order.reference}</h1><p>{formatPrice(order.totalAmount, order.currency)} · {order.itemCount} {order.itemCount === 1 ? 'item' : 'items'} · {readableStatus(order.status)}</p></div><Link className="round-link" to="/buyer">Buyer dashboard <span><Icon name="arrowRight" size={14} /></span></Link></div>
    {payable ? <SimulatedPaymentPanel order={order} onPaymentResult={(payment) => setState((current) => ({ ...current, order: { ...current.order, status: payment.currentOrderStatus, paymentFailureCode: payment.failureCode } }))} /> : <section className="payment-panel"><h2>No payment is due.</h2><p>Order {order.reference} is already {readableStatus(order.status)}.</p><Link className="button button--dark" to="/buyer">View all orders</Link></section>}
  </div></main>;
}
