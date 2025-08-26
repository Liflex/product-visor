import { marketplaceServiceFactory } from './marketplaceService.js';

// Создаем сервис для Ozon используя фабрику
export const ozonService = marketplaceServiceFactory.createOzonService();
