// src/components/EditProduct.jsx

import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate, useParams } from 'react-router-dom';

const EditProduct = ({ categories }) => {
    const { productId } = useParams();
    const navigate = useNavigate();

    const [selectedCategory, setSelectedCategory] = useState(null);
    const [productAttributes, setProductAttributes] = useState({});
    const [productName, setProductName] = useState('');
    const [dynamicFields, setDynamicFields] = useState({});
    const [attributeIds, setAttributeIds] = useState({}); // Новое состояние для хранения id атрибутов
    const [imageFile, setImageFile] = useState(null);
    const [currentImageUrl, setCurrentImageUrl] = useState(null);
    const [previewUrl, setPreviewUrl] = useState(null); // Для хранения URL предварительного просмотра изображения

    useEffect(() => {
        const fetchProduct = async () => {
            try {
                const response = await axios.get(`http://localhost:8085/api/v1/product/${productId}`);
                const product = response.data;

                setProductName(product.name);
                setSelectedCategory(categories.find(cat => cat.id === product.category.id));
                setCurrentImageUrl(`http://localhost:8085/api/v1/image/${product.imageUrl}`);

                const initialAttributes = {};
                const initialAttributeIds = {}; // Новое состояние для хранения id атрибутов

                product.productAttributeValues.forEach(attrValue => {
                    if (initialAttributes[attrValue.attribute.name]) {
                        if (!Array.isArray(initialAttributes[attrValue.attribute.name])) {
                            initialAttributes[attrValue.attribute.name] = [initialAttributes[attrValue.attribute.name]];
                        }
                        initialAttributes[attrValue.attribute.name].push(attrValue.value);
                    } else {
                        initialAttributes[attrValue.attribute.name] = attrValue.value;
                    }

                    // Сохраняем id атрибутов
                    if (initialAttributeIds[attrValue.attribute.name]) {
                        if (!Array.isArray(initialAttributeIds[attrValue.attribute.name])) {
                            initialAttributeIds[attrValue.attribute.name] = [initialAttributeIds[attrValue.attribute.name]];
                        }
                        initialAttributeIds[attrValue.attribute.name].push(attrValue.id);
                    } else {
                        initialAttributeIds[attrValue.attribute.name] = attrValue.id;
                    }
                });

                setProductAttributes(initialAttributes);
                setDynamicFields(initialAttributes);
                setAttributeIds(initialAttributeIds); // Устанавливаем id атрибутов
            } catch (error) {
                console.error('Failed to fetch product:', error);
            }
        };

        if (categories.length > 0) {
            fetchProduct();
        }
    }, [categories, productId]);

    useEffect(() => {
        if (selectedCategory) {
            const initialAttributes = {};
            selectedCategory.attributes.forEach(attr => {
                if (attr.multiple) {
                    initialAttributes[attr.name] = dynamicFields[attr.name] || [''];
                } else {
                    initialAttributes[attr.name] = dynamicFields[attr.name] || '';
                }
            });
            setProductAttributes(initialAttributes);
        }
    }, [selectedCategory, dynamicFields]);

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

    const handleSubmit = async (event) => {
        event.preventDefault();

        const productData = {
            name: productName,
            category: {
                id: selectedCategory.id
            },
            productAttributeValues: Object.entries(dynamicFields).map(([key, value]) => {
                const attribute = selectedCategory.attributes.find(attr => attr.name === key);
                const attrId = attributeIds[key]; // Получаем id атрибута

                if (Array.isArray(value)) {
                    return value.map((val, index) => {
                        const id = Array.isArray(attrId) ? attrId[index] : null; // Получаем id для каждого значения
                        return {
                            id: id, // Добавляем id
                            attribute: {
                                id: attribute.id,
                                name: attribute.name,
                                nameRus: attribute.nameRus,
                                type: attribute.type,
                                required: attribute.required,
                                multiple: attribute.multiple
                            },
                            value: val,
                            productId: productId
                        };
                    });
                } else {
                    return {
                        id: attrId, // Добавляем id
                        attribute: {
                            id: attribute.id,
                            name: attribute.name,
                            nameRus: attribute.nameRus,
                            type: attribute.type,
                            required: attribute.required,
                            multiple: attribute.multiple
                        },
                        value: value,
                        productId: productId
                    };
                }
            }).flat()
        };

        const formData = new FormData();
        formData.append('productData', JSON.stringify(productData));
        if (imageFile) {
            formData.append('image', imageFile);
        }

        try {
            const response = await axios.put(`http://localhost:8085/api/v1/product/${productId}`, formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            });
            console.log('Product updated successfully:', response.data);
            alert('Товар успешно обновлен!');
            navigate('/all-products');
        } catch (error) {
            console.error('Error updating product:', error);
            alert('Ошибка при обновлении товара.');
        }
    };

    return (
        <div className="bg-gray-800 p-8 rounded-lg shadow-lg w-full max-w-4xl">
            <h2 className="text-2xl font-bold text-white mb-6">Редактирование товара</h2>
            <form onSubmit={handleSubmit}>
                <div className="mb-4">
                    <label htmlFor="productName" className="block text-sm font-medium text-gray-300">Название товара</label>
                    <input
                        type="text"
                        id="productName"
                        name="productName"
                        value={productName}
                        onChange={(e) => setProductName(e.target.value)}
                        className="mt-1 block w-full px-3 py-2 border border-gray-700 bg-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                        required
                    />
                </div>
                <div className="mb-4">
                    <label htmlFor="category" className="block text-sm font-medium text-gray-300">Категория</label>
                    <select
                        id="category"
                        name="category"
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
                {selectedCategory && (
                    <div>
                        {selectedCategory.attributes.map(attr => (
                            <div key={attr.name} className="mb-4">
                                <label htmlFor={attr.name} className="block text-sm font-medium text-gray-300">
                                    {attr.nameRus}
                                    {attr.required && <span className="text-red-500">*</span>}
                                </label>
                                {Array.isArray(dynamicFields[attr.name]) ? (
                                    dynamicFields[attr.name].map((value, index) => (
                                        <div key={index} className="flex items-center mb-2">
                                            {attr.type === 'date' && (
                                                <input
                                                    type="date"
                                                    id={`${attr.name}-${index}`}
                                                    name={attr.name}
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
                                                    name={attr.name}
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
                                                    name={attr.name}
                                                    value={value}
                                                    onChange={(e) => handleDynamicFieldChange(e, attr.name, index)}
                                                    className="mt-1 block w-full px-3 py-2 border border-gray-700 bg-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                                                    required={attr.required}
                                                />
                                            )}
                                            <button
                                                type="button"
                                                onClick={() => handleRemoveField(attr.name, index)}
                                                className="ml-2 bg-red-600 hover:bg-red-700 text-white font-bold py-1 px-2 rounded focus:outline-none focus:shadow-outline"
                                            >
                                                -
                                            </button>
                                        </div>
                                    ))
                                ) : (
                                    <>
                                        {attr.type === 'date' && (
                                            <input
                                                type="date"
                                                id={attr.name}
                                                name={attr.name}
                                                value={dynamicFields[attr.name]}
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
                                                name={attr.name}
                                                value={dynamicFields[attr.name]}
                                                onChange={(e) => handleAttributeChange(e, attr.name)}
                                                className="mt-1 block w-full px-3 py-2 border border-gray-700 bg-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                                                required={attr.required}
                                            />
                                        )}
                                        {attr.type === 'string' && (
                                            <input
                                                type="text"
                                                id={attr.name}
                                                name={attr.name}
                                                value={dynamicFields[attr.name]}
                                                onChange={(e) => handleAttributeChange(e, attr.name)}
                                                className="mt-1 block w-full px-3 py-2 border border-gray-700 bg-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                                                required={attr.required}
                                            />
                                        )}
                                    </>
                                )}
                                {attr.multiple && (
                                    <button
                                        type="button"
                                        onClick={() => handleAddField(attr.name)}
                                        className="mt-2 bg-green-600 hover:bg-green-700 text-white font-bold py-1 px-2 rounded focus:outline-none focus:shadow-outline"
                                    >
                                        +
                                    </button>
                                )}
                            </div>
                        ))}
                    </div>
                )}
                <div className="mb-4">
                    <label htmlFor="image" className="block text-sm font-medium text-gray-300">Текущее изображение товара</label>
                    {currentImageUrl && (
                        <div className="mb-2">
                            <img
                                src={currentImageUrl}
                                alt="Текущее изображение товара"
                                className="w-full h-48 object-cover rounded-lg"
                            />
                        </div>
                    )}
                    <label htmlFor="newImage" className="block text-sm font-medium text-gray-300">Выберите новое изображение (опционально)</label>
                    <input
                        type="file"
                        id="newImage"
                        name="newImage"
                        accept="image/*"
                        onChange={handleImageChange}
                        className="mt-1 block w-full px-3 py-2 border border-gray-700 bg-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                    />
                    {previewUrl && (
                        <div className="mt-2">
                            <img
                                src={previewUrl}
                                alt="Превью нового изображения"
                                className="w-full h-48 object-cover rounded-lg"
                            />
                        </div>
                    )}
                </div>
                <button
                    type="submit"
                    className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
                >
                    Сохранить изменения
                </button>
            </form>
        </div>
    );
};

export default EditProduct;