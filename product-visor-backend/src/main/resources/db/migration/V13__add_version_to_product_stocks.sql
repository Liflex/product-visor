-- Migration to add version field to product_stocks table for optimistic locking
-- This implements repeatable read strategy to avoid dirty writes in parallel transactions

-- Step 1: Add version column to product_stocks table
ALTER TABLE visor.product_stocks 
ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- Step 2: Add index on version for better performance
CREATE INDEX IF NOT EXISTS idx_product_stocks_version 
    ON visor.product_stocks(version);

-- Step 3: Add comment for documentation
COMMENT ON COLUMN visor.product_stocks.version IS 
    'Version field for optimistic locking to prevent dirty writes in parallel transactions';

