/**
 * Enhanced Product Form Component
 * Provides form for creating new products with validation and dynamic attributes
 */

import React, { useState, useEffect, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useProducts } from '../hooks/use-products.js';
import { useCategories } from '../hooks/use-categories.js';
import { useProductFormValidation } from '../hooks/use-form-validation.js';
import InputField from './ui/input-field.jsx';
import SelectField from './ui/select-field.jsx';
import LoadingSpinner from './ui/loading-spinner.jsx';
import ErrorMessage from './ui/error-message.jsx';
import BarcodeScanner from './BarcodeScanner.jsx';
import MarketSelector from './MarketSelector.jsx';

/**
 * Dynamic attribute field component
 * @param {Object} props - Component props
 * @param {Object} props.attribute - Attribute definition
 * @param {*} props.value - Current value
 * @param {Function} props.onChange - Change handler
 * @param {Function} props.onBlur - Blur handler
 * @param {string} props.error - Error message
 * @param {boolean} props.hasError - Whether field has error
 * @returns {JSX.Element} Attribute field component
 */
const AttributeField = ({ attribute, value, onChange, onBlur, error, hasError }) => {
  const { name, nameRus, type, required, multiple } = attribute;
  const [showScanner, setShowScanner] = useState(false);

  /**
   * Handle barcode scan
   */
  const handleBarcodeScan = (scannedCode) => {
    onChange(scannedCode);
    setShowScanner(false);
  };

  /**
   * Handle multiple value change
   */
  const handleMultipleValueChange = (index, newValue) => {
    const newValues = Array.isArray(value) ? [...value] : [''];
    newValues[index] = newValue;
    onChange(newValues);
  };

  /**
   * Add new field for multiple values
   */
  const addField = () => {
    const newValues = Array.isArray(value) ? [...value, ''] : [''];
    onChange(newValues);
  };

  /**
   * Remove field for multiple values
   */
  const removeField = (index) => {
    if (Array.isArray(value) && value.length > 1) {
      const newValues = value.filter((_, i) => i !== index);
      onChange(newValues);
    }
  };

  // Render multiple fields
  if (multiple) {
    const values = Array.isArray(value) ? value : [''];
    
    return (
      <div className="mb-4">
        <label className="block text-sm font-medium text-gray-300 mb-2">
          {nameRus}
          {required && <span className="text-red-500 ml-1">*</span>}
        </label>
        
        {values.map((val, index) => (
          <div key={index} className="flex items-center mb-2">
            <div className="flex-1">
              <InputField
                type={type === 'date' ? 'date' : type === 'double' || type === 'integer' ? 'number' : 'text'}
                value={val}
                onChange={(e) => handleMultipleValueChange(index, e.target.value)}
                onBlur={onBlur}
                placeholder={`${nameRus} ${index + 1}`}
                inputProps={{
                  step: type === 'double' ? '0.01' : type === 'integer' ? '1' : undefined
                }}
                hasError={hasError && index === 0} // Show error only on first field
                error={hasError && index === 0 ? error : ''}
              />
            </div>
            
            <div className="ml-2 flex space-x-1">
              {values.length > 1 && (
                <button
                  type="button"
                  onClick={() => removeField(index)}
                  className="bg-red-600 hover:bg-red-700 text-white w-8 h-8 rounded focus:outline-none focus:ring-2 focus:ring-red-500"
                  title="Удалить поле"
                >
                  −
                </button>
              )}
              
              {index === values.length - 1 && (
                <button
                  type="button"
                  onClick={addField}
                  className="bg-green-600 hover:bg-green-700 text-white w-8 h-8 rounded focus:outline-none focus:ring-2 focus:ring-green-500"
                  title="Добавить поле"
                >
                  +
                </button>
              )}
            </div>
          </div>
        ))}
      </div>
    );
  }

  // Render single field
  if (name.toLowerCase() === 'barcode') {
    return (
      <div className="mb-4">
        <label className="block text-sm font-medium text-gray-300 mb-2">
          {nameRus}
          {required && <span className="text-red-500 ml-1">*</span>}
        </label>
        
        <div className="flex space-x-2">
          <div className="flex-1">
            <InputField
              type="text"
              value={value || ''}
              onChange={(e) => onChange(e.target.value)}
              onBlur={onBlur}
              placeholder="Введите штрих-код или отсканируйте..."
              error={error}
              hasError={hasError}
            />
          </div>
          
          <button
            type="button"
            onClick={() => setShowScanner(true)}
            className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors flex items-center space-x-2"
            title="Сканировать штрих-код"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M12 12h4.01M16 20h4M4 12h4m12 0h.01M5 8h2a1 1 0 001-1V6a1 1 0 00-1-1H5a1 1 0 00-1 1v1a1 1 0 001 1zm12 0h2a1 1 0 001-1V6a1 1 0 00-1-1h-2a1 1 0 00-1 1v1a1 1 0 001 1zM5 20h2a1 1 0 001-1v-1a1 1 0 00-1-1H5a1 1 0 00-1 1v1a1 1 0 001 1z" />
            </svg>
            <span>Сканировать</span>
          </button>
        </div>
        
        {/* Barcode Scanner Modal */}
        <BarcodeScanner
          isOpen={showScanner}
          onScan={handleBarcodeScan}
          onClose={() => setShowScanner(false)}
        />
      </div>
    );
  }

  return (
    <InputField
      label={nameRus}
      type={type === 'date' ? 'date' : type === 'double' || type === 'integer' ? 'number' : 'text'}
      value={value || ''}
      onChange={(e) => onChange(e.target.value)}
      onBlur={onBlur}
      required={required}
      error={error}
      hasError={hasError}
      inputProps={{
        step: type === 'double' ? '0.01' : type === 'integer' ? '1' : undefined
      }}
    />
  );
};

