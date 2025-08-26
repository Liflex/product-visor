-- Add owner_user_id column to product table
-- This migration adds the missing owner_user_id column that is required by the Product entity

DO $$
BEGIN
    -- Check if the column already exists
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_schema = 'visor' 
        AND table_name = 'product' 
        AND column_name = 'owner_user_id'
    ) THEN
        -- Add the owner_user_id column
        ALTER TABLE visor.product ADD COLUMN owner_user_id BIGINT;
        RAISE NOTICE 'Column "owner_user_id" added successfully to visor.product table';
    ELSE
        RAISE NOTICE 'Column "owner_user_id" already exists in visor.product table';
    END IF;
END $$;

-- Add comment to document the column
COMMENT ON COLUMN visor.product.owner_user_id IS 'ID of the user who owns this product';
