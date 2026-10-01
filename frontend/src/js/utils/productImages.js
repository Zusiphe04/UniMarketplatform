import { CATEGORY_FALLBACK_IMAGES } from '../data/categories.js';

export function getProductImage(product) {
  const images = Array.isArray(product?.images) ? [...product.images] : [];
  images.sort((first, second) => {
    if (first.primary !== second.primary) return first.primary ? -1 : 1;
    return (first.displayOrder ?? 0) - (second.displayOrder ?? 0);
  });

  const image = images.find((item) => item?.imageUrl);
  return {
    src: image?.imageUrl || getCategoryFallback(product?.category),
    alt: image?.altText || product?.title || 'UniMarket listing',
  };
}

export function getCategoryFallback(category) {
  return CATEGORY_FALLBACK_IMAGES[category] || CATEGORY_FALLBACK_IMAGES.OTHER;
}
