import { useCallback, useEffect, useState } from 'react';
import { apiRequest } from '../../js/api/client.js';
import { CATEGORIES, CATEGORY_BY_VALUE } from '../../js/data/categories.js';
import { formatPrice } from '../../js/utils/formatters.js';
import DashboardShell from '../components/DashboardShell.jsx';
import Icon from '../components/Icon.jsx';

const SPECIFICATION_NAMES = ['brand', 'model', 'storage', 'memory', 'processor', 'screenSize', 'color', 'size'];
const PHYSICAL_CONDITIONS = ['NEW', 'LIKE_NEW', 'GOOD', 'FAIR'];
const MAX_IMAGE_BYTES = 5 * 1024 * 1024;
const INITIAL_PRODUCT = { title: '', description: '', category: 'BOOKS', condition: 'GOOD', price: '', quantity: 1, brand: '', model: '', storage: '', memory: '', processor: '', screenSize: '', color: '', size: '', altText: '', publishNow: true };

function productForm(product) {
  return { ...INITIAL_PRODUCT, ...Object.fromEntries(['title', 'description', 'category', 'condition', 'brand', 'model', 'storage', 'memory', 'processor', 'screenSize', 'color', 'size'].map((name) => [name, product[name] || ''])), price: String(product.price ?? ''), quantity: product.stockQuantity ?? product.quantity ?? 0, publishNow: false };
}

