const priceFormatters = new Map();

export function formatPrice(value, currency = 'ZAR') {
  const numericValue = Number(value);
  if (!Number.isFinite(numericValue)) return 'Price unavailable';

  const safeCurrency = currency || 'ZAR';
  if (!priceFormatters.has(safeCurrency)) {
    priceFormatters.set(
      safeCurrency,
      new Intl.NumberFormat('en-ZA', {
        style: 'currency',
        currency: safeCurrency,
        minimumFractionDigits: numericValue % 1 === 0 ? 0 : 2,
        maximumFractionDigits: 2,
      }),
    );
  }

  return priceFormatters.get(safeCurrency).format(numericValue);
}

export function formatCondition(condition) {
  if (!condition) return 'Pre-owned';
  return condition
    .toLocaleLowerCase('en-ZA')
    .split('_')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
}
