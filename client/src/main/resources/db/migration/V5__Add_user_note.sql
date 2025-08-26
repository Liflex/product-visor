DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema='client' AND table_name='user' AND column_name='note') THEN
        ALTER TABLE client."user" ADD COLUMN note TEXT DEFAULT '';
    END IF;
END $$;




