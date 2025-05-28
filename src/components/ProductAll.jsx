import React, { useState, useEffect } from 'react';
import { getProducts } from '../services/productService';

const ProductAll = () => {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        // Загрузка категорий из бэкенда
        const fetchProducts = async () => {
            try {
                const data = await getProducts();
                setProducts(data);
                setLoading(false);
            } catch (error) {
                console.error('Failed to fetch categories:', error);
                setError(error);
                setLoading(false);
            }
        };

        fetchProducts();
        }, []);

    if (loading) {
        return (
            <div className="flex items-center justify-center h-screen">
                <p className="text-xl">Загрузка...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="flex items-center justify-center h-screen">
                <p className="text-xl text-red-500">Ошибка загрузки данных: {error.message}</p>
            </div>
        );
    }

    return (
        <div className="container mx-auto px-4 py-8">
            <h2 className="text-3xl font-bold mb-6">Список всех товаров</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {products.map(product => (
                    <div key={product.id} className="bg-gray-800 p-4 rounded-lg shadow-lg">
                        <img
                            src={`http://localhost:8085/api/v1/image/${product.imageUrl}`}
                            alt={product.name}
                            className="w-full h-48 object-cover rounded-t-lg"
                        />
                        <div className="p-4">
                            <h3 className="text-xl font-bold mb-2">{product.name}</h3>
                            <div className="mb-4">
                                <span className="text-gray-400">Категория:</span> {product.category.name}
                            </div>
                            <div className="mb-4">
                                <span className="text-gray-400">Атрибуты:</span>
                                {product.productAttributeValues && product.productAttributeValues.length > 0 ? (
                                    <ul className="list-disc list-inside">
                                        {product.productAttributeValues.map(attrValue => (
                                            <li key={attrValue.id}>
                                                <strong>{attrValue.attribute.nameRus}:</strong> {attrValue.value}
                                            </li>
                                        ))}
                                    </ul>
                                ) : (
                                    <p className="text-gray-400">Нет атрибутов</p>
                                )}
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default ProductAll;