export default function SellerDashboardPage() {
  const [orders, setOrders] = useState([]); const [engagement, setEngagement] = useState(null); const [products, setProducts] = useState([]);
  const [form, setForm] = useState(INITIAL_PRODUCT); const [editingProduct, setEditingProduct] = useState(null);
  const [productImage, setProductImage] = useState(null); const [imagePreview, setImagePreview] = useState(''); const [imageInputKey, setImageInputKey] = useState(0);
  const [result, setResult] = useState(null); const [error, setError] = useState(''); const [submitting, setSubmitting] = useState(false);
  const selectedCategory = CATEGORY_BY_VALUE[form.category] || CATEGORY_BY_VALUE.OTHER;
  const loadDashboard = useCallback(async (signal) => {
    const options = signal ? { signal } : {};
    try {
      const [sellerOrders, profile, productPage] = await Promise.all([
        apiRequest('/api/v1/seller/orders', options),
        apiRequest('/api/v1/engagement/me', options),
        apiRequest('/api/v1/seller/products?page=0&size=100', options),
      ]);
      setOrders(sellerOrders); setEngagement(profile); setProducts(productPage.content || []);
    } catch (requestError) { if (requestError.name !== 'AbortError') setError(requestError.message); }
  }, []);
  useEffect(() => { const controller = new AbortController(); loadDashboard(controller.signal); return () => controller.abort(); }, [loadDashboard]);
  useEffect(() => () => { if (imagePreview) URL.revokeObjectURL(imagePreview); }, [imagePreview]);

  const update = (event) => { const { name, value, type, checked } = event.target; setForm((current) => { if (name !== 'category') return { ...current, [name]: type === 'checkbox' ? checked : value }; const nextCategory = CATEGORY_BY_VALUE[value]; const allowedFields = new Set(nextCategory.fields.map((field) => field.name)); const next = { ...current, category: value, condition: value === 'SERVICE' ? 'NOT_APPLICABLE' : current.condition === 'NOT_APPLICABLE' ? 'GOOD' : current.condition }; SPECIFICATION_NAMES.forEach((fieldName) => { if (!allowedFields.has(fieldName)) next[fieldName] = ''; }); return next; }); };
  const chooseImage = (event) => { const file = event.target.files?.[0] || null; setError(''); if (file && (!file.type.startsWith('image/') || file.size > MAX_IMAGE_BYTES)) { event.target.value = ''; setProductImage(null); setError('Choose a PNG, JPEG, GIF or WebP product image no larger than 5 MB.'); return; } if (imagePreview) URL.revokeObjectURL(imagePreview); setProductImage(file); setImagePreview(file ? URL.createObjectURL(file) : ''); };
  const attachImage = async (product) => {
    if (!productImage) return;
    const existingImages = editingProduct?.id === product.id ? editingProduct.images || [] : [];
    if (existingImages.length >= 8) throw new Error('A product may have at most 8 images. Remove an image before adding another.');
    const usedOrders = new Set(existingImages.map((image) => image.displayOrder));
    let displayOrder = 0;
    while (usedOrders.has(displayOrder)) displayOrder += 1;
    const upload = new FormData(); upload.append('image', productImage);
    const stored = await apiRequest('/api/v1/seller/products/images/upload', { method: 'POST', body: upload });
    await apiRequest(`/api/v1/seller/products/${product.id}/images`, { method: 'POST', body: { imageUrl: stored.imageUrl, altText: form.altText.trim() || form.title, displayOrder, primary: existingImages.length === 0 } });
  };
  const payload = () => { const allowedFields = new Set(selectedCategory.fields.map((field) => field.name)); const specifications = Object.fromEntries(SPECIFICATION_NAMES.map((name) => [name, allowedFields.has(name) ? form[name].trim() || null : null])); return { title: form.title, description: form.description, category: form.category, condition: form.condition, price: Number(form.price), quantity: Number(form.quantity), ...specifications }; };
  const resetEditor = () => { if (imagePreview) URL.revokeObjectURL(imagePreview); setEditingProduct(null); setForm(INITIAL_PRODUCT); setProductImage(null); setImagePreview(''); setImageInputKey((key) => key + 1); };
  const editListing = (product) => { if (imagePreview) URL.revokeObjectURL(imagePreview); setError(''); setResult(null); setProductImage(null); setImagePreview(''); setImageInputKey((key) => key + 1); setEditingProduct(product); setForm(productForm(product)); window.requestAnimationFrame(() => document.getElementById('product-editor')?.scrollIntoView({ behavior: 'smooth', block: 'start' })); };
  const saveListing = async (event) => {
    event.preventDefault(); setSubmitting(true); setError(''); setResult(null);
    let product = null;
    try {
      if (editingProduct) {
        product = await apiRequest(`/api/v1/seller/products/${editingProduct.id}`, { method: 'PUT', body: payload() });
        await attachImage(product);
        window.dispatchEvent(new CustomEvent('unimarket:notifications-changed'));
      } else {
        product = await apiRequest('/api/v1/seller/products', { method: 'POST', body: payload() });
        await attachImage(product);
        if (form.publishNow) product = await apiRequest(`/api/v1/seller/products/${product.id}/publish`, { method: 'POST' });
      }
      setResult(product); resetEditor(); await loadDashboard();
    } catch (requestError) {
      if (product) {
        setEditingProduct(product);
        setForm(productForm(product));
        setError(`Product details were saved, but a later step failed: ${requestError.message}. Continue editing this saved listing instead of creating another.`);
        await loadDashboard();
      } else {
        setError(requestError.message);
      }
    } finally { setSubmitting(false); }
  };
  const changeListingStatus = async (product, action) => {
    setSubmitting(true); setError(''); setResult(null);
    try { const updated = await apiRequest(`/api/v1/seller/products/${product.id}/${action}`, { method: 'POST' }); setResult(updated); await loadDashboard(); window.dispatchEvent(new CustomEvent('unimarket:notifications-changed')); }
    catch (requestError) { setError(requestError.message); } finally { setSubmitting(false); }
  };
  const removeImage = async (imageId) => {
    if (!editingProduct) return;
    setSubmitting(true); setError('');
    try {
      await apiRequest(`/api/v1/seller/products/${editingProduct.id}/images/${imageId}`, { method: 'DELETE' });
      const images = (editingProduct.images || []).filter((image) => image.id !== imageId);
      setEditingProduct((current) => ({ ...current, images }));
      setProducts((current) => current.map((product) => product.id === editingProduct.id ? { ...product, images } : product));
      window.dispatchEvent(new CustomEvent('unimarket:notifications-changed'));
    } catch (requestError) { setError(requestError.message); } finally { setSubmitting(false); }
  };
  const updateFulfillment = async (orderId, itemId, status) => { const details = status === 'READY_FOR_COLLECTION' ? window.prompt('Enter collection instructions for the buyer.') : status === 'DISPATCHED' ? window.prompt('Enter a delivery tracking reference.') : ''; if ((status === 'READY_FOR_COLLECTION' || status === 'DISPATCHED') && !details?.trim()) return; setSubmitting(true); setError(''); try { await apiRequest(`/api/v1/seller/orders/${orderId}/items/${itemId}/fulfillment`, { method: 'PATCH', body: { status, pickupInstructions: status === 'READY_FOR_COLLECTION' ? details.trim() : null, trackingReference: status === 'DISPATCHED' ? details.trim() : null } }); await loadDashboard(); } catch (requestError) { setError(requestError.message); } finally { setSubmitting(false); } };
  const fulfillmentCount = orders.flatMap((order) => order.items || []).filter((item) => !['RECEIVED', 'CANCELLED'].includes(item.fulfillmentStatus)).length;

  return <DashboardShell role="SELLER"><section className="dashboard-metrics dashboard-metrics--compact"><article><span><Icon name="bag" size={19} /></span><div><strong>{orders.length}</strong><small>Customer orders</small></div></article><article><span><Icon name="store" size={19} /></span><div><strong>{fulfillmentCount}</strong><small>Items to fulfil</small></div></article><article><span><Icon name="spark" size={19} /></span><div><strong>{engagement?.sellerPoints ?? 0}</strong><small>Vendor points</small></div></article></section>
    {error && <div className="form-alert form-alert--error" role="alert">{error}</div>}{result && <div className="form-alert form-alert--success" role="status"><strong>{result.title}</strong> was saved and is now {result.status?.replaceAll('_', ' ').toLowerCase()}.</div>}
    <div className="form-alert form-alert--success" role="status"><strong>You are a verified vendor.</strong> Manage your listings and customer orders from this seller workspace.</div>
    <div className="dashboard-grid dashboard-grid--seller"><section className="dashboard-panel" id="product-editor"><div className="dashboard-panel__heading"><div><p className="eyebrow eyebrow--line">Product catalogue</p><h2>{editingProduct ? 'Edit product' : 'Create a product'}</h2></div>{editingProduct && <button className="button button--dark" disabled={submitting} type="button" onClick={resetEditor}>Cancel edit</button>}</div>
      <div className="compact-list compact-list--orders">{products.length ? products.map((product) => <article key={product.id}><div><strong>{product.title}</strong><small>{product.status?.replaceAll('_', ' ')} · {product.quantity} available{product.reservedQuantity ? ` · ${product.reservedQuantity} reserved` : ''}</small></div><div><strong>{formatPrice(product.price, product.currency)}</strong><button type="button" disabled={submitting || product.status === 'ARCHIVED'} onClick={() => editListing(product)}>Edit</button>{product.status === 'DRAFT' && <button type="button" disabled={submitting} onClick={() => changeListingStatus(product, 'publish')}>Publish</button>}{product.status !== 'ARCHIVED' && <button type="button" disabled={submitting} onClick={() => changeListingStatus(product, 'archive')}>Archive</button>}</div></article>) : <div className="panel-empty"><p>Your products will appear here after you save them.</p></div>}</div>
      <form className="platform-form platform-form--grid" onSubmit={saveListing}><label className="form-span-2">Title<input required maxLength="160" name="title" value={form.title} onChange={update} /></label><label className="form-span-2">Description<textarea required maxLength="4000" name="description" rows="4" value={form.description} onChange={update} /></label><label>Category<select required name="category" value={form.category} onChange={update}>{CATEGORIES.map((category) => <option key={category.value} value={category.value}>{category.label}</option>)}</select><span className="form-field-note">{selectedCategory.description}</span></label><label>Condition<select disabled={form.category === 'SERVICE'} name="condition" value={form.condition} onChange={update}>{form.category === 'SERVICE' ? <option value="NOT_APPLICABLE">Not applicable</option> : PHYSICAL_CONDITIONS.map((condition) => <option key={condition} value={condition}>{condition.replaceAll('_', ' ')}</option>)}</select></label>
      {selectedCategory.fields.map((field) => <label key={field.name}>{field.label} <span>(optional)</span><input maxLength={field.maxLength} name={field.name} placeholder={field.placeholder} value={form[field.name]} onChange={update} /></label>)}<label>Price (ZAR)<input required min="0.01" step="0.01" name="price" type="number" value={form.price} onChange={update} /></label><label>Quantity<input required min={editingProduct ? '0' : '1'} max="9999" name="quantity" type="number" value={form.quantity} onChange={update} /></label>
      {editingProduct?.images?.length > 0 && <div className="form-span-2 compact-list">{editingProduct.images.map((image) => <article key={image.id}><img src={image.imageUrl} alt={image.altText || editingProduct.title} /><div><strong>{image.primary ? 'Primary image' : 'Product image'}</strong><small>{image.altText || 'No image description'}</small></div><button type="button" disabled={submitting} onClick={() => removeImage(image.id)}>Remove</button></article>)}</div>}
      <label className="form-span-2 image-upload-field">{editingProduct ? 'Add another product image' : 'Product image'} <span>(PNG, JPEG, GIF or WebP · max 5 MB)</span><input key={imageInputKey} accept="image/png,image/jpeg,image/gif,image/webp" type="file" onChange={chooseImage} />{imagePreview && <span className="image-upload-preview"><img src={imagePreview} alt="Selected product preview" /><small>{productImage?.name}</small></span>}</label><label className="form-span-2">Image description <span>(recommended for accessibility)</span><input maxLength="200" name="altText" value={form.altText} onChange={update} /></label>{!editingProduct && <label className="check-label form-span-2"><input checked={form.publishNow} name="publishNow" type="checkbox" onChange={update} /><span>Publish immediately after saving.</span></label>}<button className="button button--orange form-submit form-span-2" disabled={submitting} type="submit">{submitting ? 'Saving…' : editingProduct ? 'Save changes' : 'Save product'} <Icon name="arrowRight" size={15} /></button></form></section>
    <section className="dashboard-panel" id="seller-orders"><div className="dashboard-panel__heading"><div><p className="eyebrow eyebrow--line">Order fulfilment</p><h2>Customer orders</h2></div></div>{orders.length ? <div className="compact-list compact-list--orders">{orders.map((order) => <article key={order.orderId}><div><strong>{order.orderReference}</strong><small>{order.status?.replaceAll('_', ' ')} · {order.items?.length || 0} items</small><div className="seller-fulfillment-actions">{order.items?.map((item) => <div key={item.id}><span>{item.productTitle}</span><button type="button" disabled={submitting} onClick={() => updateFulfillment(order.orderId, item.id, 'ACCEPTED')}>Accept</button><button type="button" disabled={submitting} onClick={() => updateFulfillment(order.orderId, item.id, 'READY_FOR_COLLECTION')}>Ready</button><button type="button" disabled={submitting} onClick={() => updateFulfillment(order.orderId, item.id, 'DISPATCHED')}>Dispatch</button></div>)}</div></div><span>{formatPrice(order.sellerTotal, order.currency)}</span></article>)}</div> : <div className="panel-empty"><p>New customer orders will appear here.</p></div>}</section></div>
  </DashboardShell>;
}
