/**
 * Category Service
 * Handles all category-related API operations
 */

import httpClient from '../utils/http-client.js';
import { ENDPOINTS } from '../config/api-config.js';

/**
 * Get all categories
 * @returns {Promise<Array>} Array of categories with attributes
 */
export const getCategories = async () => {
  try {
    const response = await httpClient.get(ENDPOINTS.CATEGORIES.BASE);
    return response.data;
  } catch (error) {
    console.error('Error fetching categories:', error);
    throw error;
  }
};

/**
 * Get category by ID
 * @param {number} id - Category ID
 * @returns {Promise<Object>} Category object with attributes
 */
export const getCategoryById = async (id) => {
  try {
    const response = await httpClient.get(ENDPOINTS.CATEGORIES.BY_ID(id));
    return response.data;
  } catch (error) {
    console.error(`Error fetching category with ID ${id}:`, error);
    throw error;
  }
};