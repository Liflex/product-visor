/**
 * Custom hook for category management
 * Provides category-related operations and state management
 */

import { useState, useEffect, useCallback } from 'react';
import { getCategories, getCategoryById } from '../services/categoryService.js';

/**
 * Custom hook for category operations
 * @returns {Object} Category state and operations
 */
export const useCategories = () => {
  const [categories, setCategories] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  /**
   * Load all categories
   */
  const loadCategories = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    
    try {
      const data = await getCategories();
      setCategories(data);
    } catch (err) {
      setError(err.message);
      console.error('Failed to load categories:', err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  /**
   * Get single category by ID
   */
  const getCategory = useCallback(async (id) => {
    setIsLoading(true);
    setError(null);

    try {
      const category = await getCategoryById(id);
      return category;
    } catch (err) {
      setError(err.message);
      console.error('Failed to get category:', err);
      return null;
    } finally {
      setIsLoading(false);
    }
  }, []);

  /**
   * Find category by ID from loaded categories
   */
  const findCategoryById = useCallback((id) => {
    return categories.find(category => category.id === id) || null;
  }, [categories]);

  /**
   * Get all attributes from all categories
   */
  const getAllAttributes = useCallback(() => {
    const allAttributes = new Map();
    
    categories.forEach(category => {
      if (category.attributes) {
        category.attributes.forEach(attr => {
          if (!allAttributes.has(attr.name)) {
            allAttributes.set(attr.name, attr);
          }
        });
      }
    });
    
    return Array.from(allAttributes.values());
  }, [categories]);

  // Load categories on mount
  useEffect(() => {
    loadCategories();
  }, [loadCategories]);

  return {
    // State
    categories,
    isLoading,
    error,
    
    // Operations
    loadCategories,
    getCategory,
    findCategoryById,
    getAllAttributes,
    
    // Utilities
    hasCategories: categories.length > 0,
    totalCategories: categories.length
  };
}; 