/**
 * Image upload component
 * @param {Object} props - Component props
 * @param {File} props.file - Selected file
 * @param {Function} props.onChange - Change handler
 * @param {string} props.previewUrl - Preview URL
 * @param {string} props.error - Error message
 * @param {boolean} props.hasError - Whether field has error
 * @returns {JSX.Element} Image upload component
 */
const ImageUpload = ({ file, onChange, previewUrl, error, hasError }) => {
  /**
   * Handle file selection
   */
  const handleFileChange = (event) => {
    const selectedFile = event.target.files[0];
    onChange(selectedFile);
  };

  return (
    <div className="mb-4">
      <label className="block text-sm font-medium text-gray-300 mb-2">
        Изображение продукта <span className="text-red-500">*</span>
      </label>
      
      <div className="space-y-4">
        {/* File Input */}
        <input
          type="file"
          accept="image/*"
          onChange={handleFileChange}
          className={`block w-full text-sm text-gray-300 file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-medium file:bg-indigo-600 file:text-white hover:file:bg-indigo-700 file:cursor-pointer cursor-pointer ${
            hasError ? 'border-red-500' : 'border-gray-700'
          }`}
        />
        
        {/* Error Message */}
        {hasError && error && (
          <p className="text-sm text-red-500">{error}</p>
        )}
        
        {/* Preview */}
        {previewUrl && (
          <div className="mt-4">
            <p className="text-sm text-gray-300 mb-2">Предварительный просмотр:</p>
            <img
              src={previewUrl}
              alt="Preview"
              className="w-32 h-32 object-cover rounded-md border border-gray-600"
            />
          </div>
        )}
      </div>
    </div>
  );
};

/**
 * Main ProductForm component
 */
