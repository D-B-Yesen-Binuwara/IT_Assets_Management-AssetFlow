-- AssetFlow manual database update V01
-- Run once in pgAdmin against the existing database before deploying this version.

BEGIN;

ALTER TABLE maintenance_tickets
    ADD COLUMN IF NOT EXISTS start_date date;

UPDATE maintenance_tickets
SET start_date = CASE
    WHEN due_date IS NULL THEN (opened_at AT TIME ZONE 'Asia/Colombo')::date
    ELSE LEAST((opened_at AT TIME ZONE 'Asia/Colombo')::date, due_date)
END
WHERE start_date IS NULL;

ALTER TABLE maintenance_tickets
    ALTER COLUMN start_date SET DEFAULT CURRENT_DATE,
    ALTER COLUMN start_date SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_maintenance_due_after_start'
          AND conrelid = 'maintenance_tickets'::regclass
    ) THEN
        ALTER TABLE maintenance_tickets
            ADD CONSTRAINT chk_maintenance_due_after_start
            CHECK (due_date IS NULL OR due_date >= start_date);
    END IF;
END
$$;

ALTER TABLE assets
    ALTER COLUMN currency SET DEFAULT 'LKR';

ALTER TABLE organization_settings
    ALTER COLUMN currency SET DEFAULT 'LKR';

UPDATE assets
SET currency = 'LKR'
WHERE currency = 'USD';

UPDATE organization_settings
SET currency = 'LKR', updated_at = now()
WHERE id = 1;

INSERT INTO system_settings (setting_key, setting_value, updated_at)
VALUES ('asset.default_currency', '"LKR"'::jsonb, now())
ON CONFLICT (setting_key) DO UPDATE
SET setting_value = EXCLUDED.setting_value,
    updated_at = EXCLUDED.updated_at;

COMMIT;
