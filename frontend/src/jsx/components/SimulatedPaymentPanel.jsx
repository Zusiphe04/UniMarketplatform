import { useEffect, useMemo, useRef, useState } from 'react';
import { fetchPaymentOptions, simulatePayment } from '../../js/api/payments.js';
import { formatPrice } from '../../js/utils/formatters.js';
import Icon from './Icon.jsx';

const SCENARIOS = [
  { value: 'SUCCESS', label: 'Successful payment', description: 'Completes the academic payment simulation.' },
  { value: 'DECLINED', label: 'Declined payment', description: 'Demonstrates a payment-provider decline.' },
  { value: 'INSUFFICIENT_FUNDS', label: 'Insufficient funds', description: 'Demonstrates an insufficient-funds response.' },
  { value: 'TIMEOUT', label: 'Provider timeout', description: 'Demonstrates a payment-provider timeout.' },
];

function createIdempotencyKey() {
  if (globalThis.crypto?.randomUUID) return globalThis.crypto.randomUUID();
  return `unimarket-${Date.now()}-${Math.random().toString(36).slice(2)}`;
}

function readableStatus(value) {
  return value?.replaceAll('_', ' ').toLowerCase() || 'unknown';
}

export default function SimulatedPaymentPanel({ order, onPaymentResult }) {
  const [optionsState, setOptionsState] = useState({ status: 'loading', data: [], error: '' });
  const [reloadKey, setReloadKey] = useState(0);
  const [paymentOption, setPaymentOption] = useState('');
  const [scenario, setScenario] = useState('SUCCESS');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [result, setResult] = useState(null);
  const idempotencyKeys = useRef(new Map());

  useEffect(() => {
    const controller = new AbortController();
    setOptionsState({ status: 'loading', data: [], error: '' });
    fetchPaymentOptions({ signal: controller.signal })
      .then((data) => {
        setOptionsState({ status: 'ready', data, error: '' });
        setPaymentOption((current) => current || data[0].code);
      })
      .catch((requestError) => {
        if (requestError.name !== 'AbortError') {
          setOptionsState({ status: 'error', data: [], error: requestError.message });
        }
      });
    return () => controller.abort();
  }, [reloadKey]);

  useEffect(() => {
    setResult(null);
    setError('');
    idempotencyKeys.current.clear();
  }, [order.id]);

  const selectedOption = useMemo(
    () => optionsState.data.find((option) => option.code === paymentOption),
    [optionsState.data, paymentOption],
  );
  const payable = ['PENDING_PAYMENT', 'PAYMENT_FAILED'].includes(result?.currentOrderStatus || order.status);

  const submit = async (event) => {
    event.preventDefault();
    if (!paymentOption || !payable) return;

    const signature = `${order.id}:${paymentOption}:${scenario}`;
    const idempotencyKey = idempotencyKeys.current.get(signature) || createIdempotencyKey();
    idempotencyKeys.current.set(signature, idempotencyKey);
    setSubmitting(true);
    setError('');

    try {
      const payment = await simulatePayment({ orderId: order.id, paymentOption, scenario, idempotencyKey });
      setResult(payment);
      onPaymentResult?.(payment);
      if (payment.status !== 'SUCCEEDED') idempotencyKeys.current.delete(signature);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setSubmitting(false);
    }
  };

  if (optionsState.status === 'loading') {
    return <section className="payment-panel payment-panel--loading" aria-live="polite"><span className="route-loading__mark" /><p>Loading safe payment options…</p></section>;
  }

  if (optionsState.status === 'error') {
    return <section className="payment-panel" role="alert"><h2>Payment options are unavailable.</h2><p>{optionsState.error}</p><button className="button button--dark" type="button" onClick={() => setReloadKey((key) => key + 1)}>Try again</button></section>;
  }

  const successful = result?.status === 'SUCCEEDED';
  return (
    <section className="payment-panel" aria-labelledby={`payment-title-${order.id}`}>
      <div className="payment-panel__heading">
        <div>
          <p className="eyebrow eyebrow--line">Academic simulation</p>
          <h2 id={`payment-title-${order.id}`}>Pay for order {order.reference}</h2>
        </div>
        <strong>{formatPrice(order.totalAmount, order.currency)}</strong>
      </div>
      <div className="payment-safety-note"><Icon name="shield" size={20} /><p><strong>No real money or credentials</strong><span>UniMarket never asks for a card number, CVV, bank password, or OTP. Choose an outcome to exercise the persisted payment workflow.</span></p></div>

      {result && <div className={`payment-result ${successful ? 'payment-result--success' : 'payment-result--failure'}`} role="status">
        <Icon name={successful ? 'check' : 'shield'} size={20} />
        <div>
          <strong>{successful ? 'Simulated payment completed.' : 'Simulated payment was not completed.'}</strong>
          <p>Payment {result.reference} · order is now {readableStatus(result.currentOrderStatus)}{result.failureCode ? ` · ${readableStatus(result.failureCode)}` : ''}.</p>
          {result.currentOrderStatus === 'HELD' && <p>The simulated funds are held in academic escrow until receipt is confirmed.</p>}
        </div>
      </div>}

      {payable ? <form className="platform-form payment-form" onSubmit={submit}>
        <label>Payment option
          <select value={paymentOption} onChange={(event) => { setPaymentOption(event.target.value); setResult(null); }}>
            {optionsState.data.map((option) => <option value={option.code} key={option.code}>{option.displayName}</option>)}
          </select>
          {selectedOption && <small className="form-field-note">{selectedOption.description}</small>}
        </label>
        <label>Simulation outcome
          <select value={scenario} onChange={(event) => { setScenario(event.target.value); setResult(null); }}>
            {SCENARIOS.map((item) => <option value={item.value} key={item.value}>{item.label}</option>)}
          </select>
          <small className="form-field-note">{SCENARIOS.find((item) => item.value === scenario)?.description}</small>
        </label>
        {error && <div className="form-alert form-alert--error" role="alert">{error}</div>}
        <button className="button button--orange form-submit" disabled={submitting || !paymentOption} type="submit">
          {submitting ? 'Running simulation…' : result ? 'Run a new payment attempt' : 'Simulate payment'} <Icon name="arrowRight" size={14} />
        </button>
      </form> : <div className="payment-panel__complete"><p>This order no longer requires payment. Its current status is <strong>{readableStatus(result?.currentOrderStatus || order.status)}</strong>.</p></div>}
    </section>
  );
}
