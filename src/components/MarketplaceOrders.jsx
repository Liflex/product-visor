import React, { useState, useEffect } from 'react';
import { orderService } from '../services/orderService.js';
import LoadingSpinner from './ui/loading-spinner.jsx';
import ErrorMessage from './ui/error-message.jsx';
import OrdersTable from './OrdersTable.jsx';

const MarketplaceOrders = ({ 
  marketplace, 
  marketplaceService, 
  marketplaceName, 
  marketplaceIcon,
  syncEndpoint,
  backfillEndpoint 
}) => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');
  const [page, setPage] = useState(0);
  const [size] = useState(50);
  const [pagination, setPagination] = useState({
    totalElements: 0,
    totalPages: 0,
    currentPage: 0,
    size: 50
  });
  const [syncStatus, setSyncStatus] = useState(null);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await orderService.getOrdersByMarket(marketplace, page, size, selectedStatus || null, dateFrom || null, dateTo || null);
      
      // Отладочная информация
      console.log(`${marketplaceName} API Response:`, response);
      
      // Обработка данных Spring Page
      let ordersData = [];
      if (response && response.content && Array.isArray(response.content)) {
        ordersData = response.content;
        setPagination({
          totalElements: response.totalElements || 0,
          totalPages: response.totalPages || 0,
          currentPage: response.number || 0,
          size: response.size || 50
        });
        console.log(`✅ Loaded ${ordersData.length} ${marketplaceName} orders from page ${response.number + 1}/${response.totalPages}`);
      } else {
        console.warn('❌ Unexpected response format:', response);
        ordersData = [];
        setPagination({
          totalElements: 0,
          totalPages: 0,
          currentPage: 0,
          size: 50
        });
      }
      
      setOrders(ordersData);
    } catch (err) {
      setError(err.message || `Ошибка при загрузке заказов ${marketplaceName}`);
      console.error(`Error loading ${marketplaceName} orders:`, err);
    } finally {
      setLoading(false);
    }
  };

  const backfill = async () => {
    if (!from || !to) {
      setError('Пожалуйста, укажите даты для загрузки');
      return;
    }

    setLoading(true);
    setError('');
    try {
      if (marketplaceService && marketplaceService.backfillFboOrders) {
        await marketplaceService.backfillFboOrders({ from, to }, 100);
      } else if (backfillEndpoint) {
        const response = await fetch(backfillEndpoint, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ from, to })
        });
        if (!response.ok) {
          throw new Error('Ошибка при загрузке данных');
        }
      }
      await load();
      await loadSyncStatus();
    } catch (error) {
      setError(error.message || `Ошибка при загрузке данных из ${marketplaceName}`);
      console.error('Error during backfill:', error);
    } finally {
      setLoading(false);
    }
  };

  const forceSync = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await fetch(syncEndpoint || `/api/${marketplace.toLowerCase()}/sync/force`, { method: 'POST' });
      if (response.ok) {
        await loadSyncStatus();
        setError('');
      } else {
        const errorData = await response.json();
        setError(errorData.message || 'Ошибка при принудительной синхронизации');
      }
    } catch (error) {
      setError('Ошибка при принудительной синхронизации');
    } finally {
      setLoading(false);
    }
  };

  const loadSyncStatus = async () => {
    try {
      const response = await fetch(`/api/${marketplace.toLowerCase()}/sync/status`);
      if (response.ok) {
        const status = await response.json();
        setSyncStatus(status);
      }
    } catch (error) {
      console.error('Error loading sync status:', error);
    }
  };

  useEffect(() => {
    load();
    loadSyncStatus();
  }, [page, selectedStatus, dateFrom, dateTo]);

  const handlePageChange = (newPage) => {
    setPage(newPage);
  };

  const handleStatusChange = (status) => {
    setSelectedStatus(status);
    setPage(0);
  };

  const handleDateFromChange = (date) => {
    setDateFrom(date);
    setPage(0);
  };

  const handleDateToChange = (date) => {
    setDateTo(date);
    setPage(0);
  };

  const clearFilters = () => {
    setSelectedStatus('');
    setDateFrom('');
    setDateTo('');
    setPage(0);
  };

  return (
    <div className="min-h-screen bg-gray-900 py-8">
      <div className="max-w-7xl mx-auto px-4">
        <div className="bg-gray-800 rounded-lg shadow-lg border border-gray-700 p-6">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center space-x-3">
              <span className="text-2xl">{marketplaceIcon}</span>
              <h1 className="text-2xl font-bold text-white">{marketplaceName} Заказы</h1>
            </div>
            <div className="flex space-x-3">
              <button
                onClick={forceSync}
                disabled={loading}
                className="px-4 py-2 bg-blue-600 hover:bg-blue-700 disabled:bg-gray-600 text-white rounded-md font-medium transition-colors"
              >
                {loading ? 'Синхронизация...' : 'Принудительная синхронизация'}
              </button>
            </div>
          </div>

          {/* Sync Status */}
          {syncStatus && (
            <div className="mb-6 p-4 bg-gray-700 rounded-lg">
              <h3 className="text-lg font-semibold text-white mb-2">Статус синхронизации</h3>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
                <div>
                  <span className="text-gray-400">Последняя синхронизация:</span>
                  <p className="text-white">{syncStatus.lastSyncTime || 'Не синхронизировалось'}</p>
                </div>
                <div>
                  <span className="text-gray-400">Статус:</span>
                  <p className={`font-medium ${syncStatus.isRunning ? 'text-yellow-400' : 'text-green-400'}`}>
                    {syncStatus.isRunning ? 'Выполняется' : 'Ожидает'}
                  </p>
                </div>
                <div>
                  <span className="text-gray-400">Обработано заказов:</span>
                  <p className="text-white">{syncStatus.processedOrders || 0}</p>
                </div>
              </div>
            </div>
          )}

          {/* Backfill Section */}
          <div className="mb-6 p-4 bg-gray-700 rounded-lg">
            <h3 className="text-lg font-semibold text-white mb-4">Загрузка исторических данных</h3>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">Дата начала</label>
                <input
                  type="date"
                  value={from}
                  onChange={(e) => setFrom(e.target.value)}
                  className="w-full px-3 py-2 bg-gray-600 border border-gray-500 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-white"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">Дата окончания</label>
                <input
                  type="date"
                  value={to}
                  onChange={(e) => setTo(e.target.value)}
                  className="w-full px-3 py-2 bg-gray-600 border border-gray-500 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-white"
                />
              </div>
              <div className="flex items-end">
                <button
                  onClick={backfill}
                  disabled={loading || !from || !to}
                  className="w-full px-4 py-2 bg-green-600 hover:bg-green-700 disabled:bg-gray-600 disabled:cursor-not-allowed text-white rounded-md font-medium transition-colors"
                >
                  {loading ? 'Загрузка...' : 'Загрузить данные'}
                </button>
              </div>
            </div>
          </div>

          {/* Filters */}
          <div className="mb-6 p-4 bg-gray-700 rounded-lg">
            <h3 className="text-lg font-semibold text-white mb-4">Фильтры</h3>
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">Статус заказа</label>
                <select
                  value={selectedStatus}
                  onChange={(e) => handleStatusChange(e.target.value)}
                  className="w-full px-3 py-2 bg-gray-600 border border-gray-500 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-white"
                >
                  <option value="">Все статусы</option>
                  <option value="AWAITING_PACKAGING">Ожидает упаковки</option>
                  <option value="AWAITING_DELIVER">Ожидает доставки</option>
                  <option value="DELIVERING">Доставляется</option>
                  <option value="DELIVERED">Доставлен</option>
                  <option value="CANCELLED">Отменен</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">Дата от</label>
                <input
                  type="date"
                  value={dateFrom}
                  onChange={(e) => handleDateFromChange(e.target.value)}
                  className="w-full px-3 py-2 bg-gray-600 border border-gray-500 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-white"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">Дата до</label>
                <input
                  type="date"
                  value={dateTo}
                  onChange={(e) => handleDateToChange(e.target.value)}
                  className="w-full px-3 py-2 bg-gray-600 border border-gray-500 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-white"
                />
              </div>
              <div className="flex items-end">
                <button
                  onClick={clearFilters}
                  className="w-full px-4 py-2 bg-gray-600 hover:bg-gray-700 text-white rounded-md font-medium transition-colors"
                >
                  Очистить фильтры
                </button>
              </div>
            </div>
          </div>

          {/* Error Message */}
          {error && <ErrorMessage message={error} />}

          {/* Loading Spinner */}
          {loading && <LoadingSpinner />}

          {/* Orders Table */}
          {!loading && (
            <OrdersTable
              orders={orders}
              pagination={pagination}
              onPageChange={handlePageChange}
            />
          )}
        </div>
      </div>
    </div>
  );
};

export default MarketplaceOrders;

