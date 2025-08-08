/**
 * API Configuration for Product Visor application
 * Contains all API endpoints and configuration settings
 */

// Base API configuration
export const API_CONFIG = {
  BASE_URL: 'http://192.168.1.59:8085',
  API_VERSION: 'v1',
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
 * @returns {string} Full API URL
 */
export const buildApiUrl = (endpoint) => {
  return `${API_CONFIG.BASE_URL}/api/${API_CONFIG.API_VERSION}${endpoint}`;
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
  }
};

export default API_CONFIG; 