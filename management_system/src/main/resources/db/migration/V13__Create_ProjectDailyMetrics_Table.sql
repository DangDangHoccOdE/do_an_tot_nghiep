-- Create project_daily_metrics table
CREATE TABLE project_daily_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    report_date DATE NOT NULL,
    total_tasks INTEGER NOT NULL DEFAULT 0 CHECK (total_tasks >= 0),
    completed_tasks INTEGER NOT NULL DEFAULT 0 CHECK (completed_tasks >= 0),
    in_progress_tasks INTEGER NOT NULL DEFAULT 0 CHECK (in_progress_tasks >= 0),
    blocked_tasks INTEGER NOT NULL DEFAULT 0 CHECK (blocked_tasks >= 0),
    pending_tasks INTEGER NOT NULL DEFAULT 0 CHECK (pending_tasks >= 0),
    completion_rate DECIMAL(5,2) DEFAULT 0 CHECK (completion_rate >= 0 AND completion_rate <= 100),
    total_estimated_hours DECIMAL(8,2) DEFAULT 0,
    total_completed_hours DECIMAL(8,2) DEFAULT 0,
    team_productivity_index DECIMAL(5,2) DEFAULT 0,
    team_members_assigned INTEGER NOT NULL DEFAULT 0,
    delete_flag BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(project_id, report_date)
);

-- Create indexes
CREATE INDEX idx_metrics_project_id ON project_daily_metrics(project_id);
CREATE INDEX idx_metrics_report_date ON project_daily_metrics(report_date);
CREATE INDEX idx_metrics_project_date ON project_daily_metrics(project_id, report_date);
CREATE INDEX idx_metrics_active ON project_daily_metrics(delete_flag) WHERE delete_flag = FALSE;
