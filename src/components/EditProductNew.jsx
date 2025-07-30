/**
 * Enhanced Edit Product Component
 * Provides form for editing existing products with validation and dynamic attributes
 */

import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useProducts } from '../hooks/use-products.js';
import { useCategories } from '../hooks/use-categories.js';
import { useProductFormValidation } from '../hooks/use-form-validation.js';
import { API_URLS } from '../config/api-config.js';
import InputField from './ui/input-field.jsx';
import SelectField from './ui/select-field.jsx';
import LoadingSpinner from './ui/loading-spinner.jsx';
import ErrorMessage from './ui/error-message.jsx';
import BarcodeScanner from './BarcodeScanner.jsx';
import MarketSelector from './MarketSelector.jsx';

/**
 * Dynamic attribute field component (reused from ProductFormNew)
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

  const handleMultipleValueChange = (index, newValue) => {
    const newValues = Array.isArray(value) ? [...value] : [''];
    newValues[index] = newValue;
    onChange(newValues);
  };

  const addField = () => {
    const newValues = Array.isArray(value) ? [...value, ''] : [''];
    onChange(newValues);
  };

  const removeField = (index) => {
    if (Array.isArray(value) && value.length > 1) {
      const newValues = value.filter((_, i) => i !== index);
      onChange(newValues);
    }
  };

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
                hasError={hasError && index === 0}
                error={hasError && index === 0 ? error : ''}
              />
            </div>
            
            <div className="ml-2 flex space-x-1">
              {values.length > 1 && (
                <button
                  type="button"
                  onClick={() => removeField(index)}
                  className="bg-red-600 hover:bg-red-700 text-white w-8 h-8 rounded focus:outline-none focus:ring-2 focus:ring-red-500"
                  title="Remove field"
                >
                  −
                </button>
              )}
              
              {index === values.length - 1 && (
                <button
                  type="button"
                  onClick={addField}
                  className="bg-green-600 hover:bg-green-700 text-white w-8 h-8 rounded focus:outline-none focus:ring-2 focus:ring-green-500"
                  title="Add field"
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
              placeholder="Enter barcode or scan..."
              error={error}
              hasError={hasError}
            />
          </div>
          
          <button
            type="button"
            onClick={() => setShowScanner(true)}
            className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors flex items-center space-x-2"
            title="Scan barcode"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M12 12h4.01M16 20h4M4 12h4m12 0h.01M5 8h2a1 1 0 001-1V6a1 1 0 00-1-1H5a1 1 0 00-1 1v1a1 1 0 001 1zm12 0h2a1 1 0 001-1V6a1 1 0 00-1-1h-2a1 1 0 00-1 1v1a1 1 0 001 1zM5 20h2a1 1 0 001-1v-1a1 1 0 00-1-1H5a1 1 0 00-1 1v1a1 1 0 001 1z" />
            </svg>
            <span>Scan</span>
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
 * Image upload component for editing
 */
