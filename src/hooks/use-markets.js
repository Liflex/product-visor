import { useState, useEffect } from 'react';
import { marketService } from '../services/marketService.js';

export const useMarkets = () => {
    const [markets, setMarkets] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const fetchMarkets = async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await marketService.getAllMarkets();
            setMarkets(data);
        } catch (err) {
            setError(err.message || 'Failed to fetch markets');
            console.error('Error fetching markets:', err);
        } finally {
            setLoading(false);
        }
    };

    const createMarket = async (marketData, imageFile = null) => {
        setLoading(true);
        setError(null);
        try {
            const newMarket = await marketService.createMarket(marketData, imageFile);
            setMarkets(prev => [...prev, newMarket]);
            return newMarket;
        } catch (err) {
            setError(err.message || 'Failed to create market');
            throw err;
        } finally {
            setLoading(false);
        }
    };

    const updateMarket = async (id, marketData, imageFile = null) => {
        setLoading(true);
        setError(null);
        try {
            const updatedMarket = await marketService.updateMarket(id, marketData, imageFile);
            setMarkets(prev => prev.map(market => 
                market.id === id ? updatedMarket : market
            ));
            return updatedMarket;
        } catch (err) {
            setError(err.message || 'Failed to update market');
            throw err;
        } finally {
            setLoading(false);
        }
    };

    const deleteMarket = async (id) => {
        setLoading(true);
        setError(null);
        try {
            await marketService.deleteMarket(id);
            setMarkets(prev => prev.filter(market => market.id !== id));
        } catch (err) {
            setError(err.message || 'Failed to delete market');
            throw err;
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchMarkets();
    }, []);

    return {
        markets,
        loading,
        error,
        fetchMarkets,
        createMarket,
        updateMarket,
        deleteMarket
    };
}; 