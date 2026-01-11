# Hướng Dẫn Quản Lý Task và Phân Công Công Việc Hằng Ngày

## Tổng Quan

Hệ thống có 2 loại task:

1. **Tasks** - Nhiệm vụ dự án dài hạn (Task Module)
2. **Daily Tasks** - Công việc hằng ngày (Daily Task Board)

### Sự Khác Biệt

| Tiêu Chí            | Tasks                           | Daily Tasks                        |
| ------------------- | ------------------------------- | ---------------------------------- |
| **Thời gian**       | Dài hạn (tuần/tháng)            | Ngắn hạn (ngày)                    |
| **Mục đích**        | Deliverable/Milestone của dự án | Công việc hằng ngày, chi tiết      |
| **Người tạo**       | Admin, PM                       | Admin, PM                          |
| **Người thực hiện** | Staff được assign               | Staff được assign                  |
| **Tracking**        | Status, Due Date                | Status, Task Date, Estimated Hours |

---

## PHẦN 1: QUẢN LÝ TASKS (NHIỆM VỤ DỰ ÁN)

### 1.1. Workflow Tạo Task

```
Admin/PM → Chọn Dự Án → Tạo Task → Assign Staff → Staff Thực Hiện
```

### 1.2. Bước 1: Truy Cập Tasks Module

**Đăng nhập với quyền Admin hoặc PM:**

1. Truy cập Admin Dashboard
2. Click menu **"✅ Tasks"** ở sidebar
3. Xem danh sách tất cả tasks trong hệ thống

### 1.3. Bước 2: Tạo Task Mới

**Từ màn hình Tasks:**

1. Click nút **"+ Tạo Task"** (góc phải trên)
2. Điền thông tin:

```javascript
{
  "projectId": "UUID của dự án",        // REQUIRED - ID dự án
  "title": "Tên nhiệm vụ",              // REQUIRED - Tối đa 200 ký tự
  "description": "Mô tả chi tiết",      // OPTIONAL - Tối đa 1000 ký tự
  "assignedToUserId": "UUID nhân viên", // REQUIRED - Staff được gán
  "status": "NOT_STARTED",              // REQUIRED - Trạng thái
  "startDate": "2026-01-09",            // OPTIONAL - Ngày bắt đầu
  "dueDate": "2026-01-31"               // OPTIONAL - Deadline
}
```

3. Click **"Tạo"**

### 1.4. Các Trạng Thái Task

```javascript
// Frontend Status Options
const statusOptions = [
  { value: "NOT_STARTED", label: "Chưa Bắt Đầu" },
  { value: "IN_PROGRESS", label: "Đang Thực Hiện" },
  { value: "COMPLETED", label: "Hoàn Thành" },
  { value: "ON_HOLD", label: "Tạm Dừng" },
  { value: "CANCELLED", label: "Đã Hủy" },
];
```

### 1.5. Cách Chọn Dự Án và Nhân Viên

#### Lấy Project ID:

**Cách 1: Từ URL dự án**

```
/admin/projects/123e4567-e89b-12d3-a456-426614174000/edit
                ↑ Project ID
```

**Cách 2: Từ API** (Development)

```javascript
// GET /api/v1/projects
const projects = await apiProjects.list();
console.log(projects[0].id); // "123e4567-..."
```

**Cách 3: Copy từ màn hình dự án**

- Vào Projects → Click dự án → Copy ID từ URL

#### Lấy User ID (Staff):

**Cách 1: Từ Staff Management**

```
/admin/staff → Xem chi tiết staff → Copy ID từ URL
```

**Cách 2: Từ Project Members**

- Vào dự án → Xem team members → Copy user ID

### 1.6. Ví Dụ Thực Tế

#### Ví Dụ 1: Tạo Task Phát Triển Feature

```json
{
  "projectId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "title": "Phát triển module đăng nhập OAuth2",
  "description": "Tích hợp Google và Facebook OAuth2.\nBao gồm: UI login, API integration, error handling",
  "assignedToUserId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "status": "NOT_STARTED",
  "startDate": "2026-01-15",
  "dueDate": "2026-02-15"
}
```

#### Ví Dụ 2: Tạo Task Testing

```json
{
  "projectId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "title": "Testing module thanh toán",
  "description": "- Unit tests\n- Integration tests\n- Manual testing\n- Write test report",
  "assignedToUserId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "status": "NOT_STARTED",
  "startDate": "2026-02-01",
  "dueDate": "2026-02-10"
}
```

### 1.7. Quyền Hạn

