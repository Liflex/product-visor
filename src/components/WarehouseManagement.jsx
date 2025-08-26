import React, { useState, useEffect } from 'react';
import { warehouseService } from '../services/warehouseService.js';
import { useAuth } from '../contexts/AuthContext.jsx';

const WarehouseManagement = () => {
    const { companyId } = useAuth();
    const [warehouses, setWarehouses] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showCreateForm, setShowCreateForm] = useState(false);
    const [editingWarehouse, setEditingWarehouse] = useState(null);
    const [formData, setFormData] = useState({
        name: '',
        description: '',
        warehouseType: 'FBS',
        externalWarehouseId: '',
        isHomeWarehouse: false,
        isActive: true,
        notes: '',
        marketplace: ''
    });
    const [showMarketplaceModal, setShowMarketplaceModal] = useState(false);

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
            name: '',
            description: '',
            warehouseType: 'FBS',
            externalWarehouseId: '',
            isHomeWarehouse: false,
            isActive: true,
            notes: '',
            marketplace: ''
        });
        setEditingWarehouse(null);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            setError(null);
            
            const payload = {
                ...formData,
                companyId
            };
            if (editingWarehouse) {
                await warehouseService.updateWarehouse(editingWarehouse.id, payload);
            } else {
                await warehouseService.createWarehouse(payload);
            }
            
            await loadWarehouses();
            setShowCreateForm(false);
            resetForm();
        } catch (err) {
            setError(err.message);
        }
    };

    const handleEdit = (warehouse) => {
        setEditingWarehouse(warehouse);
        setFormData({
            name: warehouse.name || '',
            description: warehouse.description || '',
            warehouseType: warehouse.warehouseType || 'FBS',
            externalWarehouseId: warehouse.externalWarehouseId || '',
            isHomeWarehouse: warehouse.isHomeWarehouse || false,
            isActive: (warehouse.isActive !== undefined ? warehouse.isActive : true),
            notes: warehouse.notes || '',
            marketplace: warehouse.marketplace || ''
        });
        setShowCreateForm(true);
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Вы уверены, что хотите удалить этот склад?')) {
            return;
        }
        
        try {
            setError(null);
            await warehouseService.deleteWarehouse(id);
            await loadWarehouses();
        } catch (err) {
            setError(err.message);
        }
    };

    const getWarehouseTypeLabel = (type) => {
        switch (type) {
            case 'FBS': return 'FBS - Склад продавца';
            case 'FBO': return 'FBO - Склад маркетплейса';
            default: return type;
        }
    };

    // Marketplace хранится в Warehouse модели (перечисление common-core), добавим поле

    if (loading) {
        return (
            <div className="min-h-screen bg-gray-900 flex items-center justify-center">
                <div className="text-white text-lg">Загрузка складов...</div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-900 p-6">
            <div className="max-w-6xl mx-auto">
                <div className="flex justify-between items-center mb-6">
                    <h1 className="text-3xl font-bold text-white">Управление складами</h1>
                    <button
                        onClick={() => {
                            resetForm();
                            setShowCreateForm(true);
                        }}
                        className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
                    >
                        ➕ Добавить склад
                    </button>
                </div>

                {error && (
                    <div className="mb-4 p-4 bg-red-900 border border-red-700 text-red-200 rounded">
                        {error}
                    </div>
                )}

                {/* Форма создания/редактирования */}
                {showCreateForm && (
                    <div className="mb-6 p-6 bg-gray-800 rounded-lg">
                        <h2 className="text-xl font-semibold text-white mb-4">
                            {editingWarehouse ? 'Редактировать склад' : 'Создать новый склад'}
                        </h2>
                        
                        <form onSubmit={handleSubmit} className="space-y-4">
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-sm font-medium text-gray-300 mb-2">
                                        Название склада *
                                    </label>
                                    <input
                                        type="text"
                                        required
                                        value={formData.name}
                                        onChange={(e) => setFormData({...formData, name: e.target.value})}
                                        className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-white"
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-300 mb-2">
                                        Тип склада *
                                    </label>
                                    <select
                                        value={formData.warehouseType}
                                        onChange={(e) => setFormData({...formData, warehouseType: e.target.value})}
                                        className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-white"
                                    >
                                        <option value="FBS">FBS - Склад продавца</option>
                                        <option value="FBO">FBO - Склад маркетплейса</option>
                                    </select>
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-300 mb-2">
                                        Маркетплейс
                                    </label>
                                    <div className="flex items-center space-x-2">
                                        <input
                                            type="text"
                                            readOnly
                                            value={formData.marketplace || 'Не указан'}
                                            className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-md text-white"
                                        />
                                        <button
                                            type="button"
                                            onClick={() => setShowMarketplaceModal(true)}
                                            className="px-3 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700"
                                        >
                                            Выбрать
                                        </button>
                                    </div>
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-300 mb-2">
                                        Внешний ID склада
                                    </label>
                                    <input
                                        type="text"
                                        value={formData.externalWarehouseId}
                                        onChange={(e) => setFormData({...formData, externalWarehouseId: e.target.value})}
                                        className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-white"
                                        placeholder="ID склада в маркетплейсе"
                                    />
                                </div>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-300 mb-2">
                                    Описание
                                </label>
                                <textarea
                                    value={formData.description}
                                    onChange={(e) => setFormData({...formData, description: e.target.value})}
                                    rows="3"
                                    className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-white"
                                    placeholder="Описание склада"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-300 mb-2">
                                    Примечания
                                </label>
                                <textarea
                                    value={formData.notes}
                                    onChange={(e) => setFormData({...formData, notes: e.target.value})}
                                    rows="2"
                                    className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-white"
                                    placeholder="Дополнительные примечания"
                                />
                            </div>

                            <div className="flex items-center space-x-6">
                                <label className="flex items-center">
                                    <input
                                        type="checkbox"
                                        checked={formData.isHomeWarehouse}
                                        onChange={(e) => setFormData({...formData, isHomeWarehouse: e.target.checked})}
                                        className="mr-2"
                                    />
                                    <span className="text-gray-300">Домашний склад</span>
                                </label>

                                <label className="flex items-center">
                                    <input
                                        type="checkbox"
                                        checked={formData.isActive}
                                        onChange={(e) => setFormData({...formData, isActive: e.target.checked})}
                                        className="mr-2"
                                    />
                                    <span className="text-gray-300">Активный</span>
                                </label>
                            </div>

                            <div className="flex justify-end space-x-3">
                                <button
                                    type="button"
                                    onClick={() => {
                                        setShowCreateForm(false);
                                        resetForm();
                                    }}
                                    className="px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700 transition-colors"
                                >
                                    Отмена
                                </button>
                                <button
                                    type="submit"
                                    className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
                                >
                                    {editingWarehouse ? 'Обновить' : 'Создать'}
                                </button>
                            </div>
                        </form>
                    </div>
                )}

                {/* Список складов */}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {(Array.isArray(warehouses) ? warehouses : []).map((warehouse) => (
                        <div
                            key={warehouse.id}
                            className={`bg-gray-800 rounded-lg p-6 border ${
                                warehouse.isHomeWarehouse ? 'border-indigo-400' : 'border-gray-700'
                            }`}
                        >
                            <div className="flex justify-between items-start mb-4">
                                <div>
                                    <h3 className="text-lg font-semibold text-white mb-1">
                                        {warehouse.name}
                                        {warehouse.isHomeWarehouse && (
                                            <span className="ml-2 px-2 py-1 bg-indigo-600 text-white text-xs rounded">
                                                Домашний
                                            </span>
                                        )}
                                    </h3>
                                    <p className="text-gray-400 text-sm flex items-center space-x-2">
                                        <span>{getWarehouseTypeLabel(warehouse.warehouseType)}</span>
                                        {warehouse.marketplace && (
                                            <span className="px-2 py-0.5 bg-gray-700 text-gray-200 text-xs rounded border border-gray-600">
                                                {warehouse.marketplace}
                                            </span>
                                        )}
                                    </p>
                                </div>
                                <div className="flex space-x-2">
                                    <button
                                        onClick={() => handleEdit(warehouse)}
                                        className="px-2 py-1 bg-yellow-600 text-white text-xs rounded hover:bg-yellow-700"
                                        title="Редактировать"
                                    >
                                        ✏️
                                    </button>
                                    <button
                                        onClick={() => handleDelete(warehouse.id)}
                                        className="px-2 py-1 bg-red-600 text-white text-xs rounded hover:bg-red-700"
                                        title="Удалить"
                                    >
                                        🗑️
                                    </button>
                                </div>
                            </div>

                            <div className="space-y-2 text-sm">
                                {warehouse.marketplace && (
                                    <div className="flex justify-between">
                                        <span className="text-gray-400">Маркетплейс:</span>
                                        <span className="text-white">{warehouse.marketplace}</span>
                                    </div>
                                )}
                                {warehouse.externalWarehouseId && (
                                    <div className="flex justify-between">
                                        <span className="text-gray-400">Внешний ID:</span>
                                        <span className="text-white font-mono">{warehouse.externalWarehouseId}</span>
                                    </div>
                                )}

                                <div className="flex justify-between">
                                    <span className="text-gray-400">Статус:</span>
                                    <span className={`px-2 py-1 rounded text-xs ${
                                        warehouse.isActive 
                                            ? 'bg-green-600 text-white' 
                                            : 'bg-red-600 text-white'
                                    }`}>
                                        {warehouse.isActive ? 'Активный' : 'Неактивный'}
                                    </span>
                                </div>

                                {warehouse.description && (
                                    <div className="mt-3 p-2 bg-gray-700 rounded text-gray-300 text-xs">
                                        {warehouse.description}
                                    </div>
                                )}

                                {warehouse.notes && (
                                    <div className="mt-2 p-2 bg-gray-700 rounded text-gray-400 text-xs">
                                        <strong>Примечания:</strong> {warehouse.notes}
                                    </div>
                                )}
                            </div>
                        </div>
                    ))}
                </div>

                {warehouses.length === 0 && !loading && (
                    <div className="text-center py-12">
                        <div className="text-gray-400 text-lg mb-4">Склады не найдены</div>
                        <button
                            onClick={() => {
                                resetForm();
                                setShowCreateForm(true);
                            }}
                            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
                        >
                            Создать первый склад
                        </button>
                    </div>
                )}
            </div>

            {showMarketplaceModal && (
                <div className="fixed inset-0 z-50 flex items-center justify-center">
                    <div className="absolute inset-0 bg-black opacity-60" onClick={() => setShowMarketplaceModal(false)}></div>
                    <div className="relative bg-gray-800 border border-gray-700 rounded-lg w-full max-w-md mx-4 p-4">
                        <div className="flex justify-between items-center mb-4">
                            <h3 className="text-white text-lg font-semibold">Выбор маркетплейса</h3>
                            <button onClick={() => setShowMarketplaceModal(false)} className="text-gray-300 hover:text-white">✕</button>
                        </div>
                        <div className="space-y-2">
                            {['OZON','YANDEX','WILDBERRIES','ALIEXPRESS',''].map(m => (
                                <button
                                    key={m || 'NONE'}
                                    type="button"
                                    onClick={() => { setFormData({...formData, marketplace: m}); setShowMarketplaceModal(false); }}
                                    className="w-full text-left p-3 bg-gray-700 hover:bg-gray-600 rounded border border-gray-600 text-white"
                                >
                                    {m || 'Не указан'}
                                </button>
                            ))}
                        </div>
                        <div className="mt-4 flex justify-end">
                            <button onClick={() => setShowMarketplaceModal(false)} className="px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-700">Закрыть</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default WarehouseManagement;
