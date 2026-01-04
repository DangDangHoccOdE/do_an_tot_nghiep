-- ============================================================================
-- V2__Add_Triggers_And_Constraints.sql
-- Migration: Create update triggers for all tables
-- Description: Adds automatic timestamp update triggers and comprehensive documentation
-- Date: 2024-01-03
-- ============================================================================

-- ============================================================================
-- TRIGGER FUNCTION: update_timestamp()
-- Description: Automatically updates the updated_at column to current timestamp
-- Used by: All tables to maintain update audit trail
-- ============================================================================
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION update_timestamp() IS 'Automatically updates the updated_at timestamp field when a record is modified. Used by all tables for audit trail.';

-- ============================================================================
-- TRIGGER: roles table - Update timestamp on modification
-- ============================================================================
CREATE TRIGGER trigger_roles_update_timestamp
BEFORE UPDATE ON roles
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

COMMENT ON TRIGGER trigger_roles_update_timestamp ON roles IS 'Automatically updates updated_at when a role record is modified';

-- ============================================================================
-- TRIGGER: users table - Update timestamp on modification
-- ============================================================================
CREATE TRIGGER trigger_users_update_timestamp
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

COMMENT ON TRIGGER trigger_users_update_timestamp ON users IS 'Automatically updates updated_at when a user record is modified';

-- ============================================================================
-- TRIGGER: teams table - Update timestamp on modification
-- ============================================================================
CREATE TRIGGER trigger_teams_update_timestamp
BEFORE UPDATE ON teams
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

COMMENT ON TRIGGER trigger_teams_update_timestamp ON teams IS 'Automatically updates updated_at when a team record is modified';

-- ============================================================================
-- TRIGGER: team_members table - Update timestamp on modification
-- ============================================================================
CREATE TRIGGER trigger_team_members_update_timestamp
BEFORE UPDATE ON team_members
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

COMMENT ON TRIGGER trigger_team_members_update_timestamp ON team_members IS 'Automatically updates updated_at when a team member record is modified';

-- ============================================================================
-- TRIGGER: clients table - Update timestamp on modification
-- ============================================================================
CREATE TRIGGER trigger_clients_update_timestamp
BEFORE UPDATE ON clients
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

COMMENT ON TRIGGER trigger_clients_update_timestamp ON clients IS 'Automatically updates updated_at when a client record is modified';

-- ============================================================================
-- TRIGGER: projects table - Update timestamp on modification
-- ============================================================================
CREATE TRIGGER trigger_projects_update_timestamp
BEFORE UPDATE ON projects
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

COMMENT ON TRIGGER trigger_projects_update_timestamp ON projects IS 'Automatically updates updated_at when a project record is modified';

-- ============================================================================
-- TRIGGER: project_requirements table - Update timestamp on modification
-- ============================================================================
CREATE TRIGGER trigger_project_requirements_update_timestamp
BEFORE UPDATE ON project_requirements
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

COMMENT ON TRIGGER trigger_project_requirements_update_timestamp ON project_requirements IS 'Automatically updates updated_at when a project requirement record is modified';

-- ============================================================================
-- TRIGGER: project_files table - Update timestamp on modification
-- ============================================================================
CREATE TRIGGER trigger_project_files_update_timestamp
BEFORE UPDATE ON project_files
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

COMMENT ON TRIGGER trigger_project_files_update_timestamp ON project_files IS 'Automatically updates updated_at when a project file record is modified';

-- ============================================================================
-- TRIGGER: project_assignments table - Update timestamp on modification
-- ============================================================================
CREATE TRIGGER trigger_project_assignments_update_timestamp
BEFORE UPDATE ON project_assignments
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

COMMENT ON TRIGGER trigger_project_assignments_update_timestamp ON project_assignments IS 'Automatically updates updated_at when a project assignment record is modified';

-- ============================================================================
-- TRIGGER: tasks table - Update timestamp on modification
-- ============================================================================
CREATE TRIGGER trigger_tasks_update_timestamp
BEFORE UPDATE ON tasks
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

COMMENT ON TRIGGER trigger_tasks_update_timestamp ON tasks IS 'Automatically updates updated_at when a task record is modified';

-- ============================================================================
-- TRIGGER: task_updates table - Update timestamp on modification
-- ============================================================================
CREATE TRIGGER trigger_task_updates_update_timestamp
BEFORE UPDATE ON task_updates
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

