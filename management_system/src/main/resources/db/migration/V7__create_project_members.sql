-- Create table to store project-member assignments
CREATE TABLE project_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL UNIQUE,
    project_id UUID NOT NULL,
    user_id UUID NOT NULL,
    it_role VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    delete_flag BOOLEAN DEFAULT FALSE NOT NULL,
    CONSTRAINT fk_project_members_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_project_user UNIQUE (project_id, user_id)
);

CREATE INDEX idx_project_members_project ON project_members(project_id) WHERE delete_flag = FALSE;
CREATE INDEX idx_project_members_user ON project_members(user_id) WHERE delete_flag = FALSE;

COMMENT ON TABLE project_members IS 'Assignments of users to projects with IT role metadata';
COMMENT ON COLUMN project_members.project_id IS 'Project identifier';
COMMENT ON COLUMN project_members.user_id IS 'User (staff) identifier';
COMMENT ON COLUMN project_members.it_role IS 'IT role of the member in the project';
