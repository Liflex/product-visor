import React, { useState, useEffect } from 'react';
import { getWarehouseStats, getTopProductsByValue } from '../services/warehouseService.js';

/**
 * Analytics Dashboard Component
 * Shows real analytics data from the API
 */
const Analytics = () => {
  const [stats, setStats] = useState({
    totalProducts: 0,
    totalValue: 0,
    categories: 0,
    productsWithMarkets: 0
  });
  const [topProducts, setTopProducts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchAnalyticsData();
  }, []);

  const fetchAnalyticsData = async () => {
    try {
      setLoading(true);
      
      // Получаем статистику склада
      const warehouseStats = await getWarehouseStats();
      setStats(warehouseStats);
      
      // Получаем топ продуктов по стоимости
      const topProductsData = await getTopProductsByValue(5);
      setTopProducts(topProductsData);
      
    } catch (error) {
      console.error('Error fetching analytics data:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="flex justify-center items-center h-64">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-indigo-500"></div>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="bg-gray-800 rounded-lg shadow-lg p-6">
        <h1 className="text-3xl font-bold text-white mb-6">📈 Analytics Dashboard</h1>
        
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          {/* Total Products */}
          <div className="bg-gray-700 rounded-lg p-6">
            <h3 className="text-lg font-semibold text-white mb-2">📦 Total Products</h3>
            <div className="text-3xl font-bold text-blue-400 mb-2">{stats.totalProducts}</div>
            <div className="text-sm text-gray-400">Active products in system</div>
          </div>

          {/* Total Value */}
          <div className="bg-gray-700 rounded-lg p-6">
            <h3 className="text-lg font-semibold text-white mb-2">💰 Total Value</h3>
            <div className="text-3xl font-bold text-green-400 mb-2">₽{stats.totalValue.toLocaleString()}</div>
            <div className="text-sm text-gray-400">Inventory value</div>
          </div>

          {/* Categories */}
          <div className="bg-gray-700 rounded-lg p-6">
            <h3 className="text-lg font-semibold text-white mb-2">🏷️ Categories</h3>
            <div className="text-3xl font-bold text-purple-400 mb-2">{stats.categories}</div>
            <div className="text-sm text-gray-400">Product categories</div>
          </div>

          {/* Marketplace Products */}
          <div className="bg-gray-700 rounded-lg p-6">
            <h3 className="text-lg font-semibold text-white mb-2">🏪 Marketplace</h3>
            <div className="text-3xl font-bold text-yellow-400 mb-2">{stats.productsWithMarkets}</div>
            <div className="text-sm text-gray-400">Synced products</div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Sales Chart */}
          <div className="bg-gray-700 rounded-lg p-6">
            <h3 className="text-xl font-semibold text-white mb-4">📈 Inventory Overview</h3>
            <div className="h-64 bg-gray-600 rounded flex items-center justify-center">
              <div className="text-center">
                <div className="text-gray-400 mb-2">Chart placeholder</div>
                <div className="text-sm text-gray-500">
                  Total Products: {stats.totalProducts}<br/>
                  Total Value: ₽{stats.totalValue.toLocaleString()}<br/>
                  Categories: {stats.categories}
                </div>
              </div>
            </div>
          </div>

          {/* Top Products */}
          <div className="bg-gray-700 rounded-lg p-6">
            <h3 className="text-xl font-semibold text-white mb-4">🏆 Top Products by Value</h3>
            <div className="space-y-3">
              {topProducts.map((product) => (
                <div key={product.id} className="flex justify-between items-center p-3 bg-gray-600 rounded">
                  <div>
                    <div className="text-white font-medium">{product.name}</div>
                    <div className="text-gray-400 text-sm">Article: {product.article}</div>
                  </div>
                  <div className="text-right">
                    <div className="text-green-400 font-semibold">₽{product.totalValue.toLocaleString()}</div>
                    <div className="text-gray-400 text-sm">{product.quantity || 0} × ₽{product.price || 0}</div>
                  </div>
                </div>
              ))}
              {topProducts.length === 0 && (
                <div className="text-gray-400 text-center py-4">No products with value</div>
              )}
            </div>
          </div>
        </div>

        {/* Additional Analytics */}
        <div className="mt-8 grid grid-cols-1 md:grid-cols-2 gap-8">
          {/* Category Distribution */}
          <div className="bg-gray-700 rounded-lg p-6">
            <h3 className="text-xl font-semibold text-white mb-4">📊 Category Distribution</h3>
            <div className="space-y-3">
              <div className="flex justify-between items-center p-3 bg-gray-600 rounded">
                <span className="text-white">Total Categories</span>
                <span className="text-blue-400 font-semibold">{stats.categories}</span>
              </div>
              <div className="flex justify-between items-center p-3 bg-gray-600 rounded">
                <span className="text-white">Average Products per Category</span>
                <span className="text-green-400 font-semibold">
                  {stats.categories > 0 ? Math.round(stats.totalProducts / stats.categories) : 0}
                </span>
              </div>
              <div className="flex justify-between items-center p-3 bg-gray-600 rounded">
                <span className="text-white">Marketplace Coverage</span>
                <span className="text-yellow-400 font-semibold">
                  {stats.totalProducts > 0 ? Math.round((stats.productsWithMarkets / stats.totalProducts) * 100) : 0}%
                </span>
              </div>
            </div>
          </div>

          {/* Performance Metrics */}
          <div className="bg-gray-700 rounded-lg p-6">
            <h3 className="text-xl font-semibold text-white mb-4">📈 Performance Metrics</h3>
            <div className="space-y-3">
              <div className="flex justify-between items-center p-3 bg-gray-600 rounded">
                <span className="text-white">Average Product Value</span>
                <span className="text-purple-400 font-semibold">
                  ₽{stats.totalProducts > 0 ? Math.round(stats.totalValue / stats.totalProducts) : 0}
                </span>
              </div>
              <div className="flex justify-between items-center p-3 bg-gray-600 rounded">
                <span className="text-white">Total Inventory Items</span>
                <span className="text-green-400 font-semibold">
                  {stats.totalProducts}
                </span>
              </div>
              <div className="flex justify-between items-center p-3 bg-gray-600 rounded">
                <span className="text-white">Marketplace Products</span>
                <span className="text-blue-400 font-semibold">
                  {stats.productsWithMarkets}
                </span>
              </div>
            </div>
          </div>
        </div>

        <div className="mt-8">
          <p className="text-gray-400 text-center">
            Advanced analytics features are coming soon. This dashboard will include:
          </p>
          <ul className="text-gray-400 text-center mt-4 space-y-1">
            <li>• Real-time sales and revenue tracking</li>
            <li>• Product performance analytics</li>
            <li>• Customer behavior insights</li>
            <li>• Predictive analytics and forecasting</li>
            <li>• Custom report generation</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default Analytics;
