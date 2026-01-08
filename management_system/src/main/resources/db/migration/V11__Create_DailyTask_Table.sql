-- Create enum types
CREATE TYPE task_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL');
-- CREATE TYPE task_status AS ENUM ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'BLOCKED', 'CANCELLED');

-- Create daily_tasks table
CREATE TABLE daily_tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    assigned_to UUID NOT NULL REFERENCES "users"(id) ON DELETE RESTRICT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    task_date DATE NOT NULL,
    priority task_priority NOT NULL DEFAULT 'MEDIUM',
    estimated_hours DECIMAL(8,2) CHECK (estimated_hours > 0),
    status task_status NOT NULL DEFAULT 'TODO',
    delete_flag BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES "users"(id),
    updated_by UUID REFERENCES "users"(id)
);

-- Create indexes
CREATE INDEX idx_daily_tasks_project ON daily_tasks(project_id);
CREATE INDEX idx_daily_tasks_assigned_to ON daily_tasks(assigned_to);
CREATE INDEX idx_daily_tasks_date ON daily_tasks(task_date);
CREATE INDEX idx_daily_tasks_status ON daily_tasks(status);
CREATE INDEX idx_daily_tasks_project_date ON daily_tasks(project_id, task_date);
CREATE INDEX idx_daily_tasks_active ON daily_tasks(delete_flag) WHERE delete_flag = FALSE;
