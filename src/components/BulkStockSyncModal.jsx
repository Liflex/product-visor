import React, { useState } from 'react';
import { bulkStockSyncService } from '../services/bulkStockSyncService.js';

const BulkStockSyncModal = ({ productIds, onClose }) => {
    const [selectedStockType, setSelectedStockType] = useState('FBS');
    const [isLoading, setIsLoading] = useState(false);
    const [syncStats, setSyncStats] = useState(null);
    const [error, setError] = useState(null);

    const stockTypes = [
        { value: 'FBS', label: 'FBS - Склад продавца' },
        { value: 'YANDEX_FBO', label: 'YANDEX_FBO - Склад Yandex' },
        { value: 'OZON_FBO', label: 'OZON_FBO - Склад Ozon' }
    ];

    const handleSync = async () => {
        if (productIds.length === 0) {
            setError('Не выбрано ни одного товара');
            return;
        }

        setIsLoading(true);
        setError(null);
        setSyncStats(null);

        try {
            const stats = await bulkStockSyncService.bulkSyncStocks(productIds, selectedStockType);
            setSyncStats(stats);
        } catch (err) {
            setError(err.message || 'Ошибка при синхронизации');
        } finally {
            setIsLoading(false);
        }
    };

    const handleClose = () => {
        setSyncStats(null);
        setError(null);
        onClose();
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-gray-800 rounded-lg p-6 w-full max-w-md mx-4">
                <div className="flex justify-between items-center mb-4">
                    <h2 className="text-xl font-bold text-white">
                        Массовая синхронизация остатков
                    </h2>
                    <button
                        onClick={handleClose}
                        className="text-gray-400 hover:text-white"
                    >
                        ✕
                    </button>
                </div>

                {error && (
                    <div className="mb-4 p-3 bg-red-900 border border-red-700 text-red-200 rounded">
                        {error}
                    </div>
                )}

                {syncStats && (
                    <div className="mb-4 p-3 bg-green-900 border border-green-700 text-green-200 rounded">
                        <h3 className="font-semibold mb-2">Результаты синхронизации:</h3>
                        <div className="space-y-1 text-sm">
                            <div>Всего товаров: {syncStats.totalProducts}</div>
                            <div>Успешно синхронизировано: {syncStats.syncedProducts}</div>
                            <div>Ошибок: {syncStats.errors}</div>
                        </div>
                    </div>
                )}

                <div className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-300 mb-2">
                            Выбрано товаров: {productIds.length}
                        </label>
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-300 mb-2">
                            Тип остатков:
                        </label>
                        <select
                            value={selectedStockType}
                            onChange={(e) => setSelectedStockType(e.target.value)}
                            className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-white"
                        >
                            {stockTypes.map(type => (
                                <option key={type.value} value={type.value}>
                                    {type.label}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className="flex justify-end space-x-3 pt-4">
                        <button
                            onClick={handleClose}
                            className="px-4 py-2 bg-gray-600 hover:bg-gray-700 text-white rounded-md font-medium transition-colors"
                        >
                            Отмена
                        </button>
                        <button
                            onClick={handleSync}
                            disabled={isLoading || productIds.length === 0}
                            className={`px-4 py-2 rounded-md font-medium transition-colors ${
                                isLoading || productIds.length === 0
                                    ? 'bg-gray-600 cursor-not-allowed'
                                    : 'bg-blue-600 hover:bg-blue-700'
                            } text-white`}
                        >
                            {isLoading ? 'Синхронизация...' : 'Синхронизировать'}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default BulkStockSyncModal;
