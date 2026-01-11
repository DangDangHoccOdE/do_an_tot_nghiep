-- ============================================
-- V15__create_daily_tasks.sql
-- Migration mới từ V11
-- Thêm comment cho table, column
-- Thêm trigger tự động cập nhật created_at và updated_at
-- ============================================

-- 1. Tạo enum nếu chưa tồn tại
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'task_priority') THEN
        CREATE TYPE task_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL');
    END IF;
END$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'task_status') THEN
        CREATE TYPE task_status AS ENUM ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'BLOCKED', 'CANCELLED');
    END IF;
END$$;

-- 2. Tạo table nếu chưa tồn tại
CREATE TABLE IF NOT EXISTS daily_tasks (
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

-- 3. Thêm comment cho table và column
COMMENT ON TABLE daily_tasks IS 'Bảng quản lý các công việc hàng ngày';
COMMENT ON COLUMN daily_tasks.id IS 'ID duy nhất của công việc';
COMMENT ON COLUMN daily_tasks.project_id IS 'ID dự án';
COMMENT ON COLUMN daily_tasks.assigned_to IS 'Người được giao';
COMMENT ON COLUMN daily_tasks.title IS 'Tiêu đề công việc';
COMMENT ON COLUMN daily_tasks.description IS 'Mô tả công việc';
COMMENT ON COLUMN daily_tasks.task_date IS 'Ngày thực hiện';
COMMENT ON COLUMN daily_tasks.priority IS 'Độ ưu tiên công việc';
COMMENT ON COLUMN daily_tasks.estimated_hours IS 'Thời gian ước tính (giờ)';
COMMENT ON COLUMN daily_tasks.status IS 'Trạng thái công việc';
COMMENT ON COLUMN daily_tasks.delete_flag IS 'Cờ xóa mềm';
COMMENT ON COLUMN daily_tasks.created_at IS 'Thời gian tạo';
COMMENT ON COLUMN daily_tasks.updated_at IS 'Thời gian cập nhật';
COMMENT ON COLUMN daily_tasks.created_by IS 'Người tạo';
COMMENT ON COLUMN daily_tasks.updated_by IS 'Người cập nhật';

-- 4. Tạo index
CREATE INDEX IF NOT EXISTS idx_daily_tasks_project ON daily_tasks(project_id);
CREATE INDEX IF NOT EXISTS idx_daily_tasks_assigned_to ON daily_tasks(assigned_to);
CREATE INDEX IF NOT EXISTS idx_daily_tasks_date ON daily_tasks(task_date);
CREATE INDEX IF NOT EXISTS idx_daily_tasks_status ON daily_tasks(status);
CREATE INDEX IF NOT EXISTS idx_daily_tasks_project_date ON daily_tasks(project_id, task_date);
CREATE INDEX IF NOT EXISTS idx_daily_tasks_active ON daily_tasks(delete_flag) WHERE delete_flag = FALSE;

-- 5. Trigger cập nhật updated_at tự động
-- Hàm trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = CURRENT_TIMESTAMP;
   RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger cho table
DROP TRIGGER IF EXISTS trg_update_daily_tasks_updated_at ON daily_tasks;
CREATE TRIGGER trg_update_daily_tasks_updated_at
BEFORE UPDATE ON daily_tasks
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Trigger created_at chỉ set nếu insert
CREATE OR REPLACE FUNCTION set_created_at_column()
RETURNS TRIGGER AS $$
BEGIN
   IF NEW.created_at IS NULL THEN
       NEW.created_at = CURRENT_TIMESTAMP;
   END IF;
   RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS trg_set_daily_tasks_created_at ON daily_tasks;
CREATE TRIGGER trg_set_daily_tasks_created_at
BEFORE INSERT ON daily_tasks
FOR EACH ROW
EXECUTE FUNCTION set_created_at_column();