| Hành Động | Admin | PM  | Staff | User |
| --------- | ----- | --- | ----- | ---- |
| Tạo task  | ✅    | ✅  | ❌    | ❌   |
| Xem tasks | ✅    | ✅  | ✅\*  | ❌   |
| Sửa task  | ✅    | ✅  | ✅\*  | ❌   |
| Xóa task  | ✅    | ✅  | ❌    | ❌   |

\*Staff chỉ sửa được tasks được assign cho mình

### 1.8. Backend API Reference

**Endpoint:** `/api/v1/projects/{projectId}/tasks`

```java
// TaskController.java
@PostMapping("/projects/{projectId}/tasks")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PM')")
public ResponseEntity<TaskResponse> create(
    @PathVariable UUID projectId,
    @Valid @RequestBody TaskRequest request
) {
    request.setProjectId(projectId);
    return ResponseEntity.ok(taskService.create(request));
}
```

**Request Body:**

```json
{
  "title": "string (required, max 200)",
  "description": "string (optional, max 1000)",
  "assignedToUserId": "UUID (required)",
  "status": "enum (required)",
  "startDate": "YYYY-MM-DD (optional)",
  "dueDate": "YYYY-MM-DD (optional)"
}
```

**Response:**

```json
{
  "id": "uuid",
  "projectId": "uuid",
  "title": "string",
  "description": "string",
  "assignedToUserId": "uuid",
  "assignedToUserName": "string",
  "status": "enum",
  "startDate": "YYYY-MM-DD",
  "dueDate": "YYYY-MM-DD",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

---

## PHẦN 2: QUẢN LÝ DAILY TASKS (CÔNG VIỆC HẰNG NGÀY)

### 2.1. Workflow Daily Tasks

```
PM/Admin → Chọn Dự Án → Tạo Daily Task cho ngày cụ thể →
Assign Staff → Staff cập nhật tiến độ hằng ngày
```

### 2.2. Bước 1: Truy Cập Daily Task Board

1. Đăng nhập với quyền Admin hoặc PM
2. Click menu **"📋 Daily Tasks"** ở sidebar
3. Xem daily task board với filters

### 2.3. Bước 2: Tạo Daily Task

**Từ Daily Task Board:**

1. Click nút **"+ Add"** (góc phải trên)
2. Điền form:

```javascript
{
  "projectId": "UUID dự án",          // REQUIRED
  "assignedToUserId": "UUID staff",   // REQUIRED
  "title": "Tên công việc ngày",      // REQUIRED
  "description": "Mô tả chi tiết",    // OPTIONAL
  "taskDate": "2026-01-09",           // REQUIRED - Ngày làm việc
  "estimatedHours": 4.5,              // OPTIONAL - Số giờ dự kiến
  "priority": "HIGH",                 // REQUIRED
  "status": "PENDING"                 // REQUIRED
}
```

3. Click **"Submit"**

### 2.4. Các Thuộc Tính Daily Task

#### Priority (Độ Ưu Tiên)

```javascript
const priorities = ["LOW", "MEDIUM", "HIGH", "URGENT"];
```

#### Status (Trạng Thái)

```javascript
const statuses = [
  "PENDING", // Chờ thực hiện
  "IN_PROGRESS", // Đang làm
  "COMPLETED", // Hoàn thành
  "BLOCKED", // Bị chặn
  "CANCELLED", // Đã hủy
];
```

### 2.5. Filters và Tìm Kiếm

**Daily Task Board có 3 filters:**

1. **Project Filter** - Lọc theo dự án
2. **Date Filter** - Lọc theo ngày
3. **Status Filter** - Lọc theo trạng thái

```vue
<!-- Frontend Filter Example -->
<el-select v-model="selectedProject" @change="loadTasks">
  <el-option 
    v-for="project in projects" 
    :key="project.id" 
    :label="project.projectName"
    :value="project.id" 
  />
