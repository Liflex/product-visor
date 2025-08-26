import httpClient from '../utils/http-client.js';

class ProductStockService {
    /**
     * Получить остатки товара по всем складам
     */
    async getProductStocks(productId) {
        const response = await httpClient.get(`/product-stocks/product/${productId}`);
        return response.data;
    }

    /**
     * Получить остаток товара по ID
     */
    async getProductStockById(productStockId) {
        const response = await httpClient.get(`/product-stocks/${productStockId}`);
        return response.data;
    }
    
    /**
     * Создать новый остаток товара
     */
    async createProductStock(productStockData) {
        const response = await httpClient.post('/product-stocks', productStockData);
        return response.data;
    }
    
    /**
     * Обновить остаток товара
     */
    async updateProductStock(productStockId, productStockData) {
        const response = await httpClient.put(`/product-stocks/${productStockId}`, productStockData);
        return response.data;
    }
    
    /**
     * Удалить остаток товара
     */
    async deleteProductStock(productStockId) {
        const response = await httpClient.delete(`/product-stocks/${productStockId}`);
        return response.data;
    }
}

export const productStockService = new ProductStockService();
