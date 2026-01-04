-- ============================================================================
-- V1__Initial_Schema.sql
-- Migration: Create all database tables with comprehensive comments
-- Description: Creates the complete database schema from scratch
-- Date: 2024-01-03
-- ============================================================================

-- Enable necessary extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================================
-- TABLE: roles
-- Description: User roles in the system (Admin, Manager, Staff, etc.)
-- ============================================================================
CREATE TABLE roles (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL UNIQUE,
    
    -- Role Information
    name VARCHAR(50) NOT NULL UNIQUE,
    
    -- Audit Fields
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    delete_flag BOOLEAN DEFAULT FALSE NOT NULL
);

COMMENT ON TABLE roles IS 'User roles in the system';
COMMENT ON COLUMN roles.id IS 'Unique identifier (UUID)';
COMMENT ON COLUMN roles.name IS 'Role name (e.g., Admin, Manager, Staff)';
COMMENT ON COLUMN roles.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN roles.updated_at IS 'Record last update timestamp';
COMMENT ON COLUMN roles.delete_flag IS 'Soft delete flag (true = deleted, false = active)';

-- ============================================================================
-- TABLE: users
-- Description: User account information
-- ============================================================================
CREATE TABLE users (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL UNIQUE,
    
    -- Authentication
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255),
    provider VARCHAR(50),
    provider_id VARCHAR(255),
    refresh_token TEXT,
    
    -- Personal Information
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    date_of_birth DATE,
    gender VARCHAR(20),
    phone VARCHAR(20),
    avatar VARCHAR(255),
    
    -- Role & Authorization
    role_id UUID,
    
    -- Email Verification
    activation_code VARCHAR(255),
    activation_expiry TIMESTAMP,
    email_code VARCHAR(255),
    email_expiry TIMESTAMP,
    
    -- Password Reset
    forgot_password_code VARCHAR(255),
    forgot_password_expiry TIMESTAMP,
    
    -- Skills
    skill TEXT,
    
    -- Audit Fields
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    delete_flag BOOLEAN DEFAULT FALSE NOT NULL,
    
    -- Foreign Keys
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE SET NULL
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role_id ON users(role_id);
CREATE INDEX idx_users_provider ON users(provider, provider_id);

COMMENT ON TABLE users IS 'User account information and authentication details';
COMMENT ON COLUMN users.id IS 'Unique identifier (UUID)';
COMMENT ON COLUMN users.email IS 'User email address (unique)';
COMMENT ON COLUMN users.password IS 'Hashed password';
COMMENT ON COLUMN users.provider IS 'OAuth provider (google, facebook, github, etc.)';
COMMENT ON COLUMN users.provider_id IS 'OAuth provider user ID';
COMMENT ON COLUMN users.refresh_token IS 'JWT refresh token';
COMMENT ON COLUMN users.first_name IS 'User first name';
COMMENT ON COLUMN users.last_name IS 'User last name';
COMMENT ON COLUMN users.date_of_birth IS 'User date of birth';
COMMENT ON COLUMN users.gender IS 'User gender (MALE, FEMALE, OTHER)';
COMMENT ON COLUMN users.phone IS 'User phone number';
COMMENT ON COLUMN users.avatar IS 'User avatar URL';
COMMENT ON COLUMN users.role_id IS 'User role reference';
COMMENT ON COLUMN users.activation_code IS 'Email activation code';
COMMENT ON COLUMN users.activation_expiry IS 'Activation code expiration time';
COMMENT ON COLUMN users.email_code IS 'Email verification code';
COMMENT ON COLUMN users.email_expiry IS 'Email verification code expiration';
COMMENT ON COLUMN users.forgot_password_code IS 'Password reset code';
COMMENT ON COLUMN users.forgot_password_expiry IS 'Password reset code expiration';
COMMENT ON COLUMN users.skill IS 'User skills (JSON or text format)';
COMMENT ON COLUMN users.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN users.updated_at IS 'Record last update timestamp';
COMMENT ON COLUMN users.delete_flag IS 'Soft delete flag';

-- ============================================================================
-- TABLE: teams
-- Description: Team information
-- ============================================================================
CREATE TABLE teams (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL UNIQUE,
    
    -- Team Information
    name VARCHAR(100) NOT NULL,
    description TEXT,
    
    -- Audit Fields
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    delete_flag BOOLEAN DEFAULT FALSE NOT NULL
);

