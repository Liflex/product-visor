-- Add image column to product table
-- Run this script manually in your PostgreSQL database

-- Check if column already exists
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_schema = 'visor' 
        AND table_name = 'product' 
        AND column_name = 'image'
    ) THEN
        ALTER TABLE visor.product ADD COLUMN image BYTEA;
        RAISE NOTICE 'Column "image" added successfully';
    ELSE
        RAISE NOTICE 'Column "image" already exists';
    END IF;
END $$;

-- Add comment to document the column
COMMENT ON COLUMN visor.product.image IS 'Product image stored as byte array in database'; 