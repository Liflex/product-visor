/**
 * Home Page Component
 * Provides welcome screen and overview of the Product Visor application
 */

import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useProducts } from '../hooks/use-products.js';
import { useCategories } from '../hooks/use-categories.js';
import useBarcodeScanner from '../hooks/use-barcode-scanner.js';
import Notification from './ui/notification.jsx';
import { findProductByBarcode, searchProductsPage } from '../services/productService.js';

// Вспомогательная функция для получения src изображения продукта
const getImageSrc = (product) => {
  const img = product?.image;
  if (!img && product?.imageUrl) return product.imageUrl;
  if (typeof img === 'string') {
    if (img.startsWith('data:')) return img;
    if (img.startsWith('/9j/')) return `data:image/jpeg;base64,${img}`;
  }
  if (Array.isArray(img)) {
    try {
      const base64 = btoa(String.fromCharCode(...new Uint8Array(img)));
      return `data:image/jpeg;base64,${base64}`;
    } catch (_) { return null; }
  }
  return null;
};

/**
 * Statistics card component
 */
const StatCard = ({ title, value, icon, description, linkTo, linkText }) => (
  <div className="bg-gray-800 p-6 rounded-lg shadow-lg">
    <div className="flex items-center">
      <div className="text-3xl mr-4">{icon}</div>
      <div className="flex-1">
        <h3 className="text-lg font-semibold text-white">{title}</h3>
        <p className="text-3xl font-bold text-indigo-400">{value}</p>
        <p className="text-sm text-gray-400">{description}</p>
        {linkTo && linkText && (
          <Link 
            to={linkTo} 
            className="text-sm text-indigo-400 hover:text-indigo-300 transition-colors"
          >
            {linkText} →
          </Link>
        )}
      </div>
    </div>
  </div>
);

/**
 * Feature card component
 */
const FeatureCard = ({ title, description, icon }) => (
  <div className="bg-gray-800 p-6 rounded-lg shadow-lg">
    <div className="text-center">
      <div className="text-4xl mb-4">{icon}</div>
      <h3 className="text-lg font-semibold text-white mb-2">{title}</h3>
      <p className="text-gray-400">{description}</p>
    </div>
  </div>
);

/**
 * Main Home component
 */
