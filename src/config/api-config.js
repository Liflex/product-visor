/**
 * API Configuration for Product Visor application
 * Contains all API endpoints and configuration settings for microservices architecture
 */

// Microservices configuration
export const MICROSERVICES = {
  PRODUCT_VISOR_BACKEND: {
    BASE_URL: 'http://192.168.1.59:8085',
    API_VERSION: 'v1'
  },
  ORDER_SERVICE: {
    BASE_URL: '', // Используем прокси Vite
    API_VERSION: null // order-service doesn't use versioning
  },
  OZON_SERVICE: {
    BASE_URL: '', // Используем прокси Vite
    API_VERSION: null
  },
  YANDEX_SERVICE: {
    BASE_URL: '', // Используем прокси Vite
    API_VERSION: null
  },
  CLIENT: {
    BASE_URL: 'http://192.168.1.59:9089',
    API_VERSION: null
  },
  AUTH: {
    BASE_URL: 'http://192.168.1.59:9099',
    API_VERSION: null
  }
};

// Base API configuration (default for backward compatibility)
export const API_CONFIG = {
  BASE_URL: MICROSERVICES.PRODUCT_VISOR_BACKEND.BASE_URL,
  API_VERSION: MICROSERVICES.PRODUCT_VISOR_BACKEND.API_VERSION,
  TIMEOUT: 10000,
  
  // Image configuration
  IMAGE: {
    MAX_SIZE: 5 * 1024 * 1024, // 5MB
    ALLOWED_TYPES: ['image/jpeg', 'image/png', 'image/webp'],
    QUALITY: 0.8
  }
};

/**
 * Generate full API URL with base URL and version
 * @param {string} endpoint - API endpoint path
 * @param {string} service - Service name from MICROSERVICES
 * @returns {string} Full API URL
 */
export const buildApiUrl = (endpoint, service = 'PRODUCT_VISOR_BACKEND') => {
  const serviceConfig = MICROSERVICES[service];
  const baseUrl = serviceConfig.BASE_URL;
  const version = serviceConfig.API_VERSION;
  
  if (version) {
    return `${baseUrl}/api/${version}${endpoint}`;
  } else {
    // Если baseUrl пустой (для прокси), используем только endpoint
    if (!baseUrl) {
      return `/api${endpoint}`;
    }
    return `${baseUrl}/api${endpoint}`;
  }
};

// API Endpoints
export const ENDPOINTS = {
  PRODUCTS: {
    BASE: '/product',
    BY_ID: (id) => `/product/${id}`,
    SEARCH: '/product/search',
    BARCODE: '/product/barcode'
  },
  CATEGORIES: {
    BASE: '/category',
    BY_ID: (id) => `/category/${id}`
  },
  IMAGES: {
    BASE: '/image',
    BY_FILENAME: (filename) => `/image/${filename}`
  },
  ORDERS: {
    BASE: '/orders',
    BY_ID: (id) => `/orders/${id}`,
    BY_MARKET: (market) => `/orders/market/${market}`,
    BY_POSTING_NUMBER: (postingNumber) => `/orders/${postingNumber}`
  },
  OZON: {
    BASE: '/ozon',
    ORDERS_FBO_LIST: '/ozon/orders/fbo/list',
    ORDERS_FBO_BACKFILL: '/ozon/orders/fbo/backfill',
    CREDENTIALS: '/ozon/credentials'
  },
  YANDEX: {
    BASE: '/yandex',
    ORDERS_FBO_LIST: '/yandex/orders/fbo/list',
    ORDERS_FBO_BACKFILL: '/yandex/orders/fbo/backfill',
    CREDENTIALS: '/yandex/credentials'
  },
  COMPANIES: {
    BASE: '/companies',
    BY_ID: (id) => `/companies/${id}`,
    CURRENT: '/companies/current',
    AVATAR: (id) => `/companies/${id}/avatar`
  },
  PROFILE: {
    ME: '/profile/me',
    UPDATE: '/profile',
    AVATAR: '/profile/avatar'
  },
  AUTH: {
    LOGIN: '/auth/login'
  }
};