COMMENT ON TRIGGER trigger_task_updates_update_timestamp ON task_updates IS 'Automatically updates updated_at when a task update record is modified';

-- ============================================================================
-- TRIGGER: skills table - Update timestamp on modification
-- ============================================================================
CREATE TRIGGER trigger_skills_update_timestamp
BEFORE UPDATE ON skills
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

COMMENT ON TRIGGER trigger_skills_update_timestamp ON skills IS 'Automatically updates updated_at when a skill record is modified';

-- ============================================================================
-- TRIGGER: employee_skills table - Update timestamp on modification
-- ============================================================================
CREATE TRIGGER trigger_employee_skills_update_timestamp
BEFORE UPDATE ON employee_skills
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

COMMENT ON TRIGGER trigger_employee_skills_update_timestamp ON employee_skills IS 'Automatically updates updated_at when an employee skill record is modified';

-- ============================================================================
-- TRIGGER: ai_consulting_logs table - Update timestamp on modification
-- ============================================================================
CREATE TRIGGER trigger_ai_consulting_logs_update_timestamp
BEFORE UPDATE ON ai_consulting_logs
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

COMMENT ON TRIGGER trigger_ai_consulting_logs_update_timestamp ON ai_consulting_logs IS 'Automatically updates updated_at when an AI consulting log record is modified';

-- ============================================================================
-- DETAILED TABLE DOCUMENTATION
-- ============================================================================

-- ============================================================================
-- TABLE: roles - User Roles and Permissions
-- ============================================================================
COMMENT ON TABLE roles IS 'User roles define access levels and permissions in the system. Examples: ROLE_ADMIN (administrator), ROLE_USER (regular user), ROLE_STAFF (staff member), ROLE_PM (project manager)';

-- ============================================================================
-- TABLE: users - User Accounts and Profiles
-- ============================================================================
COMMENT ON TABLE users IS 'User account information including authentication credentials, personal information, OAuth provider details, and verification codes. Stores profile data like avatar, skills, and IT role.';

-- ============================================================================
-- TABLE: teams - Team Management
-- ============================================================================
COMMENT ON TABLE teams IS 'Teams group multiple users together for project collaboration. Each team has a name, description, and can be assigned to projects.';

-- ============================================================================
-- TABLE: team_members - Team Membership
-- ============================================================================
COMMENT ON TABLE team_members IS 'Relationships between teams and users. Specifies each user''s role within a team (e.g., Team Lead, Developer, Designer).';

-- ============================================================================
-- TABLE: clients - Client Information
-- ============================================================================
COMMENT ON TABLE clients IS 'Client company information and details. Each client is linked to a user account and contains company name, address, and notes.';

-- ============================================================================
-- TABLE: projects - Project Management
-- ============================================================================
COMMENT ON TABLE projects IS 'Projects represent work contracted from clients. Contains project details, budget, timeline, assigned team, and status tracking (PENDING, IN_PROGRESS, COMPLETED, ON_HOLD, CANCELLED).';

-- ============================================================================
-- TABLE: project_requirements - Project Requirement Analysis
-- ============================================================================
COMMENT ON TABLE project_requirements IS 'Project requirements analyzed by AI system. Contains requirement text and AI-generated estimates for cost and timeline.';

-- ============================================================================
-- TABLE: project_files - Project File Management
-- ============================================================================
COMMENT ON TABLE project_files IS 'Files associated with projects. Tracks file name, path/URL, upload timestamp, and which user uploaded the file.';

-- ============================================================================
-- TABLE: project_assignments - Team Member Project Assignments
-- ============================================================================
COMMENT ON TABLE project_assignments IS 'Assignments of team members to projects. Tracks position, dates, and status (TODO, IN_PROGRESS, COMPLETED, CANCELLED) for each assignment.';

-- ============================================================================
-- TABLE: tasks - Task Management
-- ============================================================================
COMMENT ON TABLE tasks IS 'Individual tasks within projects. Each task can be assigned to a user and has status (TODO, IN_PROGRESS, COMPLETED, BLOCKED, CANCELLED), dates, and description.';

-- ============================================================================
-- TABLE: task_updates - Task Update History
-- ============================================================================
COMMENT ON TABLE task_updates IS 'History of updates and progress changes for tasks. Tracks who updated the task, what they changed, and progress percentage (0-100).';

