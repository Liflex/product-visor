/**
 * Product Detail Component
 * Displays a single product with edit and delete functionality
 */

import React, { useState, useEffect } from 'react';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import { deleteProduct, getProductById } from '../services/productService.js';
import LoadingSpinner from './ui/loading-spinner.jsx';
import ErrorMessage from './ui/error-message.jsx';
import ProductCard from './ProductCard.jsx';
import CreateOrderModal from './CreateOrderModal.jsx';
import useBarcodeScanner from '../hooks/use-barcode-scanner.js';

/**
 * Product Detail Component
 * @returns {JSX.Element} Product detail component
 */
const ProductDetail = () => {
  const navigate = useNavigate();
  const { productId } = useParams();
  const location = useLocation();
  
  const [product, setProduct] = useState(location.state?.product || null);
  const [isLoading, setIsLoading] = useState(!location.state?.product);
  const [isDeleting, setIsDeleting] = useState(false);
  const [error, setError] = useState('');
  const [isOrderModalOpen, setIsOrderModalOpen] = useState(false);
  const [isImageFullscreen, setIsImageFullscreen] = useState(false);

  const { scannedBarcode, resetBarcode } = useBarcodeScanner();

  /**
   * Load product data
   */
  useEffect(() => {
    const loadProduct = async () => {
      if (location.state?.product) {
        return; // Product already loaded from navigation state
      }

      if (!productId) {
        setError('ID продукта обязателен');
        setIsLoading(false);
        return;
      }

      try {
        setIsLoading(true);
        setError('');
        
        const productData = await getProductById(parseInt(productId, 10));
        
        if (productData) {
          setProduct(productData);
        } else {
          setError('Продукт не найден');
        }
      } catch (error) {
        console.error('Error loading product:', error);
        setError('Не удалось загрузить продукт. Попробуйте еще раз.');
      } finally {
        setIsLoading(false);
      }
    };

    loadProduct();
  }, [productId, location.state]);

  /**
   * Handle global barcode scanning for quick product navigation
   */
  useEffect(() => {
    if (scannedBarcode && !isOrderModalOpen) {
      // Navigate to the scanned product
      navigate(`/product/${scannedBarcode}`);
      resetBarcode();
    }
  }, [scannedBarcode, navigate, resetBarcode, isOrderModalOpen]);

  /**
   * Handle product deletion
   */
  const handleDelete = async () => {
    if (!product) return;
    
    if (!confirm('Вы уверены, что хотите удалить этот продукт?')) {
      return;
    }

    setIsDeleting(true);
    setError('');

    try {
      await deleteProduct(product.id);
      console.log('✅ Product deleted successfully:', product.id);
      navigate('/all-products');
    } catch (error) {
      console.error('❌ Error deleting product:', error);
      setError('Не удалось удалить продукт. Попробуйте еще раз.');
    } finally {
      setIsDeleting(false);
    }
  };

  /**
   * Handle product editing
   */
  const handleEdit = () => {
    if (!product) return;
    navigate(`/edit-product/${product.id}`, { state: { product } });
  };

  /**
   * Handle order creation
   */
  const handleCreateOrder = () => {
    setIsOrderModalOpen(true);
  };

  /**
   * Handle order created callback
   */
  const handleOrderCreated = () => {
    // Можно добавить уведомление об успешном создании заказа
    console.log('✅ Order created successfully');
  };

  /**
   * Format attribute values for display
   */
  const formatAttributeValue = (attrValue) => {
    return `${attrValue.attribute.nameRus}: ${attrValue.value}`;
  };

  const getImageSrc = (product) => {
    const img = product?.image;
    if (!img && product?.imageUrl) return product.imageUrl;
    if (typeof img === 'string') {
      if (img.startsWith('data:')) return img;
      if (img.startsWith('/9j/')) return `data:image/jpeg;base64,${img}`;
    }
    if (Array.isArray(img)) {
      try {
        const base64 = btoa(String.fromCharCode(...new Uint8Array(img)));
        return `data:image/jpeg;base64,${base64}`;
      } catch (_) { return null; }
    }
    return null;
  };

  // Loading state
  if (isLoading) {
    return <LoadingSpinner message="Загрузка продукта..." />;
  }

  // Error state
  if (error) {
    return <ErrorMessage message={error} />;
  }

  // Product not found
  if (!product) {
    return <ErrorMessage message="Продукт не найден" />;
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="max-w-5xl mx-auto">
        {/* Header */}
        <div className="mb-6">
          <div className="flex justify-between items-center">
            <div>
              <h2 className="text-3xl font-bold text-white flex items-center">
                <span className="mr-3">📦</span>
                {product.name}
              </h2>
              <p className="text-gray-400 mt-1">Детальная информация о продукте</p>
            </div>
            
            {/* Action Buttons */}
            <div className="flex space-x-3">
              <button
                onClick={handleCreateOrder}
                disabled={isDeleting}
                className="px-6 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors shadow-lg"
              >
                🛒 Оформить заказ
              </button>
              
              <button
                onClick={handleEdit}
                disabled={isDeleting}
                className="px-6 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors shadow-lg"
              >
                ✏️ Редактировать
              </button>
              
              <button
                onClick={handleDelete}
                disabled={isDeleting}
                className="px-6 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors shadow-lg"
              >
                {isDeleting ? '🗑️ Удаление...' : '🗑️ Удалить'}
              </button>
            </div>
          </div>
        </div>

        {/* Error Message */}
        {error && <ErrorMessage message={error} />}

        {/* Large Image Preview */}
        {getImageSrc(product) && (
          <div className="mb-6">
            <img
              src={getImageSrc(product)}
              alt={product.name}
              className="w-full max-h-[480px] object-contain rounded cursor-zoom-in bg-black"
              onClick={() => setIsImageFullscreen(true)}
            />
            <p className="text-gray-400 text-sm mt-2">Кликните по изображению, чтобы развернуть на весь экран</p>
          </div>
        )}

        {/* Product Details Card */}
        <ProductCard
          product={product}
          variant="detail"
          showActions={false}
        />

        {/* Fullscreen Image Modal */}
        {isImageFullscreen && (
          <div
            className="fixed inset-0 bg-black bg-opacity-90 z-50 flex items-center justify-center"
            onClick={() => setIsImageFullscreen(false)}
          >
            <img
              src={getImageSrc(product)}
              alt={product.name}
              className="max-w-[95vw] max-h-[95vh] object-contain cursor-zoom-out"
            />
          </div>
        )}

        {/* Navigation */}
        <div className="flex justify-between pt-6">
          <button
            onClick={() => navigate('/')}
            className="px-6 py-2 border border-gray-600 text-gray-300 rounded-md hover:bg-gray-700 transition-colors shadow-md"
          >
            🏠 Главная
          </button>
          
          <button
            onClick={() => navigate('/all-products')}
            className="px-6 py-2 border border-gray-600 text-gray-300 rounded-md hover:bg-gray-700 transition-colors shadow-md"
          >
            📋 Все продукты
          </button>
        </div>

        {/* Create Order Modal */}
        <CreateOrderModal
          isOpen={isOrderModalOpen}
          onClose={() => setIsOrderModalOpen(false)}
          product={product}
          onOrderCreated={handleOrderCreated}
        />
      </div>
    </div>
  );
};

export default ProductDetail; 