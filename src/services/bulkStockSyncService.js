import httpClient from '../utils/http-client.js';

/**
 * Сервис для массовой синхронизации остатков
 */
export const bulkStockSyncService = {
    /**
     * Массовая синхронизация остатков для выбранных товаров
     * @param {Array<number>} productIds - Массив ID товаров
     * @param {string} stockType - Тип остатка (FBS, YANDEX_FBO, OZON_FBO)
     * @returns {Promise<Object>} Статистика синхронизации
     */
    async bulkSyncStocks(productIds, stockType) {
        try {
            const response = await httpClient.post('/bulk-stock-sync/sync', {
                productIds,
                stockType
            });
            return response.data;
        } catch (error) {
            console.error('Error in bulk stock sync:', error);
            throw new Error(error.response?.data?.message || 'Ошибка при массовой синхронизации');
        }
    },

    /**
     * Получить статистику синхронизации для выбранных товаров
     * @param {Array<number>} productIds - Массив ID товаров
     * @param {string} stockType - Тип остатка
     * @returns {Promise<Object>} Статистика
     */
    async getSyncStats(productIds, stockType) {
        try {
            const response = await httpClient.get('/bulk-stock-sync/stats', {
                params: {
                    productIds: productIds.join(','),
                    stockType
                }
            });
            return response.data;
        } catch (error) {
            console.error('Error getting sync stats:', error);
            throw new Error(error.response?.data?.message || 'Ошибка при получении статистики');
        }
    }
};
