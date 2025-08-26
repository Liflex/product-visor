-- Ensure target schema exists
CREATE SCHEMA IF NOT EXISTS visor;

-- Add company_id column required by JPA entity Product
ALTER TABLE IF EXISTS visor.product
    ADD COLUMN IF NOT EXISTS company_id UUID;


