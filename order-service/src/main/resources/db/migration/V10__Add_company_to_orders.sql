-- Add company and owner to orders schema
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns WHERE table_schema='orders' AND table_name='orders' AND column_name='company_id'
    ) THEN
        ALTER TABLE orders.orders ADD COLUMN company_id UUID DEFAULT '00000000-0000-0000-0000-000000000000';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns WHERE table_schema='orders' AND table_name='orders' AND column_name='owner_user_id'
    ) THEN
        ALTER TABLE orders.orders ADD COLUMN owner_user_id UUID DEFAULT '00000000-0000-0000-0000-000000000000';
    END IF;
END $$;




