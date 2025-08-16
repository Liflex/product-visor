/**
 * HTTP Client utility with error handling and request/response interceptors
 * Provides centralized HTTP client configuration for the application
 */

import axios from 'axios';
import { API_CONFIG } from '../config/api-config.js';

/**
 * Create axios instance with default configuration for product-visor-backend
 */
const httpClient = axios.create({
  baseURL: `${API_CONFIG.BASE_URL}/api/${API_CONFIG.API_VERSION}`,
  timeout: API_CONFIG.TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Create axios instance for microservices (order-service, ozon-service) without baseURL
 * These services use Vite proxy, so we use relative URLs
 */
const microservicesHttpClient = axios.create({
  timeout: API_CONFIG.TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Request interceptor to add common headers or modify requests
 */
const requestInterceptor = (config) => {
  // Add timestamp to prevent caching
  if (config.method === 'get') {
    config.params = {
      ...config.params,
      _t: Date.now(),
    };
  }
  
  console.log(`Making ${config.method?.toUpperCase()} request to ${config.url}`);
  return config;
};

const requestErrorInterceptor = (error) => {
  console.error('Request error:', error);
  return Promise.reject(error);
};

httpClient.interceptors.request.use(requestInterceptor, requestErrorInterceptor);
microservicesHttpClient.interceptors.request.use(requestInterceptor, requestErrorInterceptor);

/**
 * Response interceptor to handle common response patterns and errors
 */
const responseInterceptor = (response) => {
  console.log(`Response from ${response.config.url}:`, response.status);
  return response;
};

const responseErrorInterceptor = (error) => {
  console.error('Response error:', error);
  
  // Handle different types of errors
  if (error.response) {
    // Server responded with error status
    const { status, data } = error.response;
    
    switch (status) {
      case 400:
        throw new Error(`Bad Request: ${data?.message || 'Invalid request data'}`);
      case 401:
        throw new Error('Unauthorized: Please check your credentials');
      case 403:
        throw new Error('Forbidden: You do not have permission to perform this action');
      case 404:
        throw new Error('Not Found: The requested resource was not found');
      case 500:
        throw new Error('Server Error: Please try again later');
      default:
        throw new Error(`Server Error (${status}): ${data?.message || 'Unknown error'}`);
    }
  } else if (error.request) {
    // Request was made but no response received
    throw new Error('Network Error: Unable to connect to server. Please check your internet connection.');
  } else {
    // Something else happened
    throw new Error(`Request Error: ${error.message}`);
  }
};

httpClient.interceptors.response.use(responseInterceptor, responseErrorInterceptor);
microservicesHttpClient.interceptors.response.use(responseInterceptor, responseErrorInterceptor);

/**
 * Helper function to create FormData for file uploads
 * @param {Object} data - Data to include in FormData
 * @param {File} file - File to upload
 * @param {string} fileFieldName - Name of the file field
 * @returns {FormData} FormData object
 */
export const createFormData = (data, file, fileFieldName = 'file') => {
  const formData = new FormData();
  
  // Add data fields
  Object.keys(data).forEach(key => {
    const value = data[key];
    if (value !== null && value !== undefined) {
      if (typeof value === 'object') {
        formData.append(key, JSON.stringify(value));
      } else {
        formData.append(key, value);
      }
    }
  });
  
  // Add file if provided
  if (file) {
    formData.append(fileFieldName, file);
  }
  
  return formData;
};

/**
 * Helper function for multipart requests
 * @param {string} url - Request URL
 * @param {FormData} formData - FormData to send
 * @param {string} method - HTTP method (POST or PUT)
 * @returns {Promise} Axios response promise
 */
export const uploadRequest = (url, formData, method = 'POST') => {
  return httpClient({
    method,
    url,
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};

export { microservicesHttpClient };
export default httpClient; 