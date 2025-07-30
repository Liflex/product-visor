/**
 * Global Barcode Scanner Hook
 * Provides global barcode scanning functionality with API integration
 */

import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { findProductByBarcode } from '../services/productService.js';

/**
 * Global Barcode Scanner Hook
 * @param {Object} options - Hook options
 * @param {boolean} options.enabled - Whether scanning is enabled (default: true)
 * @param {Function} options.onProductFound - Callback when product is found
 * @param {Function} options.onProductNotFound - Callback when product is not found
 * @returns {Object} Hook state and methods
 */
const useBarcodeScanner = ({ 
  enabled = true, 
  onProductFound, 
  onProductNotFound 
} = {}) => {
  const navigate = useNavigate();
  const [isScanning, setIsScanning] = useState(false);
  const [scannedBarcode, setScannedBarcode] = useState('');
  const [isSearching, setIsSearching] = useState(false);
  const [notification, setNotification] = useState(null);
  
  let barcodeBuffer = '';
  let barcodeTimeout = null;
  let keyPressTimes = [];
  let lastKeyTime = 0;

  /**
   * Show notification
   */
  const showNotification = (type, message, duration = 5000) => {
    setNotification({ type, message, duration });
  };

  /**
   * Hide notification
   */
  const hideNotification = () => {
    setNotification(null);
  };

  /**
   * Handle product found
   */
  const handleProductFound = (product) => {
    console.log('✅ Product found by barcode:', product);
    showNotification('success', `Product found: ${product.name}`, 3000);
    
    if (onProductFound) {
      onProductFound(product);
    } else {
      // Navigate to product detail page
      navigate(`/product/${product.id}`, { state: { product } });
    }
  };

  /**
   * Handle product not found
   */
  const handleProductNotFound = (barcode) => {
    console.log('❌ Product not found by barcode:', barcode);
    showNotification('info', `Product with barcode "${barcode}" not found. Redirecting to create new product...`, 3000);
    
    if (onProductNotFound) {
      onProductNotFound(barcode);
    } else {
      // Navigate to create product page with barcode pre-filled
      navigate('/add-product', { state: { barcode } });
    }
  };

  /**
   * Search product by barcode
   */
  const searchProductByBarcode = async (barcode) => {
    if (!barcode || barcode.trim().length === 0) return;

    setIsSearching(true);
    console.log('🔍 Searching product by barcode:', barcode);

    try {
      const product = await findProductByBarcode(barcode.trim());
      
      if (product) {
        handleProductFound(product);
      } else {
        handleProductNotFound(barcode);
      }
    } catch (error) {
      console.error('❌ Error searching product by barcode:', error);
      showNotification('error', 'Error searching for product. Please try again.', 5000);
    } finally {
      setIsSearching(false);
    }
  };

  /**
   * Global keyboard event handler
   */
  const handleGlobalKeyDown = (event) => {
    if (!enabled) return;

    // Ignore if user is typing in an input field
    if (event.target.tagName === 'INPUT' || event.target.tagName === 'TEXTAREA') {
      return;
    }

    const currentTime = Date.now();
    const timeSinceLastKey = currentTime - lastKeyTime;
    lastKeyTime = currentTime;

    // Check if it's a printable character (barcode scanner input)
    if (event.key.length === 1 && event.key.charCodeAt(0) >= 32) {
      barcodeBuffer += event.key;
      keyPressTimes.push(currentTime);
      
      console.log('📝 Scanner input detected:', {
        key: event.key,
        buffer: barcodeBuffer,
        timeSinceLastKey: timeSinceLastKey + 'ms'
      });
      
      // Clear previous timeout
      if (barcodeTimeout) {
        clearTimeout(barcodeTimeout);
      }
      
      // Set timeout to process barcode after scanner finishes
      barcodeTimeout = setTimeout(() => {
        if (barcodeBuffer.length > 0) {
          // Calculate timing statistics
          const intervals = [];
          for (let i = 1; i < keyPressTimes.length; i++) {
            intervals.push(keyPressTimes[i] - keyPressTimes[i-1]);
          }
          
          const avgInterval = intervals.length > 0 ? intervals.reduce((a, b) => a + b, 0) / intervals.length : 0;
          
          console.log('🎯 Barcode Analysis:', {
            barcode: barcodeBuffer,
            length: barcodeBuffer.length,
            avgInterval: avgInterval + 'ms',
            isLikelyScanner: avgInterval < 50 && barcodeBuffer.length > 5
          });
          
          // Only process if it looks like a scanner (fast typing + reasonable length)
          const isLikelyScanner = avgInterval < 50 && barcodeBuffer.length > 5;
          
          if (isLikelyScanner) {
            console.log('✅ Scanner detected - Processing barcode:', barcodeBuffer);
            setScannedBarcode(barcodeBuffer);
            searchProductByBarcode(barcodeBuffer);
          } else {
            console.log('❌ Likely manual input - Ignoring:', barcodeBuffer);
          }
          
          // Reset buffer
          barcodeBuffer = '';
          keyPressTimes = [];
        }
      }, 150);
    }
    
    // Handle Enter key (common for barcode scanners)
    if (event.key === 'Enter' && barcodeBuffer.length > 0) {
      event.preventDefault();
      
      console.log('⏎ Enter pressed with buffer:', barcodeBuffer);
      
      if (barcodeBuffer.length > 0) {
        // Calculate timing for Enter case
        const intervals = [];
        for (let i = 1; i < keyPressTimes.length; i++) {
          intervals.push(keyPressTimes[i] - keyPressTimes[i-1]);
        }
        
        const avgInterval = intervals.length > 0 ? intervals.reduce((a, b) => a + b, 0) / intervals.length : 0;
        const isLikelyScanner = avgInterval < 50 && barcodeBuffer.length > 5;
        
        if (isLikelyScanner) {
          console.log('✅ Scanner detected (Enter) - Processing barcode:', barcodeBuffer);
          setScannedBarcode(barcodeBuffer);
          searchProductByBarcode(barcodeBuffer);
        } else {
          console.log('❌ Likely manual input (Enter) - Ignoring:', barcodeBuffer);
        }
        
        // Reset buffer
        barcodeBuffer = '';
        keyPressTimes = [];
      }
    }
  };

  /**
   * Setup global event listener
   */
  useEffect(() => {
    if (enabled) {
      console.log('🔍 Global barcode scanner enabled');
      document.addEventListener('keydown', handleGlobalKeyDown);
      
      return () => {
        document.removeEventListener('keydown', handleGlobalKeyDown);
        if (barcodeTimeout) {
          clearTimeout(barcodeTimeout);
        }
      };
    }
  }, [enabled]);

  const resetBarcode = () => {
    setScannedBarcode('');
    barcodeBuffer = '';
    keyPressTimes = [];
    if (barcodeTimeout) {
      clearTimeout(barcodeTimeout);
    }
  };

  return {
    isScanning,
    isSearching,
    scannedBarcode,
    notification,
    hideNotification,
    searchProductByBarcode,
    resetBarcode
  };
};

export default useBarcodeScanner; 