</el-select>
```

### 2.6. Ví Dụ Thực Tế

#### Ví Dụ 1: Chia Task Cho Frontend Developer

**Ngày 09/01/2026:**

```json
[
  {
    "projectId": "a1b2c3d4-...",
    "assignedToUserId": "frontend-dev-uuid",
    "title": "Implement login form UI",
    "description": "- Design form layout\n- Add validation\n- Connect to API",
    "taskDate": "2026-01-09",
    "estimatedHours": 4,
    "priority": "HIGH",
    "status": "PENDING"
  },
  {
    "projectId": "a1b2c3d4-...",
    "assignedToUserId": "frontend-dev-uuid",
    "title": "Fix responsive issues on mobile",
    "description": "Test on iOS and Android devices",
    "taskDate": "2026-01-09",
    "estimatedHours": 3,
    "priority": "MEDIUM",
    "status": "PENDING"
  }
]
```

#### Ví Dụ 2: Chia Task Cho Backend Developer

**Ngày 09/01/2026:**

```json
[
  {
    "projectId": "a1b2c3d4-...",
    "assignedToUserId": "backend-dev-uuid",
    "title": "Implement authentication API",
    "description": "JWT token generation and validation",
    "taskDate": "2026-01-09",
    "estimatedHours": 5,
    "priority": "URGENT",
    "status": "IN_PROGRESS"
  },
  {
    "projectId": "a1b2c3d4-...",
    "assignedToUserId": "backend-dev-uuid",
    "title": "Database optimization",
    "description": "Add indexes, optimize queries",
    "taskDate": "2026-01-09",
    "estimatedHours": 2,
    "priority": "LOW",
    "status": "PENDING"
  }
]
```

#### Ví Dụ 3: Chia Task Cho QA Tester

**Ngày 09/01/2026:**

```json
{
  "projectId": "a1b2c3d4-...",
  "assignedToUserId": "qa-tester-uuid",
  "title": "Test user registration flow",
  "description": "- Test happy path\n- Test edge cases\n- Test error handling\n- Document bugs",
  "taskDate": "2026-01-09",
  "estimatedHours": 6,
  "priority": "HIGH",
  "status": "PENDING"
}
```

### 2.7. Workflow Hằng Ngày

#### Buổi Sáng (8:00 AM):

```
PM tạo daily tasks cho tất cả staff trong dự án
↓
Staff nhận notification và xem tasks của mình
↓
Staff bắt đầu làm việc, update status thành IN_PROGRESS
```

#### Trong Ngày:

```
Staff cập nhật tiến độ real-time
↓
PM monitor tiến độ qua Daily Task Board
↓
Nếu có blocker → Staff update status thành BLOCKED và thông báo PM
```

#### Cuối Ngày (5:00 PM):

```
Staff cập nhật status COMPLETED cho tasks đã xong
↓
PM review tổng thể tiến độ
↓
PM tạo daily tasks cho ngày hôm sau
```

### 2.8. Quyền Hạn Daily Tasks

| Hành Động          | Admin | PM  | Staff | User |
| ------------------ | ----- | --- | ----- | ---- |
| Tạo daily task     | ✅    | ✅  | ❌    | ❌   |
| Xem tất cả tasks   | ✅    | ✅  | ❌    | ❌   |
| Xem tasks của mình | ✅    | ✅  | ✅    | ❌   |
| Cập nhật status    | ✅    | ✅  | ✅\*  | ❌   |
| Xóa task           | ✅    | ✅  | ❌    | ❌   |

\*Staff chỉ cập nhật tasks của mình

### 2.9. Backend API Reference

**Base URL:** `/api/v1/daily-tasks`

#### Tạo Daily Task

```java
@PostMapping
@PreAuthorize("hasAuthority('ROLE_PROJECT_MANAGER') or hasAuthority('ROLE_ADMIN')")
public ResponseEntity<DailyTaskResponse> createDailyTask(
    @Valid @RequestBody DailyTaskRequest request
)
```

#### Lấy Tasks Theo Dự Án và Ngày

```java
@GetMapping("/project/{projectId}/date/{taskDate}")
public ResponseEntity<List<DailyTaskResponse>> getTasksByProjectAndDate(
    @PathVariable UUID projectId,
    @PathVariable LocalDate taskDate
)
```

#### Lấy Tasks Của Staff

```java
@GetMapping("/user/{userId}")
public ResponseEntity<List<DailyTaskResponse>> getTasksByAssignedUser(
    @PathVariable UUID userId
)
```

---

## PHẦN 3: BEST PRACTICES

### 3.1. Khi Nào Dùng Tasks vs Daily Tasks?

#### Dùng **Tasks** khi:

- ✅ Công việc kéo dài > 1 ngày
- ✅ Là deliverable/milestone quan trọng
- ✅ Cần tracking dài hạn
- ✅ Ví dụ: "Develop payment module", "Complete API documentation"

#### Dùng **Daily Tasks** khi:

- ✅ Công việc có thể hoàn thành trong 1 ngày
- ✅ Cần micro-management
- ✅ Tracking giờ làm việc chi tiết
- ✅ Ví dụ: "Fix button styling", "Write unit tests for User service"

### 3.2. Quy Trình Phân Công Hiệu Quả

#### Bước 1: Planning (Đầu Sprint/Tuần)

```
1. Admin/PM tạo Tasks dài hạn cho dự án
2. Assign Tasks cho các staff members
3. Set deadline và priority
```

#### Bước 2: Daily Breakdown

```
Mỗi sáng:
1. PM review Tasks đang IN_PROGRESS
2. Break down thành Daily Tasks cụ thể
3. Assign Daily Tasks cho từng staff
4. Set estimated hours realistic
```

#### Bước 3: Tracking

```
Trong ngày:
1. Staff update status real-time
2. PM monitor qua Daily Task Board
3. Daily standup review progress
```

#### Bước 4: Review

```
Cuối ngày:
1. Review completed Daily Tasks
2. Update status của Tasks chính
3. Plan cho ngày hôm sau
```

### 3.3. Tips Cho PM

#### 📋 Tạo Task Hiệu Quả:

```
✅ DO:
- Tiêu đề rõ ràng, action-oriented
- Description có checklist cụ thể
- Ước lượng thời gian realistic
- Assign đúng người có skill phù hợp

