import React from 'react';
import { yandexService } from '../services/yandexService.js';
import MarketplaceOrders from './MarketplaceOrders.jsx';

const YandexOrders = () => {
  return (
    <MarketplaceOrders
      marketplace="YANDEX"
      marketplaceService={yandexService}
      marketplaceName="Yandex"
      marketplaceIcon="🟡"
      syncEndpoint="/api/yandex/sync/force"
      backfillEndpoint="/api/yandex/orders/fbo/backfill"
    />
  );
};

export default YandexOrders;

