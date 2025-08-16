import React, { useState, useEffect } from 'react';
import LoadingSpinner from './ui/loading-spinner';
import ErrorMessage from './ui/error-message';

const OrderAnalysis = () => {
  const [stats, setStats] = useState(null);
  const [missingProducts, setMissingProducts] = useState([]);
  const [missingSkus, setMissingSkus] = useState([]);
  const [skuStats, setSkuStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState('stats');

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    setError('');
    
    try {
      // Загружаем статистику
      const statsResponse = await fetch('/api/orders/analysis/missing-products');
      if (statsResponse.ok) {
        const statsData = await statsResponse.json();
        setStats(statsData);
      }

      // Загружаем список ненайденных продуктов
      const missingResponse = await fetch('/api/orders/analysis/missing-products/list?size=50');
      if (missingResponse.ok) {
        const missingData = await missingResponse.json();
        setMissingProducts(missingData.content || []);
      }

      // Загружаем уникальные SKU
      const skusResponse = await fetch('/api/orders/analysis/missing-products/skus');
      if (skusResponse.ok) {
        const skusData = await skusResponse.json();
        setMissingSkus(skusData.missingSkus || []);
      }

      // Загружаем статистику по SKU
      const skuStatsResponse = await fetch('/api/orders/analysis/sku-stats');
      if (skuStatsResponse.ok) {
        const skuStatsData = await skuStatsResponse.json();
        setSkuStats(skuStatsData);
      }

    } catch (error) {
      setError('Ошибка при загрузке данных анализа');
      console.error('Error loading analysis data:', error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (percentage) => {
    if (percentage < 10) return 'text-green-600';
    if (percentage < 30) return 'text-yellow-600';
    return 'text-red-600';
  };

  const getStatusIcon = (percentage) => {
    if (percentage < 10) return '✅';
    if (percentage < 30) return '⚠️';
    return '❌';
  };

  if (loading) {
    return <LoadingSpinner />;
  }

  if (error) {
    return <ErrorMessage message={error} onRetry={loadData} />;
  }

  return (
    <div className="p-4">
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-white flex items-center">
          <span className="mr-2">📊</span>
          Анализ заказов
        </h2>
        <p className="text-gray-400 mt-2">
          Анализ товаров в заказах и их соответствия с продуктами в системе
        </p>
      </div>

      {/* Табы */}
      <div className="flex space-x-1 mb-6">
        <button
          onClick={() => setActiveTab('stats')}
          className={`px-4 py-2 rounded-lg font-medium transition-colors ${
            activeTab === 'stats'
              ? 'bg-blue-600 text-white'
              : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
          }`}
        >
          📈 Статистика
        </button>
        <button
          onClick={() => setActiveTab('missing')}
          className={`px-4 py-2 rounded-lg font-medium transition-colors ${
            activeTab === 'missing'
              ? 'bg-blue-600 text-white'
              : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
          }`}
        >
          ❓ Ненайденные товары
        </button>
        <button
          onClick={() => setActiveTab('skus')}
          className={`px-4 py-2 rounded-lg font-medium transition-colors ${
            activeTab === 'skus'
              ? 'bg-blue-600 text-white'
              : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
          }`}
        >
          🏷️ SKU анализ
        </button>
      </div>

      {/* Контент табов */}
      {activeTab === 'stats' && stats && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <div className="bg-gray-800 rounded-lg p-6">
            <div className="flex items-center">
              <div className="p-2 bg-blue-500 rounded-lg">
                <span className="text-white text-xl">📦</span>
              </div>
              <div className="ml-4">
                <p className="text-gray-400 text-sm">Всего товаров</p>
                <p className="text-white text-2xl font-bold">{stats.totalItems}</p>
              </div>
            </div>
          </div>

          <div className="bg-gray-800 rounded-lg p-6">
            <div className="flex items-center">
              <div className="p-2 bg-green-500 rounded-lg">
                <span className="text-white text-xl">✅</span>
              </div>
              <div className="ml-4">
                <p className="text-gray-400 text-sm">Найдено продуктов</p>
                <p className="text-white text-2xl font-bold">{stats.foundProducts}</p>
              </div>
            </div>
          </div>

          <div className="bg-gray-800 rounded-lg p-6">
            <div className="flex items-center">
              <div className="p-2 bg-red-500 rounded-lg">
                <span className="text-white text-xl">❌</span>
              </div>
              <div className="ml-4">
                <p className="text-gray-400 text-sm">Ненайдено продуктов</p>
                <p className="text-white text-2xl font-bold">{stats.missingProducts}</p>
              </div>
            </div>
          </div>

          <div className="bg-gray-800 rounded-lg p-6">
            <div className="flex items-center">
              <div className="p-2 bg-yellow-500 rounded-lg">
                <span className="text-white text-xl">{getStatusIcon(stats.missingPercentage)}</span>
              </div>
              <div className="ml-4">
                <p className="text-gray-400 text-sm">Процент ненайденных</p>
                <p className={`text-2xl font-bold ${getStatusColor(stats.missingPercentage)}`}>
                  {stats.missingPercentage}%
                </p>
              </div>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'missing' && (
        <div className="bg-gray-800 rounded-lg overflow-hidden">
          <div className="p-6 border-b border-gray-700">
            <h3 className="text-xl font-semibold text-white">
              Ненайденные товары ({missingProducts.length})
            </h3>
            <p className="text-gray-400 mt-1">
              Товары в заказах, которые не найдены в системе продуктов
            </p>
          </div>
          
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-700">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                    SKU
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                    Название
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                    Offer ID
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                    Количество
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                    Цена
                  </th>
                </tr>
              </thead>
              <tbody className="bg-gray-800 divide-y divide-gray-700">
                {missingProducts.map((item, index) => (
                  <tr key={index} className="hover:bg-gray-700">
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-blue-400 font-mono">
                      {item.sku || 'N/A'}
                    </td>
                    <td className="px-6 py-4 text-sm text-white">
                      {item.name || 'N/A'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-300">
                      {item.offerId || 'N/A'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-white">
                      {item.quantity || 0}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-green-400">
                      {item.price ? `${item.price} ₽` : 'N/A'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          
          {missingProducts.length === 0 && (
            <div className="p-6 text-center text-gray-400">
              Все товары найдены в системе! 🎉
            </div>
          )}
        </div>
      )}

      {activeTab === 'skus' && skuStats && (
        <div className="space-y-6">
          {/* Статистика по SKU */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="bg-gray-800 rounded-lg p-6">
              <div className="flex items-center">
                <div className="p-2 bg-blue-500 rounded-lg">
                  <span className="text-white text-xl">🏷️</span>
                </div>
                <div className="ml-4">
                  <p className="text-gray-400 text-sm">Уникальных SKU</p>
                  <p className="text-white text-2xl font-bold">{skuStats.totalUniqueSkus}</p>
                </div>
              </div>
            </div>

            <div className="bg-gray-800 rounded-lg p-6">
              <div className="flex items-center">
                <div className="p-2 bg-green-500 rounded-lg">
                  <span className="text-white text-xl">✅</span>
                </div>
                <div className="ml-4">
                  <p className="text-gray-400 text-sm">Найдено SKU</p>
                  <p className="text-white text-2xl font-bold">{skuStats.foundUniqueSkus}</p>
                </div>
              </div>
            </div>

            <div className="bg-gray-800 rounded-lg p-6">
              <div className="flex items-center">
                <div className="p-2 bg-red-500 rounded-lg">
                  <span className="text-white text-xl">❌</span>
                </div>
                <div className="ml-4">
                  <p className="text-gray-400 text-sm">Ненайдено SKU</p>
                  <p className="text-white text-2xl font-bold">{skuStats.missingUniqueSkus}</p>
                </div>
              </div>
            </div>
          </div>

          {/* Список ненайденных SKU */}
          <div className="bg-gray-800 rounded-lg p-6">
            <h3 className="text-xl font-semibold text-white mb-4">
              Ненайденные SKU ({missingSkus.length})
            </h3>
            
            {missingSkus.length > 0 ? (
              <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-2">
                {missingSkus.map((sku, index) => (
                  <div key={index} className="bg-gray-700 rounded px-3 py-2 text-sm font-mono text-red-400">
                    {sku}
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center text-gray-400">
                Все SKU найдены в системе! 🎉
              </div>
            )}
          </div>
        </div>
      )}

      {/* Кнопка обновления */}
      <div className="mt-6 flex justify-center">
        <button
          onClick={loadData}
          className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors"
        >
          🔄 Обновить данные
        </button>
      </div>
    </div>
  );
};

export default OrderAnalysis;
