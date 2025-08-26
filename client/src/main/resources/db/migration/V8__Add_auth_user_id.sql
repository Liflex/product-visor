DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema='client' AND table_name='user' AND column_name='auth_user_id'
    ) THEN
        ALTER TABLE client."user" ADD COLUMN auth_user_id BIGINT DEFAULT -1;
        CREATE INDEX IF NOT EXISTS idx_user_auth_user_id ON client."user"(auth_user_id);
    END IF;
END $$;



