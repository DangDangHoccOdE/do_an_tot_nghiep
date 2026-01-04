-- Add activation status flag for users
ALTER TABLE users
ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT TRUE NOT NULL;

-- Ensure existing records remain usable
UPDATE users SET active = TRUE WHERE active IS NULL;
