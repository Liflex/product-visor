-- Migration to remove the old warehouse_id column from product_stocks table
-- This should be run after V10 migration has successfully migrated all data

-- Step 1: Verify that all data has been migrated (safety check)
-- This query should return 0 if all data was migrated successfully
DO $$
DECLARE
    unmigrated_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO unmigrated_count
    FROM visor.product_stocks ps
    WHERE ps.warehouse_id IS NOT NULL
      AND NOT EXISTS (
          SELECT 1 FROM visor.product_stock_warehouses psw 
          WHERE psw.product_stock_id = ps.id AND psw.warehouse_id = ps.warehouse_id
      );
    
    IF unmigrated_count > 0 THEN
        RAISE EXCEPTION 'Found % unmigrated product_stock records. Please check migration V10 before proceeding.', unmigrated_count;
    END IF;
END $$;

-- Step 2: Remove the old warehouse_id column
ALTER TABLE visor.product_stocks DROP COLUMN IF EXISTS warehouse_id;

-- Step 3: Add comment for documentation
COMMENT ON TABLE visor.product_stocks IS 'Product stock information with ManyToMany relationship to warehouses';
