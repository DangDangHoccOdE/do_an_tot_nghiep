# ✅ SUMMARY: Các Thay Đổi Đã Thực Hiện

## 📋 TỔNG QUAN

Đã hoàn thành **4 yêu cầu chính**:

1. ✅ **Giải thích luồng Current vs Future Projects**
2. ✅ **Giải thích 5 trạng thái dự án & điều kiện tạo**
3. ✅ **Thêm logic check trùng tên dự án**
4. ✅ **Fix căn chỉnh columns trong bảng members**

---

## 🎯 1. LUỒNG CURRENT VS FUTURE PROJECTS

### Quy Tắc Phân Biệt

**Dự Án Hiện Tại (Current)**:

- Query param: `?bucket=current` hoặc không có
- API: `POST /api/v1/projects`
- Dự án đang/sắp thực hiện ngay

**Dự Án Tương Lai (Future)**:

- Query param: `?bucket=future`
- API: `POST /api/v1/projects/future`
- Dự án lên kế hoạch cho tương lai

### Code Logic

```javascript
// ProjectFormPage.vue - submitForm()
const bucket = route.query.bucket === "future" ? "future" : "current";

if (bucket === "future") {
  await apiProjects.createFuture(payload); // Future
} else {
  await apiProjects.create(payload); // Current
}
```

---

## 🔵 2. NĂM TRẠNG THÁI DỰ ÁN

| Status        | Tiếng Việt     | Badge      | Ý Nghĩa                       |
| ------------- | -------------- | ---------- | ----------------------------- |
| `PENDING`     | Đang duyệt     | 🟡 Warning | Vừa tạo, chờ phê duyệt        |
| `APPROVED`    | Đã duyệt       | 🔵 Info    | Đã duyệt, sẵn sàng triển khai |
| `IN_PROGRESS` | Đang thực hiện | 🟦 Info    | Đang triển khai thực tế       |
| `DONE`        | Hoàn thành     | 🟢 Success | Đã hoàn thành và bàn giao     |
| `CANCELLED`   | Hủy bỏ         | 🔴 Danger  | Bị hủy (từ bất kỳ trạng thái) |

### Điều Kiện Tạo Dự Án

**Bắt buộc**:

- ✅ `projectName` (max 200 ký tự, không trùng)
- ✅ `clientId` (phải tồn tại)

**Tùy chọn**:

- `description` (max 1000 ký tự)
- `budgetEstimated` (>= 0)
- `currencyUnit` (mặc định VND)
- `status` (mặc định PENDING)
- `timelineEstimated` (>= 1 ngày)
- `startDate` / `endDate` (endDate phải sau startDate)
- `memberIds` (có thể thêm sau)

---

## 🔍 3. LOGIC CHECK TRÙNG TÊN DỰ ÁN

### A. API Mới (Backend)

**Endpoint**: `GET /api/v1/projects/check-name/{projectName}?excludeId={id}`

**Controller** (`ProjectController.java`):

```java
@GetMapping("/check-name/{projectName}")
public ResponseEntity<Map<String, Object>> checkDuplicateName(
        @PathVariable String projectName,
        @RequestParam(required = false) String excludeId) {

    UUID excludeUUID = null;
    if (excludeId != null && !excludeId.isEmpty()) {
        excludeUUID = UUID.fromString(excludeId);
    }

    boolean exists = projectService.existsByNameExcludingId(projectName, excludeUUID);

    Map<String, Object> response = new HashMap<>();
    response.put("exists", exists);

    return ResponseEntity.ok(response);
}
```

**Service** (`ProjectServiceImpl.java`):

```java
@Override
public boolean existsByNameExcludingId(String projectName, UUID excludeId) {
    if (projectName == null || projectName.trim().isEmpty()) {
        return false;
    }

    String trimmedName = projectName.trim();

    if (excludeId == null) {
        // Tạo mới: check toàn bộ
        return projectRepository.existsByProjectNameAndDeleteFlagFalse(trimmedName);
    } else {
        // Edit: bỏ qua chính nó
        return projectRepository.existsByProjectNameAndIdNotAndDeleteFlagFalse(trimmedName, excludeId);
    }
}
```

**Repository** (`ProjectRepository.java`):

```java
boolean existsByProjectNameAndDeleteFlagFalse(String projectName);

boolean existsByProjectNameAndIdNotAndDeleteFlagFalse(String projectName, UUID id);
```

### B. Frontend Integration

**API Service** (`apiProjects.js`):

```javascript
checkDuplicateName: (projectName, excludeId) => {
  const params = excludeId ? { excludeId } : {};
  return get(
    `${basePath}/check-name/${encodeURIComponent(projectName)}`,
    params
  );
};
```

**Validation** (`projectRules.js`):

```javascript
const validateProjectName = async (rule, value, callback) => {
  if (!value || value.trim().length === 0) {
    callback();
    return;
  }

  try {
    const response = await apiProjects.checkDuplicateName(
      value.trim(),
      projectId
    );

    if (response?.exists) {
      callback(new Error(msg("projectNameExists")));
    } else {
      callback();
    }
  } catch (error) {
    console.error("Error checking duplicate project name:", error);
    callback(); // Không block user nếu API lỗi
  }
};
```

**Usage** (`ProjectFormPage.vue`):

```javascript
const rules = computed(() => createProjectRules(t, form, props.id));
```

