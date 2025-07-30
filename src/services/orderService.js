import httpClient from '../utils/http-client.js';

const API_BASE_URL = 'http://localhost:8085/api/orders';

export const orderService = {
    async createOrder(orderData) {
        return httpClient.post(API_BASE_URL, orderData);
    },

    async getAllOrders() {
        return httpClient.get(API_BASE_URL);
    },

    async getOrderById(id) {
        return httpClient.get(`${API_BASE_URL}/${id}`);
    },

    async getOrdersByProductId(productId) {
        return httpClient.get(`${API_BASE_URL}/product/${productId}`);
    },

    async getOrdersByMarketId(marketId) {
        return httpClient.get(`${API_BASE_URL}/market/${marketId}`);
    },

    async getOrderByBarcode(orderBarcode) {
        return httpClient.get(`${API_BASE_URL}/barcode/${orderBarcode}`);
    }
}; 