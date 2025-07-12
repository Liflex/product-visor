-- Add quantity column to product table
-- Run this script manually in your PostgreSQL database

-- Check if column already exists
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_schema = 'visor' 
        AND table_name = 'product' 
        AND column_name = 'quantity'
    ) THEN
        ALTER TABLE visor.product ADD COLUMN quantity INTEGER NOT NULL DEFAULT 0;
        RAISE NOTICE 'Column "quantity" added successfully';
    ELSE
        RAISE NOTICE 'Column "quantity" already exists';
    END IF;
END $$;

-- Add comment to document the column
COMMENT ON COLUMN visor.product.quantity IS 'Product quantity in stock'; 