❌ DON'T:
- Tiêu đề mơ hồ: "Fix bugs"
- Không có description
- Deadline quá gấp
- Assign người không có skill
```

#### ⏰ Time Estimation:

```javascript
// Rule of thumb cho Daily Tasks
const estimationGuide = {
  "Bug fix": "1-2 hours",
  "Small feature": "2-4 hours",
  "UI component": "1-3 hours",
  "API endpoint": "2-5 hours",
  Testing: "1-4 hours",
  "Code review": "0.5-1 hour",
  Documentation: "1-2 hours",
};
```

#### 🎯 Priority Guidelines:

```javascript
const priorityRules = {
  URGENT: "Blocking production, security issues",
  HIGH: "Critical features, deadline approaching",
  MEDIUM: "Regular tasks, normal timeline",
  LOW: "Nice to have, optimization",
};
```

### 3.4. Tips Cho Staff

#### ✅ Cập Nhật Status Đúng Cách:

```
PENDING → Khi vừa nhận task
IN_PROGRESS → Khi bắt đầu làm
BLOCKED → Khi gặp vấn đề cần support (notify PM ngay!)
COMPLETED → Khi hoàn thành và test xong
CANCELLED → Nếu task bị cancel (PM thực hiện)
```

#### 💬 Communication:

```
Khi BLOCKED:
1. Update status thành BLOCKED
2. Comment chi tiết vấn đề
3. Tag PM trong comment
4. Đề xuất solution nếu có
```

#### ⏱️ Time Management:

```
1. Check Daily Tasks mỗi sáng
2. Ước lượng thời gian thực tế
3. Update progress regularly
4. Alert sớm nếu không kịp deadline
```

---

## PHẦN 4: DEMO SCENARIO

### Scenario: Dự Án E-Commerce Website

#### Setup Phase (PM):

**1. Tạo Project Tasks (Dài hạn):**

```javascript
const projectTasks = [
  {
    title: "Develop User Authentication Module",
    assignedTo: "Backend Dev",
    dueDate: "2026-01-31",
    status: "NOT_STARTED",
  },
  {
    title: "Design and Implement Product Catalog UI",
    assignedTo: "Frontend Dev",
    dueDate: "2026-02-15",
    status: "NOT_STARTED",
  },
  {
    title: "Setup Database and Models",
    assignedTo: "Backend Dev",
    dueDate: "2026-01-20",
    status: "IN_PROGRESS",
  },
];
```

**2. Break Down thành Daily Tasks (09/01/2026):**

```javascript
// Backend Developer
const backendDailyTasks = [
  {
    title: "Setup database schema for users table",
    estimatedHours: 2,
    priority: "HIGH",
    taskDate: "2026-01-09",
  },
  {
    title: "Implement user registration API",
    estimatedHours: 4,
    priority: "HIGH",
    taskDate: "2026-01-09",
  },
  {
    title: "Write unit tests for registration",
    estimatedHours: 2,
    priority: "MEDIUM",
    taskDate: "2026-01-09",
  },
];

// Frontend Developer
const frontendDailyTasks = [
  {
    title: "Design registration form mockup",
    estimatedHours: 2,
    priority: "MEDIUM",
    taskDate: "2026-01-09",
  },
  {
    title: "Implement registration form component",
    estimatedHours: 4,
    priority: "HIGH",
    taskDate: "2026-01-09",
  },
  {
    title: "Add form validation",
    estimatedHours: 1.5,
    priority: "MEDIUM",
    taskDate: "2026-01-09",
  },
];
```

#### Daily Workflow:

**08:00 AM - PM tạo tasks:**

```bash
POST /api/v1/daily-tasks
# Tạo 6 daily tasks như trên
```

**09:00 AM - Staff bắt đầu:**

```bash
# Backend Dev
PUT /api/v1/daily-tasks/{task1-id}
{ "status": "IN_PROGRESS" }

