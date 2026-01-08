-- Drop old foreign key constraint referencing clients table
ALTER TABLE projects DROP CONSTRAINT IF EXISTS projects_client_id_fkey;

-- Add new foreign key constraint referencing users table
ALTER TABLE projects ADD CONSTRAINT projects_client_id_fkey 
    FOREIGN KEY (client_id) REFERENCES users(id) ON DELETE CASCADE;

-- Update comment
COMMENT ON COLUMN projects.client_id IS 'Reference to user (client)';
