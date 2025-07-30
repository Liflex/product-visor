/**
 * Universal Product Card Component
 * Can be used for both list view and detail view
 */

import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { API_URLS } from '../config/api-config.js';
import LoadingSpinner from './ui/loading-spinner.jsx';

/**
 * Product Card Component
 * @param {Object} props - Component props
 * @param {Object} props.product - Product data
 * @param {string} props.variant - Display variant ('card' | 'detail')
 * @param {Function} props.onDelete - Delete handler (optional)
 * @param {Function} props.onEdit - Edit handler (optional)
 * @param {boolean} props.showActions - Whether to show action buttons
 * @param {boolean} props.isDeleting - Whether product is being deleted
 * @param {Array} props.customActions - Array of custom action buttons
 * @returns {JSX.Element} Product card component
 */
const ProductCard = ({ 
  product, 
  variant = 'card', 
  onDelete, 
  onEdit, 
  showActions = true,
  isDeleting = false,
  customActions = []
}) => {
  const [imageError, setImageError] = useState(false);
  const [blobUrl, setBlobUrl] = useState(null);

  /**
   * Handle image error
   */
  const handleImageError = (e) => {
    if (!imageError) {
      setImageError(true);
      // Prevent infinite loop by checking if we're already showing placeholder
      if (!e.target.src.includes('data:image/svg+xml')) {
        e.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjMzc0MTUxIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCwgc2Fucy1zZXJpZiIgZm9udC1zaXplPSIxNCIgZmlsbD0iIzljYTNmYSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPk5vIEltYWdlPC90ZXh0Pjwvc3ZnPg==';
      }
    }
  };

  /**
   * Cleanup blob URL on unmount
   */
  React.useEffect(() => {
    return () => {
      if (blobUrl) {
        URL.revokeObjectURL(blobUrl);
      }
    };
  }, [blobUrl]);

  /**
   * Get image URL from product data
   */
  const getImageUrl = () => {
    // If product has image as base64 string, create data URL
    if (product.image && typeof product.image === 'string' && product.image.startsWith('/')) {
      return `data:image/jpeg;base64,${product.image}`;
    }
    // If product has image as byte array, create blob URL
    if (product.image && Array.isArray(product.image) && product.image.length > 0) {
      if (!blobUrl) {
        const blob = new Blob([new Uint8Array(product.image)], { type: 'image/jpeg' });
        const url = URL.createObjectURL(blob);
        setBlobUrl(url);
        return url;
      }
      return blobUrl;
    }
    // Fallback to old file-based URL if imageUrl exists
    if (product.imageUrl) {
      return API_URLS.IMAGES.BY_FILENAME(product.imageUrl);
    }
    return null;
  };

  /**
   * Render card variant (for list view)
   */
  const renderCardVariant = () => {
    // Get first few attributes for preview
    const previewAttributes = product.productAttributeValues?.slice(0, 2) || [];
    
    return (
      <div className="bg-gray-800 rounded-lg shadow-lg overflow-hidden transition-all duration-300 hover:scale-105 hover:shadow-xl border border-gray-700">
        {/* Product Image */}
        <div className="relative">
          <img
            src={getImageUrl()}
            alt={product.name}
            className="w-full h-48 object-cover"
            onError={handleImageError}
          />
          {isDeleting && (
            <div className="absolute inset-0 bg-black bg-opacity-50 flex items-center justify-center">
              <LoadingSpinner size="small" message="Deleting..." />
            </div>
          )}
          
          {/* Category Badge */}
          <div className="absolute top-3 left-3">
            <span className="inline-block bg-indigo-600 text-white text-xs px-2 py-1 rounded-full shadow-lg">
              {product.category?.name || 'N/A'}
            </span>
          </div>
        </div>

        {/* Product Info */}
        <div className="p-4">
          <h3 className="text-xl font-bold text-white mb-3 truncate" title={product.name}>
            {product.name}
          </h3>
          
                      {/* Key Information Cards */}
            <div className="space-y-2 mb-4">
              {/* Quantity */}
              <div className="bg-gray-700 rounded-md p-2 shadow-sm border-l-2 border-l-yellow-500">
                <div className="flex items-center space-x-2">
                  <span className="text-yellow-400">📦</span>
                  <div className="flex-1 min-w-0">
                    <div className="text-xs text-gray-400">Количество</div>
                    <div className="text-sm text-white font-semibold">{product.quantity || 0}</div>
                  </div>
                </div>
              </div>
              
              {/* Barcode if exists */}
              {product.barcode && (
                <div className="bg-gray-700 rounded-md p-2 shadow-sm border-l-2 border-l-blue-500">
                  <div className="flex items-center space-x-2">
                    <span className="text-blue-400">📊</span>
                    <div className="flex-1 min-w-0">
                      <div className="text-xs text-gray-400">Штрих-код</div>
                      <div className="text-sm text-white font-mono truncate">{product.barcode}</div>
                    </div>
                  </div>
                </div>
              )}
            
            {/* Preview Attributes */}
            {previewAttributes.map(attrValue => (
              <div key={attrValue.id} className="bg-gray-700 rounded-md p-2 shadow-sm border-l-2 border-l-green-500">
                <div className="flex items-center space-x-2">
                  <span className="text-green-400">{getAttributeIcon(attrValue.attribute.type)}</span>
                  <div className="flex-1 min-w-0">
                    <div className="text-xs text-gray-400">{attrValue.attribute.nameRus}</div>
                    <div className="text-sm text-white truncate">{attrValue.value}</div>
                  </div>
                </div>
              </div>
            ))}
            
            {/* Show more indicator if there are more attributes */}
            {product.productAttributeValues && product.productAttributeValues.length > 2 && (
              <div className="text-xs text-gray-500 text-center py-1">
                +{product.productAttributeValues.length - 2} ещё
              </div>
            )}
          </div>

          {/* Action Buttons */}
          {showActions && (
            <div className="flex justify-between items-center pt-4 border-t border-gray-700">
              <div className="flex space-x-3">
                <Link 
                  to={`/edit-product/${product.id}`}
                  className="text-indigo-400 hover:text-indigo-300 text-sm font-medium transition-colors"
                >
                  Редактировать
                </Link>
                
                {/* Custom Actions */}
                {customActions.map((action, index) => (
                  <button
                    key={index}
                    onClick={() => action.onClick(product)}
                    disabled={action.disabled}
                    className={`text-sm font-medium transition-colors disabled:opacity-50 ${action.className || 'text-gray-400 hover:text-gray-300'}`}
                    title={action.title}
                  >
                    {action.label}
                  </button>
                ))}
              </div>
              
              {onDelete && (
                <button
                  onClick={() => onDelete(product.id)}
                  disabled={isDeleting}
                  className="text-red-400 hover:text-red-300 text-sm font-medium transition-colors disabled:opacity-50"
                >
                  Удалить
                </button>
              )}
            </div>
          )}
        </div>
      </div>
    );
  };

  /**
   * Render detail variant (for detail view)
   */
  const renderDetailVariant = () => {
    // Collect all information items
    const infoItems = [
      {
        label: 'Название',
        value: product.name,
        icon: '📦',
        priority: 'high'
      },
      {
        label: 'Категория',
        value: product.category?.name || 'N/A',
        icon: '🏷️',
        priority: 'high'
      },
      {
        label: 'Количество',
        value: product.quantity || 0,
        icon: '📦',
        priority: 'high'
      },
      ...(product.barcode ? [{
        label: 'Штрих-код',
        value: product.barcode,
        icon: '📊',
        priority: 'medium'
      }] : []),
      ...(product.productAttributeValues || []).map(attrValue => ({
        label: attrValue.attribute.nameRus,
        value: attrValue.value,
        icon: getAttributeIcon(attrValue.attribute.type),
        priority: 'normal'
      }))
    ];

    return (
      <div className="bg-gray-800 rounded-lg shadow-lg p-6">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Product Image */}
          <div>
            <img
              src={getImageUrl()}
              alt={product.name}
              className="w-full h-64 object-cover rounded-lg shadow-lg"
              onError={handleImageError}
            />
          </div>

          {/* Product Information */}
          <div>
            <h3 className="text-xl font-bold text-white mb-6 flex items-center">
              <span className="mr-2">ℹ️</span>
              Информация о продукте
            </h3>
            
            <div className="grid grid-cols-1 gap-3">
              {infoItems.map((item, index) => (
                <div
                  key={index}
                  className={`
                    bg-gray-700 rounded-lg p-4 shadow-md border-l-4 transition-all duration-200 hover:shadow-lg hover:scale-[1.02]
                    ${item.priority === 'high' ? 'border-l-indigo-500' : 
                      item.priority === 'medium' ? 'border-l-yellow-500' : 'border-l-green-500'}
                  `}
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-3">
                      <span className="text-lg">{item.icon}</span>
                      <div>
                        <div className="text-sm font-medium text-gray-400 mb-1">
                          {item.label}
                        </div>
                        <div className="text-white font-semibold">
                          {item.value}
                        </div>
                      </div>
                    </div>
                    
                    {/* Priority indicator */}
                    <div className={`
                      w-2 h-2 rounded-full
                      ${item.priority === 'high' ? 'bg-indigo-500' : 
                        item.priority === 'medium' ? 'bg-yellow-500' : 'bg-green-500'}
                    `}></div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    );
  };

  /**
   * Get icon for attribute type
   */
  const getAttributeIcon = (type) => {
    switch (type) {
      case 'string':
        return '📝';
      case 'double':
        return '📊';
      case 'integer':
        return '🔢';
      case 'date':
        return '📅';
      default:
        return '📋';
    }
  };

  return variant === 'detail' ? renderDetailVariant() : renderCardVariant();
};

export default ProductCard; 