import { microservicesHttpClient } from '../utils/http-client.js';
import { API_URLS } from '../config/api-config.js';

export const orderService = {
    async createOrder(orderData) {
        const response = await microservicesHttpClient.post(API_URLS.ORDERS.BASE(), orderData);
        return response.data;
    },

    async getAllOrders(page = 0, size = 20, status = null, dateFrom = null, dateTo = null) {
        const baseUrl = API_URLS.ORDERS.BASE();
        const params = new URLSearchParams({
            page: page.toString(),
            size: size.toString()
        });
        
        if (status) params.append('status', status);
        if (dateFrom) params.append('dateFrom', dateFrom);
        if (dateTo) params.append('dateTo', dateTo);
        
        const url = `${baseUrl}?${params.toString()}`;
        const response = await microservicesHttpClient.get(url);
        return response.data;
    },

    async getOrdersByMarket(market, page = 0, size = 20, status = null, dateFrom = null, dateTo = null) {
        const params = new URLSearchParams({
            page: page.toString(),
            size: size.toString()
        });
        
        if (status) params.append('status', status);
        if (dateFrom) params.append('dateFrom', dateFrom);
        if (dateTo) params.append('dateTo', dateTo);
        
        const response = await microservicesHttpClient.get(`${API_URLS.ORDERS.BY_MARKET(market)}?${params.toString()}`);
        return response.data;
    },

    async getOrderById(id) {
        const response = await microservicesHttpClient.get(API_URLS.ORDERS.BY_ID(id));
        return response.data;
    },

    async getOrdersByProductId(productId) {
        const response = await microservicesHttpClient.get(`${API_URLS.ORDERS.BASE()}/product/${productId}`);
        return response.data;
    },

    async getOrdersByMarketId(marketId) {
        const response = await microservicesHttpClient.get(API_URLS.ORDERS.BY_MARKET(marketId));
        return response.data;
    },

    async getOrderByBarcode(orderBarcode) {
        const response = await microservicesHttpClient.get(`${API_URLS.ORDERS.BASE()}/barcode/${orderBarcode}`);
        return response.data;
    }
}; 