-- ============================================================================
-- TABLE: skills - Available Skills
-- ============================================================================
COMMENT ON TABLE skills IS 'Master list of all technical skills available in the system. Examples: Java, React, Docker, PostgreSQL. Categorized as Frontend, Backend, Database, Mobile, DevOps, Testing, Other.';

-- ============================================================================
-- TABLE: employee_skills - Employee Skill Proficiency
-- ============================================================================
COMMENT ON TABLE employee_skills IS 'Maps employees to their skills with proficiency levels (BEGINNER, INTERMEDIATE, ADVANCED, EXPERT) and years of experience. Used for resource planning and team allocation.';

-- ============================================================================
-- TABLE: ai_consulting_logs - AI Consultation Service Records
-- ============================================================================
COMMENT ON TABLE ai_consulting_logs IS 'Logs of AI consulting service interactions. Records user questions and AI responses for analysis, improvement, and audit trail.';

-- ============================================================================
-- COLUMN COMMENTS: roles table
-- ============================================================================
COMMENT ON COLUMN roles.id IS 'UUID primary key - Unique identifier for each role, auto-generated as UUID';
COMMENT ON COLUMN roles.name IS 'Role name - Required, unique. Examples: ROLE_ADMIN, ROLE_USER, ROLE_STAFF, ROLE_PM';
COMMENT ON COLUMN roles.created_at IS 'Creation timestamp - Automatically set when role record is created';
COMMENT ON COLUMN roles.updated_at IS 'Update timestamp - Automatically updated by trigger when record is modified';
COMMENT ON COLUMN roles.delete_flag IS 'Soft delete flag - true = soft deleted (hidden), false = active (visible)';

-- ============================================================================
-- COLUMN COMMENTS: users table
-- ============================================================================
COMMENT ON COLUMN users.id IS 'UUID primary key - Unique identifier for each user, auto-generated as UUID';
COMMENT ON COLUMN users.email IS 'Email address - Required, unique, used for login and verification';
COMMENT ON COLUMN users.password IS 'Password hash - Bcrypt encrypted, never stores plain password';
COMMENT ON COLUMN users.provider IS 'OAuth provider - Values: local, google, github, facebook, etc. (default: local)';
COMMENT ON COLUMN users.provider_id IS 'OAuth provider user ID - External user ID from OAuth provider';
COMMENT ON COLUMN users.refresh_token IS 'JWT refresh token - Stores long-lived refresh token for session management';
COMMENT ON COLUMN users.first_name IS 'First name - User given name';
COMMENT ON COLUMN users.last_name IS 'Last name - User family name';
COMMENT ON COLUMN users.date_of_birth IS 'Date of birth - Format: YYYY-MM-DD';
COMMENT ON COLUMN users.gender IS 'Gender - Values: MALE, FEMALE, OTHER';
COMMENT ON COLUMN users.phone IS 'Phone number - Format: VARCHAR(20) to support international numbers';
COMMENT ON COLUMN users.avatar IS 'Avatar URL - Path or URL to user avatar image from Cloudinary or storage';
COMMENT ON COLUMN users.role_id IS 'Role foreign key - References roles.id, determines user role and permissions';
COMMENT ON COLUMN users.activation_code IS 'Email activation code - Code sent during registration for email verification';
COMMENT ON COLUMN users.activation_expiry IS 'Activation code expiry - Code expiration timestamp (typically 30 minutes)';
COMMENT ON COLUMN users.email_code IS 'Email change verification code - Code for changing user email address';
COMMENT ON COLUMN users.email_expiry IS 'Email code expiry - Code expiration timestamp';
COMMENT ON COLUMN users.forgot_password_code IS 'Password reset code - Code sent for password recovery';
COMMENT ON COLUMN users.forgot_password_expiry IS 'Password reset code expiry - Code expiration timestamp (typically 15 minutes)';
COMMENT ON COLUMN users.skill IS 'Free-form skills - Deprecated, use employee_skills table instead';
COMMENT ON COLUMN users.created_at IS 'Creation timestamp - Automatically set when account is created';
COMMENT ON COLUMN users.updated_at IS 'Update timestamp - Automatically updated by trigger when record is modified';
COMMENT ON COLUMN users.delete_flag IS 'Soft delete flag - true = soft deleted (hidden), false = active (visible)';

