/**
 * Сервис для работы со складами
 */
import httpClient from '../utils/http-client.js';
import { getProducts } from './productService.js';
export const warehouseService = {
    /**
     * Получить все склады компании
     * @param {string} companyId - ID компании
     * @returns {Promise<Array>} Список складов
     */
    async getCompanyWarehouses(companyId) {
        try {
            const response = await httpClient.get(`/warehouses/company/${companyId}`);
            const data = response.data;
            return Array.isArray(data)
                ? data
                : (Array.isArray(data?.content) ? data.content : (Array.isArray(data?.items) ? data.items : []));
        } catch (error) {
            console.error('Error fetching company warehouses:', error);
            throw new Error(error.response?.data?.message || 'Ошибка при получении складов');
        }
    },

    /**
     * Получить домашний склад компании
     * @param {string} companyId - ID компании
     * @returns {Promise<Object>} Домашний склад
     */
    async getHomeWarehouse(companyId) {
        try {
            const response = await httpClient.get(`/warehouses/company/${companyId}/home`);
            return response.data;
        } catch (error) {
            console.error('Error fetching home warehouse:', error);
            throw new Error(error.response?.data?.message || 'Ошибка при получении домашнего склада');
        }
    },

    /**
     * Получить FBS склады компании
     * @param {string} companyId - ID компании
     * @returns {Promise<Array>} Список FBS складов
     */
    async getFbsWarehouses(companyId) {
        try {
            const response = await httpClient.get(`/warehouses/company/${companyId}/fbs`);
            const data = response.data;
            return Array.isArray(data)
                ? data
                : (Array.isArray(data?.content) ? data.content : (Array.isArray(data?.items) ? data.items : []));
        } catch (error) {
            console.error('Error fetching FBS warehouses:', error);
            throw new Error(error.response?.data?.message || 'Ошибка при получении FBS складов');
        }
    },

    /**
     * Получить FBO склады компании
     * @param {string} companyId - ID компании
     * @returns {Promise<Array>} Список FBO складов
     */
    async getFboWarehouses(companyId) {
        try {
            const response = await httpClient.get(`/warehouses/company/${companyId}/fbo`);
            const data = response.data;
            return Array.isArray(data)
                ? data
                : (Array.isArray(data?.content) ? data.content : (Array.isArray(data?.items) ? data.items : []));
        } catch (error) {
            console.error('Error fetching FBO warehouses:', error);
            throw new Error(error.response?.data?.message || 'Ошибка при получении FBO складов');
        }
    },

    /**
     * Создать новый склад
     * @param {Object} warehouse - Данные склада
     * @returns {Promise<Object>} Созданный склад
     */
    async createWarehouse(warehouse) {
        try {
            const response = await httpClient.post('/warehouses', warehouse);
            return response.data;
        } catch (error) {
            console.error('Error creating warehouse:', error);
            throw new Error(error.response?.data?.message || 'Ошибка при создании склада');
        }
    },

    /**
     * Обновить склад
     * @param {string} id - ID склада
     * @param {Object} warehouse - Данные склада
     * @returns {Promise<Object>} Обновленный склад
     */
    async updateWarehouse(id, warehouse) {
        try {
            const response = await httpClient.put(`/warehouses/${id}`, warehouse);
            return response.data;
        } catch (error) {
            console.error('Error updating warehouse:', error);
            throw new Error(error.response?.data?.message || 'Ошибка при обновлении склада');
        }
    },

    /**
     * Удалить склад
     * @param {string} id - ID склада
     * @returns {Promise<void>}
     */
    async deleteWarehouse(id) {
        try {
            await httpClient.delete(`/warehouses/${id}`);
        } catch (error) {
            console.error('Error deleting warehouse:', error);
            throw new Error(error.response?.data?.message || 'Ошибка при удалении склада');
        }
    },

    /**
     * Получить склад по ID
     * @param {string} id - ID склада
     * @returns {Promise<Object>} Склад
     */
    async getWarehouse(id) {
        try {
            const response = await httpClient.get(`/warehouses/${id}`);
            return response.data;
        } catch (error) {
            console.error('Error fetching warehouse:', error);
            throw new Error(error.response?.data?.message || 'Ошибка при получении склада');
        }
    }
};

/**
 * Получить агрегированную статистику по складу/продуктам
 * Возвращает объект вида { totalProducts, totalValue, categories, productsWithMarkets }
 * Реализация по данным продуктов на клиенте, пока нет отдельного backend-эндпоинта
 */
export const getWarehouseStats = async () => {
    const products = await getProducts().catch(() => []);

    const totalProducts = Array.isArray(products) ? products.length : 0;

    // Считаем итоговую стоимость как price * quantity (если доступны), иначе 0
    const totalValue = Array.isArray(products)
        ? products.reduce((sum, p) => {
            const price = Number(p?.price) || 0;
            // Ищем возможные поля количества: quantity или агрегат из stocks
            const quantity = Number(p?.quantity) || Number(p?.totalQuantity) || 0;
            return sum + price * quantity;
        }, 0)
        : 0;

    // Подсчет уникальных категорий, если есть category.id
    const categoryIds = new Set(
        (Array.isArray(products) ? products : [])
            .map(p => p?.category?.id)
            .filter(Boolean)
    );
    const categories = categoryIds.size;

    // Оценка кол-ва товаров, связанных с маркетплейсами (по наличию productMarkets)
    const productsWithMarkets = Array.isArray(products)
        ? products.filter(p => Array.isArray(p?.productMarkets) && p.productMarkets.length > 0).length
        : 0;

    return { totalProducts, totalValue, categories, productsWithMarkets };
};

/**
 * Топ продуктов по стоимости (price * quantity)
 * @param {number} limit
 * @returns {Promise<Array>} Список продуктов с полями { id, name, article, price, quantity, totalValue }
 */
export const getTopProductsByValue = async (limit = 5) => {
    const products = await getProducts().catch(() => []);
    const enriched = (Array.isArray(products) ? products : []).map(p => {
        const price = Number(p?.price) || 0;
        const quantity = Number(p?.quantity) || Number(p?.totalQuantity) || 0;
        const totalValue = price * quantity;
        return {
            id: p?.id ?? `${p?.article || p?.sku || p?.name || 'p'}-${Math.random().toString(36).slice(2)}`,
            name: p?.name || 'Unnamed',
            article: p?.article || p?.sku || '-',
            price,
            quantity,
            totalValue
        };
    });

    return enriched
        .filter(p => p.totalValue > 0)
        .sort((a, b) => b.totalValue - a.totalValue)
        .slice(0, limit);
};
