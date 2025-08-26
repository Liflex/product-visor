package ru.dmitartur.library.marketplace.service;

import ru.dmitartur.common.dto.marketplace.StockItemReqest;
import ru.dmitartur.common.dto.marketplace.StockSyncResponse;

public interface BaseStockSyncService {
    
    /**
     * Синхронизировать остатки товаров на маркетплейсе
     */
    StockSyncResponse syncStocks(StockItemReqest request);
    
    /**
     * Получить название маркетплейса
     */
    String getMarketplaceName();
    
    /**
     * Проверить доступность сервиса
     */
    boolean isAvailable();
}
