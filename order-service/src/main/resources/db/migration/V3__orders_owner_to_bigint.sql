DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema='orders' AND table_name='orders' AND column_name='owner_user_id' AND data_type='uuid'
    ) THEN
        ALTER TABLE orders.orders ALTER COLUMN owner_user_id TYPE BIGINT USING NULLIF(owner_user_id::text, '00000000-0000-0000-0000-000000000000')::bigint;
    END IF;
END $$;



