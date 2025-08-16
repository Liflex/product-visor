import React, { useState, useEffect } from 'react';
import { orderService } from '../services/orderService.js';
import { ozonService } from '../services/ozonService.js';
import LoadingSpinner from './ui/loading-spinner.jsx';
import ErrorMessage from './ui/error-message.jsx';
import OrdersTable from './OrdersTable.jsx';

const OzonOrders = () => {
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
      const response = await orderService.getOrdersByMarket('OZON', page, size, selectedStatus || null, dateFrom || null, dateTo || null);
      
      // Отладочная информация
      console.log('Ozon API Response:', response);
      
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
        console.log(`✅ Loaded ${ordersData.length} Ozon orders from page ${response.number + 1}/${response.totalPages}`);
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
      setError(err.message || 'Ошибка при загрузке заказов Ozon');
      console.error('Error loading Ozon orders:', err);
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
      await ozonService.backfillFboOrders({ from, to }, 100);
      await load();
      await loadSyncStatus(); // Обновляем статус синхронизации
    } catch (error) {
      setError(error.message || 'Ошибка при загрузке данных из Ozon');
      console.error('Error during backfill:', error);
    } finally {
      setLoading(false);
    }
  };

  const forceSync = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await fetch('/api/ozon/sync/force', { method: 'POST' });
      if (response.ok) {
        await loadSyncStatus();
        setError(''); // Очищаем ошибки
      } else {
        const errorData = await response.json();
        setError(errorData.message || 'Ошибка при принудительной синхронизации');
      }
    } catch (error) {
      setError('Ошибка при принудительной синхронизации');
      console.error('Error during force sync:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    loadSyncStatus();
  }, [page, selectedStatus, dateFrom, dateTo]);

  const loadSyncStatus = async () => {
    try {
      const response = await fetch('/api/ozon/sync/status');
      if (response.ok) {
        const status = await response.json();
        setSyncStatus(status);
      }
    } catch (error) {
      console.error('Error loading sync status:', error);
    }
  };

  const statuses = [
    { value: '', label: 'Все статусы' },
    { value: 'COMPLETED', label: 'Завершен' },
    { value: 'CANCELLED', label: 'Отменен' },
    { value: 'PROCESSING', label: 'В обработке' },
    { value: 'SHIPPED', label: 'Отправлен' },
    { value: 'DELIVERED', label: 'Доставлен' }
  ];

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleString('ru-RU');
  };

  const handleStatusChange = (e) => {
    setSelectedStatus(e.target.value);
    setPage(0);
  };

  const handleDateFromChange = (e) => {
    setDateFrom(e.target.value);
    setPage(0);
  };

  const handleDateToChange = (e) => {
    setDateTo(e.target.value);
    setPage(0);
  };

  const handlePageChange = (newPage) => {
    setPage(newPage);
  };

  const clearFilters = () => {
    setSelectedStatus('');
    setDateFrom('');
    setDateTo('');
    setPage(0);
  };

  if (loading && orders.length === 0) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error} />;

  return (
    <div className="p-4">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h2 className="text-2xl font-bold text-white flex items-center">
            <span className="mr-2">🛒</span>
            Заказы Ozon
          </h2>
          {syncStatus && (
            <div className="mt-2 space-y-2">
              {/* Статус синхронизации */}
              <div className="flex items-center space-x-2">
                <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                  syncStatus.status === 'SUCCESS' ? 'bg-green-100 text-green-800' :
                  syncStatus.status === 'FAILED' ? 'bg-red-100 text-red-800' :
                  syncStatus.status === 'IN_PROGRESS' ? 'bg-blue-100 text-blue-800' :
                  syncStatus.status === 'NEVER_SYNCED' ? 'bg-yellow-100 text-yellow-800' :
                  'bg-gray-100 text-gray-800'
                }`}>
                  {syncStatus.status === 'SUCCESS' ? '✅ Успешно' : 
                   syncStatus.status === 'FAILED' ? '❌ Ошибка' : 
                   syncStatus.status === 'IN_PROGRESS' ? '🔄 В процессе' : 
                   syncStatus.status === 'NEVER_SYNCED' ? '❓ Не синхронизировано' : '❓ Неизвестно'}
                </span>
              </div>
              
              {/* Детальная информация */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-xs text-gray-400">
                <div>
                  <span className="font-medium">Последняя синхронизация:</span>
                  <br />
                  {syncStatus.lastSyncAt ? formatDate(syncStatus.lastSyncAt) : 'Не выполнялась'}
                </div>
                <div>
                  <span className="font-medium">Обработано заказов:</span>
                  <br />
                  {syncStatus.ordersProcessed || 0}
                </div>
                <div>
                  <span className="font-medium">Длительность:</span>
                  <br />
                  {syncStatus.syncDurationMs ? `${Math.round(syncStatus.syncDurationMs / 1000)} сек` : '-'}
                </div>
              </div>
              
              {/* Ошибка если есть */}
              {syncStatus.errorMessage && (
                <div className="text-red-400 text-xs bg-red-900/20 p-2 rounded">
                  <span className="font-medium">Ошибка:</span> {syncStatus.errorMessage}
                </div>
              )}
              
              {/* Информация о периодичности */}
              <div className="text-xs text-gray-500 bg-gray-800/50 p-2 rounded">
                <span className="font-medium">Автоматическая синхронизация:</span> каждые 60 минут при разрыве более 1 часа
              </div>
            </div>
          )}
        </div>
        <div className="flex items-center space-x-4">
          <select
            value={selectedStatus}
            onChange={handleStatusChange}
            className="bg-gray-700 text-white px-3 py-2 rounded border border-gray-600 focus:outline-none focus:border-indigo-500 text-sm"
          >
            {statuses.map(status => (
              <option key={status.value} value={status.value}>
                {status.label}
              </option>
            ))}
          </select>
          <input
            type="datetime-local"
            value={dateFrom}
            onChange={handleDateFromChange}
            className="bg-gray-700 text-white px-3 py-2 rounded border border-gray-600 focus:outline-none focus:border-indigo-500 text-sm"
            placeholder="От"
          />
          <input
            type="datetime-local"
            value={dateTo}
            onChange={handleDateToChange}
            className="bg-gray-700 text-white px-3 py-2 rounded border border-gray-600 focus:outline-none focus:border-indigo-500 text-sm"
            placeholder="До"
          />
          <button
            onClick={clearFilters}
            className="bg-gray-600 text-white px-4 py-2 rounded hover:bg-gray-700 transition-colors text-sm"
          >
            Очистить
          </button>
          <div className="flex items-center space-x-2">
            <label className="text-gray-300 text-sm">С:</label>
            <input
              type="datetime-local"
              value={from}
              onChange={(e) => setFrom(e.target.value)}
              className="bg-gray-700 text-white px-3 py-2 rounded border border-gray-600 focus:outline-none focus:border-indigo-500 text-sm"
            />
          </div>
          <div className="flex items-center space-x-2">
            <label className="text-gray-300 text-sm">По:</label>
            <input
              type="datetime-local"
              value={to}
              onChange={(e) => setTo(e.target.value)}
              className="bg-gray-700 text-white px-3 py-2 rounded border border-gray-600 focus:outline-none focus:border-indigo-500 text-sm"
            />
          </div>
          <button
            onClick={backfill}
            disabled={loading || !from || !to}
            className="bg-indigo-600 text-white px-4 py-2 rounded hover:bg-indigo-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? 'Загрузка...' : 'Загрузить из Ozon'}
          </button>
          <button
            onClick={load}
            className="bg-gray-600 text-white px-4 py-2 rounded hover:bg-gray-700 transition-colors"
          >
            Обновить
          </button>
          <button
            onClick={forceSync}
            disabled={loading}
            className="bg-purple-600 text-white px-4 py-2 rounded hover:bg-purple-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? 'Синхронизация...' : '🔄 Принудительная синхронизация'}
          </button>
        </div>
      </div>

      <OrdersTable
        orders={orders}
        pagination={pagination}
        onPageChange={handlePageChange}
        showMarketColumn={false}
        showDetailsColumn={true}
      />
    </div>
  );
};

export default OzonOrders;





