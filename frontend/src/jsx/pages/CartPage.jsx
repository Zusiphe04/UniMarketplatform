import { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { formatPrice } from '../../js/utils/formatters.js';
import { useCart } from '../cart/CartContext.jsx';
import Icon from '../components/Icon.jsx';
import SimulatedPaymentPanel from '../components/SimulatedPaymentPanel.jsx';

function clampedQuantity(item, value) {
  const maximum = Math.max(1, Math.min(99, Number(item.availableQuantity) || 1));
  const parsed = Number.parseInt(value, 10);
  return Math.max(1, Math.min(maximum, Number.isFinite(parsed) ? parsed : 1));
}

export default function CartPage() {
  const location = useLocation();
  const cartState = useCart();
  const [actionError, setActionError] = useState('');
  const [workingId, setWorkingId] = useState('');
  const [checkoutDetails, setCheckoutDetails] = useState({ fulfillmentMethod: 'CAMPUS_PICKUP', deliveryAddress: '' });
  const [order, setOrder] = useState(null);

  const updateQuantity = async (item, quantity) => {
    setWorkingId(item.productId);
    setActionError('');
    try {
      await cartState.updateQuantity(item.productId, clampedQuantity(item, quantity));
    } catch (error) {
      setActionError(error.message);
    } finally {
      setWorkingId('');
    }
  };

  const removeItem = async (item) => {
    setWorkingId(item.productId);
    setActionError('');
    try {
      await cartState.removeItem(item.productId);
    } catch (error) {
      setActionError(error.message);
    } finally {
      setWorkingId('');
    }
  };

  const submitCheckout = async (event) => {
    event.preventDefault();
    setWorkingId('checkout');
    setActionError('');
    try {
      const created = await cartState.checkout({
        fulfillmentMethod: checkoutDetails.fulfillmentMethod,
        deliveryAddress: checkoutDetails.fulfillmentMethod === 'LOCAL_DELIVERY' ? checkoutDetails.deliveryAddress.trim() : null,
      });
      setOrder(created);
      window.requestAnimationFrame(() => document.getElementById('simulated-payment')?.scrollIntoView({ behavior: 'smooth', block: 'start' }));
    } catch (error) {
      setActionError(error.message);
    } finally {
      setWorkingId('');
    }
  };

  if ((cartState.status === 'loading' || cartState.status === 'idle') && !cartState.cart) {
    return <main id="main-content" className="route-loading"><span className="route-loading__mark" /><p>Loading your persisted cart…</p></main>;
  }

  if (cartState.status === 'error' && !cartState.cart) {
    return <main id="main-content" className="route-error"><h1>Your cart could not be loaded.</h1><p>{cartState.loadError}</p><button className="button button--dark" type="button" onClick={() => cartState.refresh().catch(() => {})}>Try again</button></main>;
  }

  const cart = cartState.cart;
  const error = actionError || cartState.loadError;
  return <main id="main-content" className="cart-page"><div className="container">
    <div className="page-title-row"><div><p className="eyebrow eyebrow--line">Buyer workspace</p><h1>Your cart</h1><p>Review your items and quantities before checkout.</p></div><Link className="round-link" to="/marketplace">Continue shopping <span><Icon name="arrowRight" size={14} /></span></Link></div>
    {location.state?.added && !order && <div className="form-alert form-alert--success">{location.state.added} was added to your cart.</div>}
    {error && <div className="form-alert form-alert--error" role="alert">{error}</div>}

    {order && <div className="checkout-payment-flow" id="simulated-payment">
      <div className="order-confirmation"><span><Icon name="check" size={20} /></span><div><strong>Order {order.reference} created</strong><p>{formatPrice(order.totalAmount, order.currency)} · {order.status?.replaceAll('_', ' ').toLowerCase()}</p></div><Link to="/buyer">View all orders</Link></div>
      <SimulatedPaymentPanel order={order} onPaymentResult={(payment) => setOrder((current) => ({ ...current, status: payment.currentOrderStatus, paymentFailureCode: payment.failureCode }))} />
    </div>}

    {cart?.items?.length ? <div className="cart-layout"><section className="cart-items" aria-label="Cart items">{cart.items.map((item) => {
      const maximum = Math.max(1, Math.min(99, Number(item.availableQuantity) || 1));
      const busy = workingId === item.productId || cartState.isMutating;
      return <article key={item.id}><img src={item.primaryImageUrl || '/images/brand/unimarket-logo.png'} alt={item.title} loading="lazy" decoding="async" /><div className="cart-item-copy"><strong>{item.title}</strong><small>{formatPrice(item.unitPrice, item.currency)} each · {item.availableQuantity} available</small><div><button type="button" aria-label={`Decrease ${item.title} quantity`} disabled={item.quantity <= 1 || busy} onClick={() => updateQuantity(item, item.quantity - 1)}>−</button><span aria-label={`${item.quantity} selected`}>{item.quantity}</span><button type="button" aria-label={`Increase ${item.title} quantity`} disabled={item.quantity >= maximum || busy} onClick={() => updateQuantity(item, item.quantity + 1)}>+</button></div></div><div className="cart-item-total"><strong>{formatPrice(item.subtotal, item.currency)}</strong><button type="button" disabled={busy} onClick={() => removeItem(item)}>Remove</button></div></article>;
    })}</section>
      <aside className="cart-summary"><p className="eyebrow eyebrow--line">Checkout</p><h2>Order summary</h2><dl><div><dt>Items</dt><dd>{cart.itemCount}</dd></div><div><dt>Estimated total</dt><dd>{formatPrice(cart.estimatedTotal, cart.currency)}</dd></div></dl><form className="platform-form" onSubmit={submitCheckout}><label>Fulfilment<select value={checkoutDetails.fulfillmentMethod} onChange={(event) => setCheckoutDetails((current) => ({ ...current, fulfillmentMethod: event.target.value }))}><option value="CAMPUS_PICKUP">Campus pickup</option><option value="LOCAL_DELIVERY">Local delivery</option><option value="SELLER_ARRANGEMENT">Seller arrangement</option></select></label>{checkoutDetails.fulfillmentMethod === 'LOCAL_DELIVERY' && <label>Delivery address<textarea required maxLength="500" rows="3" value={checkoutDetails.deliveryAddress} onChange={(event) => setCheckoutDetails((current) => ({ ...current, deliveryAddress: event.target.value }))} /></label>}<button className="button button--orange form-submit" disabled={workingId === 'checkout' || cartState.isMutating} type="submit">{workingId === 'checkout' ? 'Creating order…' : 'Continue to Simulated Payment'} <Icon name="arrowRight" size={14} /></button></form><small>Checkout creates a persisted order, clears this cart, and then opens a credential-free academic payment simulation.</small></aside></div> : !order && <div className="empty-cart"><Icon name="bag" size={28} /><h2>Your cart is empty</h2><p>Open a live product and add it to begin the checkout and simulated-payment flow.</p><Link className="button button--dark" to="/marketplace">Browse marketplace</Link></div>}
  </div></main>;
}