const Home = () => {
  const { totalProducts, filteredCount } = useProducts();
  const { totalCategories } = useCategories();
  const navigate = useNavigate();
  
  // Global barcode scanner
  const { notification, hideNotification } = useBarcodeScanner({
    enabled: true
  });

  // Manual barcode search state
  const [barcodeSearch, setBarcodeSearch] = useState('');
  const [isSearching, setIsSearching] = useState(false);
  const [searchError, setSearchError] = useState('');

  // Full-text search state
  const [fullQuery, setFullQuery] = useState('');
  const [fullPage, setFullPage] = useState(0);
  const [fullSize, setFullSize] = useState(10);
  const [fullResults, setFullResults] = useState([]);
  const [fullTotalPages, setFullTotalPages] = useState(0);
  const [fullTotalElements, setFullTotalElements] = useState(0);
  const [fullLoading, setFullLoading] = useState(false);
  const [fullError, setFullError] = useState('');

  // Handle manual barcode search
  const handleBarcodeSearch = async (e) => {
    e.preventDefault();
    
    if (!barcodeSearch.trim()) {
      setSearchError('Введите штрих-код для поиска');
      return;
    }

    setIsSearching(true);
    setSearchError('');

    try {
      const product = await findProductByBarcode(barcodeSearch.trim());
      
      if (product) {
        navigate(`/product/${product.id}`);
      } else {
        navigate('/add-product', { 
          state: { barcode: barcodeSearch.trim() } 
        });
      }
    } catch (error) {
      console.error('Error searching product by barcode:', error);
      setSearchError('Ошибка при поиске товара');
    } finally {
      setIsSearching(false);
    }
  };

  // Handle full-text search submit
  const handleFullSearch = async (e) => {
    e.preventDefault();
    if (!fullQuery.trim()) {
      setFullError('Введите запрос для поиска');
      return;
    }
    try {
      setFullLoading(true);
      setFullError('');
      const pageData = await searchProductsPage(fullQuery.trim(), fullPage, fullSize);
      setFullResults(pageData.content || []);
      setFullTotalPages(pageData.totalPages || 0);
      setFullTotalElements(pageData.totalElements || 0);
    } catch (err) {
      console.error('Error full-text searching:', err);
      setFullError('Ошибка при поиске');
    } finally {
      setFullLoading(false);
    }
  };

  const FullSearchPagination = () => (
    <div className="flex items-center justify-between mt-4">
      <div className="text-gray-400 text-sm">
        Найдено: {fullTotalElements} • Страниц: {fullTotalPages}
      </div>
      <div className="flex items-center space-x-2">
        <button
          className="px-3 py-1 bg-gray-700 text-white rounded disabled:opacity-50"
          onClick={async () => { if (fullPage>0){const p=fullPage-1; setFullPage(p); await searchProductsPage(fullQuery, p, fullSize).then(d=>{setFullResults(d.content||[]);setFullTotalPages(d.totalPages||0);setFullTotalElements(d.totalElements||0);});}}}
          disabled={fullPage <= 0}
        >
          ← Предыдущая
        </button>
        <span className="text-gray-300 text-sm">Стр. {fullPage + 1} из {Math.max(fullTotalPages, 1)}</span>
        <button
          className="px-3 py-1 bg-gray-700 text-white rounded disabled:opacity-50"
          onClick={async () => { if (fullPage < fullTotalPages-1){const p=fullPage+1; setFullPage(p); await searchProductsPage(fullQuery, p, fullSize).then(d=>{setFullResults(d.content||[]);setFullTotalPages(d.totalPages||0);setFullTotalElements(d.totalElements||0);});}}}
          disabled={fullPage >= fullTotalPages - 1}
        >
          Следующая →
        </button>
        <select
          className="ml-2 bg-gray-700 text-white rounded px-2 py-1"
          value={fullSize}
          onChange={async (e) => { const s=parseInt(e.target.value,10); setFullSize(s); const p=0; setFullPage(p); const d=await searchProductsPage(fullQuery, p, s); setFullResults(d.content||[]); setFullTotalPages(d.totalPages||0); setFullTotalElements(d.totalElements||0); }}
        >
          {[10,20,50].map(s => (
            <option key={s} value={s}>{s} на странице</option>
          ))}
        </select>
      </div>
    </div>
  );

  const features = [
    {
      title: 'Умный инвентарь',
      description: 'Отслеживайте инвентарь контактных линз с динамическими атрибутами: сила, цвет и сроки годности.',
      icon: '📦'
    },
    {
      title: 'Продвинутый поиск',
      description: 'Быстро находите продукты с мощными возможностями поиска и фильтрации.',
      icon: '🔍'
    },
    {
      title: 'Простое управление',
      description: 'Добавляйте, редактируйте и удаляйте продукты с интуитивным интерфейсом и валидацией в реальном времени.',
      icon: '⚡'
    },
    {
      title: 'Визуальный предпросмотр',
      description: 'Загружайте и предварительно просматривайте изображения продуктов с автоматической оптимизацией.',
      icon: '🖼️'
    }
  ];

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="space-y-8">
        {/* Hero Section */}
        <div className="text-center py-12">
        <h1 className="text-4xl font-bold text-white mb-4">
          Добро пожаловать в Product Visor
        </h1>
        <p className="text-xl text-gray-400 mb-8 max-w-2xl mx-auto">
          Ваше комплексное решение для управления инвентарем контактных линз. 
          Отслеживайте, ищите и управляйте продуктами с легкостью и точностью.
        </p>

        {/* Barcode Search Form */}
        <div className="max-w-md mx-auto mb-8">
          <form onSubmit={handleBarcodeSearch} className="space-y-4">
            <div className="relative">
              <input
                type="text"
                value={barcodeSearch}
                onChange={(e) => setBarcodeSearch(e.target.value)}
                placeholder="Введите штрих-код товара..."
                className="w-full px-4 py-3 bg-gray-700 border border-gray-600 rounded-md text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                disabled={isSearching}
              />
              <button
                type="submit"
                disabled={isSearching}
                className="absolute right-2 top-1/2 transform -translate-y-1/2 px-4 py-1 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 disabled:bg-gray-600 disabled:cursor-not-allowed transition-colors"
              >
                {isSearching ? 'Поиск...' : '🔍'}
              </button>
            </div>
            {searchError && (
              <p className="text-red-400 text-sm">{searchError}</p>
            )}
          </form>
          <p className="text-sm text-gray-500 mt-2">
            Или используйте сканер штрих-кода (нажмите любую клавишу)
          </p>
        </div>

        {/* Full-text Search */}
        <div className="max-w-3xl mx-auto">
          <form onSubmit={handleFullSearch} className="flex items-center space-x-2">
            <input
              type="text"
              value={fullQuery}
              onChange={(e) => setFullQuery(e.target.value)}
              placeholder="Поиск по всем полям и атрибутам..."
              className="flex-1 px-4 py-3 bg-gray-700 border border-gray-600 rounded-md text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
            />
            <button
              type="submit"
              className="px-4 py-3 bg-indigo-600 text-white rounded-md hover:bg-indigo-700"
            >
              Найти
            </button>
          </form>

          {/* Results */}
          {fullLoading && (
            <div className="flex justify-center items-center h-24">
              <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-500"></div>
            </div>
          )}

          {fullError && (
            <p className="text-red-400 mt-3">{fullError}</p>
          )}

          {!fullLoading && fullResults.length > 0 && (
            <div className="mt-4 bg-gray-800 rounded-lg p-4">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {fullResults.map(p => (
                  <div key={p.id} className="bg-gray-700 rounded p-3 cursor-pointer" onDoubleClick={() => navigate(`/product/${p.id}`, { state: { product: p } })}>
                    {/* превью изображения */}
                    {getImageSrc(p) && (
                      <img
                        src={getImageSrc(p)}
                        alt={p.name}
                        className="w-full h-32 object-cover rounded mb-2"
                        onError={(e) => { e.target.style.display = 'none'; }}
                      />
                    )}
                    <div className="text-white font-semibold truncate">{p.name}</div>
                    <div className="text-gray-300 text-sm">{p.category?.name || 'Без категории'}</div>
                    <div className="text-gray-400 text-xs mt-1">Артикул: {p.article} • ШК: {p.barcode || '—'}</div>
                  </div>
                ))}
              </div>
              <FullSearchPagination />
            </div>
          )}
        </div>

        <div className="flex justify-center space-x-4 mt-8">
          <Link
            to="/add-product"
            className="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-3 rounded-md transition-colors font-medium"
          >
            Добавить первый продукт
          </Link>
          <Link
            to="/all-products"
            className="border border-gray-600 text-gray-300 hover:bg-gray-700 px-6 py-3 rounded-md transition-colors font-medium"
          >
            Просмотреть каталог
          </Link>
        </div>
      </div>
      </div>
      
      {/* Global Notifications */}
      {notification && (
        <Notification
          type={notification.type}
          message={notification.message}
          duration={notification.duration}
          onClose={hideNotification}
        />
      )}
    </div>
  );
};

export default Home;