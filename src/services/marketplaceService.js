import { microservicesHttpClient } from '../utils/http-client.js';

/**
 * Универсальный сервис для работы с маркетплейсами
 * @param {string} marketplace - Название маркетплейса (например, 'ozon', 'yandex')
 * @param {Object} endpoints - Конфигурация эндпоинтов для конкретного маркетплейса
 * @returns {Object} Сервис с методами для работы с маркетплейсом
 */
export const createMarketplaceService = (marketplace, endpoints) => {
  return {
    /**
     * Получить список заказов FBO
     */
    async getFboOrdersList(requestData) {
      const response = await microservicesHttpClient.post(endpoints.ordersFboList, requestData);
      return response.data;
    },

    /**
     * Загрузить исторические данные заказов
     */
    async backfillFboOrders(dateRange, pageSize = 100) {
      const response = await microservicesHttpClient.post(
        `${endpoints.ordersFboBackfill}?pageSize=${pageSize}`, 
        dateRange
      );
      return response.data;
    },

    /**
     * Получить учетные данные
     */
    async getCredentials() {
      const response = await microservicesHttpClient.get(endpoints.credentials);
      return response.data;
    },

    /**
     * Сохранить учетные данные
     */
    async saveCredentials(credentials) {
      const response = await microservicesHttpClient.post(endpoints.credentials, credentials);
      return response.data;
    },

    /**
     * Принудительная синхронизация
     */
    async forceSync() {
      const response = await microservicesHttpClient.post(endpoints.syncForce);
      return response.data;
    },

    /**
     * Получить статус синхронизации
     */
    async getSyncStatus() {
      const response = await microservicesHttpClient.get(endpoints.syncStatus);
      return response.data;
    },

    /**
     * Получить информацию о маркетплейсе
     */
    getMarketplaceInfo() {
      return {
        name: marketplace,
        endpoints
      };
    }
  };
};

/**
 * Фабрика для создания сервисов маркетплейсов
 */
export const marketplaceServiceFactory = {
  /**
   * Создать сервис для Ozon
   */
  createOzonService() {
    return createMarketplaceService('ozon', {
      ordersFboList: '/api/ozon/orders/fbo/list',
      ordersFboBackfill: '/api/ozon/orders/fbo/backfill',
      credentials: '/api/ozon/credentials',
      syncForce: '/api/ozon/sync/force',
      syncStatus: '/api/ozon/sync/status'
    });
  },

  /**
   * Создать сервис для Yandex
   */
  createYandexService() {
    return createMarketplaceService('yandex', {
      ordersFboList: '/api/yandex/orders/fbo/list',
      ordersFboBackfill: '/api/yandex/orders/fbo/backfill',
      credentials: '/api/yandex/credentials',
      syncForce: '/api/yandex/sync/force',
      syncStatus: '/api/yandex/sync/status'
    });
  },

  /**
   * Создать сервис для произвольного маркетплейса
   */
  createCustomService(marketplace, customEndpoints) {
    return createMarketplaceService(marketplace, customEndpoints);
  }
};