const ProductFormNew = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { addProduct } = useProducts();
  const { categories, isLoading: categoriesLoading } = useCategories();
  
  // Form state
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [dynamicFields, setDynamicFields] = useState({});
  const [imageFile, setImageFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [barcode, setBarcode] = useState(location.state?.barcode || '');
  const [showBarcodeScanner, setShowBarcodeScanner] = useState(false);
  const [isListeningForBarcode, setIsListeningForBarcode] = useState(true);
  const barcodeInputRef = useRef(null);
  const [selectedMarkets, setSelectedMarkets] = useState([]);
  const [quantity, setQuantity] = useState(0);

  // Show notification if barcode was pre-filled
  useEffect(() => {
    if (location.state?.barcode) {
      console.log('📝 Barcode pre-filled from navigation:', location.state.barcode);
    }
  }, [location.state?.barcode]);
  
  // Form validation
  const {
    values,
    errors,
    touched,
    isSubmitting,
    handleFieldChange,
    handleFieldBlur,
    handleSubmit,
    updateValue,
    markFieldTouched
  } = useProductFormValidation({
    name: '',
    category: null,
    dynamicFields: {},
    imageFile: null
  });

  /**
   * Initialize dynamic fields when category changes
   */
  useEffect(() => {
    if (selectedCategory) {
      const initialFields = {};
      selectedCategory.attributes.forEach(attr => {
        if (attr.multiple) {
          initialFields[attr.name] = [''];
        } else {
          initialFields[attr.name] = '';
        }
      });
      setDynamicFields(initialFields);
      updateValue('dynamicFields', initialFields);
    }
  }, [selectedCategory, updateValue]);

  /**
   * Update form values when state changes
   */
  useEffect(() => {
    updateValue('category', selectedCategory);
    updateValue('dynamicFields', dynamicFields);
    updateValue('imageFile', imageFile);
  }, [selectedCategory, dynamicFields, imageFile, updateValue]);

  /**
   * Handle category selection
   */
  const handleCategoryChange = (event) => {
    const categoryId = parseInt(event.target.value, 10);
    const category = categories.find(cat => cat.id === categoryId) || null;
    setSelectedCategory(category);
  };

  /**
   * Handle dynamic field change
   */
  const handleDynamicFieldChange = (fieldName, value) => {
    const newFields = {
      ...dynamicFields,
      [fieldName]: value
    };
    setDynamicFields(newFields);
  };

  /**
   * Handle image file change
   */
  const handleImageChange = (file) => {
    setImageFile(file);
    
    if (file) {
      const url = URL.createObjectURL(file);
      setPreviewUrl(url);
    } else {
      setPreviewUrl(null);
    }
  };

  /**
   * Handle barcode scan
   */
  const handleBarcodeScan = (scannedCode) => {
    setBarcode(scannedCode);
    setShowBarcodeScanner(false);
  };

  /**
   * Global barcode scanner listener with detailed logging
   */
  useEffect(() => {
    let barcodeBuffer = '';
    let barcodeTimeout = null;
    let keyPressTimes = [];
    let lastKeyTime = 0;

    const handleGlobalKeyDown = (event) => {
      // Only listen if auto-scan is enabled
      if (!isListeningForBarcode) {
        return;
      }
      
      // For input fields, we need to handle differently
      if (event.target.tagName === 'INPUT' || event.target.tagName === 'TEXTAREA') {
        // Only process if it's the barcode field and we're listening for scanner
        if (event.target === barcodeInputRef.current) {
          console.log('🔍 Barcode field focused - allowing scanner input');
          // Continue processing for barcode field
        } else {
          console.log('🔍 Other input field - ignoring scanner input');
          return;
        }
      }

      const currentTime = Date.now();
      const timeSinceLastKey = currentTime - lastKeyTime;
      lastKeyTime = currentTime;

      // Log only important events (scanner detection)
      if (event.key.length === 1 && event.key.charCodeAt(0) >= 32) {
        console.log('🔍 Scanner input detected:', {
          key: event.key,
          timeSinceLastKey: timeSinceLastKey + 'ms',
          target: event.target.tagName,
          isBarcodeField: event.target === barcodeInputRef.current
        });
      }

      // Check if it's a printable character (barcode scanner input)
      // Barcode can contain any printable characters: digits, letters, symbols, etc.
      if (event.key.length === 1 && event.key.charCodeAt(0) >= 32) {
        barcodeBuffer += event.key;
        keyPressTimes.push(currentTime);
        
        // Only log buffer updates for debugging (can be removed later)
        if (barcodeBuffer.length <= 3) {
          console.log('📝 Buffer:', barcodeBuffer);
        }
        
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
            const minInterval = intervals.length > 0 ? Math.min(...intervals) : 0;
            const maxInterval = intervals.length > 0 ? Math.max(...intervals) : 0;
            
            console.log('🎯 Barcode Analysis:', {
              barcode: barcodeBuffer,
              length: barcodeBuffer.length,
              avgInterval: avgInterval + 'ms',
              isLikelyScanner: avgInterval < 50 && barcodeBuffer.length > 5
            });
            
            // Only process if it looks like a scanner (fast typing + reasonable length)
            // Scanner typically types very fast (< 50ms between characters)
            // Human typing is typically 150-300ms between characters
            // Barcode can contain any characters: digits, letters, symbols, etc.
            const isLikelyScanner = avgInterval < 50 && barcodeBuffer.length > 5;
            
            if (isLikelyScanner) {
              console.log('✅ Scanner detected - Auto-filling barcode:', barcodeBuffer);
              
              // Auto-fill the barcode field and focus on it
              if (barcodeInputRef.current) {
                setBarcode(barcodeBuffer);
                barcodeInputRef.current.focus();
                console.log('🎯 Focused on barcode field');
              }
            } else {
              console.log('❌ Likely manual input - Ignoring:', barcodeBuffer);
            }
            
            // Reset buffer
            barcodeBuffer = '';
            keyPressTimes = [];
          }
        }, 150); // Slightly longer delay to capture full barcode
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
            console.log('✅ Scanner detected (Enter) - Auto-filling barcode:', barcodeBuffer);
            
            // Auto-fill the barcode field and focus on it
            if (barcodeInputRef.current) {
              setBarcode(barcodeBuffer);
              barcodeInputRef.current.focus();
              console.log('🎯 Focused on barcode field (Enter)');
            }
          } else {
            console.log('❌ Likely manual input (Enter) - Ignoring:', barcodeBuffer);
          }
          
          // Reset buffer
          barcodeBuffer = '';
          keyPressTimes = [];
        }
      }
    };

    // Add global event listener
    document.addEventListener('keydown', handleGlobalKeyDown);

    return () => {
      document.removeEventListener('keydown', handleGlobalKeyDown);
      if (barcodeTimeout) {
        clearTimeout(barcodeTimeout);
      }
    };
  }, [barcode, isListeningForBarcode]);

  /**
   * Handle form submission
   */
  const handleFormSubmit = async (formData) => {
    const productData = {
      name: formData.name,
      barcode: barcode.trim() || null,
      quantity: quantity, // Используем quantity из MarketSelector
      category: {
        id: selectedCategory.id
      },
      productAttributeValues: Object.entries(dynamicFields).map(([key, value]) => {
        const attribute = selectedCategory.attributes.find(attr => attr.name === key);
        
        if (Array.isArray(value)) {
          return value.filter(v => v.trim()).map(val => ({
            attribute: {
              id: attribute.id,
              name: attribute.name,
              nameRus: attribute.nameRus,
              type: attribute.type,
              required: attribute.required,
              multiple: attribute.multiple
            },
            value: val
          }));
        } else {
          return {
            attribute: {
              id: attribute.id,
              name: attribute.name,
              nameRus: attribute.nameRus,
              type: attribute.type,
              required: attribute.required,
              multiple: attribute.multiple
            },
            value: value
          };
        }
      }).flat().filter(item => item.value && item.value.trim()),
      // Добавляем данные о маркетах
      marketIds: selectedMarkets.map(m => m.marketId),
      marketQuantities: selectedMarkets.map(m => m.quantity),
      marketPrices: selectedMarkets.map(m => m.price)
    };

    const success = await addProduct(productData, imageFile);
    if (success) {
      navigate('/all-products');
    }
  };

  // Loading state
  if (categoriesLoading) {
    return <LoadingSpinner message="Загрузка категорий..." />;
  }

  // Convert categories to options
  const categoryOptions = categories.map(category => ({
    value: category.id,
    label: category.name
  }));

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="mb-6">
          <h2 className="text-3xl font-bold text-white">Добавить новый продукт</h2>
          <p className="text-gray-400 mt-1">Создайте новый продукт с динамическими атрибутами</p>
        </div>

        {/* Form */}
        <div className="bg-gray-800 rounded-lg shadow-lg p-6">
          <form onSubmit={(e) => {
            e.preventDefault();
            handleSubmit(handleFormSubmit);
          }}>
            {/* Product Name */}
            <InputField
              label="Название продукта"
              type="text"
              placeholder="Введите название продукта..."
              required
              {...values.name && handleFieldChange('name')}
              value={values.name}
              onChange={handleFieldChange('name')}
              onBlur={handleFieldBlur('name')}
              error={touched.name ? errors.name : ''}
              hasError={touched.name && !!errors.name}
            />

            {/* Barcode Field */}
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-300 mb-2">
                Штрих-код (Необязательно)
              </label>
              
              <div className="flex space-x-2">
                              <div className="flex-1">
                <InputField
                  ref={barcodeInputRef}
                  type="text"
                  value={barcode}
                  onChange={(e) => setBarcode(e.target.value)}
                  placeholder={location.state?.barcode ? `Штрих-код: ${location.state.barcode}` : "Наведите сканер в любое место и нажмите курок (поддерживаются любые символы)..."}
                  inputProps={{
                    id: 'barcode-input-field',
                    name: 'barcode'
                  }}
                  onKeyDown={(e) => {
                    // Auto-submit on Enter key (common for barcode scanners)
                    if (e.key === 'Enter' && barcode.trim()) {
                      e.preventDefault();
                      console.log('✅ Barcode Field - Processing barcode:', barcode.trim());
                      // The barcode is already set, just close scanner if open
                      if (showBarcodeScanner) {
                        setShowBarcodeScanner(false);
                      }
                    }
                  }}
                />
              </div>
                
                <button
                  type="button"
                  onClick={() => setShowBarcodeScanner(true)}
                  className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors flex items-center space-x-2"
                  title="Открыть сканер"
                >
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M12 12h4.01M16 20h4M4 12h4m12 0h.01M5 8h2a1 1 0 001-1V6a1 1 0 00-1-1H5a1 1 0 00-1 1v1a1 1 0 001 1zm12 0h2a1 1 0 001-1V6a1 1 0 00-1-1h-2a1 1 0 00-1 1v1a1 1 0 001 1zM5 20h2a1 1 0 001-1v-1a1 1 0 00-1-1H5a1 1 0 00-1 1v1a1 1 0 001 1z" />
                  </svg>
                  <span>Сканировать</span>
                </button>
              </div>
              
              {/* Scanner Status Indicator */}
              <div className="mt-2 flex items-center space-x-2">
                <div className={`w-2 h-2 rounded-full ${isListeningForBarcode ? 'bg-green-500 animate-pulse' : 'bg-red-500'}`}></div>
                <span className="text-xs text-gray-400">
                  {location.state?.barcode 
                    ? `Штрих-код предзаполнен: ${location.state.barcode}`
                    : isListeningForBarcode 
                      ? 'Автосканирование активно - Наведите сканер в любое место и нажмите курок' 
                      : 'Автосканирование отключено'
                  }
                </span>
                <button
                  type="button"
                  onClick={() => setIsListeningForBarcode(!isListeningForBarcode)}
                  className="text-xs text-indigo-400 hover:text-indigo-300 underline"
                >
                  {isListeningForBarcode ? 'Отключить' : 'Включить'}
                </button>
              </div>
            </div>

            {/* Category Selection */}
            <SelectField
              label="Категория"
              placeholder="Выберите категорию"
              options={categoryOptions}
              required
              value={selectedCategory?.id || ''}
              onChange={handleCategoryChange}
              onBlur={() => markFieldTouched('category')}
              error={touched.category ? errors.category : ''}
              hasError={touched.category && !!errors.category}
            />

            {/* Image Upload */}
            <ImageUpload
              file={imageFile}
              onChange={handleImageChange}
              previewUrl={previewUrl}
              error={touched.image ? errors.image : ''}
              hasError={touched.image && !!errors.image}
            />

            {/* Market Selection */}
            <MarketSelector
              selectedMarkets={selectedMarkets}
              onMarketsChange={setSelectedMarkets}
              quantity={quantity}
              onQuantityChange={setQuantity}
            />

            {/* Dynamic Attributes */}
            {selectedCategory && selectedCategory.attributes && (
              <div className="space-y-4">
                <h3 className="text-lg font-medium text-white border-b border-gray-700 pb-2">
                  Атрибуты продукта
                </h3>
                
                {selectedCategory.attributes.map(attribute => (
                  <AttributeField
                    key={attribute.name}
                    attribute={attribute}
                    value={dynamicFields[attribute.name]}
                    onChange={(value) => handleDynamicFieldChange(attribute.name, value)}
                    onBlur={() => markFieldTouched(attribute.name)}
                    error={touched[attribute.name] ? errors[attribute.name] : ''}
                    hasError={touched[attribute.name] && !!errors[attribute.name]}
                  />
                ))}
              </div>
            )}

            {/* Submit Button */}
            <div className="flex justify-end pt-6 border-t border-gray-700">
              <button
                type="submit"
                disabled={isSubmitting}
                className="px-6 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              >
                {isSubmitting ? 'Создание...' : 'Создать продукт'}
              </button>
            </div>
          </form>
          
          {/* Navigation Buttons */}
          <div className="flex justify-end space-x-4 pt-4 border-t border-gray-700 mt-4">
            <button
              type="button"
              onClick={() => navigate('/')}
              className="px-6 py-2 border border-gray-600 text-gray-300 rounded-md hover:bg-gray-700 transition-colors"
              disabled={isSubmitting}
            >
              Главная
            </button>
            
            <button
              type="button"
              onClick={() => navigate('/all-products')}
              className="px-6 py-2 border border-gray-600 text-gray-300 rounded-md hover:bg-gray-700 transition-colors"
              disabled={isSubmitting}
            >
              Отмена
            </button>
          </div>
        </div>
      </div>
      
      {/* Barcode Scanner Modal */}
      <BarcodeScanner
        isOpen={showBarcodeScanner}
        onScan={handleBarcodeScan}
        onClose={() => setShowBarcodeScanner(false)}
      />
    </div>
  );
};

export default ProductFormNew; 