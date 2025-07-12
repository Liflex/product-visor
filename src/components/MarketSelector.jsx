import React, { useState, useEffect } from 'react';
import { useMarkets } from '../hooks/use-markets.js';
import InputField from './ui/input-field.jsx';

const MarketSelector = ({ selectedMarkets, onMarketsChange, quantity, onQuantityChange }) => {
    const { markets, loading } = useMarkets();
    const [localMarkets, setLocalMarkets] = useState(selectedMarkets || []);
    const [manualQuantity, setManualQuantity] = useState(quantity || 0);



    useEffect(() => {
        setLocalMarkets(selectedMarkets || []);
    }, [selectedMarkets]);

    useEffect(() => {
        setManualQuantity(quantity || 0);
    }, [quantity]);

    // Инициализируем manualQuantity из quantity при первой загрузке
    useEffect(() => {
        if (quantity !== undefined && quantity !== null) {
            setManualQuantity(quantity);
        }
    }, [quantity]);

    const handleMarketToggle = (marketId) => {
        const isSelected = localMarkets.some(m => m.market?.id === marketId);
        
        if (isSelected) {
            // Удаляем маркет
            const newMarkets = localMarkets.filter(m => m.market?.id !== marketId);
            setLocalMarkets(newMarkets);
            onMarketsChange(newMarkets);
            
            // Пересчитываем общее количество
            const totalQuantity = newMarkets.reduce((sum, m) => sum + (m.quantity || 0), 0);
            onQuantityChange(totalQuantity);
        } else {
            // Добавляем маркет
            const market = markets.find(m => m.id === marketId);
            if (market) {
                const newMarket = {
                    id: null, // Новый ProductMarket
                    quantity: 0,
                    price: 0.0,
                    market: {
                        id: market.id,
                        name: market.name,
                        description: market.description,
                        imageUrl: market.imageUrl,
                        image: market.image
                    }
                };
                const newMarkets = [...localMarkets, newMarket];
                setLocalMarkets(newMarkets);
                onMarketsChange(newMarkets);
            }
        }
    };

    const handleMarketQuantityChange = (marketId, newQuantity) => {
        const newMarkets = localMarkets.map(m => 
            m.market?.id === marketId 
                ? { ...m, quantity: parseInt(newQuantity) || 0 }
                : m
        );
        setLocalMarkets(newMarkets);
        onMarketsChange(newMarkets);
        
        // Пересчитываем общее количество
        const totalQuantity = newMarkets.reduce((sum, m) => sum + (m.quantity || 0), 0);
        onQuantityChange(totalQuantity);
    };

    const handleMarketPriceChange = (marketId, newPrice) => {
        const newMarkets = localMarkets.map(m => 
            m.market?.id === marketId 
                ? { ...m, price: parseFloat(newPrice) || 0.0 }
                : m
        );
        setLocalMarkets(newMarkets);
        onMarketsChange(newMarkets);
    };

    const handleManualQuantityChange = (newQuantity) => {
        setManualQuantity(parseInt(newQuantity) || 0);
        onQuantityChange(parseInt(newQuantity) || 0);
    };

    if (loading) {
        return <div className="text-gray-500">Loading markets...</div>;
    }

    const hasMarkets = localMarkets.length > 0;
    const totalQuantity = localMarkets.reduce((sum, m) => sum + (m.quantity || 0), 0);
    
    // Если есть selectedMarkets, но localMarkets еще пустой, используем quantity из props
    const displayQuantity = hasMarkets ? totalQuantity : (selectedMarkets && selectedMarkets.length > 0 ? 
        selectedMarkets.reduce((sum, m) => sum + (m.quantity || 0), 0) : (quantity || manualQuantity));
    


    return (
        <div className="space-y-4">
            <div className="border-b border-gray-300 pb-4">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">Markets</h3>
                
                {/* Ручное управление количеством */}
                <div className="mb-4">
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                        Total Quantity
                    </label>
                    <input
                        type="number"
                        value={displayQuantity}
                        onChange={(e) => handleManualQuantityChange(e.target.value)}
                        disabled={hasMarkets}
                        className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                            hasMarkets 
                                ? 'bg-gray-100 text-gray-500 cursor-not-allowed' 
                                : 'border-gray-300'
                        }`}
                    />
                    {(hasMarkets || (selectedMarkets && selectedMarkets.length > 0)) && (
                        <p className="text-sm text-gray-500 mt-1">
                            Quantity is calculated from markets
                        </p>
                    )}
                    {!hasMarkets && (!selectedMarkets || selectedMarkets.length === 0) && (
                        <p className="text-sm text-gray-500 mt-1">
                            Enter quantity manually
                        </p>
                    )}
                </div>

                {/* Список маркетов */}
                <div className="space-y-2">
                    {markets.map((market) => {
                        const isSelected = localMarkets.some(m => m.market?.id === market.id);
                        const selectedMarket = localMarkets.find(m => m.market?.id === market.id);
                        
                        return (
                            <div key={market.id} className="border rounded-lg p-4">
                                <div className="flex items-center justify-between mb-3">
                                    <div className="flex items-center space-x-3">
                                        <input
                                            type="checkbox"
                                            checked={isSelected}
                                            onChange={() => handleMarketToggle(market.id)}
                                            className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                                        />
                                        <div>
                                            <h4 className="font-medium text-gray-900">{market.name}</h4>
                                            {market.description && (
                                                <p className="text-sm text-gray-500">{market.description}</p>
                                            )}
                                        </div>
                                    </div>
                                </div>
                                
                                {isSelected && (
                                    <div className="grid grid-cols-2 gap-4">
                                        <div>
                                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                                Quantity
                                            </label>
                                            <input
                                                type="number"
                                                value={selectedMarket?.quantity || 0}
                                                onChange={(e) => handleMarketQuantityChange(market.id, e.target.value)}
                                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                                min="0"
                                            />
                                        </div>
                                        <div>
                                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                                Price
                                            </label>
                                            <input
                                                type="number"
                                                step="0.01"
                                                value={selectedMarket?.price || 0}
                                                onChange={(e) => handleMarketPriceChange(market.id, e.target.value)}
                                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                                min="0"
                                            />
                                        </div>
                                    </div>
                                )}
                            </div>
                        );
                    })}
                </div>
            </div>
        </div>
    );
};

export default MarketSelector; 