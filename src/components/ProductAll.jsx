/**
 * Product List Component
 * Displays all products with search, filtering, and CRUD operations
 */

import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useProducts } from '../hooks/use-products.js';
import { useCategories } from '../hooks/use-categories.js';
import LoadingSpinner from './ui/loading-spinner.jsx';
import ErrorMessage from './ui/error-message.jsx';
import ProductSearch from './search/product-search.jsx';
import useBarcodeScanner from '../hooks/use-barcode-scanner.js';
import Notification from './ui/notification.jsx';
import ProductCard from './ProductCard.jsx';

/**
 * Product card wrapper for list view
 * @param {Object} props - Component props
 * @param {Object} props.product - Product data
 * @param {Function} props.onDelete - Delete handler
 * @param {Array} props.customActions - Custom action buttons
 * @returns {JSX.Element} Product card
 */
const ProductCardWrapper = ({ product, onDelete, customActions = [] }) => {
  const [isDeleting, setIsDeleting] = useState(false);

  /**
   * Handle product deletion with confirmation
   */
  const handleDelete = async () => {
    if (!window.confirm(`Are you sure you want to delete "${product.name}"?`)) {
      return;
    }

    setIsDeleting(true);
    try {
      await onDelete(product.id);
    } finally {
      setIsDeleting(false);
    }
  };

  return (
    <ProductCard
      product={product}
      variant="card"
      onDelete={handleDelete}
      isDeleting={isDeleting}
      customActions={customActions}
    />
  );
};

/**
 * Main ProductAll component
 */
const ProductAll = () => {
  const {
    products,
    isLoading,
    error,
    searchQuery,
    filters,
    updateSearchQuery,
    updateFilters,
    clearSearchAndFilters,
    removeProduct,
    loadProducts,
    hasProducts,
    filteredCount,
    totalProducts
  } = useProducts();

  const { categories } = useCategories();
  
  // Global barcode scanner
  const { notification, hideNotification } = useBarcodeScanner({
    enabled: true
  });

  /**
   * Handle product deletion
   */
  const handleDeleteProduct = async (productId) => {
    const success = await removeProduct(productId);
    if (!success) {
      // Error is already handled in the hook
      return false;
    }
    return true;
  };

  /**
   * Custom actions for product cards
   */
  const customActions = [
    {
      label: '👁️ View',
      onClick: (product) => {
        // Navigate to product detail page
        window.open(`/product/${product.id}`, '_blank');
      },
      className: 'text-blue-400 hover:text-blue-300',
      title: 'View product details'
    },
    {
      label: '📋 Copy ID',
      onClick: (product) => {
        navigator.clipboard.writeText(product.id.toString());
        // You could show a notification here
        console.log('Product ID copied:', product.id);
      },
      className: 'text-green-400 hover:text-green-300',
      title: 'Copy product ID to clipboard'
    }
  ];

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Page Header */}
      <div className="flex justify-between items-center mb-6">
        <div>
          <h2 className="text-3xl font-bold text-white">Product Catalog</h2>
          <p className="text-gray-400 mt-1">
            {isLoading ? 'Loading...' : `${filteredCount} of ${totalProducts} products`}
          </p>
        </div>
        <Link
          to="/add-product"
          className="bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-md transition-colors"
        >
          Add Product
        </Link>
      </div>

      {/* Search and Filters */}
      <ProductSearch
        searchQuery={searchQuery}
        filters={filters}
        categories={categories}
        onSearchChange={updateSearchQuery}
        onFiltersChange={updateFilters}
        onClearFilters={clearSearchAndFilters}
        isLoading={isLoading}
      />

      {/* Error Display */}
      {error && (
        <ErrorMessage
          message={error}
          onRetry={loadProducts}
          onDismiss={() => {/* Could add error dismissal logic here */}}
        />
      )}

      {/* Loading State */}
      {isLoading && !error && (
        <LoadingSpinner message="Loading products..." />
      )}

      {/* Products Grid */}
      {!isLoading && !error && (
        <>
          {hasProducts ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
              {products.map(product => (
                <ProductCardWrapper
                  key={product.id}
                  product={product}
                  onDelete={handleDeleteProduct}
                  customActions={customActions}
                />
              ))}
            </div>
          ) : (
            <div className="text-center py-12">
              <div className="text-gray-400 text-lg mb-4">
                {searchQuery || Object.keys(filters).length > 0 
                  ? 'No products found matching your criteria'
                  : 'No products available'
                }
              </div>
              {searchQuery || Object.keys(filters).length > 0 ? (
                <button
                  onClick={clearSearchAndFilters}
                  className="text-indigo-400 hover:text-indigo-300 transition-colors"
                >
                  Clear filters to see all products
                </button>
              ) : (
                <Link
                  to="/add-product"
                  className="text-indigo-400 hover:text-indigo-300 transition-colors"
                >
                  Add your first product
                </Link>
              )}
            </div>
          )}
        </>
      )}
      
      {/* Global Notifications */}
      {notification && (
        <Notification
          type={notification.type}
          message={notification.message}
          duration={notification.duration}
          onClose={hideNotification}
        />
      )}
    </div>
  );
};

export default ProductAll;