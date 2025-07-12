-- Check and fix database schema
-- Run this script to ensure all required columns exist

-- Check current schema
SELECT 
    column_name, 
    data_type, 
    is_nullable, 
    column_default,
    CASE 
        WHEN column_name = 'quantity' AND data_type = 'integer' THEN '✅ OK'
        WHEN column_name = 'image' AND data_type = 'bytea' THEN '✅ OK'
        WHEN column_name = 'quantity' AND data_type != 'integer' THEN '❌ WRONG TYPE'
        WHEN column_name = 'image' AND data_type != 'bytea' THEN '❌ WRONG TYPE'
        WHEN column_name = 'quantity' THEN '❌ MISSING'
        WHEN column_name = 'image' THEN '❌ MISSING'
        ELSE 'ℹ️ OTHER'
    END as status
FROM information_schema.columns 
WHERE table_schema = 'visor' 
AND table_name = 'product'
ORDER BY ordinal_position;

-- Add missing columns if they don't exist
DO $$
BEGIN
    -- Add quantity column if it doesn't exist
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

    -- Add image column if it doesn't exist
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

-- Add comments
COMMENT ON COLUMN visor.product.quantity IS 'Product quantity in stock';
COMMENT ON COLUMN visor.product.image IS 'Product image stored as byte array in database';

-- Show final schema
SELECT 
    column_name, 
    data_type, 
    is_nullable, 
    column_default
FROM information_schema.columns 
WHERE table_schema = 'visor' 
AND table_name = 'product'
ORDER BY ordinal_position; 