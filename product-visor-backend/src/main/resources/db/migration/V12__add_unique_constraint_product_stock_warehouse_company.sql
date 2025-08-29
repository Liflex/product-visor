-- Migration to add unique constraint ensuring one ProductStock per product-warehouse-company combination
-- This prevents duplicate ProductStock records for the same product on the same warehouse within the same company

-- Step 1: Add unique constraint on junction table to prevent duplicate product-warehouse combinations
-- This ensures that a product can only be associated with a specific warehouse once per ProductStock
ALTER TABLE visor.product_stock_warehouses 
ADD CONSTRAINT uk_product_stock_warehouse_unique 
UNIQUE (product_stock_id, warehouse_id);

-- Step 2: Add indexes for better performance on common queries
CREATE INDEX IF NOT EXISTS idx_product_stock_warehouses_warehouse_company
    ON visor.product_stock_warehouses(warehouse_id);

CREATE INDEX IF NOT EXISTS idx_product_stock_product_id_user_id
    ON visor.product_stocks(product_id, user_id);

-- Step 3: Add comments for documentation
COMMENT ON CONSTRAINT uk_product_stock_warehouse_unique ON visor.product_stock_warehouses IS 
    'Ensures unique product-warehouse combination within each ProductStock';
COMMENT ON INDEX visor.idx_product_stock_warehouses_warehouse_company IS 
    'Index for efficient warehouse-based queries';
