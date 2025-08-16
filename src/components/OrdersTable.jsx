import React, { useState } from 'react';
import CancelReasonModal from './ui/cancel-reason-modal.jsx';

const OrdersTable = ({ 
  orders, 
  pagination, 
  onPageChange, 
  title, 
  showMarketColumn = true,
  showDetailsColumn = false 
}) => {
  const [modalState, setModalState] = useState({
    isOpen: false,
    cancelReason: '',
    orderNumber: ''
  });

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleString('ru-RU');
  };

  const getDeliveryTime = (order) => {
    if (!order.shipmentDate || !order.ozonCreatedAt) return null;
    
    const createdDate = new Date(order.ozonCreatedAt);
    const shipmentDate = new Date(order.shipmentDate);
    const diffTime = shipmentDate.getTime() - createdDate.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays <= 0) return null;
    
    return diffDays;
  };

  const isFinalStatus = (status) => {
    return ['COMPLETED', 'DELIVERED', 'CANCELLED'].includes(status);
  };

  const getMarketDisplayName = (market) => {
    const markets = [
      { value: 'ALL', label: 'Все маркеты' },
      { value: 'OZON', label: 'Ozon' },
      { value: 'WILDBERRIES', label: 'Wildberries' },
      { value: 'YANDEX_MARKET', label: 'Yandex Market' },
      { value: 'ALIEXPRESS', label: 'AliExpress' },
      { value: 'OTHER', label: 'Другие' }
    ];
    const marketOption = markets.find(m => m.value === market);
    return marketOption ? marketOption.label : market;
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'COMPLETED':
        return 'bg-green-100 text-green-800';
      case 'CANCELLED':
        return 'bg-red-100 text-red-800';
      case 'PROCESSING':
        return 'bg-blue-100 text-blue-800';
      case 'SHIPPED':
        return 'bg-purple-100 text-purple-800';
      case 'DELIVERED':
        return 'bg-emerald-100 text-emerald-800';
      default:
        return 'bg-yellow-100 text-yellow-800';
    }
  };

  const handleShowCancelReason = (order) => {
    setModalState({
      isOpen: true,
      cancelReason: order.cancelReason,
      orderNumber: order.postingNumber || order.orderBarcode || '-'
    });
  };

  const closeModal = () => {
    setModalState({
      isOpen: false,
      cancelReason: '',
      orderNumber: ''
    });
  };

  if (!Array.isArray(orders) || orders.length === 0) {
    return (
      <div className="text-center py-8">
        <p className="text-gray-400">
          {!Array.isArray(orders) ? 'Ошибка формата данных' : 'Заказы не найдены'}
        </p>
        {!Array.isArray(orders) && (
          <p className="text-gray-500 text-sm mt-2">
            Получено: {typeof orders} - {JSON.stringify(orders).substring(0, 100)}...
          </p>
        )}
      </div>
    );
  }

  return (
    <>
      <div className="overflow-x-auto">
        <table className="min-w-full bg-gray-800 rounded-lg overflow-hidden">
          <thead className="bg-gray-700">
            <tr>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                Номер заказа
              </th>
              {showMarketColumn && (
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                  Маркет
                </th>
              )}
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                Статус
              </th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                Дата создания
              </th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                Клиент
              </th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                Сумма
              </th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                Товары
              </th>
              {showDetailsColumn && (
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                  Детали
                </th>
              )}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-700">
            {orders.map((order) => (
              <tr key={order.postingNumber || order.id} className="hover:bg-gray-700">
                <td className="px-4 py-3 text-sm text-white font-medium">
                  {order.postingNumber || order.orderBarcode || '-'}
                </td>
                {showMarketColumn && (
                  <td className="px-4 py-3 text-sm text-gray-300">
                    {getMarketDisplayName(order.market)}
                  </td>
                )}
                                 <td className="px-4 py-3 text-sm">
                   <div className="flex items-center space-x-2">
                     <span className={`px-2 py-1 text-xs rounded-full ${getStatusColor(order.status)}`}>
                       {order.status}
                     </span>
                                           {order.status === 'CANCELLED' && order.cancelReason && (
                        <button
                          onClick={() => handleShowCancelReason(order)}
                          className="px-2 py-1 text-xs bg-red-600 text-white rounded hover:bg-red-700 transition-colors"
                          title="Показать причину отмены"
                        >
                          Причина
                        </button>
                      )}
                   </div>
                 </td>
                                 <td className="px-4 py-3 text-sm text-gray-300">
                   <div>
                     <div>{formatDate(order.ozonCreatedAt || order.createdAt || order.orderDate)}</div>
                     {isFinalStatus(order.status) && getDeliveryTime(order) && (
                       <div className="text-xs text-green-400 mt-1">
                         🚚 Доставлен за {getDeliveryTime(order)} дн.
                       </div>
                     )}
                   </div>
                 </td>
                <td className="px-4 py-3 text-sm text-gray-300">
                  {order.customerName || '-'}
                </td>
                <td className="px-4 py-3 text-sm text-gray-300">
                  {order.totalPrice || order.price ? `${order.totalPrice || order.price} ₽` : '-'}
                </td>
                <td className="px-4 py-3 text-sm text-gray-300">
                  {order.items ? order.items.length : 1} шт.
                </td>
                {showDetailsColumn && (
                  <td className="px-4 py-3 text-sm text-gray-300">
                    {order.items && order.items.length > 0 ? (
                      <div className="max-w-xs">
                        {order.items.slice(0, 2).map((item, index) => (
                          <div key={index} className="text-xs mb-1">
                            {item.offerId || item.name || 'Товар'} - {item.quantity || 1} шт.
                          </div>
                        ))}
                        {order.items.length > 2 && (
                          <div className="text-xs text-gray-500">
                            +{order.items.length - 2} еще
                          </div>
                        )}
                      </div>
                    ) : '-'}
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Пагинация */}
      {pagination && (
        <div className="flex justify-between items-center mt-6">
          <button
            onClick={() => onPageChange(Math.max(0, pagination.currentPage - 1))}
            disabled={pagination.currentPage === 0}
            className="px-4 py-2 bg-gray-700 text-white rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-600"
          >
            Предыдущая
          </button>
          <span className="text-gray-300">
            Страница {pagination.currentPage + 1} из {pagination.totalPages} 
            ({pagination.totalElements} заказов)
          </span>
          <button
            onClick={() => onPageChange(pagination.currentPage + 1)}
            disabled={pagination.currentPage >= pagination.totalPages - 1}
            className="px-4 py-2 bg-gray-700 text-white rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-600"
          >
            Следующая
          </button>
        </div>
      )}

      {/* Модальное окно с причиной отмены */}
      <CancelReasonModal
        isOpen={modalState.isOpen}
        onClose={closeModal}
        cancelReason={modalState.cancelReason}
        orderNumber={modalState.orderNumber}
      />
    </>
  );
};

export default OrdersTable;