const ImageUpload = ({ file, onChange, currentImageUrl, previewUrl, error, hasError }) => {
  const handleFileChange = (event) => {
    const selectedFile = event.target.files[0];
    onChange(selectedFile);
  };

  return (
    <div className="mb-4">
      <label className="block text-sm font-medium text-gray-300 mb-2">
        Изображение продукта
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
        <div className="flex space-x-4">
          {/* Current Image */}
          {currentImageUrl && !previewUrl && (
            <div>
              <p className="text-sm text-gray-300 mb-2">Текущее изображение:</p>
              <img
                src={currentImageUrl}
                alt="Current"
                className="w-32 h-32 object-cover rounded-md border border-gray-600"
              />
            </div>
          )}
          
          {/* New Preview */}
          {previewUrl && (
            <div>
              <p className="text-sm text-gray-300 mb-2">Новое изображение:</p>
              <img
                src={previewUrl}
                alt="Preview"
                className="w-32 h-32 object-cover rounded-md border border-gray-600"
              />
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

/**
 * Main EditProduct component
 */
const EditProductNew = () => {
  const navigate = useNavigate();
  const { productId } = useParams();
  const { editProduct, getProduct } = useProducts();
  const { categories, findCategoryById } = useCategories();
  
  // Local state
  const [isLoadingProduct, setIsLoadingProduct] = useState(true);
  const [productError, setProductError] = useState(null);
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [dynamicFields, setDynamicFields] = useState({});
  const [attributeIds, setAttributeIds] = useState({});
  const [imageFile, setImageFile] = useState(null);
  const [currentImageUrl, setCurrentImageUrl] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [barcode, setBarcode] = useState('');
  const [blobUrl, setBlobUrl] = useState(null);
  const [originalImageBytes, setOriginalImageBytes] = useState(null);
  const [selectedMarkets, setSelectedMarkets] = useState([]);
  const [quantity, setQuantity] = useState(0);
  
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
    markFieldTouched,
    setFormValues
  } = useProductFormValidation({
    name: '',
    category: null,
    dynamicFields: {},
    imageFile: null
  }, true); // isEdit = true

  /**
   * Load product data
   */
  useEffect(() => {
    const loadProductData = async () => {
      if (!productId) return;
      
      setIsLoadingProduct(true);
      setProductError(null);
      
      try {
        const product = await getProduct(productId);
        if (!product) {
          throw new Error('Product not found');
        }

        // Set basic product data
        setFormValues({
          name: product.name,
          quantity: product.quantity || 0,
          category: product.category,
          dynamicFields: {},
          imageFile: null
        });
        
        // Set category and barcode
        const category = findCategoryById(product.category.id);
        setSelectedCategory(category);
        // Create URL from image data if available
        if (product.image) {
          if (typeof product.image === 'string' && product.image.startsWith('/')) {
            // Handle base64 string
            const dataUrl = `data:image/jpeg;base64,${product.image}`;
            setCurrentImageUrl(dataUrl);
            setOriginalImageBytes(product.image); // Save original base64 for editing
          } else if (Array.isArray(product.image) && product.image.length > 0) {
            // Handle byte array
            const blob = new Blob([new Uint8Array(product.image)], { type: 'image/jpeg' });
            const newBlobUrl = URL.createObjectURL(blob);
            setBlobUrl(newBlobUrl);
            setCurrentImageUrl(newBlobUrl);
            setOriginalImageBytes(product.image); // Save original bytes for editing
          }
        } else if (product.imageUrl) {
          setCurrentImageUrl(API_URLS.IMAGES.BY_FILENAME(product.imageUrl));
        } else {
          setCurrentImageUrl(null);
        }
        setBarcode(product.barcode || '');

        // Загружаем данные о маркетах
        if (product.productMarkets && product.productMarkets.length > 0) {
          // Используем ProductMarketDto напрямую
          setSelectedMarkets(product.productMarkets);
          
          // Рассчитываем общее количество
          const totalQuantity = product.productMarkets.reduce((sum, pm) => sum + (pm.quantity || 0), 0);
          setQuantity(totalQuantity);
        } else {
          setSelectedMarkets([]);
          setQuantity(product.quantity || 0);
        }

        // Process product attributes
        const initialFields = {};
        const initialAttributeIds = {};

        if (product.productAttributeValues) {
          product.productAttributeValues.forEach(attrValue => {
            const attrName = attrValue.attribute.name;
            
            if (initialFields[attrName]) {
              // Convert to array for multiple values
              if (!Array.isArray(initialFields[attrName])) {
                initialFields[attrName] = [initialFields[attrName]];
                initialAttributeIds[attrName] = [initialAttributeIds[attrName]];
              }
              initialFields[attrName].push(attrValue.value);
              initialAttributeIds[attrName].push(attrValue.id);
            } else {
              // Check if this attribute should be multiple based on category attributes
              const categoryAttribute = category?.attributes?.find(attr => attr.name === attrName);
              if (categoryAttribute && categoryAttribute.multiple) {
                initialFields[attrName] = [attrValue.value];
                initialAttributeIds[attrName] = [attrValue.id];
              } else {
                initialFields[attrName] = attrValue.value;
                initialAttributeIds[attrName] = attrValue.id;
              }
            }
          });
        }

        // Initialize empty arrays for multiple attributes that don't have values yet
        if (category && category.attributes) {
          category.attributes.forEach(attr => {
            if (attr.multiple && !initialFields[attr.name]) {
              initialFields[attr.name] = [''];
              initialAttributeIds[attr.name] = [];
            }
          });
        }

        setDynamicFields(initialFields);
        setAttributeIds(initialAttributeIds);
        
      } catch (error) {
        console.error('Failed to load product:', error);
        setProductError(error.message);
      } finally {
        setIsLoadingProduct(false);
      }
    };

    loadProductData();
  }, [productId, getProduct, findCategoryById, setFormValues]);

  /**
   * Update form values when state changes
   */
  useEffect(() => {
    updateValue('category', selectedCategory);
    updateValue('dynamicFields', dynamicFields);
    updateValue('imageFile', imageFile);
  }, [selectedCategory, dynamicFields, imageFile, updateValue]);

  /**
   * Cleanup blob URL on unmount
   */
  useEffect(() => {
    return () => {
      if (blobUrl) {
        URL.revokeObjectURL(blobUrl);
      }
    };
  }, [blobUrl]);

  /**
   * Initialize dynamic fields when category changes
   */
  useEffect(() => {
    if (selectedCategory && selectedCategory.attributes) {
      const initialFields = {};
      selectedCategory.attributes.forEach(attr => {
        if (attr.multiple && !dynamicFields[attr.name]) {
          initialFields[attr.name] = [''];
        } else if (!dynamicFields[attr.name]) {
          initialFields[attr.name] = '';
        }
      });
      
      if (Object.keys(initialFields).length > 0) {
        setDynamicFields(prev => ({
          ...prev,
          ...initialFields
        }));
      }
    }
  }, [selectedCategory, dynamicFields]);

  /**
   * Handle category selection
   */
  const handleCategoryChange = (event) => {
    const categoryId = parseInt(event.target.value, 10);
    const category = findCategoryById(categoryId) || null;
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
        const attrId = attributeIds[key];
        
        if (Array.isArray(value)) {
          return value.filter(v => v && v.trim()).map((val, index) => {
            const id = Array.isArray(attrId) ? attrId[index] : null;
            return {
              id: id,
              attribute: {
                id: attribute.id,
                name: attribute.name,
                nameRus: attribute.nameRus,
                type: attribute.type,
                required: attribute.required,
                multiple: attribute.multiple
              },
              value: val,
              productId: parseInt(productId)
            };
          });
        } else {
          if (!value || !value.trim()) return null;
          
          return {
            id: attrId,
            attribute: {
              id: attribute.id,
              name: attribute.name,
              nameRus: attribute.nameRus,
              type: attribute.type,
              required: attribute.required,
              multiple: attribute.multiple
            },
            value: value,
            productId: parseInt(productId)
          };
        }
      }).flat().filter(item => item !== null),
      // Добавляем данные о маркетах как ProductMarketDto
      productMarkets: selectedMarkets.map(m => ({
        id: m.id,
        quantity: m.quantity,
        price: m.price,
        market: m.market
      }))
    };

    // Determine which image to send
    let imageToSend = imageFile;
    if (!imageFile && originalImageBytes) {
      if (typeof originalImageBytes === 'string' && originalImageBytes.startsWith('/')) {
        // Convert base64 string to File object
        const base64Data = originalImageBytes;
        const byteCharacters = atob(base64Data);
        const byteNumbers = new Array(byteCharacters.length);
        for (let i = 0; i < byteCharacters.length; i++) {
          byteNumbers[i] = byteCharacters.charCodeAt(i);
        }
        const byteArray = new Uint8Array(byteNumbers);
        const blob = new Blob([byteArray], { type: 'image/jpeg' });
        imageToSend = new File([blob], 'existing-image.jpg', { type: 'image/jpeg' });
      } else if (Array.isArray(originalImageBytes)) {
        // Create a File object from the original image bytes
        const blob = new Blob([new Uint8Array(originalImageBytes)], { type: 'image/jpeg' });
        imageToSend = new File([blob], 'existing-image.jpg', { type: 'image/jpeg' });
      }
    }

    const success = await editProduct(productId, productData, imageToSend);
    if (success) {
      navigate('/all-products');
    }
  };

  // Loading state
  if (isLoadingProduct) {
    return <LoadingSpinner message="Loading product..." />;
  }

  // Error state
  if (productError) {
    return (
      <div className="container mx-auto px-4 py-8">
        <ErrorMessage
          message={productError}
          onRetry={() => window.location.reload()}
        />
      </div>
    );
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
          <h2 className="text-3xl font-bold text-white">Редактировать продукт</h2>
          <p className="text-gray-400 mt-1">Обновите информацию о продукте и атрибуты</p>
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
              <InputField
                type="text"
                value={barcode}
                onChange={(e) => setBarcode(e.target.value)}
                placeholder="Введите штрих-код..."
                inputProps={{
                  id: 'barcode-input-field',
                  name: 'barcode'
                }}
              />
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
              currentImageUrl={currentImageUrl}
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
                {isSubmitting ? 'Обновление...' : 'Обновить продукт'}
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
    </div>
  );
};

export default EditProductNew; 