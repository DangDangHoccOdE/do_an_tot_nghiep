# Hướng Dẫn Hệ Thống Phân Quyền (Authorization System)

## Tổng Quan

Hệ thống quản lý dự án sử dụng hệ thống phân quyền dựa trên vai trò (Role-Based Access Control - RBAC) với 4 vai trò chính.

## Các Vai Trò (Roles)

### 1. ROLE_ADMIN (Quản Trị Viên)

- **Quyền hạn**: Toàn quyền quản trị hệ thống
- **Truy cập**:
  - Quản lý người dùng (customers)
  - Quản lý nhân viên (staff)
  - Quản lý dự án (tạo, sửa, xóa)
  - Quản lý nhóm (teams)
  - Quản lý nhiệm vụ (tasks)
  - Quản lý doanh thu (revenue)
  - Xem và tính toán metrics
  - Quản lý daily tasks
  - Xem và phê duyệt task reports

### 2. ROLE_PM (Project Manager - Quản Lý Dự Án)

- **Quyền hạn**: Quản lý dự án và nhiệm vụ
- **Truy cập**:
  - Xem và quản lý dự án được gán
  - Tạo và quản lý nhiệm vụ
  - Tạo daily tasks
  - Xem metrics (không thể tính toán)
  - Xem và phê duyệt task reports
  - Quản lý team members trong dự án

### 3. ROLE_STAFF (Nhân Viên)

- **Quyền hạn**: Thực hiện nhiệm vụ được gán
- **Truy cập**:
  - Xem dự án được gán
  - Xem và cập nhật nhiệm vụ của mình
  - Xem daily tasks của mình
  - Tạo và cập nhật task reports
  - Xem thông tin team

### 4. ROLE_USER (Khách Hàng)

- **Quyền hạn**: Xem thông tin dự án của mình
- **Truy cập**:
  - Xem danh sách dự án của mình (/my-projects)
  - Xem tiến độ dự án
  - Không có quyền truy cập admin panel

## Cấu Trúc Code

### Frontend (Vue.js + Pinia)

#### useAuthStore.js - Store Quản Lý Authentication

**State:**

```javascript
{
  isLoggedIn: Boolean,
  user: Object,
  accessToken: String,
  refreshToken: String,
  role: String // 'ROLE_ADMIN', 'ROLE_PM', 'ROLE_STAFF', 'ROLE_USER'
}
```

**Getters:**

```javascript
// Kiểm tra authentication
canAuthenticate; // Có thể authenticate không (token còn hạn)
isTokenValid; // Access token có hợp lệ không

// Kiểm tra role cụ thể
hasRole(roleName); // Kiểm tra có role cụ thể: hasRole('ROLE_ADMIN')
isAdmin; // Có phải Admin không
isPM; // Có phải PM không
isStaff; // Có phải Staff không
isUser; // Có phải User (khách hàng) không

// Kiểm tra quyền truy cập
canManage; // Có quyền quản lý (Admin hoặc PM)
canAccessAdmin; // Có quyền vào admin panel (Admin, PM, Staff)
```

**Actions:**

```javascript
login(payload); // Đăng nhập
updateTokens(access, refresh); // Cập nhật tokens
logout(); // Đăng xuất
hydrate(); // Load từ localStorage
validateAndCleanup(); // Kiểm tra và xóa token hết hạn
```

### Sử Dụng Trong Components

#### 1. Kiểm Tra Quyền Trong Template

```vue
<template>
  <div>
    <!-- Chỉ Admin và PM mới thấy -->
    <button v-if="authStore.canManage">Tạo Dự Án</button>

    <!-- Chỉ Admin mới thấy -->
    <button v-if="authStore.isAdmin">Quản Lý Users</button>

    <!-- Kiểm tra role cụ thể -->
    <div v-if="authStore.hasRole('ROLE_STAFF')">Staff content</div>
  </div>
</template>

<script setup>
import { useAuthStore } from "@/stores/auth/useAuthStore";

const authStore = useAuthStore();
</script>
```

#### 2. Kiểm Tra Quyền Trong Computed

```javascript
import { computed } from "vue";
import { useAuthStore } from "@/stores/auth/useAuthStore";

const authStore = useAuthStore();

// Cách 1: Sử dụng getter sẵn có
const canCreate = computed(() => authStore.canManage);
const canDelete = computed(() => authStore.isAdmin);

// Cách 2: Sử dụng hasRole cho kiểm tra cụ thể
const canApprove = computed(() => {
  return authStore.hasRole("ROLE_PM") || authStore.hasRole("ROLE_ADMIN");
});

// Cách 3: Logic phức tạp hơn
const canEditProject = computed(() => {
  return authStore.isAdmin || (authStore.isPM && isOwnProject.value);
});
```

### Router Guards

File: `FE/src/router/index.js`

