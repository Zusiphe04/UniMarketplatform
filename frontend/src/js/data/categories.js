const FIELD_DEFINITIONS = {
  brand: { name: 'brand', label: 'Brand', maxLength: 80, placeholder: 'e.g. HP, Apple or Nike' },
  model: { name: 'model', label: 'Model', maxLength: 120, placeholder: 'e.g. MacBook Air M1' },
  storage: { name: 'storage', label: 'Storage', maxLength: 80, placeholder: 'e.g. 512 GB SSD' },
  memory: { name: 'memory', label: 'Memory (RAM)', maxLength: 80, placeholder: 'e.g. 16 GB RAM' },
  processor: { name: 'processor', label: 'Processor', maxLength: 120, placeholder: 'e.g. Apple M1 or Intel Core i5' },
  screenSize: { name: 'screenSize', label: 'Screen size', maxLength: 80, placeholder: 'e.g. 13.3 inches' },
  color: { name: 'color', label: 'Color', maxLength: 80, placeholder: 'e.g. Space Grey' },
  size: { name: 'size', label: 'Size / dimensions', maxLength: 80, placeholder: 'e.g. UK 7, Medium or 120 × 60 cm' },
};

const fields = (...names) => names.map((name) => FIELD_DEFINITIONS[name]);

export const CATEGORIES = [
  {
    value: 'BOOKS',
    label: 'Books',
    description: 'Textbooks and study guides',
    image: '/images/categories/books.png',
    featured: true,
    featureCopy: 'Find all your study needs.',
    fields: [],
  },
  {
    value: 'TECH',
    label: 'Tech',
    description: 'Laptops, phones, audio and devices',
    image: '/images/categories/electronics.png',
    featured: true,
    featureCopy: 'Devices and accessories.',
    fields: fields('brand', 'model', 'storage', 'memory', 'processor', 'screenSize', 'color'),
  },
  {
    value: 'CLOTHING',
    label: 'Clothing',
    description: 'Clothes, shoes and campus-ready style',
    image: '/images/categories/fashion.png',
    featured: false,
    featureCopy: 'Clothes and footwear.',
    fields: fields('brand', 'size', 'color'),
  },
  {
    value: 'ROOM_AND_HOME',
    label: 'Room & home',
    description: 'Furniture, appliances and room essentials',
    image: '/images/categories/home-and-furniture.png',
    featured: true,
    featureCopy: 'For your space.',
    fields: fields('brand', 'model', 'size', 'color'),
  },
  {
    value: 'SERVICE',
    label: 'Service',
    description: 'Skills and everyday help from the community',
    image: '/images/categories/services.png',
    featured: true,
    featureCopy: 'Skills and everyday help.',
    fields: [],
  },
  {
    value: 'OTHER',
    label: 'Other',
    description: 'Everything that does not fit another category',
    image: '/images/brand/unimarket-logo.png',
    featured: false,
    featureCopy: 'More community finds.',
    fields: fields('brand', 'model', 'size', 'color'),
  },
];

export const CATEGORY_BY_VALUE = Object.fromEntries(CATEGORIES.map((category) => [category.value, category]));

export const CATEGORY_LABELS = {
  ...Object.fromEntries(CATEGORIES.map((category) => [category.value, category.label])),
  ELECTRONICS: 'Tech',
  FASHION: 'Clothing',
  HOME: 'Room & home',
  SPORTS: 'Other',
  SERVICES: 'Service',
};

export const CATEGORY_FALLBACK_IMAGES = {
  ...Object.fromEntries(CATEGORIES.map((category) => [category.value, category.image])),
  ELECTRONICS: '/images/categories/electronics.png',
  FASHION: '/images/categories/fashion.png',
  HOME: '/images/categories/home-and-furniture.png',
  SPORTS: '/images/products/hybrid-bicycle.png',
  SERVICES: '/images/categories/services.png',
};

const SPECIFICATION_ROWS = [
  ['brand', 'Brand'],
  ['model', 'Model'],
  ['storage', 'Storage'],
  ['memory', 'Memory'],
  ['processor', 'Processor'],
  ['screenSize', 'Screen size'],
  ['color', 'Color'],
  ['size', 'Size / dimensions'],
];

const SUMMARY_KEYS = {
  TECH: ['storage', 'memory', 'color', 'screenSize'],
  CLOTHING: ['size', 'color', 'brand'],
  ROOM_AND_HOME: ['size', 'color', 'brand'],
  OTHER: ['size', 'color', 'brand'],
};

export function getProductSpecificationRows(product) {
  return SPECIFICATION_ROWS
    .map(([key, label]) => ({ key, label, value: product?.[key] }))
    .filter((item) => item.value !== null && item.value !== undefined && String(item.value).trim());
}

export function getProductSpecificationSummary(product, limit = 3) {
  const keys = SUMMARY_KEYS[product?.category] || [];
  return keys
    .map((key) => product?.[key])
    .filter((value) => value !== null && value !== undefined && String(value).trim())
    .slice(0, limit);
}
