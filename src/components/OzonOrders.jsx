import React from 'react';
import { ozonService } from '../services/ozonService.js';
import MarketplaceOrders from './MarketplaceOrders.jsx';

const OzonOrders = () => {
  return (
    <MarketplaceOrders
      marketplace="OZON"
      marketplaceService={ozonService}
      marketplaceName="Ozon"
      marketplaceIcon="🛒"
      syncEndpoint="/api/ozon/sync/force"
      backfillEndpoint="/api/ozon/orders/fbo/backfill"
    />
  );
};

export default OzonOrders;






