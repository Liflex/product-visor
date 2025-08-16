import { microservicesHttpClient } from '../utils/http-client.js';
import { API_URLS } from '../config/api-config.js';

export const ozonService = {
    async getFboOrdersList(requestData) {
        const response = await microservicesHttpClient.post(API_URLS.OZON.ORDERS_FBO_LIST(), requestData);
        return response.data;
    },

    async backfillFboOrders(dateRange, pageSize = 100) {
        const response = await microservicesHttpClient.post(`${API_URLS.OZON.ORDERS_FBO_BACKFILL()}?pageSize=${pageSize}`, dateRange);
        return response.data;
    }
};