-- ============================================================================
-- COLUMN COMMENTS: teams table
-- ============================================================================
COMMENT ON COLUMN teams.id IS 'UUID primary key - Unique identifier for each team';
COMMENT ON COLUMN teams.name IS 'Team name - Required, name of the team';
COMMENT ON COLUMN teams.description IS 'Team description - Details about team purpose and focus';
COMMENT ON COLUMN teams.created_at IS 'Creation timestamp - Automatically set when team is created';
COMMENT ON COLUMN teams.updated_at IS 'Update timestamp - Automatically updated by trigger when record is modified';
COMMENT ON COLUMN teams.delete_flag IS 'Soft delete flag - true = soft deleted (hidden), false = active (visible)';

-- ============================================================================
-- COLUMN COMMENTS: team_members table
-- ============================================================================
COMMENT ON COLUMN team_members.id IS 'UUID primary key - Unique identifier for each team member relationship';
COMMENT ON COLUMN team_members.team_id IS 'Team foreign key - References teams.id';
COMMENT ON COLUMN team_members.user_id IS 'User foreign key - References users.id';
COMMENT ON COLUMN team_members.role_in_team IS 'Role in team - Examples: Team Lead, Senior Developer, QA Engineer, Designer';
COMMENT ON COLUMN team_members.created_at IS 'Creation timestamp - Automatically set when user joins team';
COMMENT ON COLUMN team_members.updated_at IS 'Update timestamp - Automatically updated by trigger when record is modified';
COMMENT ON COLUMN team_members.delete_flag IS 'Soft delete flag - true = user removed from team, false = active member';

-- ============================================================================
-- COLUMN COMMENTS: clients table
-- ============================================================================
COMMENT ON COLUMN clients.id IS 'UUID primary key - Unique identifier for each client';
COMMENT ON COLUMN clients.user_id IS 'User foreign key - References users.id, links to client user account';
COMMENT ON COLUMN clients.company_name IS 'Company name - Name of client''s company';
COMMENT ON COLUMN clients.address IS 'Company address - Physical address of company';
COMMENT ON COLUMN clients.note IS 'Additional notes - Any notes about the client';
COMMENT ON COLUMN clients.created_at IS 'Creation timestamp - Automatically set when client record is created';
COMMENT ON COLUMN clients.updated_at IS 'Update timestamp - Automatically updated by trigger when record is modified';
COMMENT ON COLUMN clients.delete_flag IS 'Soft delete flag - true = soft deleted (hidden), false = active (visible)';

-- ============================================================================
-- COLUMN COMMENTS: projects table
-- ============================================================================
COMMENT ON COLUMN projects.id IS 'UUID primary key - Unique identifier for each project';
COMMENT ON COLUMN projects.client_id IS 'Client foreign key - References clients.id';
COMMENT ON COLUMN projects.team_id IS 'Team foreign key - References teams.id (can be NULL if team not yet assigned)';
COMMENT ON COLUMN projects.project_name IS 'Project name - Required, name of the project';
COMMENT ON COLUMN projects.description IS 'Project description - Detailed description of project scope and goals';
COMMENT ON COLUMN projects.budget_estimated IS 'Estimated budget - Amount in NUMERIC(18,2) format, with 2 decimal places';
COMMENT ON COLUMN projects.timeline_estimated IS 'Estimated timeline - Number of days for project completion';
COMMENT ON COLUMN projects.start_date IS 'Project start date - Format: YYYY-MM-DD';
COMMENT ON COLUMN projects.end_date IS 'Project end date - Format: YYYY-MM-DD';
COMMENT ON COLUMN projects.status IS 'Project status - Values: PENDING, IN_PROGRESS, COMPLETED, ON_HOLD, CANCELLED (default: PENDING)';
COMMENT ON COLUMN projects.created_at IS 'Creation timestamp - Automatically set when project is created';
COMMENT ON COLUMN projects.updated_at IS 'Update timestamp - Automatically updated by trigger when record is modified';
COMMENT ON COLUMN projects.delete_flag IS 'Soft delete flag - true = soft deleted (hidden), false = active (visible)';

