import React, { useState, useEffect } from 'react';
import axios from 'axios';

const ProductForm = ({ categories }) => {
    const [selectedCategory, setSelectedCategory] = useState(null);
    const [productAttributes, setProductAttributes] = useState({});
    const [productName, setProductName] = useState('');
    const [dynamicFields, setDynamicFields] = useState({});
    const [imageFile, setImageFile] = useState(null); // Для хранения выбранного файла изображения

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
            setDynamicFields(initialAttributes);
        }
    }, [selectedCategory]);

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
        setImageFile(event.target.files[0]);
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        // Собираем данные о товаре
        const productData = {
            name: productName,
            category: {  // Изменено с categoryId на вложенный объект category
                id: selectedCategory.id
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
        formData.append('image', imageFile);

        try {
            const response = await axios.post('http://localhost:8085/api/v1/product', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            });

            console.log('Product created successfully:', response.data);
            alert('Товар успешно добавлен!');
        } catch (error) {
            console.error('Error creating product:', error);
            alert('Ошибка при добавлении товара.');
        }
    };

    return (
        <div className="bg-gray-800 p-8 rounded-lg shadow-lg w-full max-w-4xl">
            <h2 className="text-2xl font-bold text-white mb-6">Добавление нового товара</h2>
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
                    <label htmlFor="image" className="block text-sm font-medium text-gray-300">Изображение товара</label>
                    <input
                        type="file"
                        id="image"
                        name="image"
                        accept="image/*"
                        onChange={handleImageChange}
                        className="mt-1 block w-full px-3 py-2 border border-gray-700 bg-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                    />
                </div>
                <button
                    type="submit"
                    className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
                >
                    Добавить товар
                </button>
            </form>
        </div>
    );
};

export default ProductForm;