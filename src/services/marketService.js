import httpClient from '../utils/http-client.js';

const API_BASE_URL = '/market';

export const marketService = {
    // Получить все маркеты
    async getAllMarkets() {
        try {
            const response = await httpClient.get(API_BASE_URL);
            return response.data;
        } catch (error) {
            console.error('Error fetching markets:', error);
            throw error;
        }
    },

    // Получить маркет по ID
    async getMarketById(id) {
        try {
            const response = await httpClient.get(`${API_BASE_URL}/${id}`);
            return response.data;
        } catch (error) {
            console.error('Error fetching market:', error);
            throw error;
        }
    },

    // Создать новый маркет
    async createMarket(marketData, imageFile = null) {
        try {
            const formData = new FormData();
            formData.append('marketData', JSON.stringify(marketData));
            
            if (imageFile) {
                formData.append('image', imageFile);
            }

            const response = await httpClient.post(API_BASE_URL, formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            });
            return response.data;
        } catch (error) {
            console.error('Error creating market:', error);
            throw error;
        }
    },

    // Обновить маркет
    async updateMarket(id, marketData, imageFile = null) {
        try {
            const formData = new FormData();
            formData.append('marketData', JSON.stringify(marketData));
            
            if (imageFile) {
                formData.append('image', imageFile);
            }

            const response = await httpClient.put(`${API_BASE_URL}/${id}`, formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            });
            return response.data;
        } catch (error) {
            console.error('Error updating market:', error);
            throw error;
        }
    },

    // Удалить маркет
    async deleteMarket(id) {
        try {
            await httpClient.delete(`${API_BASE_URL}/${id}`);
        } catch (error) {
            console.error('Error deleting market:', error);
            throw error;
        }
    }
}; 