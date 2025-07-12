/**
 * Product Search Component
 * Provides search and filtering functionality for products
 */

import React, { useState, useEffect } from 'react';
import InputField from '../ui/input-field.jsx';
import SelectField from '../ui/select-field.jsx';

/**
 * Product search and filter component
 * @param {Object} props - Component props
 * @param {string} props.searchQuery - Current search query
 * @param {Object} props.filters - Current filters
 * @param {Array} props.categories - Available categories
 * @param {Function} props.onSearchChange - Search change handler
 * @param {Function} props.onFiltersChange - Filters change handler
 * @param {Function} props.onClearFilters - Clear filters handler
 * @param {boolean} props.isLoading - Whether search is in progress
 * @returns {JSX.Element} Product search component
 */
const ProductSearch = ({
  searchQuery = '',
  filters = {},
  categories = [],
  onSearchChange,
  onFiltersChange,
  onClearFilters,
  isLoading = false
}) => {
  const [localSearchQuery, setLocalSearchQuery] = useState(searchQuery);
  const [localFilters, setLocalFilters] = useState(filters);
  const [isExpanded, setIsExpanded] = useState(false);

  // Debounce search input
  useEffect(() => {
    const timer = setTimeout(() => {
      if (localSearchQuery !== searchQuery) {
        onSearchChange?.(localSearchQuery);
      }
    }, 300);

    return () => clearTimeout(timer);
  }, [localSearchQuery, searchQuery, onSearchChange]);

  // Update local state when props change
  useEffect(() => {
    setLocalSearchQuery(searchQuery);
    setLocalFilters(filters);
  }, [searchQuery, filters]);

  /**
   * Handle search input change
   */
  const handleSearchChange = (event) => {
    setLocalSearchQuery(event.target.value);
  };

  /**
   * Handle filter change
   */
  const handleFilterChange = (filterName, value) => {
    const newFilters = {
      ...localFilters,
      [filterName]: value
    };

    // Remove empty filters
    if (!value) {
      delete newFilters[filterName];
    }

    setLocalFilters(newFilters);
    onFiltersChange?.(newFilters);
  };

  /**
   * Handle clear all filters
   */
  const handleClearAll = () => {
    setLocalSearchQuery('');
    setLocalFilters({});
    onClearFilters?.();
  };

  /**
   * Check if any filters are active
   */
  const hasActiveFilters = localSearchQuery || Object.keys(localFilters).length > 0;

  // Convert categories to select options
  const categoryOptions = categories.map(category => ({
    value: category.id,
    label: category.name
  }));

  return (
    <div className="bg-gray-800 rounded-lg shadow-lg p-6 mb-6">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-medium text-white">Search Products</h3>
        <div className="flex items-center space-x-2">
          {hasActiveFilters && (
            <button
              type="button"
              onClick={handleClearAll}
              className="text-sm text-gray-400 hover:text-white transition-colors"
              disabled={isLoading}
            >
              Clear All
            </button>
          )}
          <button
            type="button"
            onClick={() => setIsExpanded(!isExpanded)}
            className="text-sm text-indigo-400 hover:text-indigo-300 transition-colors"
          >
            {isExpanded ? 'Hide Filters' : 'Show Filters'}
          </button>
        </div>
      </div>

      {/* Search Input */}
      <div className="mb-4">
        <InputField
          type="text"
          placeholder="Search by name, category, or attributes..."
          value={localSearchQuery}
          onChange={handleSearchChange}
          inputProps={{
            disabled: isLoading
          }}
        />
      </div>

      {/* Advanced Filters */}
      {isExpanded && (
        <div className="border-t border-gray-700 pt-4">
          <h4 className="text-md font-medium text-gray-300 mb-3">Advanced Filters</h4>
          
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {/* Category Filter */}
            <SelectField
              label="Category"
              placeholder="All Categories"
              options={categoryOptions}
              value={localFilters.categoryId || ''}
              onChange={(e) => handleFilterChange('categoryId', parseInt(e.target.value) || '')}
              disabled={isLoading}
            />

            {/* Additional filters can be added here based on common attributes */}
            {/* For now, we'll add a few common ones for contact lenses */}
            
            <InputField
              label="Brand"
              type="text"
              placeholder="Filter by brand..."
              value={localFilters.brand || ''}
              onChange={(e) => handleFilterChange('brand', e.target.value)}
              inputProps={{
                disabled: isLoading
              }}
            />

            <InputField
              label="Power (Dioptries)"
              type="text"
              placeholder="e.g., -2.00, +1.50"
              value={localFilters.power || ''}
              onChange={(e) => handleFilterChange('power', e.target.value)}
              inputProps={{
                disabled: isLoading
              }}
            />

            <InputField
              label="Color"
              type="text"
              placeholder="Filter by color..."
              value={localFilters.color || ''}
              onChange={(e) => handleFilterChange('color', e.target.value)}
              inputProps={{
                disabled: isLoading
              }}
            />

            <SelectField
              label="Expiry Status"
              placeholder="All Products"
              options={[
                { value: 'valid', label: 'Valid (Not Expired)' },
                { value: 'expiring_soon', label: 'Expiring Soon (30 days)' },
                { value: 'expired', label: 'Expired' }
              ]}
              value={localFilters.expiryStatus || ''}
              onChange={(e) => handleFilterChange('expiryStatus', e.target.value)}
              disabled={isLoading}
            />

            <SelectField
              label="Sort By"
              placeholder="Default Order"
              options={[
                { value: 'name_asc', label: 'Name (A-Z)' },
                { value: 'name_desc', label: 'Name (Z-A)' },
                { value: 'category_asc', label: 'Category (A-Z)' },
                { value: 'expiry_date_asc', label: 'Expiry Date (Earliest)' },
                { value: 'expiry_date_desc', label: 'Expiry Date (Latest)' }
              ]}
              value={localFilters.sortBy || ''}
              onChange={(e) => handleFilterChange('sortBy', e.target.value)}
              disabled={isLoading}
            />
          </div>
        </div>
      )}

      {/* Active Filters Display */}
      {hasActiveFilters && (
        <div className="mt-4 pt-4 border-t border-gray-700">
          <div className="flex flex-wrap gap-2">
            {localSearchQuery && (
              <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-indigo-100 text-indigo-800">
                Search: "{localSearchQuery}"
                <button
                  type="button"
                  onClick={() => {
                    setLocalSearchQuery('');
                    onSearchChange?.('');
                  }}
                  className="ml-2 text-indigo-600 hover:text-indigo-500"
                  disabled={isLoading}
                >
                  ×
                </button>
              </span>
            )}
            
            {Object.entries(localFilters).map(([key, value]) => {
              if (!value) return null;
              
              let displayValue = value;
              if (key === 'categoryId') {
                const category = categories.find(c => c.id === parseInt(value));
                displayValue = category ? category.name : value;
              }
              
              return (
                <span
                  key={key}
                  className="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-800"
                >
                  {key}: {displayValue}
                  <button
                    type="button"
                    onClick={() => handleFilterChange(key, '')}
                    className="ml-2 text-gray-600 hover:text-gray-500"
                    disabled={isLoading}
                  >
                    ×
                  </button>
                </span>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
};

export default ProductSearch; 