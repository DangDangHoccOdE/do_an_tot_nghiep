# Database Migration Guide

## Overview

Tôi đã tạo 3 migration file hoàn chỉnh để xây dựng cơ sở dữ liệu từ đầu với comments chi tiết cho cả bảng và cột dữ liệu, kèm triggers tự động cập nhật timestamp.

## Migration Files

### 1. **V1\_\_Initial_Schema.sql** (24.55 KB)

**Mục đích:** Tạo tất cả bảng cơ sở dữ liệu

**Bảng được tạo:**

- `roles` - Vai trò người dùng trong hệ thống
- `users` - Tài khoản và hồ sơ người dùng
- `teams` - Thông tin nhóm công việc
- `team_members` - Thành viên trong nhóm
- `clients` - Thông tin khách hàng
- `projects` - Thông tin dự án
- `project_requirements` - Yêu cầu dự án và AI estimates
- `project_files` - Quản lý file dự án
- `project_assignments` - Phân công nhân viên cho dự án
- `tasks` - Quản lý công việc
- `task_updates` - Lịch sử cập nhật công việc
- `skills` - Danh sách kỹ năng
- `employee_skills` - Kỹ năng của nhân viên
- `ai_consulting_logs` - Log tư vấn AI

**Đặc điểm:**

- Tất cả bảng có UUID làm primary key
- Tất cả bảng có `created_at`, `updated_at`, `delete_flag` (soft delete)
- Có comments chi tiết cho mỗi bảng
- Có comments chi tiết cho mỗi cột
- Tạo indexes cho cột thường dùng trong WHERE/JOIN
- Foreign keys với ON DELETE CASCADE hoặc ON DELETE SET NULL

---

### 2. **V2\_\_Add_Triggers_And_Constraints.sql** (30.54 KB)

**Mục đích:** Tạo triggers tự động cập nhật timestamp và thêm comments chi tiết

**Nội dung:**

- **Trigger Function:** `update_timestamp()` - Hàm tự động cập nhật `updated_at` khi record bị modify
- **13 Triggers** - Một cho mỗi bảng chính:

  - `trigger_roles_update_timestamp`
  - `trigger_users_update_timestamp`
  - `trigger_teams_update_timestamp`
  - `trigger_team_members_update_timestamp`
  - `trigger_clients_update_timestamp`
  - `trigger_projects_update_timestamp`
  - `trigger_project_requirements_update_timestamp`
  - `trigger_project_files_update_timestamp`
  - `trigger_project_assignments_update_timestamp`
  - `trigger_tasks_update_timestamp`
  - `trigger_task_updates_update_timestamp`
  - `trigger_skills_update_timestamp`
  - `trigger_employee_skills_update_timestamp`
  - `trigger_ai_consulting_logs_update_timestamp`

- **Comprehensive Documentation:**
  - Comments cho mỗi bảng (mô tả chi tiết, mục đích sử dụng)
  - Comments cho mỗi cột (kiểu dữ liệu, format, mục đích)
  - Comments cho mỗi trigger

**Ví dụ Comments:**

```sql
COMMENT ON TABLE users IS 'User account information and authentication details';
COMMENT ON COLUMN users.email IS 'Email address - Required, unique, used for login and verification';
```

---

### 3. **V3\_\_Insert_Sample_Data.sql** (24.79 KB)

**Mục đích:** Thêm dữ liệu mẫu cho testing và development

**Dữ liệu được thêm:**

#### Roles (4 vai trò)

- `ROLE_ADMIN` - Quản trị viên
- `ROLE_MANAGER` - Quản lý
- `ROLE_STAFF` - Nhân viên
- `ROLE_CLIENT` - Khách hàng

#### Users (9 người dùng mẫu)

- 1 Admin
- 1 Manager
- 5 Staff (Backend Dev, Frontend Dev, QA, DevOps, Project Manager)
- 2 Clients

#### Teams (3 nhóm)

- Team Alpha - Backend team
- Team Beta - Frontend team
- Quality Assurance Team - QA team

#### Team Members (6 thành viên)

- Các nhân viên được thêm vào từng nhóm với vai trò cụ thể

#### Skills (37 kỹ năng)

- Backend: Java, Spring Boot, Python, Node.js, PHP, C#
- Frontend: React, Vue.js, Angular, TypeScript, HTML/CSS, Tailwind CSS
- Database: PostgreSQL, MySQL, MongoDB, Redis
- Mobile: React Native, Flutter, Kotlin, Swift
- DevOps: Docker, Kubernetes, AWS, Azure, Git, Jenkins, Linux
- Testing: Jest, JUnit, Selenium, Cypress
- Other: REST API, GraphQL, Microservices, Agile, Scrum

