-- Migration to add ManyToMany relationship between ProductStock and Warehouse
-- This migration handles the transition from single warehouse_id to multiple warehouses per ProductStock

-- Step 1: Create the junction table for ManyToMany relationship
CREATE TABLE IF NOT EXISTS visor.product_stock_warehouses (
    product_stock_id UUID NOT NULL,
    warehouse_id UUID NOT NULL,
    PRIMARY KEY (product_stock_id, warehouse_id),
    CONSTRAINT fk_product_stock_warehouses_product_stock 
        FOREIGN KEY (product_stock_id) REFERENCES visor.product_stocks(id) ON DELETE CASCADE,
    CONSTRAINT fk_product_stock_warehouses_warehouse 
        FOREIGN KEY (warehouse_id) REFERENCES visor.warehouses(id) ON DELETE CASCADE
);

-- Step 2: Migrate existing data from single warehouse_id to ManyToMany relationship
-- Copy existing warehouse_id relationships to the new junction table
INSERT INTO visor.product_stock_warehouses (product_stock_id, warehouse_id)
SELECT ps.id, ps.warehouse_id
FROM visor.product_stocks ps
WHERE ps.warehouse_id IS NOT NULL
  AND EXISTS (SELECT 1 FROM visor.warehouses w WHERE w.id = ps.warehouse_id);

-- Step 3: Remove the old warehouse_id column from product_stocks table
-- Note: This is done in a separate migration to ensure data safety
-- ALTER TABLE visor.product_stocks DROP COLUMN warehouse_id;

-- Step 4: Add indexes for better performance
CREATE INDEX IF NOT EXISTS idx_product_stock_warehouses_product_stock_id 
    ON visor.product_stock_warehouses(product_stock_id);
CREATE INDEX IF NOT EXISTS idx_product_stock_warehouses_warehouse_id 
    ON visor.product_stock_warehouses(warehouse_id);

-- Step 5: Add comment for documentation
COMMENT ON TABLE visor.product_stock_warehouses IS 'Junction table for ManyToMany relationship between ProductStock and Warehouse';
COMMENT ON COLUMN visor.product_stock_warehouses.product_stock_id IS 'Reference to ProductStock';
COMMENT ON COLUMN visor.product_stock_warehouses.warehouse_id IS 'Reference to Warehouse';
