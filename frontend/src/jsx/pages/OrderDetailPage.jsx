import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { apiRequest } from '../../js/api/client.js';
import { formatPrice } from '../../js/utils/formatters.js';
import Icon from '../components/Icon.jsx';

function stateText(value) { return value ? value.replaceAll('_', ' ') : 'Not started'; }

export default function OrderDetailPage() {
  const { orderId } = useParams();
  const [state, setState] = useState({ status: 'loading', order: null, fulfillment: [], escrow: null, escrowError: '', error: '' });
  const [workingId, setWorkingId] = useState('');

  const load = useCallback(async (signal) => {
    setState((current) => ({ ...current, status: 'loading', error: '' }));
    try {
      const [order, fulfillment] = await Promise.all([
        apiRequest(`/api/v1/orders/${orderId}`, { signal }),
        apiRequest(`/api/v1/orders/${orderId}/fulfillment`, { signal }),
      ]);
      let escrow = null;
      let escrowError = '';
      if (order.status === 'HELD') {
        try {
          escrow = await apiRequest(`/api/v1/escrow/orders/${orderId}`, { signal });
        } catch (requestError) {
          if (requestError.name === 'AbortError') throw requestError;
          escrowError = requestError.message;
        }
      }
      setState({ status: 'ready', order, fulfillment, escrow, escrowError, error: '' });
    } catch (error) {
      if (error.name !== 'AbortError') setState({ status: 'error', order: null, fulfillment: [], escrow: null, escrowError: '', error: error.message });
    }
  }, [orderId]);

  useEffect(() => { const controller = new AbortController(); load(controller.signal); return () => controller.abort(); }, [load]);

  const confirmItem = async (itemId) => {
    setWorkingId(itemId);
    try { await apiRequest(`/api/v1/orders/${orderId}/items/${itemId}/confirm-receipt`, { method: 'POST' }); await load(); }
    catch (error) { setState((current) => ({ ...current, error: error.message })); }
    finally { setWorkingId(''); }
  };

  const confirmEscrow = async () => {
    setWorkingId('escrow');
    try { await apiRequest(`/api/v1/escrow/orders/${orderId}/confirm-receipt`, { method: 'POST' }); await load(); }
    catch (error) { setState((current) => ({ ...current, error: error.message })); }
    finally { setWorkingId(''); }
  };

  const disputeEscrow = async () => {
    const reason = window.prompt('Describe the delivery or item problem for the moderator.');
    if (!reason?.trim()) return;
    setWorkingId('escrow');
    try { await apiRequest(`/api/v1/escrow/orders/${orderId}/disputes`, { method: 'POST', body: { reason: reason.trim() } }); await load(); }
    catch (error) { setState((current) => ({ ...current, error: error.message })); }
    finally { setWorkingId(''); }
  };

  if (state.status === 'loading') return <main className="route-loading"><span className="route-loading__mark" /><p>Loading order activity…</p></main>;
  if (state.status === 'error' || !state.order) return <main className="route-error"><h1>Order unavailable</h1><p>{state.error}</p><Link className="button button--dark" to="/buyer">Return to dashboard</Link></main>;

  const fulfillmentByItem = Object.fromEntries(state.fulfillment.map((entry) => [entry.orderItemId, entry]));
  return <main className="order-detail-page"><div className="container"><Link className="back-link" to="/buyer"><Icon name="chevronLeft" size={14} /> Buyer dashboard</Link><section className="dashboard-panel"><div className="dashboard-panel__heading"><div><p className="eyebrow eyebrow--line">Order lifecycle</p><h1>{state.order.reference}</h1></div><strong>{formatPrice(state.order.totalAmount, state.order.currency)}</strong></div><p className="order-status-line">Payment status: <strong>{stateText(state.order.status)}</strong> · {state.order.fulfillmentMethod?.replaceAll('_', ' ')}</p>{state.error && <div className="form-alert form-alert--error" role="alert">{state.error}</div>}<div className="order-item-timeline">{state.order.items.map((item) => { const fulfillment = fulfillmentByItem[item.id]; const receivable = ['READY_FOR_COLLECTION', 'DISPATCHED'].includes(fulfillment?.status); return <article key={item.id}><div><strong>{item.productTitle}</strong><small>{item.quantity} × {formatPrice(item.unitPrice, item.currency)}</small><p>Fulfilment: <strong>{stateText(fulfillment?.status)}</strong></p>{fulfillment?.pickupInstructions && <p>Collection: {fulfillment.pickupInstructions}</p>}{fulfillment?.trackingReference && <p>Tracking: {fulfillment.trackingReference}</p>}</div><div>{receivable && <button className="button button--orange" disabled={workingId === item.id} type="button" onClick={() => confirmItem(item.id)}>{workingId === item.id ? 'Saving…' : 'Confirm receipt'}</button>}<strong>{formatPrice(item.subtotal, item.currency)}</strong></div></article>; })}</div></section>{state.escrowError && <div className="form-alert form-alert--error" role="alert">Escrow details could not be loaded: {state.escrowError} <button type="button" onClick={() => load()}>Retry</button></div>}{state.escrow && <section className="dashboard-panel"><div className="dashboard-panel__heading"><div><p className="eyebrow eyebrow--line">Simulated escrow</p><h2>{stateText(state.escrow.status)}</h2></div></div><p>{state.escrow.disputeReason || 'Funds are held until you confirm receipt or open a dispute.'}</p>{state.escrow.status === 'HELD' && <div className="event-card-actions"><button className="button button--dark" disabled={workingId === 'escrow'} type="button" onClick={confirmEscrow}>Release after receipt</button><button className="button button--orange" disabled={workingId === 'escrow'} type="button" onClick={disputeEscrow}>Open dispute</button></div>}</section>}</div></main>;
}