COMMENT ON TABLE teams IS 'Team information and details';
COMMENT ON COLUMN teams.id IS 'Unique identifier (UUID)';
COMMENT ON COLUMN teams.name IS 'Team name';
COMMENT ON COLUMN teams.description IS 'Team description';
COMMENT ON COLUMN teams.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN teams.updated_at IS 'Record last update timestamp';
COMMENT ON COLUMN teams.delete_flag IS 'Soft delete flag';

-- ============================================================================
-- TABLE: team_members
-- Description: Team member relationships
-- ============================================================================
CREATE TABLE team_members (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL UNIQUE,
    
    -- Foreign Keys
    team_id UUID NOT NULL,
    user_id UUID NOT NULL,
    
    -- Role Information
    role_in_team VARCHAR(100),
    
    -- Audit Fields
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    delete_flag BOOLEAN DEFAULT FALSE NOT NULL,
    
    -- Foreign Keys Constraints
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_team_members_team_id ON team_members(team_id);
CREATE INDEX idx_team_members_user_id ON team_members(user_id);

COMMENT ON TABLE team_members IS 'Team member relationships and roles';
COMMENT ON COLUMN team_members.id IS 'Unique identifier (UUID)';
COMMENT ON COLUMN team_members.team_id IS 'Reference to team';
COMMENT ON COLUMN team_members.user_id IS 'Reference to user';
COMMENT ON COLUMN team_members.role_in_team IS 'User role in team (Leader, Developer, etc.)';
COMMENT ON COLUMN team_members.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN team_members.updated_at IS 'Record last update timestamp';
COMMENT ON COLUMN team_members.delete_flag IS 'Soft delete flag';

-- ============================================================================
-- TABLE: clients
-- Description: Client information
-- ============================================================================
CREATE TABLE clients (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL UNIQUE,
    
    -- Foreign Keys
    user_id UUID NOT NULL,
    
    -- Client Information
    company_name VARCHAR(200),
    address VARCHAR(255),
    note TEXT,
    
    -- Audit Fields
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    delete_flag BOOLEAN DEFAULT FALSE NOT NULL,
    
    -- Foreign Keys Constraints
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_clients_user_id ON clients(user_id);

COMMENT ON TABLE clients IS 'Client information and company details';
COMMENT ON COLUMN clients.id IS 'Unique identifier (UUID)';
COMMENT ON COLUMN clients.user_id IS 'Reference to user account';
COMMENT ON COLUMN clients.company_name IS 'Client company name';
COMMENT ON COLUMN clients.address IS 'Client company address';
COMMENT ON COLUMN clients.note IS 'Additional notes about the client';
COMMENT ON COLUMN clients.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN clients.updated_at IS 'Record last update timestamp';
COMMENT ON COLUMN clients.delete_flag IS 'Soft delete flag';

-- ============================================================================
-- TABLE: projects
-- Description: Project information and management
-- ============================================================================
CREATE TABLE projects (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL UNIQUE,
    
    -- Foreign Keys
    client_id UUID NOT NULL,
    team_id UUID,
    
    -- Project Information
    project_name VARCHAR(200) NOT NULL,
    description TEXT,
    
    -- Financial Information
    budget_estimated NUMERIC(18, 2),
    
    -- Timeline
    timeline_estimated INTEGER,
    start_date DATE,
    end_date DATE,
    
    -- Status
    status VARCHAR(20) DEFAULT 'PENDING' NOT NULL,
    
    -- Audit Fields
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    delete_flag BOOLEAN DEFAULT FALSE NOT NULL,
    
    -- Foreign Keys Constraints
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE SET NULL
);

CREATE INDEX idx_projects_client_id ON projects(client_id);
CREATE INDEX idx_projects_team_id ON projects(team_id);
CREATE INDEX idx_projects_status ON projects(status);

COMMENT ON TABLE projects IS 'Project information and management details';
COMMENT ON COLUMN projects.id IS 'Unique identifier (UUID)';
COMMENT ON COLUMN projects.client_id IS 'Reference to client';
COMMENT ON COLUMN projects.team_id IS 'Reference to assigned team';
COMMENT ON COLUMN projects.project_name IS 'Project name';
COMMENT ON COLUMN projects.description IS 'Detailed project description';
COMMENT ON COLUMN projects.budget_estimated IS 'Estimated project budget';
COMMENT ON COLUMN projects.timeline_estimated IS 'Estimated timeline in days';
COMMENT ON COLUMN projects.start_date IS 'Project start date';
COMMENT ON COLUMN projects.end_date IS 'Project end date';
COMMENT ON COLUMN projects.status IS 'Project status (PENDING, IN_PROGRESS, COMPLETED, ON_HOLD, CANCELLED)';
COMMENT ON COLUMN projects.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN projects.updated_at IS 'Record last update timestamp';
COMMENT ON COLUMN projects.delete_flag IS 'Soft delete flag';

-- ============================================================================
-- TABLE: project_requirements
-- Description: Project requirements and AI estimates
-- ============================================================================
CREATE TABLE project_requirements (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL UNIQUE,
    
    -- Foreign Keys
    project_id UUID NOT NULL,
    
    -- Requirements
    requirement_text TEXT,
    
    -- AI Estimates
    ai_estimate_cost NUMERIC(18, 2),
    ai_estimate_time INTEGER,
    
    -- Audit Fields
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    delete_flag BOOLEAN DEFAULT FALSE NOT NULL,
    
    -- Foreign Keys Constraints
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE INDEX idx_project_requirements_project_id ON project_requirements(project_id);

COMMENT ON TABLE project_requirements IS 'Project requirements and AI analysis results';
COMMENT ON COLUMN project_requirements.id IS 'Unique identifier (UUID)';
COMMENT ON COLUMN project_requirements.project_id IS 'Reference to project';
COMMENT ON COLUMN project_requirements.requirement_text IS 'Detailed requirement description';
COMMENT ON COLUMN project_requirements.ai_estimate_cost IS 'AI estimated cost';
COMMENT ON COLUMN project_requirements.ai_estimate_time IS 'AI estimated time in days';
COMMENT ON COLUMN project_requirements.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN project_requirements.updated_at IS 'Record last update timestamp';
COMMENT ON COLUMN project_requirements.delete_flag IS 'Soft delete flag';

-- ============================================================================
-- TABLE: project_files
-- Description: Project file management
-- ============================================================================
CREATE TABLE project_files (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL UNIQUE,
    
    -- Foreign Keys
    project_id UUID NOT NULL,
    uploaded_by_user_id UUID,
    
    -- File Information
    file_name VARCHAR(200),
    file_path VARCHAR(255),
    
    -- Upload Information
    uploaded_at TIMESTAMP,
    
    -- Audit Fields
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    delete_flag BOOLEAN DEFAULT FALSE NOT NULL,
    
    -- Foreign Keys Constraints
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by_user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_project_files_project_id ON project_files(project_id);
CREATE INDEX idx_project_files_uploaded_by ON project_files(uploaded_by_user_id);

COMMENT ON TABLE project_files IS 'Project file uploads and management';
COMMENT ON COLUMN project_files.id IS 'Unique identifier (UUID)';
COMMENT ON COLUMN project_files.project_id IS 'Reference to project';
COMMENT ON COLUMN project_files.uploaded_by_user_id IS 'Reference to uploading user';
COMMENT ON COLUMN project_files.file_name IS 'File name';
COMMENT ON COLUMN project_files.file_path IS 'File path or URL';
COMMENT ON COLUMN project_files.uploaded_at IS 'File upload timestamp';
COMMENT ON COLUMN project_files.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN project_files.updated_at IS 'Record last update timestamp';
COMMENT ON COLUMN project_files.delete_flag IS 'Soft delete flag';

-- ============================================================================
-- TABLE: project_assignments
-- Description: Team member project assignments
-- ============================================================================
CREATE TABLE project_assignments (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL UNIQUE,
    
    -- Foreign Keys
    project_id UUID NOT NULL,
    user_id UUID NOT NULL,
    
    -- Assignment Information
    position VARCHAR(100),
    
    -- Dates
    assigned_date DATE,
    start_date DATE,
    end_date DATE,
    
    -- Status
    status VARCHAR(20) DEFAULT 'TODO' NOT NULL,
    
    -- Audit Fields
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    delete_flag BOOLEAN DEFAULT FALSE NOT NULL,
    
    -- Foreign Keys Constraints
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_project_assignments_project_id ON project_assignments(project_id);
CREATE INDEX idx_project_assignments_user_id ON project_assignments(user_id);
CREATE INDEX idx_project_assignments_status ON project_assignments(status);

COMMENT ON TABLE project_assignments IS 'Team member assignments to projects';
COMMENT ON COLUMN project_assignments.id IS 'Unique identifier (UUID)';
COMMENT ON COLUMN project_assignments.project_id IS 'Reference to project';
COMMENT ON COLUMN project_assignments.user_id IS 'Reference to user/team member';
COMMENT ON COLUMN project_assignments.position IS 'Position in project (Developer, QA, etc.)';
COMMENT ON COLUMN project_assignments.assigned_date IS 'Date of assignment';
COMMENT ON COLUMN project_assignments.start_date IS 'Assignment start date';
COMMENT ON COLUMN project_assignments.end_date IS 'Assignment end date';
COMMENT ON COLUMN project_assignments.status IS 'Assignment status (TODO, IN_PROGRESS, COMPLETED, CANCELLED)';
COMMENT ON COLUMN project_assignments.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN project_assignments.updated_at IS 'Record last update timestamp';
COMMENT ON COLUMN project_assignments.delete_flag IS 'Soft delete flag';

-- ============================================================================
-- TABLE: tasks
-- Description: Task management
-- ============================================================================
CREATE TABLE tasks (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL UNIQUE,
    
    -- Foreign Keys
    project_id UUID NOT NULL,
    assigned_to_user_id UUID,
    
    -- Task Information
    title VARCHAR(200),
    description TEXT,
    
    -- Dates
    start_date DATE,
    due_date DATE,
    
    -- Status
    status VARCHAR(20) DEFAULT 'TODO' NOT NULL,
    
    -- Audit Fields
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    delete_flag BOOLEAN DEFAULT FALSE NOT NULL,
    
    -- Foreign Keys Constraints
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_to_user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_tasks_project_id ON tasks(project_id);
CREATE INDEX idx_tasks_assigned_to_user_id ON tasks(assigned_to_user_id);
CREATE INDEX idx_tasks_status ON tasks(status);

COMMENT ON TABLE tasks IS 'Task management and assignment';
COMMENT ON COLUMN tasks.id IS 'Unique identifier (UUID)';
COMMENT ON COLUMN tasks.project_id IS 'Reference to project';
COMMENT ON COLUMN tasks.assigned_to_user_id IS 'Reference to assigned user';
COMMENT ON COLUMN tasks.title IS 'Task title';
COMMENT ON COLUMN tasks.description IS 'Task description';
COMMENT ON COLUMN tasks.start_date IS 'Task start date';
COMMENT ON COLUMN tasks.due_date IS 'Task due date';
COMMENT ON COLUMN tasks.status IS 'Task status (TODO, IN_PROGRESS, COMPLETED, BLOCKED, CANCELLED)';
COMMENT ON COLUMN tasks.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN tasks.updated_at IS 'Record last update timestamp';
COMMENT ON COLUMN tasks.delete_flag IS 'Soft delete flag';

-- ============================================================================
-- TABLE: task_updates
-- Description: Task update history
-- ============================================================================
CREATE TABLE task_updates (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL UNIQUE,
    
    -- Foreign Keys
    task_id UUID NOT NULL,
    updated_by_user_id UUID NOT NULL,
    
    -- Update Information
    updated_text TEXT,
    progress_percent INTEGER,
    
    -- Audit Fields
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    delete_flag BOOLEAN DEFAULT FALSE NOT NULL,
    
    -- Foreign Keys Constraints
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (updated_by_user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_task_updates_task_id ON task_updates(task_id);
CREATE INDEX idx_task_updates_updated_by_user_id ON task_updates(updated_by_user_id);

COMMENT ON TABLE task_updates IS 'Task update history and progress tracking';
COMMENT ON COLUMN task_updates.id IS 'Unique identifier (UUID)';
COMMENT ON COLUMN task_updates.task_id IS 'Reference to task';
COMMENT ON COLUMN task_updates.updated_by_user_id IS 'Reference to updating user';
COMMENT ON COLUMN task_updates.updated_text IS 'Update description and comments';
COMMENT ON COLUMN task_updates.progress_percent IS 'Task progress percentage (0-100)';
COMMENT ON COLUMN task_updates.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN task_updates.updated_at IS 'Record last update timestamp';
COMMENT ON COLUMN task_updates.delete_flag IS 'Soft delete flag';

-- ============================================================================
-- TABLE: skills
-- Description: Available skills in the system
-- ============================================================================
CREATE TABLE skills (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL UNIQUE,
    
    -- Skill Information
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    category VARCHAR(100),
    
    -- Audit Fields
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    delete_flag BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE INDEX idx_skills_category ON skills(category);

COMMENT ON TABLE skills IS 'Available skills and competencies in the system';
COMMENT ON COLUMN skills.id IS 'Unique identifier (UUID)';
COMMENT ON COLUMN skills.name IS 'Skill name (e.g., Java, React, Python)';
COMMENT ON COLUMN skills.description IS 'Skill description';
COMMENT ON COLUMN skills.category IS 'Skill category (Backend, Frontend, Mobile, DevOps, etc.)';
COMMENT ON COLUMN skills.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN skills.updated_at IS 'Record last update timestamp';
COMMENT ON COLUMN skills.delete_flag IS 'Soft delete flag';

-- ============================================================================
-- TABLE: employee_skills
-- Description: Employee skill proficiency
-- ============================================================================
CREATE TABLE employee_skills (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL UNIQUE,
    
    -- Foreign Keys
    user_id UUID NOT NULL,
    skill_id UUID NOT NULL,
    
    -- Skill Information
    level VARCHAR(50) NOT NULL,
    years_of_experience INTEGER,
    
    -- Audit Fields
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    delete_flag BOOLEAN DEFAULT FALSE NOT NULL,
    
    -- Foreign Keys Constraints
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE,
    
    -- Unique Constraint
    CONSTRAINT uk_user_skill UNIQUE (user_id, skill_id)
);

CREATE INDEX idx_employee_skills_user_id ON employee_skills(user_id);
CREATE INDEX idx_employee_skills_skill_id ON employee_skills(skill_id);

COMMENT ON TABLE employee_skills IS 'Employee skill proficiency and experience levels';
COMMENT ON COLUMN employee_skills.id IS 'Unique identifier (UUID)';
COMMENT ON COLUMN employee_skills.user_id IS 'Reference to user/employee';
COMMENT ON COLUMN employee_skills.skill_id IS 'Reference to skill';
COMMENT ON COLUMN employee_skills.level IS 'Proficiency level (BEGINNER, INTERMEDIATE, ADVANCED, EXPERT)';
COMMENT ON COLUMN employee_skills.years_of_experience IS 'Years of experience with this skill';
COMMENT ON COLUMN employee_skills.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN employee_skills.updated_at IS 'Record last update timestamp';
COMMENT ON COLUMN employee_skills.delete_flag IS 'Soft delete flag';

-- ============================================================================
-- TABLE: ai_consulting_logs
-- Description: AI consulting service logs
-- ============================================================================
CREATE TABLE ai_consulting_logs (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL UNIQUE,
    
    -- Foreign Keys
    user_id UUID,
    
    -- Consultation Information
    question TEXT,
    ai_response TEXT,
    
    -- Audit Fields
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    delete_flag BOOLEAN DEFAULT FALSE NOT NULL,
    
    -- Foreign Keys Constraints
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_ai_consulting_logs_user_id ON ai_consulting_logs(user_id);
CREATE INDEX idx_ai_consulting_logs_created_at ON ai_consulting_logs(created_at);

COMMENT ON TABLE ai_consulting_logs IS 'AI consulting service logs and responses';
COMMENT ON COLUMN ai_consulting_logs.id IS 'Unique identifier (UUID)';
COMMENT ON COLUMN ai_consulting_logs.user_id IS 'Reference to user making the consultation';
COMMENT ON COLUMN ai_consulting_logs.question IS 'User question or prompt';
COMMENT ON COLUMN ai_consulting_logs.ai_response IS 'AI response or answer';
COMMENT ON COLUMN ai_consulting_logs.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN ai_consulting_logs.updated_at IS 'Record last update timestamp';
COMMENT ON COLUMN ai_consulting_logs.delete_flag IS 'Soft delete flag';

-- ============================================================================
-- END OF SCHEMA CREATION
-- ============================================================================
