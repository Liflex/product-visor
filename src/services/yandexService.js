import { marketplaceServiceFactory } from './marketplaceService.js';

// Создаем сервис для Yandex используя фабрику
export const yandexService = marketplaceServiceFactory.createYandexService();

