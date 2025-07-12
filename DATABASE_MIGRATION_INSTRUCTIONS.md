# Database Migration Instructions

## Problem
You're getting this error:
```
ERROR: column "image" is of type bytea but expression is of type bigint
```

This means the `image` column doesn't exist in the database yet.

## Solution

### Option 1: Use Flyway (Recommended)

1. **Restart the application** - Flyway should automatically run the migration
2. **Check logs** for Flyway migration messages
3. **If migration fails**, check that:
   - Flyway dependency is in `pom.xml` ✅
   - Flyway configuration is in `application.yml` ✅
   - Migration file exists at `src/main/resources/db/migration/V2__add_image_column.sql` ✅

### Option 2: Manual SQL Execution

If Flyway doesn't work, run this SQL manually in your PostgreSQL database:

```sql
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
```

### Option 3: Using psql command line

```bash
psql -h localhost -U admin -d postgres -f product-visor-backend/add_image_column.sql
```

## Verification

After running the migration, verify the column exists:

```sql
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_schema = 'visor' 
AND table_name = 'product' 
AND column_name = 'image';
```

You should see:
```
column_name | data_type
------------+----------
image       | bytea
```

## Restart Application

After adding the column, restart your Spring Boot application and try the operation again. 