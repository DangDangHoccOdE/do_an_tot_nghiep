-- V3__Add_Revenue_Tracking.sql
-- Migration: Add budget tracking for revenue management
-- Created: 2025-01-08

-- Verify projects table structure has required columns for revenue
-- Columns needed: budget_estimated, currency_unit, status, end_date

-- This is a verification migration - actual columns should already exist
-- but we ensure they're present and properly indexed

-- Ensure budget_estimated exists and is not null for revenue tracking
ALTER TABLE projects 
ALTER COLUMN budget_estimated SET DEFAULT 0,
ALTER COLUMN currency_unit SET DEFAULT 'VND';

-- Add index for faster revenue queries
CREATE INDEX IF NOT EXISTS idx_projects_status ON projects(status);
CREATE INDEX IF NOT EXISTS idx_projects_end_date ON projects(end_date);
CREATE INDEX IF NOT EXISTS idx_projects_budget ON projects(budget_estimated);

-- Create view for revenue analytics (optional, for performance)
CREATE OR REPLACE VIEW v_completed_projects_revenue AS
SELECT 
    p.id,
    p.project_name,
    p.client_id,
    p.budget_estimated,
    p.currency_unit,
    p.status,
    p.end_date,
    p.created_at,
    EXTRACT(YEAR FROM p.end_date) as revenue_year,
    EXTRACT(MONTH FROM p.end_date) as revenue_month
FROM projects p
WHERE p.status = 'DONE' 
    AND p.delete_flag = FALSE
    AND p.end_date IS NOT NULL
    AND p.budget_estimated > 0;
