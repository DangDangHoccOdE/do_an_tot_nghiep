-- Create enum type for report status
CREATE TYPE report_status AS ENUM ('PENDING', 'COMPLETED', 'BLOCKED', 'CANCELLED');

-- Create task_reports table
CREATE TABLE task_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    daily_task_id UUID NOT NULL REFERENCES daily_tasks(id) ON DELETE CASCADE,
    reported_by UUID NOT NULL REFERENCES "users"(id) ON DELETE RESTRICT,
    reported_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status report_status NOT NULL DEFAULT 'PENDING',
    completed_hours DECIMAL(8,2) CHECK (completed_hours >= 0),
    completion_percentage INTEGER CHECK (completion_percentage >= 0 AND completion_percentage <= 100),
    notes TEXT,
    evidence_link TEXT,
    delete_flag BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_task_reports_daily_task ON task_reports(daily_task_id);
CREATE INDEX idx_task_reports_reported_by ON task_reports(reported_by);
CREATE INDEX idx_task_reports_reported_at ON task_reports(reported_at);
CREATE INDEX idx_task_reports_active ON task_reports(delete_flag) WHERE delete_flag = FALSE;
