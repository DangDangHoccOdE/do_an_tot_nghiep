-- Add project_id column to teams table and create foreign key constraint
ALTER TABLE teams ADD COLUMN project_id UUID;

-- Update existing teams to set a default project_id (you may need to adjust this based on your data)
-- For now, we'll allow NULL temporarily for migration

-- Add foreign key constraint
ALTER TABLE teams ADD CONSTRAINT fk_teams_project 
    FOREIGN KEY (project_id) REFERENCES projects(id);

-- After you've manually updated all existing teams with valid project_ids, 
-- you can make the column NOT NULL:
-- ALTER TABLE teams ALTER COLUMN project_id SET NOT NULL;

-- Add index for better query performance
CREATE INDEX idx_teams_project_id ON teams(project_id);
