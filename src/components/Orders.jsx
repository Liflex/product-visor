import React, { useState, useEffect } from 'react';
import { orderService } from '../services/orderService.js';
import LoadingSpinner from './ui/loading-spinner.jsx';
import ErrorMessage from './ui/error-message.jsx';
import OrdersTable from './OrdersTable.jsx';

const Orders = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [selectedMarket, setSelectedMarket] = useState('ALL');
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

  const markets = [
    { value: 'ALL', label: 'Все маркеты' },
    { value: 'OZON', label: 'Ozon' },
    { value: 'WILDBERRIES', label: 'Wildberries' },
    { value: 'YANDEX_MARKET', label: 'Yandex Market' },
    { value: 'ALIEXPRESS', label: 'AliExpress' },
    { value: 'OTHER', label: 'Другие' }
  ];

  const statuses = [
    { value: '', label: 'Все статусы' },
    { value: 'COMPLETED', label: 'Завершен' },
    { value: 'CANCELLED', label: 'Отменен' },
    { value: 'PROCESSING', label: 'В обработке' },
    { value: 'SHIPPED', label: 'Отправлен' },
    { value: 'DELIVERED', label: 'Доставлен' }
  ];

  const loadOrders = async () => {
    setLoading(true);
    setError('');
    try {
      let response;
      if (selectedMarket === 'ALL') {
        response = await orderService.getAllOrders(page, size, selectedStatus || null, dateFrom || null, dateTo || null);
      } else {
        response = await orderService.getOrdersByMarket(selectedMarket, page, size, selectedStatus || null, dateFrom || null, dateTo || null);
      }
      
      // Отладочная информация
      console.log('API Response:', response);
      
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
        console.log(`✅ Loaded ${ordersData.length} orders from page ${response.number + 1}/${response.totalPages}`);
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
      setError(err.message || 'Ошибка при загрузке заказов');
      console.error('Error loading orders:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadOrders();
  }, [selectedMarket, selectedStatus, dateFrom, dateTo, page]);

  const handleMarketChange = (e) => {
    setSelectedMarket(e.target.value);
    setPage(0);
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
        <h2 className="text-2xl font-bold text-white flex items-center">
          <span className="mr-2">📋</span>
          Заказы
        </h2>
        <div className="flex items-center space-x-4">
          <select
            value={selectedMarket}
            onChange={handleMarketChange}
            className="bg-gray-700 text-white px-3 py-2 rounded border border-gray-600 focus:outline-none focus:border-indigo-500"
          >
            {markets.map(market => (
              <option key={market.value} value={market.value}>
                {market.label}
              </option>
            ))}
          </select>
          <select
            value={selectedStatus}
            onChange={handleStatusChange}
            className="bg-gray-700 text-white px-3 py-2 rounded border border-gray-600 focus:outline-none focus:border-indigo-500"
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
            className="bg-gray-700 text-white px-3 py-2 rounded border border-gray-600 focus:outline-none focus:border-indigo-500"
            placeholder="От"
          />
          <input
            type="datetime-local"
            value={dateTo}
            onChange={handleDateToChange}
            className="bg-gray-700 text-white px-3 py-2 rounded border border-gray-600 focus:outline-none focus:border-indigo-500"
            placeholder="До"
          />
          <button
            onClick={clearFilters}
            className="bg-gray-600 text-white px-4 py-2 rounded hover:bg-gray-700 transition-colors"
          >
            Очистить
          </button>
          <button
            onClick={loadOrders}
            className="bg-indigo-600 text-white px-4 py-2 rounded hover:bg-indigo-700 transition-colors"
          >
            Обновить
          </button>
        </div>
      </div>

      <OrdersTable
        orders={orders}
        pagination={pagination}
        onPageChange={handlePageChange}
        showMarketColumn={true}
        showDetailsColumn={false}
      />
    </div>
  );
};

export default Orders;
