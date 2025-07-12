import React, { useState } from 'react';
import { useMarkets } from '../hooks/use-markets.js';
import LoadingSpinner from './ui/loading-spinner.jsx';
import ErrorMessage from './ui/error-message.jsx';
import Notification from './ui/notification.jsx';
import MarketForm from './MarketForm.jsx';

const MarketList = () => {
    const { markets, loading, error, deleteMarket } = useMarkets();
    const [showForm, setShowForm] = useState(false);
    const [editingMarket, setEditingMarket] = useState(null);
    const [notification, setNotification] = useState(null);

    const handleEdit = (market) => {
        setEditingMarket(market);
        setShowForm(true);
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this market?')) {
            try {
                await deleteMarket(id);
                setNotification({ type: 'success', message: 'Market deleted successfully' });
            } catch (err) {
                setNotification({ type: 'error', message: 'Failed to delete market' });
            }
        }
    };

    const handleFormClose = () => {
        setShowForm(false);
        setEditingMarket(null);
    };

    const handleFormSuccess = () => {
        setShowForm(false);
        setEditingMarket(null);
        setNotification({ type: 'success', message: 'Market saved successfully' });
    };

    if (loading) return <LoadingSpinner />;
    if (error) return <ErrorMessage message={error} />;

    return (
        <div className="container mx-auto px-4 py-8">
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-3xl font-bold text-gray-900">Markets</h1>
                <button
                    onClick={() => setShowForm(true)}
                    className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
                >
                    Add Market
                </button>
            </div>

            {notification && (
                <Notification
                    type={notification.type}
                    message={notification.message}
                    onClose={() => setNotification(null)}
                />
            )}

            {showForm && (
                <MarketForm
                    market={editingMarket}
                    onClose={handleFormClose}
                    onSuccess={handleFormSuccess}
                />
            )}

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {markets.map((market) => (
                    <div key={market.id} className="bg-white rounded-lg shadow-md overflow-hidden">
                        {market.imageUrl && (
                            <img
                                src={market.imageUrl}
                                alt={market.name}
                                className="w-full h-48 object-cover"
                            />
                        )}
                        <div className="p-6">
                            <h3 className="text-xl font-semibold text-gray-900 mb-2">
                                {market.name}
                            </h3>
                            {market.description && (
                                <p className="text-gray-600 mb-4">{market.description}</p>
                            )}
                            <div className="flex justify-end space-x-2">
                                <button
                                    onClick={() => handleEdit(market)}
                                    className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-1 rounded text-sm"
                                >
                                    Edit
                                </button>
                                <button
                                    onClick={() => handleDelete(market.id)}
                                    className="bg-red-600 hover:bg-red-700 text-white px-3 py-1 rounded text-sm"
                                >
                                    Delete
                                </button>
                            </div>
                        </div>
                    </div>
                ))}
            </div>

            {markets.length === 0 && (
                <div className="text-center py-8">
                    <p className="text-gray-500">No markets found. Create your first market!</p>
                </div>
            )}
        </div>
    );
};

export default MarketList; 