-- ============================================================================
-- COLUMN COMMENTS: project_requirements table
-- ============================================================================
COMMENT ON COLUMN project_requirements.id IS 'UUID primary key - Unique identifier for each requirement';
COMMENT ON COLUMN project_requirements.project_id IS 'Project foreign key - References projects.id';
COMMENT ON COLUMN project_requirements.requirement_text IS 'Requirement description - Text describing the requirement';
COMMENT ON COLUMN project_requirements.ai_estimate_cost IS 'AI estimated cost - Cost estimate in NUMERIC(18,2) format';
COMMENT ON COLUMN project_requirements.ai_estimate_time IS 'AI estimated time - Time estimate in days';
COMMENT ON COLUMN project_requirements.created_at IS 'Creation timestamp - Automatically set when requirement is created';
COMMENT ON COLUMN project_requirements.updated_at IS 'Update timestamp - Automatically updated by trigger when record is modified';
COMMENT ON COLUMN project_requirements.delete_flag IS 'Soft delete flag - true = soft deleted (hidden), false = active (visible)';

-- ============================================================================
-- COLUMN COMMENTS: project_files table
-- ============================================================================
COMMENT ON COLUMN project_files.id IS 'UUID primary key - Unique identifier for each file record';
COMMENT ON COLUMN project_files.project_id IS 'Project foreign key - References projects.id';
COMMENT ON COLUMN project_files.uploaded_by_user_id IS 'Uploader foreign key - References users.id, tracks who uploaded the file';
COMMENT ON COLUMN project_files.file_name IS 'File name - Name of the uploaded file';
COMMENT ON COLUMN project_files.file_path IS 'File path/URL - Path or URL to the file in storage';
COMMENT ON COLUMN project_files.uploaded_at IS 'Upload timestamp - When the file was uploaded';
COMMENT ON COLUMN project_files.created_at IS 'Creation timestamp - Automatically set when file record is created';
COMMENT ON COLUMN project_files.updated_at IS 'Update timestamp - Automatically updated by trigger when record is modified';
COMMENT ON COLUMN project_files.delete_flag IS 'Soft delete flag - true = soft deleted (hidden), false = active (visible)';

-- ============================================================================
-- COLUMN COMMENTS: project_assignments table
-- ============================================================================
COMMENT ON COLUMN project_assignments.id IS 'UUID primary key - Unique identifier for each assignment';
COMMENT ON COLUMN project_assignments.project_id IS 'Project foreign key - References projects.id';
COMMENT ON COLUMN project_assignments.user_id IS 'User foreign key - References users.id, the assigned team member';
COMMENT ON COLUMN project_assignments.position IS 'Position/role - Position of user in project (e.g., Lead Developer, QA)';
COMMENT ON COLUMN project_assignments.assigned_date IS 'Assignment date - Date user was assigned to project';
COMMENT ON COLUMN project_assignments.start_date IS 'Start date - When user starts work on project';
COMMENT ON COLUMN project_assignments.end_date IS 'End date - When user''s assignment ends';
COMMENT ON COLUMN project_assignments.status IS 'Assignment status - Values: TODO, IN_PROGRESS, COMPLETED, CANCELLED (default: TODO)';
COMMENT ON COLUMN project_assignments.created_at IS 'Creation timestamp - Automatically set when assignment is created';
COMMENT ON COLUMN project_assignments.updated_at IS 'Update timestamp - Automatically updated by trigger when record is modified';
COMMENT ON COLUMN project_assignments.delete_flag IS 'Soft delete flag - true = soft deleted (hidden), false = active (visible)';

-- ============================================================================
-- COLUMN COMMENTS: tasks table
-- ============================================================================
COMMENT ON COLUMN tasks.id IS 'UUID primary key - Unique identifier for each task';
COMMENT ON COLUMN tasks.project_id IS 'Project foreign key - References projects.id';
COMMENT ON COLUMN tasks.assigned_to_user_id IS 'Assignee foreign key - References users.id, user assigned to task';
COMMENT ON COLUMN tasks.title IS 'Task title - Short title describing the task';
COMMENT ON COLUMN tasks.description IS 'Task description - Detailed description of task requirements';
COMMENT ON COLUMN tasks.start_date IS 'Task start date - Format: YYYY-MM-DD';
COMMENT ON COLUMN tasks.due_date IS 'Task due date - Format: YYYY-MM-DD';
COMMENT ON COLUMN tasks.status IS 'Task status - Values: TODO, IN_PROGRESS, COMPLETED, BLOCKED, CANCELLED (default: TODO)';
COMMENT ON COLUMN tasks.created_at IS 'Creation timestamp - Automatically set when task is created';
COMMENT ON COLUMN tasks.updated_at IS 'Update timestamp - Automatically updated by trigger when record is modified';
COMMENT ON COLUMN tasks.delete_flag IS 'Soft delete flag - true = soft deleted (hidden), false = active (visible)';

