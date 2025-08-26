import React, { useState, useEffect } from 'react';
import { warehouseService } from '../services/warehouseService.js';
import { productStockService } from '../services/productStockService.js';
import { useAuth } from '../contexts/AuthContext.jsx';
import InputField from './ui/input-field.jsx';
import SelectField from './ui/select-field.jsx';
import LoadingSpinner from './ui/loading-spinner.jsx';
import ErrorMessage from './ui/error-message.jsx';

const ProductStockManagement = ({ productId, productName }) => {
    const { companyId } = useAuth();
    const [warehouses, setWarehouses] = useState([]);
    const [productStocks, setProductStocks] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [showAddForm, setShowAddForm] = useState(false);
    const [editingIndex, setEditingIndex] = useState(null);
    const [formData, setFormData] = useState({
        selectedWarehouses: [],
        stockType: 'FBS',
        quantity: 0,
        notes: ''
    });

    useEffect(() => {
        if (productId && companyId) {
            loadData();
        }
    }, [productId, companyId]);

    const loadData = async () => {
        try {
            setLoading(true);
            setError(null);
            
            // Загружаем склады и остатки параллельно
            const [warehousesData, stocksData] = await Promise.all([
                warehouseService.getCompanyWarehouses(companyId),
                productStockService.getProductStocks(productId)
            ]);
            
            setWarehouses(warehousesData);
            setProductStocks(stocksData);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const resetForm = () => {
        setFormData({
            selectedWarehouses: [],
            stockType: 'FBS',
            quantity: 0,
            notes: ''
        });
        setEditingIndex(null);
        setShowAddForm(false);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            setError(null);
            
            // Преобразуем ID складов в объекты WarehouseDto
            const warehouseDtos = formData.selectedWarehouses.map(warehouseId => ({
                id: warehouseId
            }));
            
            if (editingIndex !== null) {
                // Обновление существующего остатка
                const stock = productStocks[editingIndex];
                const stockData = {
                    productId: productId,
                    warehouses: warehouseDtos,
                    stockType: formData.stockType,
                    quantity: parseInt(formData.quantity) || 0,
                    notes: formData.notes
                };
                await productStockService.updateProductStock(stock.id, stockData);
            } else {
                // Создание нового остатка
                const stockData = {
                    productId: productId,
                    warehouses: warehouseDtos,
                    stockType: formData.stockType,
                    quantity: parseInt(formData.quantity) || 0,
                    notes: formData.notes
                };
                await productStockService.createProductStock(stockData);
            }
            
            // Перезагружаем остатки
            await loadData();
            resetForm();
        } catch (err) {
            setError(err.message);
        }
    };

    const handleEdit = (index) => {
        const stock = productStocks[index];
        setFormData({
            selectedWarehouses: stock.warehouses ? stock.warehouses.map(w => w.id) : [],
            stockType: stock.stockType || 'FBS',
            quantity: stock.quantity || 0,
            notes: stock.notes || ''
        });
        setEditingIndex(index);
        setShowAddForm(true);
    };

    const handleDelete = async (stockId) => {
        if (window.confirm('Вы уверены, что хотите удалить этот остаток?')) {
            try {
                setError(null);
                await productStockService.deleteProductStock(stockId);
                await loadData();
            } catch (err) {
                setError(err.message);
            }
        }
    };

    const handleWarehouseToggle = (warehouseId) => {
        const isSelected = formData.selectedWarehouses.includes(warehouseId);
        if (isSelected) {
            setFormData({
                ...formData,
                selectedWarehouses: formData.selectedWarehouses.filter(id => id !== warehouseId)
            });
        } else {
            setFormData({
                ...formData,
                selectedWarehouses: [...formData.selectedWarehouses, warehouseId]
            });
        }
    };

    const getWarehouseNames = (warehouseIds) => {
        if (!warehouseIds || warehouseIds.length === 0) return 'Неизвестный склад';
        return warehouseIds.map(id => {
            const warehouse = warehouses.find(w => w.id === id);
            return warehouse ? warehouse.name : 'Неизвестный склад';
        }).join(', ');
    };

    const stockTypeOptions = [
        { value: 'FBS', label: 'FBS (Склад продавца)' },
        { value: 'FBO', label: 'FBO (Склад маркетплейса)' }
    ];

    if (loading) {
        return <LoadingSpinner />;
    }

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h2 className="text-xl font-semibold text-white">
                    Управление остатками товара: {productName}
                </h2>
                <button
                    type="button"
                    onClick={() => setShowAddForm(true)}
                    className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition-colors"
                >
                    Добавить остаток
                </button>
            </div>

            {error && <ErrorMessage message={error} />}

            {/* Форма добавления/редактирования */}
            {showAddForm && (
                <div className="bg-gray-800 p-6 rounded-lg border border-gray-700">
                    <h3 className="text-lg font-medium text-white mb-4">
                        {editingIndex !== null ? 'Редактировать остаток' : 'Добавить остаток'}
                    </h3>
                    
                    <form onSubmit={handleSubmit} className="space-y-4">
                        {/* Выбор складов */}
                        <div>
                            <label className="block text-sm font-medium text-gray-300 mb-2">
                                Склады *
                            </label>
                            <div className="space-y-2 max-h-40 overflow-y-auto border border-gray-600 rounded-md p-3">
                                {warehouses.length === 0 ? (
                                    <p className="text-gray-400 text-sm">Нет доступных складов</p>
                                ) : (
                                    warehouses.map(warehouse => (
                                        <label key={warehouse.id} className="flex items-center space-x-2">
                                            <input
                                                type="checkbox"
                                                checked={formData.selectedWarehouses.includes(warehouse.id)}
                                                onChange={() => handleWarehouseToggle(warehouse.id)}
                                                className="rounded border-gray-600 text-indigo-600 focus:ring-indigo-500"
                                            />
                                            <span className="text-sm text-white">{warehouse.name}</span>
                                        </label>
                                    ))
                                )}
                            </div>
                            {formData.selectedWarehouses.length === 0 && (
                                <p className="text-red-400 text-sm mt-1">Выберите хотя бы один склад</p>
                            )}
                        </div>

                        <SelectField
                            label="Тип остатка"
                            placeholder="Выберите тип"
                            options={stockTypeOptions}
                            required
                            value={formData.stockType}
                            onChange={(e) => setFormData({...formData, stockType: e.target.value})}
                            error=""
                            hasError={false}
                        />

                        <InputField
                            label="Количество"
                            type="number"
                            value={formData.quantity}
                            onChange={(e) => setFormData({...formData, quantity: e.target.value})}
                            error=""
                            hasError={false}
                        />

                        <InputField
                            label="Примечания"
                            type="text"
                            value={formData.notes}
                            onChange={(e) => setFormData({...formData, notes: e.target.value})}
                            error=""
                            hasError={false}
                        />

                        <div className="flex space-x-4">
                            <button
                                type="submit"
                                disabled={formData.selectedWarehouses.length === 0}
                                className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                            >
                                {editingIndex !== null ? 'Обновить' : 'Добавить'}
                            </button>
                            <button
                                type="button"
                                onClick={resetForm}
                                className="px-4 py-2 border border-gray-600 text-gray-300 rounded-md hover:bg-gray-700 transition-colors"
                            >
                                Отмена
                            </button>
                        </div>
                    </form>
                </div>
            )}

            {/* Список остатков */}
            <div className="bg-gray-800 rounded-lg border border-gray-700">
                <div className="px-6 py-4 border-b border-gray-700">
                    <h3 className="text-lg font-medium text-white">Текущие остатки</h3>
                </div>
                
                {productStocks.length === 0 ? (
                    <div className="p-6 text-center text-gray-400">
                        Остатки не найдены. Добавьте первый остаток.
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="min-w-full divide-y divide-gray-700">
                            <thead className="bg-gray-700">
                                <tr>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                                        Склады
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                                        Тип
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                                        Количество
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                                        Статус синхронизации
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                                        Примечания
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                                        Действия
                                    </th>
                                </tr>
                            </thead>
                            <tbody className="bg-gray-800 divide-y divide-gray-700">
                                {productStocks.map((stock, index) => {
                                    const warehouseIds = stock.warehouses ? stock.warehouses.map(w => w.id) : [];
                                    return (
                                        <tr key={stock.id || index}>
                                            <td className="px-6 py-4 text-sm text-white">
                                                <div className="space-y-1">
                                                    {warehouseIds.length > 0 ? (
                                                        warehouseIds.map(warehouseId => {
                                                            const warehouse = warehouses.find(w => w.id === warehouseId);
                                                            return (
                                                                <div key={warehouseId} className="text-sm text-white">
                                                                    {warehouse ? warehouse.name : 'Неизвестный склад'}
                                                                </div>
                                                            );
                                                        })
                                                    ) : (
                                                        <span className="text-gray-400">Склады не назначены</span>
                                                    )}
                                                </div>
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap text-sm text-white">
                                                {stock.stockType}
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap text-sm text-white">
                                                {stock.quantity}
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap text-sm text-white">
                                                <span className={`px-2 py-1 rounded-full text-xs ${
                                                    stock.syncStatus === 'SYNCED' ? 'bg-green-100 text-green-800' :
                                                    stock.syncStatus === 'SYNCING' ? 'bg-yellow-100 text-yellow-800' :
                                                    'bg-gray-100 text-gray-800'
                                                }`}>
                                                    {stock.syncStatus || 'NEVER_SYNCED'}
                                                </span>
                                            </td>
                                            <td className="px-6 py-4 text-sm text-gray-300">
                                                {stock.notes || '-'}
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                                <button
                                                    onClick={() => handleEdit(index)}
                                                    className="text-indigo-400 hover:text-indigo-300 mr-4"
                                                >
                                                    Редактировать
                                                </button>
                                                <button
                                                    onClick={() => handleDelete(stock.id)}
                                                    className="text-red-400 hover:text-red-300"
                                                >
                                                    Удалить
                                                </button>
                                            </td>
                                        </tr>
                                    );
                                })}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
};

export default ProductStockManagement;