# Frontend Dev
PUT /api/v1/daily-tasks/{task4-id}
{ "status": "IN_PROGRESS" }
```

**12:00 PM - Update tiến độ:**

```bash
# Backend Dev completed task 1
PUT /api/v1/daily-tasks/{task1-id}
{ "status": "COMPLETED" }

# Bắt đầu task 2
PUT /api/v1/daily-tasks/{task2-id}
{ "status": "IN_PROGRESS" }
```

**03:00 PM - Frontend Dev gặp blocker:**

```bash
PUT /api/v1/daily-tasks/{task5-id}
{
  "status": "BLOCKED",
  "notes": "Waiting for API endpoint from backend"
}
# → PM được notify và assign task khác
```

**05:00 PM - End of day review:**

```bash
GET /api/v1/daily-tasks/project/{projectId}/date/2026-01-09

Response:
{
  "completed": 4,
  "in_progress": 1,
  "blocked": 1,
  "pending": 0
}
```

---

## PHẦN 5: TROUBLESHOOTING

### Issue 1: Không Tìm Thấy Project ID

**Giải pháp:**

```javascript
// Console trong browser
const projects = await fetch("/api/v1/projects").then((r) => r.json());
console.table(projects.map((p) => ({ name: p.projectName, id: p.id })));
```

### Issue 2: Không Biết User ID Của Staff

**Giải pháp:**

```javascript
// Console trong browser
const staff = await fetch("/api/v1/users/staff").then((r) => r.json());
console.table(
  staff.map((s) => ({ name: s.fullName, id: s.id, email: s.email }))
);
```

### Issue 3: Lỗi 403 Khi Tạo Task

**Nguyên nhân:** Không có quyền

**Giải pháp:**

- Kiểm tra role: Phải là ROLE_ADMIN hoặc ROLE_PM
- Check token còn hạn không
- Logout và login lại

### Issue 4: Daily Task Không Hiển Thị

**Kiểm tra:**

1. Filter đúng project chưa?
2. Filter đúng date chưa?
3. Task có tồn tại không? (check API trực tiếp)

```bash
GET /api/v1/daily-tasks/project/{projectId}
```

---

## PHẦN 6: ADVANCED FEATURES

### 6.1. Task Reports

Staff có thể tạo reports cho tasks:

```javascript
// API: POST /api/v1/task-reports
{
  "taskId": "uuid",
  "reportDate": "2026-01-09",
  "hoursSpent": 5.5,
  "progress": 75,
  "notes": "Completed API integration, pending testing",
  "status": "PENDING_APPROVAL"
}
```

### 6.2. Metrics và Analytics

Admin có thể xem metrics:

```javascript
// GET /api/v1/project-metrics/{projectId}
{
  "totalTasks": 45,
  "completedTasks": 32,
  "inProgressTasks": 10,
  "overdueTask": 3,
  "averageCompletionTime": "3.5 days",
  "teamProductivity": 85
}
```

### 6.3. Notifications (Future)

```javascript
// Khi được assign task mới
notification.show({
  title: "New Task Assigned",
  body: "You have been assigned: 'Implement login form'",
  action: "View Task",
});

// Khi task bị block
notification.show({
  title: "Task Blocked",
  body: "Frontend Dev blocked: Waiting for API",
  action: "Resolve",
});
```

---

## KẾT LUẬN

### Tóm Tắt Workflow:

```
1. Admin/PM tạo Project
2. Admin/PM tạo Tasks dài hạn cho dự án
3. PM assign Tasks cho Staff
4. Mỗi ngày PM tạo Daily Tasks chi tiết
5. Staff thực hiện và update status
6. PM monitor và adjust
7. Review và iterate
```

### Key Points:

✅ **Tasks** = Công việc dài hạn, milestone  
✅ **Daily Tasks** = Công việc hằng ngày, chi tiết  
✅ **Communication** = Key to success  
✅ **Regular updates** = Keep everyone in sync  
✅ **Realistic estimates** = Better planning

### Resources:

- Frontend code: `FE/src/components/admin/pages/TaskFormPage.vue`
- Backend API: `TaskController.java`, `DailyTaskController.java`
- Permission guide: `AUTHORIZATION_SYSTEM_GUIDE.md`

---

**Created:** 2026-01-09  
**Version:** 1.0  
**Author:** System Documentation
