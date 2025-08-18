/**
 * Product Service
 * Handles all product-related API operations
 */

import httpClient, { createFormData, uploadRequest } from '../utils/http-client.js';
import { ENDPOINTS } from '../config/api-config.js';

/**
 * Get all products
 * @returns {Promise<Array>} Array of products
 */
export const getProducts = async () => {
  try {
    const response = await httpClient.get(ENDPOINTS.PRODUCTS.BASE);
    return response.data;
  } catch (error) {
    console.error('Error fetching products:', error);
    throw error;
  }
};

export const getProductsPage = async (page = 0, size = 10) => {
  try {
    const response = await httpClient.get(ENDPOINTS.PRODUCTS.BASE, {
      params: { page, size }
    });
    return response.data; // Spring Page<ProductDto>
  } catch (error) {
    console.error('Error fetching products page:', error);
    throw error;
  }
};

export const searchProductsPage = async (q, page = 0, size = 10) => {
  try {
    const response = await httpClient.get(ENDPOINTS.PRODUCTS.SEARCH, {
      params: { q, page, size }
    });
    return response.data; // Spring Page<ProductDto>
  } catch (error) {
    console.error('Error searching products page:', error);
    throw error;
  }
};

/**
 * Get product by ID
 * @param {number} id - Product ID
 * @returns {Promise<Object>} Product object
 */
export const getProductById = async (id) => {
  try {
    const response = await httpClient.get(ENDPOINTS.PRODUCTS.BY_ID(id));
    return response.data;
  } catch (error) {
    console.error(`Error fetching product with ID ${id}:`, error);
    throw error;
  }
};

/**
 * Create new product
 * @param {Object} productData - Product data
 * @param {File} imageFile - Product image file
 * @returns {Promise<Object>} Created product
 */
export const createProduct = async (productData, imageFile) => {
  try {
    const formData = createFormData({ productData }, imageFile, 'image');
    const response = await uploadRequest(ENDPOINTS.PRODUCTS.BASE, formData, 'POST');
    return response.data;
  } catch (error) {
    console.error('Error creating product:', error);
    throw error;
  }
};

/**
 * Update existing product
 * @param {number} id - Product ID
 * @param {Object} productData - Updated product data
 * @param {File} imageFile - New product image file (optional)
 * @returns {Promise<Object>} Updated product
 */
export const updateProduct = async (id, productData, imageFile) => {
  try {
    const formData = createFormData({ productData }, imageFile, 'image');
    const response = await uploadRequest(ENDPOINTS.PRODUCTS.BY_ID(id), formData, 'PATCH');
    return response.data;
  } catch (error) {
    console.error(`Error updating product with ID ${id}:`, error);
    throw error;
  }
};

/**
 * Delete product
 * @param {number} id - Product ID
 * @returns {Promise<void>}
 */
export const deleteProduct = async (id) => {
  try {
    await httpClient.delete(ENDPOINTS.PRODUCTS.BY_ID(id));
  } catch (error) {
    console.error(`Error deleting product with ID ${id}:`, error);
    throw error;
  }
};

/**
 * Find product by barcode
 * @param {string} barcode - Product barcode
 * @returns {Promise<Object|null>} Product object or null if not found
 */
export const findProductByBarcode = async (barcode) => {
  try {
    console.log('🔍 Searching product by barcode:', barcode);
    const response = await httpClient.get(ENDPOINTS.PRODUCTS.BARCODE, {
      params: { barcode }
    });
    
    if (response.data) {
      console.log('✅ Product found by barcode:', response.data);
      return response.data;
    } else {
      console.log('❌ Product not found by barcode:', barcode);
      return null;
    }
  } catch (error) {
    console.error(`Error searching product by barcode ${barcode}:`, error);
    return null;
  }
};

/**
 * Search products
 * @param {string} query - Search query
 * @param {Object} filters - Filter options
 * @returns {Promise<Array>} Array of filtered products
 */
export const searchProducts = async (query, filters = {}) => {
  try {
    const params = {
      q: query,
      ...filters
    };
    const response = await httpClient.get(ENDPOINTS.PRODUCTS.SEARCH, { params });
    return response.data;
  } catch (error) {
    console.error('Error searching products:', error);
    // Fallback to client-side filtering if server search is not available
    const allProducts = await getProducts();
    return filterProductsLocally(allProducts, query, filters);
  }
};

/**
 * Client-side product filtering (fallback)
 * @param {Array} products - Array of products
 * @param {string} query - Search query
 * @param {Object} filters - Filter options
 * @returns {Array} Filtered products
 */
const filterProductsLocally = (products, query, filters) => {
  let filtered = products;

  // Text search
  if (query && query.trim()) {
    const searchQuery = query.toLowerCase().trim();
    filtered = filtered.filter(product => 
      product.name.toLowerCase().includes(searchQuery) ||
      product.category.name.toLowerCase().includes(searchQuery) ||
      (product.productAttributeValues && product.productAttributeValues.some(attr => 
        attr.value.toLowerCase().includes(searchQuery)
      ))
    );
  }

  // Category filter
  if (filters.categoryId) {
    filtered = filtered.filter(product => product.category.id === filters.categoryId);
  }

  // Attribute filters
  if (filters.attributes) {
    Object.entries(filters.attributes).forEach(([attrName, attrValue]) => {
      if (attrValue) {
        filtered = filtered.filter(product => 
          product.productAttributeValues &&
          product.productAttributeValues.some(attr => 
            attr.attribute.name === attrName &&
            attr.value.toLowerCase().includes(attrValue.toLowerCase())
          )
        );
      }
    });
  }

  return filtered;
};