-- Add marketplace column to warehouses
-- Enum is stored as VARCHAR via JPA @Enumerated(EnumType.STRING)

ALTER TABLE warehouses
    ADD COLUMN IF NOT EXISTS marketplace VARCHAR(50);

-- Optional: backfill or set default could be added here if needed