```javascript
router.beforeEach((to, from, next) => {
  const auth = useAuthStore();

  // 1. Hydrate store từ localStorage
  if (!auth.isLoggedIn) {
    auth.hydrate();
  }

  // 2. Validate và cleanup tokens hết hạn
  auth.validateAndCleanup();

  // 3. Redirect đã login khỏi trang auth
  const isAuthPage = ["/login", "/register", "/activate"].includes(to.path);
  if (isAuthPage && auth.canAuthenticate) {
    if (auth.canAccessAdmin) {
      return next("/admin");
    }
    return next("/my-projects");
  }

  // 4. Yêu cầu authentication
  if (to.meta.requiresAuth && !auth.canAuthenticate) {
    return next(`/login?redirect=${to.fullPath}`);
  }

  // 5. Kiểm tra role permission
  if (to.meta.roles && to.meta.roles.length > 0) {
    if (!auth.role) {
      return next(`/login?redirect=${to.fullPath}`);
    }

    if (!to.meta.roles.includes(auth.role)) {
      return next("/unauthorized");
    }
  }

  next();
});
```

#### Cấu Hình Meta Cho Routes

```javascript
{
  path: "/admin",
  component: AdminDashboard,
  meta: {
    requiresAuth: true,
    roles: ["ROLE_ADMIN", "ROLE_PM", "ROLE_STAFF"],
    hideLayout: true
  }
}
```

## Backend (Spring Boot)

### Annotation @PreAuthorize

Sử dụng trong Controllers để kiểm soát quyền truy cập API:

```java
// Chỉ Admin
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@PostMapping("/users")
public ResponseEntity<?> createUser(@RequestBody UserRequest request) {
    // ...
}

// Admin hoặc PM
@PreAuthorize("hasAuthority('ROLE_PROJECT_MANAGER') or hasAuthority('ROLE_ADMIN')")
@PostMapping("/projects")
public ResponseEntity<?> createProject(@RequestBody ProjectRequest request) {
    // ...
}

// Admin, PM, hoặc Staff
@PreAuthorize("hasAuthority('ROLE_STAFF') or hasAuthority('ROLE_PROJECT_MANAGER') or hasAuthority('ROLE_ADMIN')")
@GetMapping("/tasks")
public ResponseEntity<?> getTasks() {
    // ...
}
```

### JWT Token

**Payload chứa:**

```json
{
  "userId": "uuid",
  "type": "accessToken",
  "role": "ROLE_ADMIN",
  "isAdmin": true,
  "isPm": false,
  "isStaff": false,
  "isUser": false,
  "exp": 1234567890,
  "sub": "user@example.com"
}
```

### AuthController Response

**Login/Refresh Response:**

```json
{
  "status": "success",
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGc...",
    "refreshToken": "eyJhbGc...",
    "userId": "uuid",
    "email": "user@example.com",
    "fullName": "Nguyen Van A",
    "role": "ROLE_ADMIN"
  }
}
```

## Ma Trận Quyền (Permission Matrix)

| Chức Năng                          | ADMIN | PM   | STAFF | USER |
| ---------------------------------- | ----- | ---- | ----- | ---- |
| **Quản Lý Người Dùng (Customers)** |
| Xem danh sách customers            | ✅    | ❌   | ❌    | ❌   |
| Tạo customer                       | ✅    | ❌   | ❌    | ❌   |
| Sửa customer                       | ✅    | ❌   | ❌    | ❌   |
| Xóa customer                       | ✅    | ❌   | ❌    | ❌   |
| **Quản Lý Nhân Viên (Staff)**      |
| Xem danh sách staff                | ✅    | ✅\* | ❌    | ❌   |
| Tạo staff                          | ✅    | ❌   | ❌    | ❌   |
| Sửa staff                          | ✅    | ❌   | ❌    | ❌   |
| Xóa staff                          | ✅    | ❌   | ❌    | ❌   |
| **Quản Lý Dự Án**                  |
| Xem tất cả dự án                   | ✅    | ✅   | ✅    | ❌   |
| Xem dự án của mình                 | ✅    | ✅   | ✅    | ✅   |
| Tạo dự án                          | ✅    | ✅   | ❌    | ❌   |
| Sửa dự án                          | ✅    | ✅\* | ❌    | ❌   |
| Xóa dự án                          | ✅    | ❌   | ❌    | ❌   |
| **Quản Lý Nhiệm Vụ**               |
| Xem nhiệm vụ                       | ✅    | ✅   | ✅    | ❌   |
| Tạo nhiệm vụ                       | ✅    | ✅   | ❌    | ❌   |
| Sửa nhiệm vụ                       | ✅    | ✅   | ✅\*  | ❌   |
| Xóa nhiệm vụ                       | ✅    | ✅   | ❌    | ❌   |
| **Daily Tasks**                    |
| Tạo daily task                     | ✅    | ✅   | ❌    | ❌   |
| Xem daily tasks                    | ✅    | ✅   | ✅    | ❌   |
| Cập nhật daily task                | ✅    | ✅   | ✅\*  | ❌   |
| **Task Reports**                   |
| Tạo report                         | ✅    | ✅   | ✅    | ❌   |
| Xem reports                        | ✅    | ✅   | ✅\*  | ❌   |
| Phê duyệt report                   | ✅    | ✅   | ❌    | ❌   |
| **Doanh Thu**                      |
| Xem doanh thu                      | ✅    | ❌   | ❌    | ❌   |
| Quản lý doanh thu                  | ✅    | ❌   | ❌    | ❌   |
| **Metrics**                        |
| Xem metrics                        | ✅    | ✅   | ❌    | ❌   |
| Tính toán metrics                  | ✅    | ❌   | ❌    | ❌   |

