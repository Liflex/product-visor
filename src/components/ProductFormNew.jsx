/**
 * Enhanced Product Form Component
 * Provides form for creating new products with validation and dynamic attributes
 */

import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import BarcodeScanner from './BarcodeScanner';
import { createProduct } from '../services/productService';
import { getCategories } from '../services/categoryService';
import { API_URLS } from '../config/api-config.js';
import axios from 'axios'; // Added axios import

const ProductFormNew = ({ categories, onProductCreated, initialProduct = null, locationState = null }) => {
    const [selectedCategory, setSelectedCategory] = useState(null);
    const [productAttributes, setProductAttributes] = useState({});
    const [productName, setProductName] = useState('');
    const [productPrice, setProductPrice] = useState('');
    const [productArticle, setProductArticle] = useState('');
    const [productBarcode, setProductBarcode] = useState('');
    const [productQuantity, setProductQuantity] = useState(0);
    const [dynamicFields, setDynamicFields] = useState({});
    const [imageFile, setImageFile] = useState(null);
    const [previewUrl, setPreviewUrl] = useState(null);
    const [showBarcodeScanner, setShowBarcodeScanner] = useState(false);
    
    // Поля упаковки
    const [packageWidth, setPackageWidth] = useState('');
    const [packageHeight, setPackageHeight] = useState('');
    const [packageLength, setPackageLength] = useState('');
    const [packageWeight, setPackageWeight] = useState('');
    const [packageQuantity, setPackageQuantity] = useState('');

    // Функция для генерации уникального 12-значного артикула
    const generateArticle = () => {
        const timestamp = Date.now().toString();
        const random = Math.floor(Math.random() * 1000).toString().padStart(3, '0');
        const article = (timestamp + random).slice(-12);
        return article;
    };

    // Инициализация формы при копировании товара или переходе с главной страницы
    useEffect(() => {
        if (initialProduct) {
            setProductName(initialProduct.name || '');
            setProductPrice(initialProduct.price || '');
            setProductBarcode(initialProduct.barcode || '');
            setProductQuantity(initialProduct.quantity || 0);
            
            // Копируем данные об упаковке
            if (initialProduct.packageInfo) {
                setPackageWidth(initialProduct.packageInfo.width || '');
                setPackageHeight(initialProduct.packageInfo.height || '');
                setPackageLength(initialProduct.packageInfo.length || '');
                setPackageWeight(initialProduct.packageInfo.weight || '');
                setPackageQuantity(initialProduct.packageInfo.quantityInPackage || '');
            }
            
            // Устанавливаем категорию
            if (initialProduct.category) {
                const category = categories.find(cat => cat.id === initialProduct.category.id);
                if (category) {
                    setSelectedCategory(category);
                    console.log('✅ Category set for copying:', category.name);
                }
            }
        } else if (locationState && locationState.barcode) {
            // Заполняем штрих-код при переходе с главной страницы
            setProductBarcode(locationState.barcode);
            console.log('📝 Pre-filled barcode from navigation:', locationState.barcode);
        }
    }, [initialProduct, categories, locationState]);

    // useEffect для установки категории при копировании
    useEffect(() => {
        if (initialProduct && initialProduct.category) {
            setSelectedCategory(initialProduct.category);
            console.log('✅ Category set for copying:', initialProduct.category.name);
        }
    }, [initialProduct]);

    // useEffect для инициализации атрибутов категории
    useEffect(() => {
        if (selectedCategory) {
            const initialAttributes = {};
            selectedCategory.attributes.forEach(attr => {
                if (attr.multiple) {
                    initialAttributes[attr.name] = [''];
                } else {
                    initialAttributes[attr.name] = '';
                }
            });
            setProductAttributes(initialAttributes);
            
            // Если это копирование товара, не перезаписываем dynamicFields
            if (!initialProduct) {
                setDynamicFields(initialAttributes);
                console.log('🔄 Initialized category attributes:', initialAttributes);
            }
        }
    }, [selectedCategory, initialProduct]);

    // Отдельный useEffect для копирования атрибутов после установки категории
    useEffect(() => {
        if (initialProduct && selectedCategory && initialProduct.productAttributeValues) {
            console.log('🔄 Copying attributes for category:', selectedCategory.name);
            
            const attributeFields = {};
            
            // Сначала инициализируем поля для всех атрибутов категории
            selectedCategory.attributes.forEach(attr => {
                if (attr.multiple) {
                    attributeFields[attr.name] = [];
                } else {
                    attributeFields[attr.name] = '';
                }
            });
            
            // Теперь заполняем значениями из копируемого товара
            initialProduct.productAttributeValues.forEach(attrValue => {
                const attrName = attrValue.attribute.name;
                const categoryAttr = selectedCategory.attributes.find(attr => attr.name === attrName);
                
                if (categoryAttr) {
                    if (categoryAttr.multiple) {
                        // Для множественных атрибутов добавляем в массив
                        if (!attributeFields[attrName]) {
                            attributeFields[attrName] = [];
                        }
                        attributeFields[attrName].push(attrValue.value);
                    } else {
                        // Для одиночных атрибутов устанавливаем значение
                        attributeFields[attrName] = attrValue.value;
                    }
                }
            });
            
            console.log('📋 Copied attribute fields:', attributeFields);
            setDynamicFields(attributeFields);
        }
    }, [initialProduct, selectedCategory]);

    const handleCategoryChange = (event) => {
        const categoryId = parseInt(event.target.value, 10);
        const category = categories.find(cat => cat.id === categoryId);
        setSelectedCategory(category);
    };

    const handleAttributeChange = (event, fieldName) => {
        const { value } = event.target;
        setDynamicFields({
            ...dynamicFields,
            [fieldName]: value
        });
    };

    const handleDynamicFieldChange = (event, fieldName, index) => {
        const { value } = event.target;
        const updatedFields = { ...dynamicFields };
        if (Array.isArray(updatedFields[fieldName])) {
            updatedFields[fieldName][index] = value;
        } else {
            updatedFields[fieldName] = value;
        }
        setDynamicFields(updatedFields);
    };

    const handleAddField = (fieldName) => {
        const updatedFields = { ...dynamicFields };
        if (Array.isArray(updatedFields[fieldName])) {
            updatedFields[fieldName].push('');
        }
        setDynamicFields(updatedFields);
    };

    const handleRemoveField = (fieldName, index) => {
        const updatedFields = { ...dynamicFields };
        if (Array.isArray(updatedFields[fieldName])) {
            updatedFields[fieldName].splice(index, 1);
        }
        setDynamicFields(updatedFields);
    };

    const handleImageChange = (event) => {
        const file = event.target.files[0];
        setImageFile(file);
        if (file) {
            setPreviewUrl(URL.createObjectURL(file));
        } else {
            setPreviewUrl(null);
        }
    };

    const handleBarcodeScan = (scannedCode) => {
        setProductBarcode(scannedCode);
        setShowBarcodeScanner(false);
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        
        // Собираем данные о товаре
        const productData = {
            name: productName,
            price: parseFloat(productPrice),
            barcode: productBarcode,
            quantity: parseInt(productQuantity),
            category: {
                id: selectedCategory.id
            },
            packageInfo: {
                width: packageWidth ? parseFloat(packageWidth) : null,
                height: packageHeight ? parseFloat(packageHeight) : null,
                length: packageLength ? parseFloat(packageLength) : null,
                weight: packageWeight ? parseFloat(packageWeight) : null,
                quantityInPackage: packageQuantity ? parseInt(packageQuantity) : null
            },
            productAttributeValues: Object.entries(dynamicFields).map(([key, value]) => {
                const attribute = selectedCategory.attributes.find(attr => attr.name === key);
                if (Array.isArray(value)) {
                    return value.map(val => ({
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
            }).flat()
        };

        // Преобразуем данные в JSON строку
        const formData = new FormData();
        formData.append('productData', JSON.stringify(productData));
        if (imageFile) {
            formData.append('image', imageFile);
        }
        
        try {
            const response = await axios.post(API_URLS.PRODUCTS.BASE, formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            });
            console.log('Product created successfully:', response.data);
            alert('Товар успешно добавлен!');

            // Сбрасываем форму
            setProductName('');
            setProductPrice('');
            setProductBarcode('');
            setProductQuantity(0);
            setPackageWidth('');
            setPackageHeight('');
            setPackageLength('');
            setPackageWeight('');
            setPackageQuantity('');
            setImageFile(null);
            setPreviewUrl(null);
            setSelectedCategory(null);
            setDynamicFields({});
            
            if (onProductCreated) {
                onProductCreated(response.data);
            }
        } catch (error) {
            console.error('Error creating product:', error);
            alert('Ошибка при добавлении товара.');
        }
    };

    return (
        <div className="container mx-auto px-4 py-8">
            <div className="bg-gray-800 p-8 rounded-lg shadow-lg w-full max-w-4xl mx-auto">
                <h2 className="text-2xl font-bold text-white mb-6">
                    {initialProduct ? 'Копирование товара' : 'Добавление нового товара'}
                </h2>
                
                {/* Уведомления */}
                {/* Уведомления */}

                {/* Показываем уведомление о предзаполненном штрих-коде */}
                {locationState && locationState.barcode && (
                    <div className="mb-4 p-3 bg-blue-600 text-white rounded-md">
                        <p className="text-sm">
                            📝 Штрих-код предзаполнен: <strong>{locationState.barcode}</strong>
                        </p>
                    </div>
                )}
                
                <form onSubmit={handleSubmit} className="space-y-6">
                    {/* Основная информация о товаре */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div>
                            <label htmlFor="productName" className="block text-sm font-medium text-gray-300">
                                Название товара <span className="text-red-500">*</span>
                            </label>
                            <input
                                type="text"
                                id="productName"
                                value={productName}
                                onChange={(e) => setProductName(e.target.value)}
                                className="mt-1 block w-full px-3 py-2 border border-gray-700 bg-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                                required
                            />
                        </div>
                        
                        <div>
                            <label htmlFor="productPrice" className="block text-sm font-medium text-gray-300">
                                Цена <span className="text-red-500">*</span>
                            </label>
                            <input
                                type="number"
                                step="0.01"
                                id="productPrice"
                                value={productPrice}
                                onChange={(e) => setProductPrice(e.target.value)}
                                className="mt-1 block w-full px-3 py-2 border border-gray-700 bg-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                                required
                            />
                        </div>
                        
                        <div>
                            <label htmlFor="productBarcode" className="block text-sm font-medium text-gray-300">
                                Штрих-код
                            </label>
                            <div className="flex space-x-2">
                                <input
                                    type="text"
                                    id="productBarcode"
                                    value={productBarcode}
                                    onChange={(e) => setProductBarcode(e.target.value)}
                                    className="mt-1 block w-full px-3 py-2 border border-gray-700 bg-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                                    placeholder="Введите штрих-код или отсканируйте..."
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowBarcodeScanner(true)}
                                    className="mt-1 px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors flex items-center space-x-2"
                                    title="Сканировать штрих-код"
                                >
                                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M12 12h4.01M16 20h4M4 12h4m12 0h.01M5 8h2a1 1 0 001-1V6a1 1 0 00-1-1H5a1 1 0 00-1 1v1a1 1 0 001 1zm12 0h2a1 1 0 001-1V6a1 1 0 00-1-1h-2a1 1 0 00-1 1v1a1 1 0 001 1zM5 20h2a1 1 0 001-1v-1a1 1 0 00-1-1H5a1 1 0 00-1 1v1a1 1 0 001 1z" />
                                    </svg>
                                    <span>Сканировать</span>
                                </button>
                            </div>
                        </div>
                        
                        <div>
                            <label htmlFor="productQuantity" className="block text-sm font-medium text-gray-300">
                                Количество на складе
                            </label>
                            <input
                                type="number"
                                id="productQuantity"
                                value={productQuantity}
                                onChange={(e) => setProductQuantity(e.target.value)}
                                className="mt-1 block w-full px-3 py-2 border border-gray-700 bg-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                            />
                        </div>
                    </div>

                    {/* Категория */}
                    <div>
                        <label htmlFor="category" className="block text-sm font-medium text-gray-300">
                            Категория <span className="text-red-500">*</span>
                        </label>
                        <select
                            id="category"
                            value={selectedCategory ? selectedCategory.id : ''}
                            onChange={handleCategoryChange}
                            className="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-700 bg-gray-700 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                            required
                        >
                            <option value="">Выберите категорию</option>
                            {categories.map(category => (
                                <option key={category.id} value={category.id}>{category.name}</option>
                            ))}
                        </select>
                    </div>

                    {/* Информация об упаковке */}
                    <div className="border border-gray-600 rounded-lg p-4">
                        <h3 className="text-lg font-medium text-white mb-4">Информация об упаковке</h3>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                            <div>
                                <label htmlFor="packageWidth" className="block text-sm font-medium text-gray-300">
                                    Ширина (см)
                                </label>
                                <input
                                    type="number"
                                    step="0.1"
                                    id="packageWidth"
                                    value={packageWidth}
                                    onChange={(e) => setPackageWidth(e.target.value)}
                                    className="mt-1 block w-full px-3 py-2 border border-gray-700 bg-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                                />
                            </div>
                            
                            <div>
                                <label htmlFor="packageHeight" className="block text-sm font-medium text-gray-300">
                                    Высота (см)
                                </label>
                                <input
                                    type="number"
                                    step="0.1"
                                    id="packageHeight"
                                    value={packageHeight}
                                    onChange={(e) => setPackageHeight(e.target.value)}
                                    className="mt-1 block w-full px-3 py-2 border border-gray-700 bg-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                                />
                            </div>
                            
                            <div>
                                <label htmlFor="packageLength" className="block text-sm font-medium text-gray-300">
                                    Длина (см)
                                </label>
                                <input
                                    type="number"
                                    step="0.1"
                                    id="packageLength"
                                    value={packageLength}
                                    onChange={(e) => setPackageLength(e.target.value)}
                                    className="mt-1 block w-full px-3 py-2 border border-gray-700 bg-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                                />
                            </div>
                            
                            <div>
                                <label htmlFor="packageWeight" className="block text-sm font-medium text-gray-300">
                                    Вес (кг)
                                </label>
                                <input
                                    type="number"
                                    step="0.01"
                                    id="packageWeight"
                                    value={packageWeight}
                                    onChange={(e) => setPackageWeight(e.target.value)}
                                    className="mt-1 block w-full px-3 py-2 border border-gray-700 bg-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                                />
                            </div>
                            
                            <div>
                                <label htmlFor="packageQuantity" className="block text-sm font-medium text-gray-300">
                                    Количество в упаковке
                                </label>
                                <input
                                    type="number"
                                    id="packageQuantity"
                                    value={packageQuantity}
                                    onChange={(e) => setPackageQuantity(e.target.value)}
                                    className="mt-1 block w-full px-3 py-2 border border-gray-700 bg-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                                />
                            </div>
                        </div>
                    </div>

                    {/* Изображение */}
                    <div>
                        <label htmlFor="image" className="block text-sm font-medium text-gray-300">
                            Изображение товара
                        </label>
                        <input
                            type="file"
                            id="image"
                            accept="image/*"
                            onChange={handleImageChange}
                            className="mt-1 block w-full px-3 py-2 border border-gray-700 bg-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                        />
                        {previewUrl && (
                            <div className="mt-2">
                                <img src={previewUrl} alt="Preview" className="h-32 w-32 object-cover rounded" />
                            </div>
                        )}
                    </div>

                    {/* Динамические атрибуты */}
                    {selectedCategory && (
                        <div className="border border-gray-600 rounded-lg p-4">
                            <h3 className="text-lg font-medium text-white mb-4">Характеристики товара</h3>
                            {selectedCategory.attributes.map(attr => {
                                const fieldValue = dynamicFields[attr.name];
                                console.log(`📝 Rendering field ${attr.name}:`, fieldValue);
                                
                                return (
                                    <div key={attr.name} className="mb-4">
                                        <label htmlFor={attr.name} className="block text-sm font-medium text-gray-300">
                                            {attr.nameRus}
                                            {attr.required && <span className="text-red-500">*</span>}
                                        </label>
                                        {Array.isArray(fieldValue) ? (
                                            fieldValue.map((value, index) => (
                                                <div key={index} className="flex items-center mb-2">
                                                    {attr.type === 'date' && (
                                                        <input
                                                            type="date"
                                                            id={`${attr.name}-${index}`}
                                                            value={value}
                                                            onChange={(e) => handleDynamicFieldChange(e, attr.name, index)}
                                                            className="mt-1 block w-full px-3 py-2 border border-gray-700 bg-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                                                            required={attr.required}
                                                        />
                                                    )}
                                                    {attr.type === 'double' && (
                                                        <input
                                                            type="number"
                                                            step="0.01"
                                                            id={`${attr.name}-${index}`}
                                                            value={value}
                                                            onChange={(e) => handleDynamicFieldChange(e, attr.name, index)}
                                                            className="mt-1 block w-full px-3 py-2 border border-gray-700 bg-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                                                            required={attr.required}
                                                        />
                                                    )}
                                                    {attr.type === 'integer' && (
                                                        <input
                                                            type="number"
                                                            id={`${attr.name}-${index}`}
                                                            value={value}
                                                            onChange={(e) => handleDynamicFieldChange(e, attr.name, index)}
                                                            className="mt-1 block w-full px-3 py-2 border border-gray-700 bg-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                                                            required={attr.required}
                                                        />
                                                    )}
                                                    {attr.type === 'string' && (
                                                        <input
                                                            type="text"
                                                            id={`${attr.name}-${index}`}
                                                            value={value}
                                                            onChange={(e) => handleDynamicFieldChange(e, attr.name, index)}
                                                            className="mt-1 block w-full px-3 py-2 border border-gray-700 bg-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                                                            required={attr.required}
                                                        />
                                                    )}
                                                    <button
                                                        type="button"
                                                        onClick={() => handleRemoveField(attr.name, index)}
                                                        className="ml-2 px-3 py-2 bg-red-600 text-white rounded-md hover:bg-red-700"
                                                    >
                                                        Удалить
                                                    </button>
                                                </div>
                                            ))
                                        ) : (
                                            <div>
                                                {attr.type === 'date' && (
                                                    <input
                                                        type="date"
                                                        id={attr.name}
                                                        value={fieldValue || ''}
                                                        onChange={(e) => handleAttributeChange(e, attr.name)}
                                                        className="mt-1 block w-full px-3 py-2 border border-gray-700 bg-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                                                        required={attr.required}
                                                    />
                                                )}
                                                {attr.type === 'double' && (
                                                    <input
                                                        type="number"
                                                        step="0.01"
                                                        id={attr.name}
                                                        value={fieldValue || ''}
                                                        onChange={(e) => handleAttributeChange(e, attr.name)}
                                                        className="mt-1 block w-full px-3 py-2 border border-gray-700 bg-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                                                        required={attr.required}
                                                    />
                                                )}
                                                {attr.type === 'integer' && (
                                                    <input
                                                        type="number"
                                                        id={attr.name}
                                                        value={fieldValue || ''}
                                                        onChange={(e) => handleAttributeChange(e, attr.name)}
                                                        className="mt-1 block w-full px-3 py-2 border border-gray-700 bg-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                                                        required={attr.required}
                                                    />
                                                )}
                                                {attr.type === 'string' && (
                                                    <input
                                                        type="text"
                                                        id={attr.name}
                                                        value={fieldValue || ''}
                                                        onChange={(e) => handleAttributeChange(e, attr.name)}
                                                        className="mt-1 block w-full px-3 py-2 border border-gray-700 bg-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                                                        required={attr.required}
                                                    />
                                                )}
                                            </div>
                                        )}
                                        {attr.multiple && (
                                            <button
                                                type="button"
                                                onClick={() => handleAddField(attr.name)}
                                                className="mt-2 px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700"
                                            >
                                                Добавить {attr.nameRus}
                                            </button>
                                        )}
                                    </div>
                                );
                            })}
                        </div>
                    )}

                    <div className="flex justify-end space-x-4">
                        <button
                            type="submit"
                            className="px-6 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                        >
                            {initialProduct ? 'Создать копию' : 'Создать товар'}
                        </button>
                    </div>
                </form>
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