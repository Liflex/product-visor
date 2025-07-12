/**
 * Custom hook for product management
 * Provides all product-related operations and state management
 */

import { useState, useEffect, useCallback } from 'react';
import { 
  getProducts, 
  getProductById, 
  createProduct, 
  updateProduct, 
  deleteProduct, 
  searchProducts 
} from '../services/productService.js';

/**
 * Custom hook for product operations
 * @returns {Object} Product state and operations
 */
export const useProducts = () => {
  const [products, setProducts] = useState([]);
  const [filteredProducts, setFilteredProducts] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [filters, setFilters] = useState({});

  /**
   * Load all products
   */
  const loadProducts = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    
    try {
      const data = await getProducts();
      setProducts(data);
      setFilteredProducts(data);
    } catch (err) {
      setError(err.message);
      console.error('Failed to load products:', err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  /**
   * Search and filter products
   */
  const searchAndFilterProducts = useCallback(async (query = searchQuery, filterOptions = filters) => {
    setIsLoading(true);
    setError(null);

    try {
      let filtered;
      if (query || Object.keys(filterOptions).length > 0) {
        filtered = await searchProducts(query, filterOptions);
      } else {
        filtered = products;
      }
      setFilteredProducts(filtered);
    } catch (err) {
      setError(err.message);
      console.error('Failed to search products:', err);
    } finally {
      setIsLoading(false);
    }
  }, [searchQuery, filters, products]);

  /**
   * Add new product
   */
  const addProduct = useCallback(async (productData, imageFile) => {
    setIsLoading(true);
    setError(null);

    try {
      await createProduct(productData, imageFile);
      // Reload products to get the updated list
      await loadProducts();
      return true;
    } catch (err) {
      setError(err.message);
      console.error('Failed to create product:', err);
      return false;
    } finally {
      setIsLoading(false);
    }
  }, [loadProducts]);

  /**
   * Update existing product
   */
  const editProduct = useCallback(async (id, productData, imageFile) => {
    setIsLoading(true);
    setError(null);

    try {
      await updateProduct(id, productData, imageFile);
      // Reload products to get the updated list
      await loadProducts();
      return true;
    } catch (err) {
      setError(err.message);
      console.error('Failed to update product:', err);
      return false;
    } finally {
      setIsLoading(false);
    }
  }, [loadProducts]);

  /**
   * Remove product
   */
  const removeProduct = useCallback(async (id) => {
    setIsLoading(true);
    setError(null);

    try {
      await deleteProduct(id);
      // Remove from local state immediately for better UX
      setProducts(prev => prev.filter(p => p.id !== id));
      setFilteredProducts(prev => prev.filter(p => p.id !== id));
      return true;
    } catch (err) {
      setError(err.message);
      console.error('Failed to delete product:', err);
      // Reload products to ensure consistency
      await loadProducts();
      return false;
    } finally {
      setIsLoading(false);
    }
  }, [loadProducts]);

  /**
   * Get single product by ID
   */
  const getProduct = useCallback(async (id) => {
    setIsLoading(true);
    setError(null);

    try {
      const product = await getProductById(id);
      return product;
    } catch (err) {
      setError(err.message);
      console.error('Failed to get product:', err);
      return null;
    } finally {
      setIsLoading(false);
    }
  }, []);

  /**
   * Update search query and trigger search
   */
  const updateSearchQuery = useCallback((query) => {
    setSearchQuery(query);
    searchAndFilterProducts(query, filters);
  }, [searchAndFilterProducts, filters]);

  /**
   * Update filters and trigger search
   */
  const updateFilters = useCallback((newFilters) => {
    setFilters(newFilters);
    searchAndFilterProducts(searchQuery, newFilters);
  }, [searchAndFilterProducts, searchQuery]);

  /**
   * Clear search and filters
   */
  const clearSearchAndFilters = useCallback(() => {
    setSearchQuery('');
    setFilters({});
    setFilteredProducts(products);
  }, [products]);

  // Load products on mount
  useEffect(() => {
    loadProducts();
  }, [loadProducts]);

  return {
    // State
    products: filteredProducts,
    allProducts: products,
    isLoading,
    error,
    searchQuery,
    filters,
    
    // Operations
    loadProducts,
    searchAndFilterProducts,
    addProduct,
    editProduct,
    removeProduct,
    getProduct,
    
    // Search and filter
    updateSearchQuery,
    updateFilters,
    clearSearchAndFilters,
    
    // Utilities
    hasProducts: filteredProducts.length > 0,
    totalProducts: products.length,
    filteredCount: filteredProducts.length
  };
}; 