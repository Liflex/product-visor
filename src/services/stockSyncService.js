import httpClient from '../utils/http-client.js';

/**
 * Stock Sync Service
 * Handles bulk stock synchronization with marketplaces
 */
export const stockSyncService = {
  /**
   * Perform bulk sync for selected products
   * @param {Array} products - Array of products to sync
   * @param {string} stockType - Type of stock to sync (AVAILABLE, RESERVED, etc.)
   * @returns {Promise<Object>} Sync result
   */
  async bulkSync(products, stockType = 'AVAILABLE') {
    try {
      console.log(`🔄 StockSyncService: Starting bulk sync for ${products.length} products with stock type: ${stockType}`);
      
      // Prepare sync request
      const syncRequest = {
        stockType: stockType,
        products: products.map(product => ({
          id: product.id,
          article: product.article,
          name: product.name,
          productStocks: product.productStocks || []
        }))
      };

      console.log('📤 StockSyncService: Sending sync request:', {
        stockType,
        productCount: products.length,
        productIds: products.map(p => p.id)
      });

      // Call backend sync endpoint
      const response = await httpClient.post('/api/stock-sync/bulk', syncRequest);
      
      console.log('✅ StockSyncService: Sync request successful:', response.data);
      
      return {
        success: true,
        syncedCount: response.data.syncedCount || products.length,
        message: response.data.message || 'Bulk sync completed successfully',
        details: response.data
      };
      
    } catch (error) {
      console.error('❌ StockSyncService: Bulk sync failed:', error);
      
      return {
        success: false,
        error: error.response?.data?.message || error.message || 'Unknown error occurred',
        details: error.response?.data
      };
    }
  },

  /**
   * Sync individual product
   * @param {Object} product - Product to sync
   * @param {string} stockType - Type of stock to sync
   * @returns {Promise<Object>} Sync result
   */
  async syncProduct(product, stockType = 'AVAILABLE') {
    try {
      console.log(`🔄 StockSyncService: Syncing product ${product.id} (${product.article}) with stock type: ${stockType}`);
      
      const syncRequest = {
        stockType: stockType,
        product: {
          id: product.id,
          article: product.article,
          name: product.name,
          productStocks: product.productStocks || []
        }
      };

      const response = await httpClient.post('/api/stock-sync/product', syncRequest);
      
      console.log('✅ StockSyncService: Product sync successful:', response.data);
      
      return {
        success: true,
        message: response.data.message || 'Product sync completed successfully',
        details: response.data
      };
      
    } catch (error) {
      console.error('❌ StockSyncService: Product sync failed:', error);
      
      return {
        success: false,
        error: error.response?.data?.message || error.message || 'Unknown error occurred',
        details: error.response?.data
      };
    }
  },

  /**
   * Get sync status and history
   * @returns {Promise<Object>} Sync status
   */
  async getSyncStatus() {
    try {
      console.log('📊 StockSyncService: Fetching sync status...');
      
      const response = await httpClient.get('/api/stock-sync/status');
      
      console.log('✅ StockSyncService: Sync status retrieved:', response.data);
      
      return {
        success: true,
        status: response.data
      };
      
    } catch (error) {
      console.error('❌ StockSyncService: Failed to get sync status:', error);
      
      return {
        success: false,
        error: error.response?.data?.message || error.message || 'Unknown error occurred'
      };
    }
  },

  /**
   * Get sync logs
   * @param {number} limit - Number of logs to retrieve
   * @returns {Promise<Object>} Sync logs
   */
  async getSyncLogs(limit = 50) {
    try {
      console.log(`📝 StockSyncService: Fetching last ${limit} sync logs...`);
      
      const response = await httpClient.get(`/api/stock-sync/logs?limit=${limit}`);
      
      console.log('✅ StockSyncService: Sync logs retrieved:', response.data);
      
      return {
        success: true,
        logs: response.data.logs || []
      };
      
    } catch (error) {
      console.error('❌ StockSyncService: Failed to get sync logs:', error);
      
      return {
        success: false,
        error: error.response?.data?.message || error.message || 'Unknown error occurred',
        logs: []
      };
    }
  },

  /**
   * Test Kafka event publishing
   * @param {Object} product - Product to test
   * @returns {Promise<Object>} Test result
   */
  async testKafkaEvent(product) {
    try {
      console.log(`🧪 StockSyncService: Testing Kafka event for product ${product.id} (${product.article})`);
      
      const testRequest = {
        productId: product.id,
        article: product.article,
        testEvent: true
      };

      const response = await httpClient.post('/api/stock-sync/test-kafka', testRequest);
      
      console.log('✅ StockSyncService: Kafka test successful:', response.data);
      
      return {
        success: true,
        message: response.data.message || 'Kafka event test completed successfully',
        details: response.data
      };
      
    } catch (error) {
      console.error('❌ StockSyncService: Kafka test failed:', error);
      
      return {
        success: false,
        error: error.response?.data?.message || error.message || 'Unknown error occurred',
        details: error.response?.data
      };
    }
  }
};
