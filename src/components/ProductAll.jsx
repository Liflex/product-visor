/**
 * Product List Component
 * Displays all products with search, filtering, and CRUD operations
 */

import React, { useState, useEffect } from 'react';
import axios from 'axios';
import ProductFormNew from './ProductFormNew.jsx';
import { API_URLS } from '../config/api-config.js';

const ProductAll = () => {
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showCreateForm, setShowCreateForm] = useState(false);
    const [copyProduct, setCopyProduct] = useState(null);
    const [showCopyForm, setShowCopyForm] = useState(false);

    useEffect(() => {
        fetchProducts();
        fetchCategories();
    }, []);

    const fetchProducts = async () => {
        try {
            const response = await axios.get(API_URLS.PRODUCTS.BASE);
            console.log('📦 Fetched products:', response.data);
            
            // Отладочная информация для первого товара с изображением
            const productWithImage = response.data.find(p => p.image);
            if (productWithImage) {
                console.log('🖼️ Product with image:', {
                    name: productWithImage.name,
                    imageType: typeof productWithImage.image,
                    imageIsArray: Array.isArray(productWithImage.image),
                    imageLength: productWithImage.image ? productWithImage.image.length : 'null',
                    imageFirstBytes: productWithImage.image ? productWithImage.image.slice(0, 10) : 'null'
                });
            }
            
            setProducts(response.data);
        } catch (error) {
            console.error('Error fetching products:', error);
        } finally {
            setLoading(false);
        }
    };

    const fetchCategories = async () => {
        try {
            const response = await axios.get(API_URLS.CATEGORIES.BASE);
            setCategories(response.data);
        } catch (error) {
            console.error('Error fetching categories:', error);
        }
    };

    const handleDelete = async (productId) => {
        if (window.confirm('Вы уверены, что хотите удалить этот товар?')) {
            try {
                await axios.delete(API_URLS.PRODUCTS.BY_ID(productId));
                fetchProducts();
                alert('Товар успешно удален!');
            } catch (error) {
                console.error('Error deleting product:', error);
                alert('Ошибка при удалении товара.');
            }
        }
    };

    const handleCopyProduct = (product) => {
        console.log('📋 Copying product:', {
            name: product.name,
            hasAttributes: !!product.productAttributeValues,
            attributesCount: product.productAttributeValues ? product.productAttributeValues.length : 0,
            attributes: product.productAttributeValues,
            hasPackageInfo: !!product.packageInfo,
            packageInfo: product.packageInfo
        });
        
        setCopyProduct(product);
        setShowCopyForm(true);
    };

    const handleProductCreated = () => {
        fetchProducts();
        setShowCreateForm(false);
        setShowCopyForm(false);
        setCopyProduct(null);
    };

    const formatPrice = (price) => {
        return new Intl.NumberFormat('ru-RU', {
            style: 'currency',
            currency: 'RUB'
        }).format(price);
    };

    const formatPackageInfo = (packageInfo) => {
        if (!packageInfo) return 'Не указано';
        
        const parts = [];
        if (packageInfo.width) parts.push(`Ш: ${packageInfo.width}см`);
        if (packageInfo.height) parts.push(`В: ${packageInfo.height}см`);
        if (packageInfo.length) parts.push(`Д: ${packageInfo.length}см`);
        if (packageInfo.weight) parts.push(`Вес: ${packageInfo.weight}кг`);
        if (packageInfo.quantityInPackage) parts.push(`В упаковке: ${packageInfo.quantityInPackage}шт`);
        
        return parts.length > 0 ? parts.join(', ') : 'Не указано';
    };

    // Функция для конвертации изображения в data URL
    const convertImageToDataUrl = (imageData) => {
        console.log('🔄 Converting image data:', {
            imageData,
            type: typeof imageData,
            isArray: Array.isArray(imageData),
            length: imageData ? imageData.length : 'null',
            startsWithDataUrl: imageData && imageData.startsWith('data:'),
            startsWithBase64: imageData && imageData.startsWith('/9j/')
        });
        
        if (!imageData) {
            console.log('❌ No image data provided');
            return null;
        }
        
        // Если уже data URL
        if (typeof imageData === 'string' && imageData.startsWith('data:')) {
            console.log('✅ Already a data URL');
            return imageData;
        }
        
        // Если это base64 строка (начинается с /9j/ для JPEG)
        if (typeof imageData === 'string' && imageData.startsWith('/9j/')) {
            console.log('✅ Base64 string detected, converting to data URL');
            const dataUrl = `data:image/jpeg;base64,${imageData}`;
            console.log('✅ Created data URL, length:', dataUrl.length);
            return dataUrl;
        }
        
        // Если это массив байтов
        if (Array.isArray(imageData)) {
            console.log('✅ Byte array detected, converting to base64');
            try {
                const uint8Array = new Uint8Array(imageData);
                const base64 = btoa(String.fromCharCode(...uint8Array));
                const dataUrl = `data:image/jpeg;base64,${base64}`;
                console.log('✅ Converted byte array to data URL');
                return dataUrl;
            } catch (error) {
                console.error('❌ Error converting byte array:', error);
                return null;
            }
        }
        
        console.log('❌ Unknown image data format:', typeof imageData);
        return null;
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center h-64">
                <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-indigo-500"></div>
            </div>
        );
    }

    if (showCreateForm) {
        return (
            <div className="container mx-auto px-4 py-8">
                <div className="mb-6">
                    <button
                        onClick={() => setShowCreateForm(false)}
                        className="px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700"
                    >
                        ← Назад к списку
                    </button>
                </div>
                <ProductFormNew 
                    categories={categories} 
                    onProductCreated={handleProductCreated}
                />
            </div>
        );
    }

    if (showCopyForm) {
        return (
            <div className="container mx-auto px-4 py-8">
                <div className="mb-6">
                    <button
                        onClick={() => {
                            setShowCopyForm(false);
                            setCopyProduct(null);
                        }}
                        className="px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700"
                    >
                        ← Назад к списку
                    </button>
                </div>
                <ProductFormNew 
                    categories={categories} 
                    onProductCreated={handleProductCreated}
                    initialProduct={copyProduct}
                />
            </div>
        );
    }

    return (
        <div className="container mx-auto px-4 py-8">
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-3xl font-bold text-white">Все товары</h1>
                <button
                    onClick={() => setShowCreateForm(true)}
                    className="px-6 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700"
                >
                    Добавить товар
                </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                {products.map((product) => (
                    <div key={product.id} className="bg-gray-800 rounded-lg shadow-lg p-6">
                        <div className="flex justify-between items-start mb-4">
                            <h3 className="text-lg font-semibold text-white truncate">
                                {product.name}
                            </h3>
                            <div className="flex space-x-2">
                                <button
                                    onClick={() => handleCopyProduct(product)}
                                    className="px-2 py-1 bg-blue-600 text-white text-xs rounded hover:bg-blue-700"
                                    title="Копировать товар"
                                >
                                    Копировать
                                </button>
                                <button
                                    onClick={() => handleDelete(product.id)}
                                    className="px-2 py-1 bg-red-600 text-white text-xs rounded hover:bg-red-700"
                                    title="Удалить товар"
                                >
                                    Удалить
                                </button>
                            </div>
                        </div>

                        {/* Отображение изображения */}
                        {product.image && (
                            <div className="mb-4">
                                <img
                                    src={convertImageToDataUrl(product.image)}
                                    alt={product.name}
                                    className="w-full h-32 object-cover rounded"
                                    onError={(e) => {
                                        console.error('Error loading image for product:', product.name);
                                        e.target.style.display = 'none';
                                    }}
                                />
                            </div>
                        )}

                        <div className="space-y-2 text-sm">
                            <div className="flex justify-between">
                                <span className="text-gray-400">Артикул:</span>
                                <span className="text-white font-mono">{product.article}</span>
                            </div>
                            
                            <div className="flex justify-between">
                                <span className="text-gray-400">Цена:</span>
                                <span className="text-green-400 font-semibold">
                                    {product.price ? formatPrice(product.price) : 'Не указана'}
                                </span>
                            </div>
                            
                            <div className="flex justify-between">
                                <span className="text-gray-400">Категория:</span>
                                <span className="text-white">{product.category?.name || 'Не указана'}</span>
                            </div>
                            
                            <div className="flex justify-between">
                                <span className="text-gray-400">На складе:</span>
                                <span className="text-white">{product.quantity || 0} шт.</span>
                            </div>
                            
                            {product.barcode && (
                                <div className="flex justify-between">
                                    <span className="text-gray-400">Штрих-код:</span>
                                    <span className="text-white font-mono">{product.barcode}</span>
                                </div>
                            )}
                            
                            <div className="border-t border-gray-700 pt-2">
                                <div className="text-gray-400 text-xs mb-1">Упаковка:</div>
                                <div className="text-white text-xs">
                                    {formatPackageInfo(product.packageInfo)}
                                </div>
                            </div>
                        </div>
                    </div>
                ))}
            </div>

            {products.length === 0 && (
                <div className="text-center py-12">
                    <p className="text-gray-400 text-lg">Товары не найдены</p>
                    <button
                        onClick={() => setShowCreateForm(true)}
                        className="mt-4 px-6 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700"
                    >
                        Добавить первый товар
                    </button>
                </div>
            )}
        </div>
    );
};

export default ProductAll;