_Lưu ý:_

- ✅\* = Có quyền nhưng giới hạn (chỉ với dữ liệu của mình hoặc dự án được gán)

## Best Practices

### 1. Luôn Kiểm Tra Quyền Ở Cả Frontend Và Backend

```javascript
// Frontend - để UX tốt hơn
<button v-if="authStore.canManage">Tạo</button>

// Backend - để bảo mật
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_PM')")
```

### 2. Sử Dụng Getters Có Sẵn

```javascript
// ✅ Tốt - dễ đọc, dễ maintain
const canCreate = computed(() => authStore.canManage);

// ❌ Tránh - code trùng lặp
const canCreate = computed(
  () => authStore.hasRole("ROLE_ADMIN") || authStore.hasRole("ROLE_PM")
);
```

### 3. Centralize Permission Logic

```javascript
// store hoặc composable
export const usePermissions = () => {
  const authStore = useAuthStore();

  const canEditProject = (projectId) => {
    if (authStore.isAdmin) return true;
    if (authStore.isPM && isUserProject(projectId)) return true;
    return false;
  };

  return { canEditProject };
};
```

### 4. Validation Layers

```
1. Router Guards          -> Ngăn truy cập routes không được phép
2. Component Logic        -> Ẩn/hiện UI elements
3. API Interceptor        -> Xử lý lỗi 401/403
4. Backend @PreAuthorize  -> Bảo mật cuối cùng
```

## Troubleshooting

### Lỗi: "authStore.hasRole is not a function"

**Nguyên nhân:** Getter được định nghĩa sai hoặc chưa được định nghĩa.

**Giải pháp:**

1. Kiểm tra useAuthStore.js có getter `hasRole` chưa
2. Đảm bảo sử dụng đúng cú pháp: `authStore.hasRole('ROLE_ADMIN')`
3. Import đúng store: `import { useAuthStore } from '@/stores/auth/useAuthStore'`

### Lỗi: Redirect loop

**Nguyên nhân:** Logic router guard không đúng.

**Giải pháp:**

1. Kiểm tra `auth.canAuthenticate` và `auth.validateAndCleanup()`
2. Đảm bảo trang login/register không có `meta: { requiresAuth: true }`
3. Kiểm tra redirect từ authenticated user về trang auth

### Lỗi: 403 Forbidden

**Nguyên nhân:** User không có quyền truy cập API.

**Giải pháp:**

1. Kiểm tra role của user trong token
2. Kiểm tra @PreAuthorize annotation trên backend
3. Kiểm tra router meta roles trên frontend

## Testing

### Test Permission Logic

```javascript
import { describe, it, expect, beforeEach } from "vitest";
import { setActivePinia, createPinia } from "pinia";
import { useAuthStore } from "@/stores/auth/useAuthStore";

describe("useAuthStore permissions", () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it("should check admin role correctly", () => {
    const store = useAuthStore();
    store.role = "ROLE_ADMIN";

    expect(store.isAdmin).toBe(true);
    expect(store.isPM).toBe(false);
    expect(store.canManage).toBe(true);
    expect(store.hasRole("ROLE_ADMIN")).toBe(true);
  });

  it("should check PM permissions", () => {
    const store = useAuthStore();
    store.role = "ROLE_PM";

    expect(store.isPM).toBe(true);
    expect(store.canManage).toBe(true);
    expect(store.canAccessAdmin).toBe(true);
  });
});
```

## Migration Notes

Nếu cần thêm role mới:

1. **Backend:**

   - Thêm role vào database (bảng `roles`)
   - Cập nhật JwtUtil.java (thêm case trong switch)
   - Thêm @PreAuthorize cho endpoints cần thiết

2. **Frontend:**
   - Thêm getter mới vào useAuthStore.js
   - Cập nhật router guards nếu cần
   - Cập nhật permission matrix trong docs

## Kết Luận

Hệ thống phân quyền được thiết kế:

- **Rõ ràng**: 4 roles với quyền hạn được định nghĩa rõ ràng
- **An toàn**: Kiểm tra ở cả frontend và backend
- **Linh hoạt**: Dễ dàng mở rộng và thay đổi
- **Dễ maintain**: Code DRY với getters tái sử dụng

Luôn nhớ: **Frontend security là UX, Backend security là must-have!**
