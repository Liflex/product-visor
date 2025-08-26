import React, { useState, useEffect } from 'react';
import { warehouseService } from '../services/warehouseService.js';
import { productStockService } from '../services/productStockService.js';
import { useAuth } from '../contexts/AuthContext.jsx';

const ProductStockEditor = ({ 
    productStocks = [], 
    onStocksChange, 
    isEditMode = false, 
    productId = null 
}) => {
    const { companyId } = useAuth();
    const [warehouses, setWarehouses] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [showAddForm, setShowAddForm] = useState(false);
    const [showWarehouseModal, setShowWarehouseModal] = useState(false);
    const [selectedWarehouse, setSelectedWarehouse] = useState(null);
    const [editingIndex, setEditingIndex] = useState(null);
    const [formData, setFormData] = useState({
        warehouseId: '',
        stockType: 'FBS',
        quantity: 0,
        notes: ''
    });

    useEffect(() => {
        if (companyId) {
            loadWarehouses();
        }
    }, [companyId]);

    const loadWarehouses = async () => {
        try {
            setLoading(true);
            setError(null);
            const data = await warehouseService.getCompanyWarehouses(companyId);
            setWarehouses(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const resetForm = () => {
        setFormData({
            warehouseId: '',
            stockType: 'FBS',
            quantity: 0,
            notes: ''
        });
        setEditingIndex(null);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            setError(null);
            
            if (isEditMode && productId) {
                // В режиме редактирования используем API
                if (editingIndex !== null) {
                    // Обновление существующего остатка
                    const stock = productStocks[editingIndex];
                    await productStockService.updateStock(
                        productId,
                        stock.warehouseId,
                        formData.stockType,
                        parseInt(formData.quantity) || 0,
                        formData.notes
                    );
                } else {
                    // Создание нового остатка
                    await productStockService.createProductStock(
                        productId,
                        formData.warehouseId,
                        formData.stockType,
                        parseInt(formData.quantity) || 0,
                        formData.notes
                    );
                }
                
                // Перезагружаем остатки с сервера
                const updatedStocks = await productStockService.getProductStocks(productId);
                onStocksChange(updatedStocks);
            } else {
                // В режиме создания работаем с локальным состоянием
                const newStock = {
                    ...formData,
                    id: editingIndex !== null ? productStocks[editingIndex].id : null,
                    quantity: parseInt(formData.quantity) || 0
                };
                
                let updatedStocks;
                if (editingIndex !== null) {
                    // Редактирование существующего остатка
                    updatedStocks = [...productStocks];
                    updatedStocks[editingIndex] = newStock;
                } else {
                    // Добавление нового остатка
                    updatedStocks = [...productStocks, newStock];
                }
                
                onStocksChange(updatedStocks);
            }
            
            setShowAddForm(false);
            resetForm();
        } catch (err) {
            setError(err.message);
        }
    };

    const handleEdit = (index) => {
        const stock = productStocks[index];
        setEditingIndex(index);
        setFormData({
            warehouseId: stock.warehouseId || '',
            stockType: stock.stockType || 'FBS',
            quantity: stock.quantity || 0,
            notes: stock.notes || ''
        });
        setSelectedWarehouse(warehouses.find(w => w.id === stock.warehouseId) || null);
        setShowAddForm(true);
    };

    const handleDelete = async (index) => {
        if (!window.confirm('Вы уверены, что хотите удалить этот остаток?')) {
            return;
        }
        
        try {
            setError(null);
            
            if (isEditMode && productId) {
                // В режиме редактирования используем API
                const stock = productStocks[index];
                await productStockService.deleteProductStock(stock.id);
                
                // Перезагружаем остатки с сервера
                const updatedStocks = await productStockService.getProductStocks(productId);
                onStocksChange(updatedStocks);
            } else {
                // В режиме создания работаем с локальным состоянием
                const updatedStocks = productStocks.filter((_, i) => i !== index);
                onStocksChange(updatedStocks);
            }
        } catch (err) {
            setError(err.message);
        }
    };

    const getStockTypeLabel = (type) => {
        switch (type) {
            case 'FBS': return 'FBS - Склад продавца';
            case 'YANDEX_FBO': return 'YANDEX_FBO - Склад Yandex';
            case 'OZON_FBO': return 'OZON_FBO - Склад Ozon';
            default: return type;
        }
    };

    const getWarehouseName = (warehouseId) => {
        const warehouse = warehouses.find(w => w.id === warehouseId);
        return warehouse ? warehouse.name : 'Неизвестный склад';
    };

    const getWarehouseDisplay = (w) => {
        if (!w) return 'Не выбран';
        const marketName = w.marketplaceName || w.marketName || w.market?.name || w.market || '';
        const typeName = w.warehouseTypeName || w.warehouseType || '';
        const parts = [w.name, marketName && `(${marketName})`, typeName && `- ${typeName}`].filter(Boolean);
        return parts.join(' ');
    };

    const openWarehouseModal = () => {
        setShowWarehouseModal(true);
    };

    const closeWarehouseModal = () => {
        setShowWarehouseModal(false);
    };

    const handleSelectWarehouse = (warehouse) => {
        setSelectedWarehouse(warehouse);
        setFormData({ ...formData, warehouseId: warehouse.id });
        setShowWarehouseModal(false);
    };

    const validateForm = () => {
        if (!formData.warehouseId) {
            setError('Выберите склад');
            return false;
        }
        if (formData.quantity < 0) {
            setError('Количество не может быть отрицательным');
            return false;
        }
        return true;
    };

    const handleFormSubmit = (e) => {
        e.preventDefault();
        if (validateForm()) {
            handleSubmit(e);
        }
    };

    return (
        <div className="bg-gray-800 rounded-lg p-4">
            <div className="flex justify-between items-center mb-4">
                <h3 className="text-lg font-semibold text-white">Остатки товара</h3>
                <button
                    type="button"
                    onClick={() => {
                        resetForm();
                        setShowAddForm(true);
                    }}
                    className="px-3 py-1 bg-blue-600 text-white text-sm rounded hover:bg-blue-700 transition-colors"
                >
                    ➕ Добавить остаток
                </button>
            </div>

            {error && (
                <div className="mb-4 p-3 bg-red-900 border border-red-700 text-red-200 rounded text-sm">
                    {error}
                </div>
            )}

            {/* Форма добавления/редактирования */}
            {showAddForm && (
                <div className="mb-4 p-4 bg-gray-700 rounded-lg">
                    <h4 className="text-md font-medium text-white mb-3">
                        {editingIndex !== null ? 'Редактировать остаток' : 'Добавить новый остаток'}
                    </h4>
                    
                    <form onSubmit={handleFormSubmit} className="space-y-3">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                            <div>
                                <label className="block text-sm font-medium text-gray-300 mb-1">
                                    Склад *
                                </label>
                                <div className="flex items-center space-x-2">
                                    <input
                                        type="text"
                                        readOnly
                                        value={getWarehouseDisplay(selectedWarehouse) || (formData.warehouseId ? getWarehouseDisplay(warehouses.find(w=>w.id===formData.warehouseId)) : '')}
                                        placeholder="Не выбран"
                                        className="flex-1 px-2 py-1 bg-gray-600 border border-gray-500 rounded text-sm text-white placeholder-gray-400"
                                    />
                                    <button
                                        type="button"
                                        onClick={openWarehouseModal}
                                        className="px-2 py-1 bg-indigo-600 text-white text-sm rounded hover:bg-indigo-700"
                                    >
                                        Выбрать
                                    </button>
                                </div>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-300 mb-1">
                                    Тип остатка *
                                </label>
                                <select
                                    value={formData.stockType}
                                    onChange={(e) => setFormData({...formData, stockType: e.target.value})}
                                    className="w-full px-2 py-1 bg-gray-600 border border-gray-500 rounded text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500 focus:border-indigo-500 text-white"
                                >
                                    <option value="FBS">FBS - Склад продавца</option>
                                    <option value="YANDEX_FBO">YANDEX_FBO - Склад Yandex</option>
                                    <option value="OZON_FBO">OZON_FBO - Склад Ozon</option>
                                </select>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-300 mb-1">
                                    Количество *
                                </label>
                                <input
                                    type="number"
                                    required
                                    min="0"
                                    value={formData.quantity}
                                    onChange={(e) => setFormData({...formData, quantity: parseInt(e.target.value) || 0})}
                                    className="w-full px-2 py-1 bg-gray-600 border border-gray-500 rounded text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500 focus:border-indigo-500 text-white"
                                />
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-300 mb-1">
                                Примечания
                            </label>
                            <textarea
                                value={formData.notes}
                                onChange={(e) => setFormData({...formData, notes: e.target.value})}
                                rows="2"
                                className="w-full px-2 py-1 bg-gray-600 border border-gray-500 rounded text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500 focus:border-indigo-500 text-white"
                                placeholder="Дополнительные примечания"
                            />
                        </div>

                        <div className="flex justify-end space-x-2">
                            <button
                                type="button"
                                onClick={() => {
                                    setShowAddForm(false);
                                    resetForm();
                                }}
                                className="px-3 py-1 bg-gray-600 text-white text-sm rounded hover:bg-gray-700 transition-colors"
                            >
                                Отмена
                            </button>
                            <button
                                type="submit"
                                className="px-3 py-1 bg-blue-600 text-white text-sm rounded hover:bg-blue-700 transition-colors"
                            >
                                {editingIndex !== null ? 'Обновить' : 'Добавить'}
                            </button>
                        </div>
                    </form>
                </div>
            )}

            {/* Warehouse selection modal */}
            {showWarehouseModal && (
                <div className="fixed inset-0 z-50 flex items-center justify-center">
                    <div className="absolute inset-0 bg-black opacity-60" onClick={closeWarehouseModal}></div>
                    <div className="relative bg-gray-800 border border-gray-700 rounded-lg w-full max-w-2xl mx-4 p-4">
                        <div className="flex justify-between items-center mb-4">
                            <h3 className="text-white text-lg font-semibold">Выбор склада</h3>
                            <button onClick={closeWarehouseModal} className="text-gray-300 hover:text-white">✕</button>
                        </div>
                        <div className="max-h-80 overflow-y-auto space-y-2">
                            {warehouses.map(w => (
                                <button
                                    key={w.id}
                                    type="button"
                                    onClick={() => handleSelectWarehouse(w)}
                                    className="w-full text-left p-3 bg-gray-700 hover:bg-gray-600 rounded border border-gray-600"
                                >
                                    <div className="text-white text-sm font-medium">{w.name}</div>
                                    <div className="text-gray-300 text-xs">
                                        {getWarehouseDisplay(w)}
                                    </div>
                                </button>
                            ))}
                            {warehouses.length === 0 && (
                                <div className="text-gray-400 text-sm">Нет доступных складов</div>
                            )}
                        </div>
                        <div className="mt-4 flex justify-end">
                            <button onClick={closeWarehouseModal} className="px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-700">Закрыть</button>
                        </div>
                    </div>
                </div>
            )}

            {/* Список остатков */}
            <div className="space-y-2">
                {productStocks.map((stock, index) => (
                    <div
                        key={index}
                        className="bg-gray-700 rounded p-3 border border-gray-600"
                    >
                        <div className="flex justify-between items-start mb-2">
                            <div>
                                <h4 className="text-white font-medium text-sm">
                                    {getWarehouseName(stock.warehouseId)}
                                </h4>
                                <p className="text-gray-400 text-xs">
                                    {getStockTypeLabel(stock.stockType)}
                                </p>
                            </div>
                            <div className="flex space-x-1">
                                <button
                                    type="button"
                                    onClick={() => handleEdit(index)}
                                    className="px-2 py-1 bg-yellow-600 text-white text-xs rounded hover:bg-yellow-700"
                                    title="Редактировать"
                                >
                                    ✏️
                                </button>
                                <button
                                    type="button"
                                    onClick={() => handleDelete(index)}
                                    className="px-2 py-1 bg-red-600 text-white text-xs rounded hover:bg-red-700"
                                    title="Удалить"
                                >
                                    🗑️
                                </button>
                            </div>
                        </div>

                        <div className="grid grid-cols-2 gap-2 text-xs">
                            <div>
                                <span className="text-gray-400">Количество:</span>
                                <div className="text-white font-semibold">{stock.quantity}</div>
                            </div>
                            
                            {stock.notes && (
                                <div className="col-span-2">
                                    <span className="text-gray-400">Примечания:</span>
                                    <div className="text-white text-xs">{stock.notes}</div>
                                </div>
                            )}
                        </div>
                    </div>
                ))}
            </div>

            {productStocks.length === 0 && !loading && (
                <div className="text-center py-4">
                    <div className="text-gray-400 text-sm mb-2">Остатки не добавлены</div>
                    <button
                        type="button"
                        onClick={() => {
                            resetForm();
                            setShowAddForm(true);
                        }}
                        className="px-3 py-1 bg-blue-600 text-white text-sm rounded hover:bg-blue-700 transition-colors"
                    >
                        Добавить первый остаток
                    </button>
                </div>
            )}

            {loading && (
                <div className="text-center py-4">
                    <div className="text-gray-400 text-sm">Загрузка складов...</div>
                </div>
            )}
        </div>
    );
};

export default ProductStockEditor;
