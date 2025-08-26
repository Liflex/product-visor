import React, { useState, useEffect } from 'react';
import { getProductsPage } from '../services/productService.js';

/**
 * Inventory Management Component
 * Shows real inventory data from the API
 */
const Inventory = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    totalItems: 0,
    categories: 0,
    value: 0,
    critical: 0,
    lowStock: 0,
    expiringSoon: 0
  });

  useEffect(() => {
    fetchInventoryData();
  }, []);

  const fetchInventoryData = async () => {
    try {
      setLoading(true);
      // Получаем все продукты
      const response = await getProductsPage(0, 1000);
      const productsData = response.content || [];
      setProducts(productsData);
      
      // Вычисляем статистику
      calculateStats(productsData);
    } catch (error) {
      console.error('Error fetching inventory data:', error);
    } finally {
      setLoading(false);
    }
  };

  const calculateStats = (productsData) => {
    const totalItems = productsData.length;
    const uniqueCategories = new Set(productsData.map(p => p.category?.id).filter(Boolean));
    const categories = uniqueCategories.size;
    
    const value = productsData.reduce((sum, p) => {
      const quantity = p.quantity || 0;
      const price = p.price || 0;
      return sum + (quantity * price);
    }, 0);
    
    const critical = productsData.filter(p => (p.quantity || 0) === 0).length;
    const lowStock = productsData.filter(p => (p.quantity || 0) > 0 && (p.quantity || 0) <= 5).length;
    const expiringSoon = 0; // Пока нет данных о сроке годности

    setStats({
      totalItems,
      categories,
      value,
      critical,
      lowStock,
      expiringSoon
    });
  };

  const getRecentChanges = () => {
    // Здесь можно было бы получить реальную историю изменений
    // Пока возвращаем заглушку на основе текущих данных
    const changes = [];
    
    // Добавляем продукты с низким запасом
    const lowStockProducts = products.filter(p => (p.quantity || 0) > 0 && (p.quantity || 0) <= 5).slice(0, 2);
    lowStockProducts.forEach(product => {
      changes.push({
        type: 'low_stock',
        product: product.name,
        change: `Low stock: ${product.quantity} units`,
        time: 'Recently'
      });
    });

    // Добавляем продукты без запаса
    const outOfStockProducts = products.filter(p => (p.quantity || 0) === 0).slice(0, 1);
    outOfStockProducts.forEach(product => {
      changes.push({
        type: 'out_of_stock',
        product: product.name,
        change: 'Out of stock',
        time: 'Recently'
      });
    });

    return changes;
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
        <h1 className="text-3xl font-bold text-white mb-6">📋 Inventory Management</h1>
        
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {/* Inventory Stats Card */}
          <div className="bg-gray-700 rounded-lg p-6">
            <h3 className="text-xl font-semibold text-white mb-4">📊 Inventory Stats</h3>
            <div className="space-y-3">
              <div className="flex justify-between">
                <span className="text-gray-300">Total Items:</span>
                <span className="text-white font-semibold">{stats.totalItems}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-300">Categories:</span>
                <span className="text-white font-semibold">{stats.categories}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-300">Value:</span>
                <span className="text-green-400 font-semibold">₽{stats.value.toLocaleString()}</span>
              </div>
            </div>
          </div>

          {/* Stock Alerts Card */}
          <div className="bg-gray-700 rounded-lg p-6">
            <h3 className="text-xl font-semibold text-white mb-4">🚨 Stock Alerts</h3>
            <div className="space-y-3">
              <div className="flex justify-between items-center">
                <span className="text-red-400">Critical:</span>
                <span className="text-red-400 font-semibold">{stats.critical} items</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-yellow-400">Low Stock:</span>
                <span className="text-yellow-400 font-semibold">{stats.lowStock} items</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-blue-400">Expiring Soon:</span>
                <span className="text-blue-400 font-semibold">{stats.expiringSoon} items</span>
              </div>
            </div>
          </div>

          {/* Inventory Actions Card */}
          <div className="bg-gray-700 rounded-lg p-6">
            <h3 className="text-xl font-semibold text-white mb-4">⚡ Actions</h3>
            <div className="space-y-3">
              <button className="w-full bg-blue-600 hover:bg-blue-700 text-white py-2 px-4 rounded transition-colors">
                📦 Add Stock
              </button>
              <button className="w-full bg-green-600 hover:bg-green-700 text-white py-2 px-4 rounded transition-colors">
                📋 Export Report
              </button>
              <button className="w-full bg-purple-600 hover:bg-purple-700 text-white py-2 px-4 rounded transition-colors">
                🔍 Search Items
              </button>
            </div>
          </div>
        </div>

        {/* Recent Inventory Changes */}
        <div className="mt-8 bg-gray-700 rounded-lg p-6">
          <h3 className="text-xl font-semibold text-white mb-4">📝 Recent Changes</h3>
          <div className="space-y-3">
            {getRecentChanges().map((change, index) => (
              <div key={index} className="flex items-center justify-between p-3 bg-gray-600 rounded">
                <div className="flex items-center space-x-3">
                  <span className={`text-lg ${
                    change.type === 'low_stock' ? 'text-yellow-400' :
                    change.type === 'out_of_stock' ? 'text-red-400' :
                    'text-green-400'
                  }`}>
                    {change.type === 'low_stock' ? '⚠' : 
                     change.type === 'out_of_stock' ? '🚨' : '+'}
                  </span>
                  <span className="text-white">{change.product}</span>
                </div>
                <div className="text-right">
                  <span className="text-gray-400 text-sm">{change.change}</span>
                  <div className="text-gray-400 text-xs">{change.time}</div>
                </div>
              </div>
            ))}
            {getRecentChanges().length === 0 && (
              <div className="text-gray-400 text-center py-4">No recent changes</div>
            )}
          </div>
        </div>

        {/* Top Products by Value */}
        <div className="mt-8 bg-gray-700 rounded-lg p-6">
          <h3 className="text-xl font-semibold text-white mb-4">💰 Top Products by Value</h3>
          <div className="space-y-3">
            {products
              .filter(p => (p.quantity || 0) > 0 && (p.price || 0) > 0)
              .sort((a, b) => {
                const valueA = (a.quantity || 0) * (a.price || 0);
                const valueB = (b.quantity || 0) * (b.price || 0);
                return valueB - valueA;
              })
              .slice(0, 5)
              .map((product) => {
                const value = (product.quantity || 0) * (product.price || 0);
                return (
                  <div key={product.id} className="flex items-center justify-between p-3 bg-gray-600 rounded">
                    <div>
                      <div className="text-white font-medium">{product.name}</div>
                      <div className="text-gray-400 text-sm">Article: {product.article}</div>
                    </div>
                    <div className="text-right">
                      <div className="text-green-400 font-semibold">₽{value.toLocaleString()}</div>
                      <div className="text-gray-400 text-sm">{product.quantity || 0} × ₽{product.price || 0}</div>
                    </div>
                  </div>
                );
              })}
            {products.filter(p => (p.quantity || 0) > 0 && (p.price || 0) > 0).length === 0 && (
              <div className="text-gray-400 text-center py-4">No products with value</div>
            )}
          </div>
        </div>

        <div className="mt-8">
          <p className="text-gray-400 text-center">
            Advanced inventory management features are coming soon. This page will include:
          </p>
          <ul className="text-gray-400 text-center mt-4 space-y-1">
            <li>• Detailed inventory tracking and history</li>
            <li>• Stock movement analysis</li>
            <li>• Automated reorder points</li>
            <li>• Inventory valuation reports</li>
            <li>• Barcode scanning integration</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default Inventory;