-- ============================================================================
-- COLUMN COMMENTS: task_updates table
-- ============================================================================
COMMENT ON COLUMN task_updates.id IS 'UUID primary key - Unique identifier for each update record';
COMMENT ON COLUMN task_updates.task_id IS 'Task foreign key - References tasks.id';
COMMENT ON COLUMN task_updates.updated_by_user_id IS 'Update author - References users.id, who made this update';
COMMENT ON COLUMN task_updates.updated_text IS 'Update description - Text describing the update, comments, or status change';
COMMENT ON COLUMN task_updates.progress_percent IS 'Progress percentage - Task completion percentage (0-100)';
COMMENT ON COLUMN task_updates.created_at IS 'Creation timestamp - Automatically set when update is recorded';
COMMENT ON COLUMN task_updates.updated_at IS 'Update timestamp - Automatically updated by trigger when record is modified';
COMMENT ON COLUMN task_updates.delete_flag IS 'Soft delete flag - true = soft deleted (hidden), false = active (visible)';

-- ============================================================================
-- COLUMN COMMENTS: skills table
-- ============================================================================
COMMENT ON COLUMN skills.id IS 'UUID primary key - Unique identifier for each skill';
COMMENT ON COLUMN skills.name IS 'Skill name - Required, unique. Examples: Java, React, Docker, PostgreSQL, Python, etc.';
COMMENT ON COLUMN skills.description IS 'Skill description - Description of what this skill involves';
COMMENT ON COLUMN skills.category IS 'Skill category - Classification: Frontend, Backend, Database, Mobile, DevOps, Testing, Other';
COMMENT ON COLUMN skills.created_at IS 'Creation timestamp - Automatically set when skill is added';
COMMENT ON COLUMN skills.updated_at IS 'Update timestamp - Automatically updated by trigger when record is modified';
COMMENT ON COLUMN skills.delete_flag IS 'Soft delete flag - true = soft deleted (hidden), false = active (visible)';

-- ============================================================================
-- COLUMN COMMENTS: employee_skills table
-- ============================================================================
COMMENT ON COLUMN employee_skills.id IS 'UUID primary key - Unique identifier for each employee skill mapping';
COMMENT ON COLUMN employee_skills.user_id IS 'Employee foreign key - References users.id';
COMMENT ON COLUMN employee_skills.skill_id IS 'Skill foreign key - References skills.id';
COMMENT ON COLUMN employee_skills.level IS 'Proficiency level - Values: BEGINNER, INTERMEDIATE, ADVANCED, EXPERT';
COMMENT ON COLUMN employee_skills.years_of_experience IS 'Years of experience - Number of years working with this skill (default: 0)';
COMMENT ON COLUMN employee_skills.created_at IS 'Creation timestamp - Automatically set when skill is assigned to employee';
COMMENT ON COLUMN employee_skills.updated_at IS 'Update timestamp - Automatically updated by trigger when record is modified';
COMMENT ON COLUMN employee_skills.delete_flag IS 'Soft delete flag - true = skill removed from employee, false = active';

-- ============================================================================
-- COLUMN COMMENTS: ai_consulting_logs table
-- ============================================================================
COMMENT ON COLUMN ai_consulting_logs.id IS 'UUID primary key - Unique identifier for each log entry';
COMMENT ON COLUMN ai_consulting_logs.user_id IS 'User foreign key - References users.id, user who made the consultation';
COMMENT ON COLUMN ai_consulting_logs.question IS 'User question - The question or prompt sent to AI system';
COMMENT ON COLUMN ai_consulting_logs.ai_response IS 'AI response - The response generated by the AI system';
COMMENT ON COLUMN ai_consulting_logs.created_at IS 'Creation timestamp - Automatically set when log is created';
COMMENT ON COLUMN ai_consulting_logs.updated_at IS 'Update timestamp - Automatically updated by trigger when record is modified';
COMMENT ON COLUMN ai_consulting_logs.delete_flag IS 'Soft delete flag - true = soft deleted (hidden), false = active (visible)';

-- ============================================================================
-- END OF TRIGGERS AND DOCUMENTATION
-- ============================================================================
