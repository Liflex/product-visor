import React, { useState, useEffect } from 'react';
import { orderService } from '../services/orderService.js';
import { marketService } from '../services/marketService.js';
import BarcodeScanner from './BarcodeScanner.jsx';
import useBarcodeScanner from '../hooks/use-barcode-scanner.js';

const CreateOrderModal = ({ isOpen, onClose, product, onOrderCreated }) => {
    const [selectedMarket, setSelectedMarket] = useState(null);
    const [orderBarcode, setOrderBarcode] = useState('');
    const [price, setPrice] = useState('');
    const [markets, setMarkets] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [isScanning, setIsScanning] = useState(false);

    const { scannedBarcode, resetBarcode } = useBarcodeScanner();

    useEffect(() => {
        if (product && product.productMarkets) {
            setMarkets(product.productMarkets.map(pm => pm.market));
        }
    }, [product]);

    useEffect(() => {
        if (scannedBarcode && isScanning) {
            setOrderBarcode(scannedBarcode);
            setIsScanning(false);
            resetBarcode();
        }
    }, [scannedBarcode, isScanning, resetBarcode]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        if (!selectedMarket || !orderBarcode || !price) {
            setError('Пожалуйста, заполните все поля');
            return;
        }

        setLoading(true);
        setError('');

        try {
            const orderData = {
                product: { id: product.id },
                market: { id: selectedMarket.id },
                orderBarcode: orderBarcode,
                price: parseFloat(price)
            };

            await orderService.createOrder(orderData);
            onOrderCreated();
            onClose();
        } catch (err) {
            setError(err.message || 'Ошибка при создании заказа');
        } finally {
            setLoading(false);
        }
    };

    const handleMarketChange = (market) => {
        setSelectedMarket(market);
        // Устанавливаем цену из выбранного маркета
        if (product && product.productMarkets) {
            const productMarket = product.productMarkets.find(pm => pm.market.id === market.id);
            if (productMarket) {
                setPrice(productMarket.price.toString());
            }
        }
    };

    const startScanning = () => {
        setIsScanning(true);
        resetBarcode();
    };

    const stopScanning = () => {
        setIsScanning(false);
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-gray-800 border border-gray-600 rounded-lg p-6 w-full max-w-md mx-4">
                <div className="flex justify-between items-center mb-4">
                    <h2 className="text-xl font-bold text-white">Оформить заказ</h2>
                    <button
                        onClick={onClose}
                        className="text-gray-400 hover:text-white"
                    >
                        ✕
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-300 mb-1">
                            Товар
                        </label>
                        <input
                            type="text"
                            value={product?.name || ''}
                            disabled
                            className="w-full px-3 py-2 border border-gray-600 rounded-md bg-gray-700 text-white"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-300 mb-1">
                            Магазин
                        </label>
                        <select
                            value={selectedMarket?.id || ''}
                            onChange={(e) => {
                                const market = markets.find(m => m.id === parseInt(e.target.value));
                                handleMarketChange(market);
                            }}
                            className="w-full px-3 py-2 border border-gray-600 rounded-md bg-gray-700 text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                            required
                        >
                            <option value="">Выберите магазин</option>
                            {markets.map(market => (
                                <option key={market.id} value={market.id} className="bg-gray-700 text-white">
                                    {market.name}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-300 mb-1">
                            Цена
                        </label>
                        <input
                            type="number"
                            step="0.01"
                            value={price}
                            onChange={(e) => setPrice(e.target.value)}
                            className="w-full px-3 py-2 border border-gray-600 rounded-md bg-gray-700 text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                            required
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-300 mb-1">
                            Штрихкод заказа
                        </label>
                        <div className="flex space-x-2">
                            <input
                                type="text"
                                value={orderBarcode}
                                onChange={(e) => setOrderBarcode(e.target.value)}
                                className="flex-1 px-3 py-2 border border-gray-600 rounded-md bg-gray-700 text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                                placeholder="Отсканируйте или введите штрихкод"
                                required
                            />
                            <button
                                type="button"
                                onClick={startScanning}
                                className="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500"
                            >
                                📷
                            </button>
                        </div>
                    </div>

                    {isScanning && (
                        <div className="border-2 border-blue-500 rounded-lg p-4 bg-gray-700">
                            <p className="text-sm text-gray-300 mb-2">Сканирование штрихкода...</p>
                            <BarcodeScanner onBarcodeScanned={(barcode) => {
                                setOrderBarcode(barcode);
                                setIsScanning(false);
                            }} />
                            <button
                                type="button"
                                onClick={stopScanning}
                                className="mt-2 px-3 py-1 bg-red-500 text-white rounded text-sm hover:bg-red-600"
                            >
                                Отменить сканирование
                            </button>
                        </div>
                    )}

                    {error && (
                        <div className="text-red-400 text-sm">{error}</div>
                    )}

                    <div className="flex space-x-3 pt-4">
                        <button
                            type="button"
                            onClick={onClose}
                            className="flex-1 px-4 py-2 border border-gray-600 rounded-md text-gray-300 hover:bg-gray-700"
                        >
                            Отмена
                        </button>
                        <button
                            type="submit"
                            disabled={loading}
                            className="flex-1 px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 disabled:opacity-50"
                        >
                            {loading ? 'Создание...' : 'Создать заказ'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default CreateOrderModal; 