### C. i18n Messages

**Tiếng Việt**:

```json
{
  "validations": {
    "projectNameExists": "Tên dự án đã tồn tại trong hệ thống"
  }
}
```

**Tiếng Anh**:

```json
{
  "validations": {
    "projectNameExists": "Project name already exists in the system"
  }
}
```

---

## 📐 4. FIX CĂN CHỈNH COLUMNS

### Vấn Đề

2 bảng có column widths KHÁC NHAU:

- Bảng Staff List: Actions width = 160px
- Bảng Selected Members: Actions width = 200px
  → Các cột khác KHÔNG THẲNG HÀNG

### Giải Pháp

**Thống nhất widths**:

```vue
<!-- Bảng 1: Staff List -->
<el-table-column label="Họ và tên" min-width="200" />
<el-table-column label="Vai trò IT" width="180" />
<!-- Thay đổi từ 160 -->
<el-table-column label="SĐT" width="140" />
<!-- Thay đổi từ 160 -->
<el-table-column width="100" align="right" />
<!-- Thay đổi từ 160 -->

<!-- Bảng 2: Selected Members -->
<el-table-column label="Họ và tên" min-width="200" />
<el-table-column label="Vai trò IT" width="180" />
<!-- Thay đổi từ 160 -->
<el-table-column label="SĐT" width="140" />
<!-- Thay đổi từ 160 -->
<el-table-column width="180" align="right" />
<!-- Giữ nguyên -->
```

### Kết Quả

Giờ "Vai trò IT" và "Số điện thoại" **THẲNG HÀNG** giữa 2 bảng ✅

---

## 📁 FILE ĐÃ THAY ĐỔI

### Frontend (7 files)

1. **FE/src/services/apiProjects.js**

   - ➕ Thêm `checkDuplicateName(projectName, excludeId)`

2. **FE/src/validations/projectRules.js**

   - ➕ Import `apiProjects`
   - ➕ Thêm parameter `projectId`
   - ➕ Thêm `validateProjectName()` async validator

3. **FE/src/locales/vi/admin.json**

   - ➕ Thêm `"projectNameExists": "Tên dự án đã tồn tại..."`

4. **FE/src/locales/en/admin.json**

   - ➕ Thêm `"projectNameExists": "Project name already exists..."`

5. **FE/src/components/admin/pages/ProjectFormPage.vue**
   - 🔧 Cập nhật `rules` computed để truyền `props.id`
   - 🔧 Fix column widths: 180, 140, 100/180

### Backend (4 files)

6. **ProjectController.java**

   - ➕ Import `HashMap`, `Map`
   - ➕ Thêm endpoint `@GetMapping("/check-name/{projectName}")`

7. **IProjectService.java**

   - ➕ Thêm method `existsByNameExcludingId(String, UUID)`

8. **ProjectServiceImpl.java**

   - ➕ Implement `existsByNameExcludingId()`

9. **ProjectRepository.java**
   - ➕ `existsByProjectNameAndDeleteFlagFalse(String)`
   - ➕ `existsByProjectNameAndIdNotAndDeleteFlagFalse(String, UUID)`

### Documentation (2 files)

10. **PROJECT_FLOW_EXPLAINED.md** (Mới)

    - Giải thích chi tiết toàn bộ luồng

11. **PROJECT_CHANGES_SUMMARY.md** (File này)
    - Tóm tắt các thay đổi

---

## 🧪 CÁCH TEST

### Test 1: Check Duplicate - Tạo Mới

1. Vào "Dự Án Hiện Tại" → Click "Thêm Mới"
2. Nhập tên dự án đã tồn tại (vd: "Website ABC")
3. Blur ra ngoài field
4. **Kỳ vọng**: Hiện lỗi "Tên dự án đã tồn tại trong hệ thống"

### Test 2: Check Duplicate - Edit

1. Vào "Dự Án Hiện Tại" → Click "Chỉnh Sửa" dự án "Website ABC"
2. KHÔNG đổi tên (vẫn "Website ABC")
3. Blur ra ngoài field
4. **Kỳ vọng**: KHÔNG hiện lỗi (bỏ qua chính nó)

### Test 3: Căn Chỉnh Columns

1. Vào "Dự Án Hiện Tại" → Click "Thêm Mới"
2. Scroll xuống phần "Thành viên dự án"
3. Quan sát 2 bảng
4. **Kỳ vọng**: Cột "Vai trò IT" và "SĐT" thẳng hàng

### Test 4: Phân Biệt Current vs Future

1. Vào "Dự Án Hiện Tại" → Thêm mới → Submit
   - **Kỳ vọng**: Gọi `POST /projects`
2. Vào "Dự Án Tương Lai" → Thêm mới → Submit
   - **Kỳ vọng**: Gọi `POST /projects/future`

---

## 🚀 NEXT STEPS

1. **Backend**: Deploy code mới lên server
2. **Frontend**: Build và deploy
3. **Test**: Chạy qua 4 test cases trên
4. **Monitor**: Theo dõi logs để đảm bảo API check-name hoạt động tốt

---

## 📚 TÀI LIỆU THAM KHẢO

- **PROJECT_FLOW_EXPLAINED.md**: Giải thích chi tiết luồng và logic
- **PROJECT_CREATION_LOGIC.md**: Tài liệu ban đầu về trạng thái

---

**Tất cả đã hoàn thành! 🎉**
