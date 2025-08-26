/**
 * Product List Component
 * Displays all products with search, filtering, and CRUD operations
 */

import React, { useState, useEffect, useRef, useCallback, useReducer } from 'react';
import axios from 'axios';
import ProductFormNew from './ProductFormNew.jsx';
import { API_URLS } from '../config/api-config.js';
import { getProductsPage } from '../services/productService.js';
import { getCategories } from '../services/categoryService.js';
import { useNavigate } from 'react-router-dom';
import { exportLensesExcel } from '../services/excelService.js';
import BulkStockSyncModal from './BulkStockSyncModal.jsx';

// Reducer for managing selected IDs
const selectedIdsReducer = (state, action) => {
    switch (action.type) {
        case 'TOGGLE':
            const productId = typeof action.payload === 'string' ? parseInt(action.payload, 10) : action.payload;
            const newState = new Set(state);
            if (newState.has(productId)) {
                newState.delete(productId);
            } else {
                newState.add(productId);
            }
            return newState;
        case 'SET_ALL':
            return new Set(action.payload.map(p => typeof p.id === 'string' ? parseInt(p.id, 10) : p.id));
        case 'CLEAR':
            return new Set();
        default:
            return state;
    }
};

const ProductAll = () => {
    const navigate = useNavigate();
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showCreateForm, setShowCreateForm] = useState(false);
    const [copyProduct, setCopyProduct] = useState(null);
    const [showCopyForm, setShowCopyForm] = useState(false);

    // Selection state using reducer
    const [selectedIds, dispatch] = useReducer(selectedIdsReducer, new Set());

    // Pagination state
    const [page, setPage] = useState(0);
    const [size, setSize] = useState(50); // по умолчанию побольше
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);

    // Bulk sync state
    const [showBulkSyncModal, setShowBulkSyncModal] = useState(false);

    useEffect(() => {
        fetchPage(page, size);
        fetchCategories();
    }, [page, size]);

    const fetchPage = async (pageNum = 0, pageSize = 50) => {
        try {
            setLoading(true);
            const pageData = await getProductsPage(pageNum, pageSize);
            setProducts(pageData.content || []);
            setTotalPages(pageData.totalPages || 0);
            setTotalElements(pageData.totalElements || 0);
            dispatch({ type: 'CLEAR' });
        } catch (error) {
            console.error('Error fetching products page:', error);
        } finally {
            setLoading(false);
        }
    };

    const fetchCategories = async () => {
        try {
            const categoriesData = await getCategories();
            setCategories(categoriesData);
        } catch (error) {
            console.error('Error fetching categories:', error);
        }
    };

    const handleDelete = async (productId) => {
        if (window.confirm('Вы уверены, что хотите удалить этот товар?')) {
            try {
                await axios.delete(API_URLS.PRODUCTS.BY_ID(productId));
                fetchPage(page, size);
                alert('Товар успешно удален!');
            } catch (error) {
                console.error('Error deleting product:', error);
                alert('Ошибка при удалении товара.');
            }
        }
    };

    const handleCopyProduct = (product) => {
        setCopyProduct(product);
        setShowCopyForm(true);
    };

    const handleProductCreated = () => {
        fetchPage(page, size);
        if (showCopyForm) {
        } else {
            setShowCreateForm(false);
        }
    };

    const handleCardDoubleClick = (product) => {
        navigate(`/product/${product.id}`, { state: { product } });
    };

    // Checkbox selection
    const toggleSelect = useCallback((id) => {
        dispatch({ type: 'TOGGLE', payload: id });
    }, []);

    const isSelected = (id) => {
        // Ensure consistent type handling - convert string IDs to numbers
        const productId = typeof id === 'string' ? parseInt(id, 10) : id;
        return selectedIds.has(productId);
    };

    const selectAllOnPage = () => {
        dispatch({ type: 'SET_ALL', payload: products });
    };

    const clearSelection = () => dispatch({ type: 'CLEAR' });

    // Drag selection (rectangle)
    const gridRef = useRef(null);
    const selectionRef = useRef(null);
    const startPointRef = useRef(null);

    useEffect(() => {
        const grid = gridRef.current;
        if (!grid) return;

        const onMouseDown = (e) => {
            if (e.button !== 0) return;
            const rect = grid.getBoundingClientRect();
            startPointRef.current = { x: e.clientX - rect.left, y: e.clientY - rect.top };

            const sel = document.createElement('div');
            sel.style.position = 'absolute';
            sel.style.border = '1px dashed #60a5fa';
            sel.style.background = 'rgba(99,102,241,0.15)';
            sel.style.pointerEvents = 'none';
            sel.style.left = `${startPointRef.current.x}px`;
            sel.style.top = `${startPointRef.current.y}px`;
            sel.style.width = '0px';
            sel.style.height = '0px';
            selectionRef.current = sel;
            grid.appendChild(sel);

            const onMouseMove = (ev) => {
                const curX = ev.clientX - rect.left;
                const curY = ev.clientY - rect.top;
                const x = Math.min(curX, startPointRef.current.x);
                const y = Math.min(curY, startPointRef.current.y);
                const w = Math.abs(curX - startPointRef.current.x);
                const h = Math.abs(curY - startPointRef.current.y);
                sel.style.left = `${x}px`;
                sel.style.top = `${y}px`;
                sel.style.width = `${w}px`;
                sel.style.height = `${h}px`;
            };

            const onMouseUp = (ev) => {
                const selRect = selectionRef.current.getBoundingClientRect();
                // выбрать карточки, которые пересекаются с selRect
                const cards = grid.querySelectorAll('[data-card-id]');
                const selectedCards = [];
                cards.forEach((el) => {
                    const r = el.getBoundingClientRect();
                    const intersects = !(selRect.right < r.left || selRect.left > r.right || selRect.bottom < r.top || selRect.top > r.bottom);
                    if (intersects) {
                        const id = Number(el.getAttribute('data-card-id'));
                        selectedCards.push({ id });
                    }
                });
                
                // Add selected cards to current selection
                const currentIds = Array.from(selectedIds);
                const newIds = [...new Set([...currentIds, ...selectedCards.map(c => c.id)])];
                dispatch({ type: 'SET_ALL', payload: newIds.map(id => ({ id })) });

                document.removeEventListener('mousemove', onMouseMove);
                document.removeEventListener('mouseup', onMouseUp);
                if (selectionRef.current) {
                    selectionRef.current.remove();
                    selectionRef.current = null;
                }
                startPointRef.current = null;
            };

            document.addEventListener('mousemove', onMouseMove);
            document.addEventListener('mouseup', onMouseUp);
        };

        grid.addEventListener('mousedown', onMouseDown);
        return () => grid.removeEventListener('mousedown', onMouseDown);
    }, [products, selectedIds]);

    const downloadExcel = async () => {
        if (selectedIds.size === 0) { alert('Выберите товары'); return; }
        const blob = await exportLensesExcel(Array.from(selectedIds));
        const url = window.URL.createObjectURL(new Blob([blob]));
        const a = document.createElement('a');
        a.href = url;
        a.download = selectedIds.size === 1 ? `lenses_${Array.from(selectedIds)[0]}.xlsx` : 'lenses_export.xlsx';
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
    };

    const handleBulkSync = () => {
        if (selectedIds.size === 0) {
            alert('Выберите товары для синхронизации');
            return;
        }
        setShowBulkSyncModal(true);
    };

    const PaginationControls = () => (
        <div className="flex items-center justify-between mt-6">
            <div className="text-gray-400 text-sm">
                Всего: {totalElements} • Страниц: {totalPages}
            </div>
            <div className="flex items-center space-x-2">
                <button
                    className="px-3 py-1 bg-gray-700 text-white rounded disabled:opacity-50"
                    onClick={() => setPage((p) => Math.max(0, p - 1))}
                    disabled={page <= 0}
                >
                    ← Предыдущая
                </button>
                <span className="text-gray-300 text-sm">Стр. {page + 1} из {Math.max(totalPages, 1)}</span>
                <button
                    className="px-3 py-1 bg-gray-700 text-white rounded disabled:opacity-50"
                    onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                    disabled={page >= totalPages - 1}
                >
                    Следующая →
                </button>
                <select
                    className="ml-2 bg-gray-700 text-white rounded px-2 py-1"
                    value={size}
                    onChange={(e) => { setPage(0); setSize(parseInt(e.target.value, 10)); }}
                >
                    {[10, 20, 50, 100, 500, 1000].map(s => (
                        <option key={s} value={s}>{s === 1000 ? 'Все (1000)' : `${s} на странице`}</option>
                    ))}
                </select>
            </div>
        </div>
    );

    if (loading) {
        return (
            <div className="flex justify-center items-center h-64">
                <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-indigo-500"></div>
            </div>
        );
    }

    if (showCreateForm) {
  return (
    <div className="container mx-auto px-4 py-8">
                <div className="mb-6">
                    <button
                        onClick={() => setShowCreateForm(false)}
                        className="px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700"
                    >
                        ← Назад к списку
                    </button>
      </div>
                <ProductFormNew 
        categories={categories}
                    onProductCreated={handleProductCreated}
                />
            </div>
        );
    }

    if (showCopyForm) {
        return (
            <div className="container mx-auto px-4 py-8">
                <div className="mb-6">
                    <button
                        onClick={() => {
                            setShowCopyForm(false);
                            setCopyProduct(null);
                        }}
                        className="px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700"
                    >
                        ← Назад к списку
                    </button>
                </div>
                <ProductFormNew 
                    categories={categories} 
                    onProductCreated={handleProductCreated}
                    initialProduct={copyProduct}
                />
            </div>
        );
    }

    return (
        <div className="container mx-auto px-4 py-8">
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-3xl font-bold text-white">Все товары</h1>
                <div className="flex items-center space-x-2">
                    <button
                        onClick={() => setShowCreateForm(true)}
                        className="px-6 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700"
                    >
                        Добавить товар
                    </button>
                    <button
                        onClick={selectAllOnPage}
                        className="px-6 py-2 bg-gray-700 text-white rounded-md hover:bg-gray-600"
                    >
                        Выделить всё на странице
                    </button>
                <button
                        onClick={clearSelection}
                        className="px-6 py-2 bg-gray-700 text-white rounded-md hover:bg-gray-600"
                >
                        Очистить выделение
                </button>
                    <div className="relative">
                        <button
                            onClick={handleBulkSync}
                            className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50"
                            disabled={selectedIds.size === 0}
                        >
                            Действия с выбранными ▼
                        </button>
                        <div className="absolute right-0 mt-2 w-48 bg-gray-800 rounded-md shadow-lg z-10">
                            <div className="py-1">
                                <button
                                    onClick={downloadExcel}
                                    className="block w-full text-left px-4 py-2 text-sm text-white hover:bg-gray-700"
                                    disabled={selectedIds.size === 0}
                                >
                                    📊 Экспорт в Excel
                                </button>
                                <button
                                    onClick={handleBulkSync}
                                    className="block w-full text-left px-4 py-2 text-sm text-white hover:bg-gray-700"
                                    disabled={selectedIds.size === 0}
                                >
                                    🔄 Синхронизация остатков
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <PaginationControls />

            <div ref={gridRef} className="relative">
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 select-none">
                    {products.map((product) => (
                        <div
                            key={product.id}
                            data-card-id={product.id}
                            className={`bg-gray-800 rounded-lg shadow-lg p-6 cursor-pointer border ${isSelected(product.id) ? 'border-indigo-400' : 'border-transparent'}`}
                            onDoubleClick={() => handleCardDoubleClick(product)}
                            onClick={(e) => { 
                                // Don't handle clicks that originated from checkboxes
                                if (e.target.type === 'checkbox') {
                                    return;
                                }
                                if (e.shiftKey) toggleSelect(product.id); 
                            }}
                        >
                            <div className="flex items-center justify-between mb-4">
                                <div className="flex items-center space-x-2">
                                    <input
                                        type="checkbox"
                                        checked={isSelected(product.id)}
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            e.preventDefault();
                                            toggleSelect(product.id);
                                        }}
                                    />
                                    <h3 className="text-lg font-semibold text-white truncate">
                                        {product.name}
                                    </h3>
                                </div>
                                <div className="flex items-center space-x-2">
                                    <button
                                        onClick={(e) => { e.stopPropagation(); handleCopyProduct(product); }}
                                        className="px-2 py-1 bg-blue-600 text-white text-xs rounded hover:bg-blue-700"
                                        title="Копировать товар"
                                    >
                                        Копировать
                                    </button>
                                    <button
                                        onClick={(e) => { e.stopPropagation(); window.location.href = `/edit-product/${product.id}`; }}
                                        className="px-2 py-1 bg-yellow-600 text-white text-xs rounded hover:bg-yellow-700"
                                        title="Редактировать товар"
                                    >
                                        Редактировать
                                    </button>
                                    <button
                                        onClick={(e) => { e.stopPropagation(); handleDelete(product.id); }}
                                        className="px-2 py-1 bg-red-600 text-white text-xs rounded hover:bg-red-700"
                                        title="Удалить товар"
                                    >
                                        Удалить
                                    </button>
                                </div>
                            </div>

                            {product.image && (
                                <div className="mb-4">
                                    <img
                                        src={(() => {
                                            const img = product.image;
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
                                        })()}
                                        alt={product.name}
                                        className="w-full h-32 object-cover rounded"
                                        onError={(e) => { e.target.style.display = 'none'; }}
                                    />
                                </div>
                            )}

                            <div className="space-y-2 text-sm">
                                <div className="flex justify-between">
                                    <span className="text-gray-400">Артикул:</span>
                                    <span className="text-white font-mono">{product.article}</span>
                                </div>
                                <div className="flex justify-between">
                                    <span className="text-gray-400">Цена:</span>
                                    <span className="text-green-400 font-semibold">
                                        {product.price ? new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB' }).format(product.price) : 'Не указана'}
                                    </span>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            <PaginationControls />

            {/* Bulk Stock Sync Modal */}
            {showBulkSyncModal && (
                <BulkStockSyncModal
                    productIds={Array.from(selectedIds)}
                    onClose={() => setShowBulkSyncModal(false)}
                />
            )}
    </div>
  );
};

export default ProductAll;