#### Employee Skills (21 mapping)

- Backend Dev: Java, Spring Boot, PostgreSQL, Git, REST API (5 kỹ năng)
- Frontend Dev: React, TypeScript, HTML/CSS, Tailwind CSS, Git (5 kỹ năng)
- QA Engineer: Cypress, Selenium, Agile, Scrum (4 kỹ năng)
- DevOps Engineer: Docker, Kubernetes, AWS, Jenkins, Linux (5 kỹ năng)

#### Projects (1 dự án mẫu)

- **E-Commerce Platform Development**
  - Client: A Solutions Co., Ltd
  - Team: Team Alpha
  - Budget: $150,000
  - Timeline: 180 days
  - Status: IN_PROGRESS

#### Project Assignments (4)

- Backend Lead, Frontend Lead, QA Lead, DevOps Engineer

#### Tasks (5 công việc)

- Setup Project Infrastructure (COMPLETED)
- Implement User Authentication (IN_PROGRESS)
- Design Product List Page (IN_PROGRESS)
- Build Shopping Cart Component (TODO)
- Write Unit Tests (TODO)

**Test Credentials:**

- Email: admin@luvina.com | Password: Admin@123456
- Email: manager@luvina.com | Password: Admin@123456
- Email: backend@luvina.com | Password: Admin@123456
- ... và các user khác

---

## Tính năng chính

### ✅ Comments Chi Tiết

- **Table Comments**: Mô tả mục đích và nội dung của mỗi bảng
- **Column Comments**: Giải thích từng cột bao gồm:
  - Kiểu dữ liệu
  - Giá trị mặc định (nếu có)
  - Format (ví dụ: UUID, DATE YYYY-MM-DD, etc.)
  - Giải thích chi tiết mục đích sử dụng

### ✅ Automatic Update Triggers

- Mỗi bảng có trigger tự động cập nhật `updated_at` khi record bị modify
- Sử dụng `BEFORE UPDATE` trigger
- Hàm `update_timestamp()` được tạo một lần, tái sử dụng cho tất cả bảng

### ✅ Data Integrity

- Primary Key: UUID cho tất cả bảng
- Foreign Keys: Với ON DELETE CASCADE hoặc ON DELETE SET NULL
- Unique Constraints: Cho các cột cần unique (email, skill name, etc.)
- Indexes: Cho cột thường dùng trong WHERE, JOIN, ORDER BY

### ✅ Soft Delete

- Tất cả bảng có `delete_flag` boolean
- Mặc định: `FALSE` (active)
- Set `TRUE` để soft delete (giấu record nhưng không xóa)

### ✅ Sample Data

- 37 kỹ năng đã được thêm sẵn
- Test users với các role khác nhau
- Test project với assignments và tasks

---

## Cách sử dụng

### Chạy migrations

```bash
# Spring Boot sẽ tự động chạy migrations khi ứng dụng start
# hoặc chạy thủ công với:
flyway migrate
```

### Xem comments trong PostgreSQL

```sql
-- Xem comment của bảng
SELECT table_name, obj_description(('public.' || table_name)::regclass, 'pg_class')
FROM information_schema.tables
WHERE table_schema = 'public';

-- Xem comment của cột
SELECT column_name, col_description(('public.' || table_name)::regclass::oid, ordinal_position)
FROM information_schema.columns
WHERE table_schema = 'public' AND table_name = 'users';

-- Xem comment của trigger
SELECT * FROM pg_trigger WHERE tgname LIKE 'trigger_%';
```

---

## File Location

```
management_system/src/main/resources/db/migration/
├── V1__Initial_Schema.sql                    (tạo bảng)
├── V2__Add_Triggers_And_Constraints.sql      (triggers + comments)
└── V3__Insert_Sample_Data.sql                (dữ liệu mẫu)
```

---

## Lưu ý

- Tất cả UUID được generate tự động hoặc sử dụng UUID cố định cho data mẫu
- Password trong sample data là hashed (không lưu plain text)
- Soft delete flag cho phép archive data mà không mất dữ liệu
- Triggers tự động cập nhật `updated_at` - không cần update thủ công
- Tất cả comments được viết bằng tiếng Anh để dễ bảo trì

---

**Hoàn thành:** 2024-01-03
**Status:** Sẵn sàng deploy