// Full API URLs
export const API_URLS = {
  PRODUCTS: {
    BASE: buildApiUrl(ENDPOINTS.PRODUCTS.BASE),
    BY_ID: (id) => buildApiUrl(ENDPOINTS.PRODUCTS.BY_ID(id)),
    SEARCH: buildApiUrl(ENDPOINTS.PRODUCTS.SEARCH),
    BARCODE: buildApiUrl(ENDPOINTS.PRODUCTS.BARCODE)
  },
  CATEGORIES: {
    BASE: buildApiUrl(ENDPOINTS.CATEGORIES.BASE),
    BY_ID: (id) => buildApiUrl(ENDPOINTS.CATEGORIES.BY_ID(id))
  },
  IMAGES: {
    BASE: buildApiUrl(ENDPOINTS.IMAGES.BASE),
    BY_FILENAME: (filename) => buildApiUrl(ENDPOINTS.IMAGES.BY_FILENAME(filename))
  },
  ORDERS: {
    BASE: () => buildApiUrl(ENDPOINTS.ORDERS.BASE, 'ORDER_SERVICE'),
    BY_ID: (id) => buildApiUrl(ENDPOINTS.ORDERS.BY_ID(id), 'ORDER_SERVICE'),
    BY_MARKET: (market) => buildApiUrl(ENDPOINTS.ORDERS.BY_MARKET(market), 'ORDER_SERVICE'),
    BY_POSTING_NUMBER: (postingNumber) => buildApiUrl(ENDPOINTS.ORDERS.BY_POSTING_NUMBER(postingNumber), 'ORDER_SERVICE')
  },
  OZON: {
    BASE: () => buildApiUrl(ENDPOINTS.OZON.BASE, 'OZON_SERVICE'),
    ORDERS_FBO_LIST: () => buildApiUrl(ENDPOINTS.OZON.ORDERS_FBO_LIST, 'OZON_SERVICE'),
    ORDERS_FBO_BACKFILL: () => buildApiUrl(ENDPOINTS.OZON.ORDERS_FBO_BACKFILL, 'OZON_SERVICE'),
    CREDENTIALS: () => buildApiUrl(ENDPOINTS.OZON.CREDENTIALS, 'OZON_SERVICE')
  },
  YANDEX: {
    BASE: () => buildApiUrl(ENDPOINTS.YANDEX.BASE, 'YANDEX_SERVICE'),
    ORDERS_FBO_LIST: () => buildApiUrl(ENDPOINTS.YANDEX.ORDERS_FBO_LIST, 'YANDEX_SERVICE'),
    ORDERS_FBO_BACKFILL: () => buildApiUrl(ENDPOINTS.YANDEX.ORDERS_FBO_BACKFILL, 'YANDEX_SERVICE'),
    CREDENTIALS: () => buildApiUrl(ENDPOINTS.YANDEX.CREDENTIALS, 'YANDEX_SERVICE')
  },
  COMPANIES: {
    BASE: () => buildApiUrl(ENDPOINTS.COMPANIES.BASE, 'CLIENT'),
    BY_ID: (id) => buildApiUrl(ENDPOINTS.COMPANIES.BY_ID(id), 'CLIENT'),
    CURRENT: () => buildApiUrl(ENDPOINTS.COMPANIES.CURRENT, 'CLIENT'),
    AVATAR: (id) => buildApiUrl(ENDPOINTS.COMPANIES.AVATAR(id), 'CLIENT')
  },
  PROFILE: {
    ME: () => buildApiUrl(ENDPOINTS.PROFILE.ME, 'CLIENT'),
    UPDATE: () => buildApiUrl(ENDPOINTS.PROFILE.UPDATE, 'CLIENT'),
    AVATAR: () => buildApiUrl(ENDPOINTS.PROFILE.AVATAR, 'CLIENT')
  },
  AUTH: {
    LOGIN: () => buildApiUrl(ENDPOINTS.AUTH.LOGIN, 'AUTH')
  }
};

export default API_CONFIG; 