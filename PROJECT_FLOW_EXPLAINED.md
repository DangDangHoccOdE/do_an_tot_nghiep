# 🎯 GIẢI THÍCH CHI TIẾT LUỒNG DỰ ÁN & LOGIC

## 📌 MỤC LỤC

1. [Khi Nào Dự Án Vào "Hiện Tại" vs "Tương Lai"](#1-khi-nào-dự-án-vào-hiện-tại-vs-tương-lai)
2. [Giải Thích 5 Trạng Thái Dự Án](#2-giải-thích-5-trạng-thái-dự-án)
3. [Điều Kiện Tạo Dự Án](#3-điều-kiện-tạo-dự-án)
4. [Logic Check Trùng Tên Dự Án](#4-logic-check-trùng-tên-dự-án)
5. [Fix Căn Chỉnh Columns](#5-fix-căn-chỉnh-columns)

---

## 1️⃣ KHI NÀO DỰ ÁN VÀO "HIỆN TẠI" VS "TƯƠNG LAI"

### 🔹 A. Dự Án Hiện Tại (Current Projects)

**Định nghĩa**: Dự án đang được thực hiện NGAY BÂY GIỜ hoặc SẮP ĐƯỢC TRIỂN KHAI

**Đặc điểm**:

- Có ngày bắt đầu trong hiện tại hoặc quá khứ gần
- Đã được phê duyệt và có đội ngũ triển khai
- Cần theo dõi tiến độ thường xuyên
- Status thường là: `APPROVED`, `IN_PROGRESS`, hoặc `DONE`

**Cách phân biệt trong code**:

```javascript
// ProjectsPage.vue - Khi click "Thêm Mới" từ trang Current Projects
const goCreate = () => {
  router.push({
    name: "admin-projects-new",
    query: { bucket: "current" }, // ← Đánh dấu là current
  });
};

// ProjectFormPage.vue - Khi submit
const submitForm = () => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return;

    const bucket = route.query.bucket === "future" ? "future" : "current";

    if (bucket === "future") {
      await apiProjects.createFuture(payload); // ❌ KHÔNG gọi API này
    } else {
      await apiProjects.create(payload); // ✅ Gọi API này
    }
  });
};
```

**API Endpoint**:

```
POST /projects
Body: { projectName, clientId, status, ... }
```

---

### 🔹 B. Dự Án Tương Lai (Future Projects)

**Định nghĩa**: Dự án ĐƯỢC LÊN KẾ HOẠCH cho tương lai, chưa được triển khai

**Đặc điểm**:

- Ngày bắt đầu trong tương lai (ví dụ: sau 3 tháng, sau 6 tháng)
- Đang trong giai đoạn lên kế hoạch, đàm phán với khách hàng
- Chưa cần phân công đội ngũ chi tiết
- Status thường là: `PENDING` hoặc `APPROVED`

**Cách phân biệt trong code**:

```javascript
// ProjectsPage.vue - Khi click "Thêm Mới" từ trang Future Projects
const goCreate = () => {
  router.push({
    name: "admin-projects-new",
    query: { bucket: "future" }, // ← Đánh dấu là future
  });
};

// ProjectFormPage.vue - Khi submit
const submitForm = () => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return;

    const bucket = route.query.bucket === "future" ? "future" : "current";

    if (bucket === "future") {
      await apiProjects.createFuture(payload); // ✅ Gọi API này
    } else {
      await apiProjects.create(payload); // ❌ KHÔNG gọi API này
    }
  });
};
```

**API Endpoint**:

```
POST /projects/future
Body: { projectName, clientId, status, ... }
```

---

### 🔹 C. Bảng So Sánh Chi Tiết

| Tiêu Chí                | Dự Án Hiện Tại                 | Dự Án Tương Lai               |
| ----------------------- | ------------------------------ | ----------------------------- |
| **Thời gian bắt đầu**   | Hiện tại hoặc quá khứ gần      | Tương lai (> 1-3 tháng)       |
| **Trạng thái phổ biến** | APPROVED, IN_PROGRESS, DONE    | PENDING, APPROVED             |
| **Query Param**         | `?bucket=current`              | `?bucket=future`              |
| **API Create**          | `POST /projects`               | `POST /projects/future`       |
| **API List**            | `GET /projects?status=current` | `GET /projects?status=future` |
| **Menu trong Admin**    | "Dự án hiện tại"               | "Dự án tương lai"             |
| **Route Name**          | `admin-projects-current`       | `admin-projects-future`       |
| **Props trong Page**    | `status="current"`             | `status="future"`             |
| **Mục đích**            | Quản lý dự án đang chạy        | Lên kế hoạch dự án sắp tới    |
| **Đội ngũ**             | Đã có đội ngũ cụ thể           | Có thể chưa phân công         |
| **Theo dõi**            | Theo dõi tiến độ hàng ngày     | Theo dõi milestone lớn        |

---

### 🔹 D. Flow Chọn Bucket (Current vs Future)

```
User ở trang "Dự Án Hiện Tại"
    ↓
Click nút "Thêm Mới"
    ↓
goCreate() → router.push({ query: { bucket: 'current' } })
    ↓
ProjectFormPage nhận route.query.bucket = 'current'
    ↓
User điền form và submit
    ↓
submitForm() kiểm tra:
    const bucket = route.query.bucket === 'future' ? 'future' : 'current'
    ↓ (bucket = 'current')
await apiProjects.create(payload)
    ↓
Backend lưu vào bảng projects với flag status_type = 'current'
    ↓
Redirect về danh sách "Dự Án Hiện Tại"
```

```
User ở trang "Dự Án Tương Lai"
    ↓
Click nút "Thêm Mới"
    ↓
goCreate() → router.push({ query: { bucket: 'future' } })
    ↓
ProjectFormPage nhận route.query.bucket = 'future'
    ↓
User điền form và submit
    ↓
submitForm() kiểm tra:
    const bucket = route.query.bucket === 'future' ? 'future' : 'current'
    ↓ (bucket = 'future')
await apiProjects.createFuture(payload)
    ↓
Backend lưu vào bảng projects với flag status_type = 'future'
    ↓
Redirect về danh sách "Dự Án Tương Lai"
```

---

## 2️⃣ GIẢI THÍCH 5 TRẠNG THÁI DỰ ÁN

### 🟡 1. PENDING (Đang duyệt)

**Ý nghĩa**: Dự án VỪA MỚI TẠO, đang chờ được phê duyệt

**Khi nào dùng**:

- Admin/PM tạo dự án mới
- Dự án đang trong giai đoạn đàm phán với khách hàng
- Chưa có quyết định chính thức về triển khai

**Hành động tiếp theo**:

- Ban lãnh đạo xem xét → Phê duyệt (APPROVED) hoặc Từ chối (CANCELLED)

**Badge màu**: 🟡 Warning (Vàng)

**Ví dụ**:

```
Dự án: "Website Thương Mại Điện Tử ABC"
Status: PENDING
Lý do: Đang đàm phán giá và timeline với khách hàng
```

---

### 🔵 2. APPROVED (Đã duyệt)

**Ý nghĩa**: Dự án ĐÃ ĐƯỢC PHÊ DUYỆT, sẵn sàng triển khai

**Khi nào dùng**:

- Ban lãnh đạo đã chấp thuận dự án
- Đã ký hợp đồng với khách hàng
- Đang chuẩn bị đội ngũ và tài nguyên

**Hành động tiếp theo**:

- Phân công đội ngũ
- Bắt đầu triển khai → Chuyển sang IN_PROGRESS

**Badge màu**: 🔵 Info (Xanh dương)

**Ví dụ**:

```
Dự án: "Website Thương Mại Điện Tử ABC"
Status: APPROVED
Lý do: Hợp đồng đã ký, đang chọn đội ngũ
```

---

### 🟦 3. IN_PROGRESS (Đang thực hiện)

**Ý nghĩa**: Dự án ĐANG ĐƯỢC TRIỂN KHAI thực tế

**Khi nào dùng**:

- Đội ngũ đã bắt đầu làm việc
- Đang code, design, test,...
- Có tiến độ cụ thể theo tuần/tháng

**Hành động tiếp theo**:

- Theo dõi tiến độ hàng ngày
- Khi hoàn thành → Chuyển sang DONE
- Nếu có vấn đề nghiêm trọng → CANCELLED

**Badge màu**: 🟦 Info (Xanh)

**Ví dụ**:

```
Dự án: "Website Thương Mại Điện Tử ABC"
Status: IN_PROGRESS
Lý do: Team đang code sprint 3/10
```

---

### 🟢 4. DONE (Hoàn thành)

**Ý nghĩa**: Dự án ĐÃ HOÀN THÀNH và BÀN GIAO

**Khi nào dùng**:

- Đã deploy production
- Khách hàng đã chấp nhận và thanh toán
- Tất cả tasks đã đóng

**Hành động tiếp theo**:

- Lưu trữ tài liệu
- Đánh giá hiệu suất đội ngũ
- Không còn thay đổi gì (trừ hotfix)

**Badge màu**: 🟢 Success (Xanh lá)

**Ví dụ**:

```
Dự án: "Website Thương Mại Điện Tử ABC"
Status: DONE
Lý do: Đã deploy và bàn giao cho khách ngày 15/12/2025
```

---

### 🔴 5. CANCELLED (Hủy bỏ)

**Ý nghĩa**: Dự án BỊ HỦY BỎ, không tiếp tục nữa

**Khi nào dùng**:

- Khách hàng hủy hợp đồng
- Ngân sách không đủ
- Yêu cầu thay đổi quá lớn
- Có thể hủy ở BẤT KỲ TRẠNG THÁI NÀO

**Hành động tiếp theo**:

- Lưu trữ lý do hủy
- Giải phóng tài nguyên (nhân viên, máy chủ,...)
- Không còn hoạt động gì nữa

**Badge màu**: 🔴 Danger (Đỏ)

**Ví dụ**:

```
Dự án: "Website Thương Mại Điện Tử ABC"
Status: CANCELLED
Lý do: Khách hàng hủy hợp đồng do khủng hoảng tài chính
```

---

### 🔹 Biểu Đồ Chuyển Đổi Trạng Thái

```
┌──────────────────────────────────────────────────┐
│         LUỒNG TRẠNG THÁI DỰ ÁN CHUẨN             │
└──────────────────────────────────────────────────┘

   TẠO DỰ ÁN MỚI
        ↓
   🟡 PENDING (Đang duyệt)
        ↓
   [Ban lãnh đạo duyệt]
        ↓
   🔵 APPROVED (Đã duyệt)
        ↓
   [Bắt đầu triển khai]
        ↓
   🟦 IN_PROGRESS (Đang thực hiện)
        ↓
   [Hoàn thành và bàn giao]
        ↓
   🟢 DONE (Hoàn thành)


        🔴 CANCELLED (Hủy bỏ)
        ↑
        └── Có thể HỦY từ BẤT KỲ trạng thái nào
```

---

## 3️⃣ ĐIỀU KIỆN TẠO DỰ ÁN

### 🔹 A. Validation Bắt Buộc (Required)

| Field                 | Bắt buộc?   | Lý do                                              |
| --------------------- | ----------- | -------------------------------------------------- |
| **projectName**       | ✅ BẮT BUỘC | Phải có tên để định danh dự án                     |
| **clientId**          | ✅ BẮT BUỘC | Phải biết dự án cho khách hàng nào                 |
| **description**       | ❌ Tùy chọn | Mô tả chi tiết (khuyến khích nhưng không bắt buộc) |
| **budgetEstimated**   | ❌ Tùy chọn | Ngân sách ước tính (có thể chưa rõ)                |
| **currencyUnit**      | ❌ Tùy chọn | Mặc định VND                                       |
| **status**            | ❌ Tùy chọn | Mặc định PENDING                                   |
| **timelineEstimated** | ❌ Tùy chọn | Timeline ước tính (ngày)                           |
| **startDate**         | ❌ Tùy chọn | Ngày bắt đầu dự án                                 |
| **endDate**           | ❌ Tùy chọn | Ngày kết thúc dự án                                |
| **memberIds**         | ❌ Tùy chọn | Danh sách thành viên (có thể thêm sau)             |

---

### 🔹 B. Validation Nghiệp Vụ

#### 1. **Tên Dự Án (projectName)**

```javascript
// Các quy tắc:
✅ Bắt buộc phải có
✅ Tối đa 200 ký tự
✅ KHÔNG được trùng với dự án khác (mới thêm)

// Code validation:
{
  required: true,
  max: 200,
  validator: validateProjectName  // ← Check trùng qua API
}
```

#### 2. **Khách Hàng (clientId)**

```javascript
// Các quy tắc:
✅ Bắt buộc phải chọn
✅ Phải tồn tại trong danh sách Users

// Code validation:
{
  required: true
}
```

#### 3. **Ngân Sách (budgetEstimated)**

```javascript
// Các quy tắc:
✅ Phải >= 0 (không âm)
✅ Nếu không nhập → null (không bắt buộc)

// Code validation:
{
  type: 'number',
  min: 0
}
```

#### 4. **Timeline (timelineEstimated)**

```javascript
// Các quy tắc:
✅ Phải >= 1 ngày (nếu nhập)
✅ Nếu không nhập → null

// Code validation:
{
  type: 'number',
  min: 1
}
```

#### 5. **Ngày Bắt Đầu & Kết Thúc**

```javascript
// Các quy tắc:
✅ endDate phải SAU startDate
✅ Nếu không nhập → null (không bắt buộc)

// Code validation:
const validateDates = (rule, value, callback) => {
  if (value && rule?.field === 'endDate' && model?.startDate) {
    const start = new Date(model.startDate)
    const end = new Date(value)

    if (end < start) {
      callback(new Error('Ngày kết thúc phải sau ngày bắt đầu'))
      return
    }
  }
  callback()
}
```

---

### 🔹 C. Flow Validation Khi Submit

```
User nhập form và click "Tạo Mới"
    ↓
submitForm() được gọi
    ↓
formRef.value?.validate((valid) => {
    ↓
  [1] Check projectName: required, max, duplicate
  [2] Check clientId: required
  [3] Check budgetEstimated: >= 0
  [4] Check timelineEstimated: >= 1
  [5] Check startDate < endDate
    ↓
  if (valid) {
    ✅ Gọi API create/createFuture
  } else {
    ❌ Hiển thị lỗi ở các field
  }
})
```

---

## 4️⃣ LOGIC CHECK TRÙNG TÊN DỰ ÁN

### 🔹 A. Vấn Đề Ban Đầu

**Trước đây**: Không có validation check trùng tên dự án
→ Có thể tạo nhiều dự án cùng tên
→ Gây nhầm lẫn khi quản lý

**Giải pháp**: Thêm API endpoint và async validator

---

### 🔹 B. API Mới: Check Duplicate Name

**File**: `FE/src/services/apiProjects.js`

```javascript
export const apiProjects = {
  // ... các API cũ

  // API mới: Check tên dự án có tồn tại không
  checkDuplicateName: (projectName, excludeId) => {
    const params = excludeId ? { excludeId } : {};
    return get(
      `${basePath}/check-name/${encodeURIComponent(projectName)}`,
      params
    );
  },
};
```

**Endpoint**:

```
GET /projects/check-name/{projectName}?excludeId={id}

Response:
{
  "exists": true,        // ← Tên đã tồn tại
  "projectId": "abc123"  // ← ID của dự án trùng tên
}

hoặc

{
  "exists": false  // ← Tên chưa tồn tại, OK
}
```

**Giải thích tham số**:

- `projectName`: Tên dự án cần check
- `excludeId` (optional): ID của dự án hiện tại (khi EDIT, bỏ qua chính nó)

---

### 🔹 C. Validator Check Trùng Tên

**File**: `FE/src/validations/projectRules.js`

```javascript
import { apiProjects } from "@/services/apiProjects";

export const createProjectRules = (t, model, projectId = null) => {
  // Validator async: gọi API check duplicate
  const validateProjectName = async (rule, value, callback) => {
    // Nếu chưa nhập gì, skip (required rule sẽ bắt)
    if (!value || value.trim().length === 0) {
      callback();
      return;
    }

    try {
      // Gọi API check tên có tồn tại không
      const response = await apiProjects.checkDuplicateName(
        value.trim(),
        projectId // ← Khi edit, bỏ qua chính nó
      );

      if (response?.exists) {
        // ❌ Tên đã tồn tại
        callback(new Error("Tên dự án đã tồn tại trong hệ thống"));
      } else {
        // ✅ Tên chưa tồn tại, OK
        callback();
      }
    } catch (error) {
      // Nếu API lỗi, vẫn cho phép tiếp tục
      // (không block user vì lỗi server)
      console.error("Error checking duplicate project name:", error);
      callback();
    }
  };

  return {
    projectName: [
      { required: true, message: "Tên dự án là bắt buộc" },
      { max: 200, message: "Tên dự án tối đa 200 ký tự" },
      { validator: validateProjectName, trigger: "blur" }, // ← Check khi blur
    ],
  };
};
```

---

### 🔹 D. Cách Dùng Trong Component

**File**: `FE/src/components/admin/pages/ProjectFormPage.vue`

```javascript
// Truyền projectId vào để bỏ qua khi edit
const rules = computed(() => createProjectRules(t, form, props.id));
```

**Giải thích**:

- Khi **TẠO MỚI**: `props.id = null` → Check toàn bộ dự án
- Khi **EDIT**: `props.id = "abc123"` → Check nhưng bỏ qua dự án hiện tại

---

### 🔹 E. Flow Hoạt Động

```
User nhập tên dự án: "Website ABC"
    ↓
User blur ra ngoài field (rời khỏi ô input)
    ↓
validateProjectName() được gọi
    ↓
await apiProjects.checkDuplicateName("Website ABC", props.id)
    ↓
Backend query database:
  SELECT COUNT(*) FROM projects
  WHERE projectName = 'Website ABC'
  AND id != 'abc123'  -- ← Bỏ qua chính nó (nếu edit)
    ↓
  if (count > 0) {
    return { exists: true }   // ❌ Đã tồn tại
  } else {
    return { exists: false }  // ✅ Chưa tồn tại
  }
    ↓
Frontend nhận response
    ↓
  if (exists === true) {
    ❌ Hiển thị lỗi: "Tên dự án đã tồn tại trong hệ thống"
    ❌ Không cho submit form
  } else {
    ✅ OK, cho phép tiếp tục
  }
```

---

### 🔹 F. Trường Hợp Đặc Biệt: Edit Project

**Vấn đề**: Khi EDIT dự án, nếu user không đổi tên, sẽ báo "trùng tên" với chính nó

**Giải pháp**: Truyền `excludeId` = ID dự án hiện tại

```javascript
// TẠO MỚI
const rules = computed(() => createProjectRules(t, form, null));
// → excludeId = null → Check toàn bộ

// EDIT
const rules = computed(() => createProjectRules(t, form, props.id));
// → excludeId = props.id → Bỏ qua chính nó
```

**Backend Logic**:

```sql
-- Khi tạo mới (excludeId = null)
SELECT COUNT(*) FROM projects
WHERE projectName = 'Website ABC'

-- Khi edit (excludeId = 'abc123')
SELECT COUNT(*) FROM projects
WHERE projectName = 'Website ABC'
AND id != 'abc123'  -- ← Bỏ qua chính nó
```

---

### 🔹 G. Messages i18n Đã Thêm

**Tiếng Việt** (`locales/vi/admin.json`):

```json
{
  "validations": {
    "projectNameExists": "Tên dự án đã tồn tại trong hệ thống"
  }
}
```

**Tiếng Anh** (`locales/en/admin.json`):

```json
{
  "validations": {
    "projectNameExists": "Project name already exists in the system"
  }
}
```

---

## 5️⃣ FIX CĂN CHỈNH COLUMNS

### 🔹 A. Vấn Đề Ban Đầu

**Hiện tượng**: 2 bảng (Staff List & Selected Members) có column widths KHÁC NHAU
→ Vai trò IT và Số điện thoại KHÔNG THẲNG HÀNG

**Trước đây**:

```vue
<!-- Bảng 1: Staff List -->
<el-table-column label="Vai trò IT" width="160" />
<!-- 160px -->
<el-table-column label="Số điện thoại" width="160" />
<!-- 160px -->
<el-table-column width="160" />
<!-- Actions: 160px -->

<!-- Bảng 2: Selected Members -->
<el-table-column label="Vai trò IT" width="160" />
<!-- 160px -->
<el-table-column label="Số điện thoại" width="160" />
<!-- 160px -->
<el-table-column width="200" />
<!-- Actions: 200px ❌ KHÁC -->
```

→ Column "Actions" khác nhau (160px vs 200px)
→ Các cột khác cũng lệch

---

### 🔹 B. Giải Pháp

**Thống nhất width cho TẤT CẢ columns**:

```vue
<!-- Bảng 1: Staff List -->
<el-table>
  <el-table-column label="Họ và tên" min-width="200" />
  <el-table-column label="Vai trò IT" width="180" />  <!-- ✅ 180px -->
  <el-table-column label="Số điện thoại" width="140" />  <!-- ✅ 140px -->
  <el-table-column width="100" align="right" />  <!-- ✅ 100px -->
</el-table>

<!-- Bảng 2: Selected Members -->
<el-table>
  <el-table-column label="Họ và tên" min-width="200" />
  <el-table-column label="Vai trò IT" width="180" />  <!-- ✅ 180px -->
  <el-table-column label="Số điện thoại" width="140" />  <!-- ✅ 140px -->
  <el-table-column width="180" align="right" />  <!-- ✅ 180px (rộng hơn vì có 2 nút) -->
</el-table>
```

---

### 🔹 C. Giải Thích Width

| Column               | Width             | Lý do                            |
| -------------------- | ----------------- | -------------------------------- |
| **Họ và tên**        | `min-width="200"` | Tên có thể dài, cần linh hoạt    |
| **Vai trò IT**       | `width="180"`     | Đủ chứa "Lập trình viên Backend" |
| **Số điện thoại**    | `width="140"`     | Đủ chứa số VN: 0123 456 789      |
| **Actions (Bảng 1)** | `width="100"`     | Chỉ 1 nút "Thêm"                 |
| **Actions (Bảng 2)** | `width="180"`     | 2 nút "Xem" + "Xóa"              |

---

### 🔹 D. Kết Quả

**Trước khi fix**:

```
┌────────────┬──────────┬────────────┬─────────┐
│ Họ tên     │ Vai trò  │ SĐT        │ Actions │
├────────────┼──────────┼────────────┼─────────┤
│ Nguyễn A   │ Frontend │ 0987654321 │ [Thêm]  │
└────────────┴──────────┴────────────┴─────────┘

┌────────────┬──────────┬────────────┬──────────────┐
│ Họ tên     │ Vai trò  │ SĐT        │ Actions      │  ← RỘNG HƠN
├────────────┼──────────┼────────────┼──────────────┤
│ Nguyễn A   │ Frontend │ 0987654321 │ [Xem][Xóa]   │
└────────────┴──────────┴────────────┴──────────────┘
                ↑            ↑
            KHÔNG THẲNG HÀNG
```

**Sau khi fix**:

```
┌────────────┬────────────┬──────────┬─────────┐
│ Họ tên     │ Vai trò IT │ SĐT      │ Actions │
├────────────┼────────────┼──────────┼─────────┤
│ Nguyễn A   │ Frontend   │ 09876... │ [Thêm]  │
└────────────┴────────────┴──────────┴─────────┘

┌────────────┬────────────┬──────────┬──────────────┐
│ Họ tên     │ Vai trò IT │ SĐT      │ Actions      │
├────────────┼────────────┼──────────┼──────────────┤
│ Nguyễn A   │ Frontend   │ 09876... │ [Xem][Xóa]   │
└────────────┴────────────┴──────────┴──────────────┘
                ↑            ↑
            THẲNG HÀNG ✅
```

---

## 📊 TÓM TẮT TOÀN BỘ

### ✅ Đã Giải Quyết

1. **Phân biệt Current vs Future**:

   - Current: `bucket=current` → `apiProjects.create()`
   - Future: `bucket=future` → `apiProjects.createFuture()`

2. **5 Trạng Thái**:

   - PENDING → APPROVED → IN_PROGRESS → DONE
   - Có thể CANCELLED bất kỳ lúc

3. **Validation**:

   - projectName, clientId bắt buộc
   - endDate phải sau startDate
   - budgetEstimated >= 0

4. **Check Trùng Tên**:

   - API: `GET /projects/check-name/{name}`
   - Async validator khi blur
   - Bỏ qua chính nó khi edit

5. **Căn Chỉnh Columns**:
   - Vai trò IT: 180px
   - SĐT: 140px
   - Actions: 100px (bảng 1), 180px (bảng 2)

---

## 🎯 BACKEND API CẦN THÊM

**Endpoint mới cần implement**:

```java
@GetMapping("/check-name/{projectName}")
public ResponseEntity<Map<String, Object>> checkDuplicateName(
    @PathVariable String projectName,
    @RequestParam(required = false) String excludeId
) {
    boolean exists = projectService.existsByNameExcludingId(projectName, excludeId);

    Map<String, Object> response = new HashMap<>();
    response.put("exists", exists);

    if (exists) {
        Project existingProject = projectService.findByName(projectName);
        response.put("projectId", existingProject.getId());
    }

    return ResponseEntity.ok(response);
}
```

**Service Logic**:

```java
public boolean existsByNameExcludingId(String projectName, String excludeId) {
    if (excludeId == null) {
        return projectRepository.existsByProjectName(projectName);
    } else {
        return projectRepository.existsByProjectNameAndIdNot(projectName, excludeId);
    }
}
```

---

## 🚀 HƯỚNG DẪN SỬ DỤNG

1. **Tạo Dự Án Hiện Tại**:

   - Vào menu "Dự Án Hiện Tại"
   - Click "Thêm Mới"
   - Điền form → Submit
   - Hệ thống gọi `POST /projects`

2. **Tạo Dự Án Tương Lai**:

   - Vào menu "Dự Án Tương Lai"
   - Click "Thêm Mới"
   - Điền form → Submit
   - Hệ thống gọi `POST /projects/future`

3. **Check Trùng Tên**:

   - Nhập tên dự án
   - Blur ra ngoài
   - Nếu trùng → Hiện lỗi "Tên dự án đã tồn tại"

4. **Thêm Thành Viên**:
   - Tìm nhân viên theo vai trò IT
   - Click "Thêm" → Xuất hiện ở bảng dưới
   - Columns giờ thẳng hàng

---

**Tài liệu này đã giải thích CHI TIẾT mọi thứ bạn cần biết! 🎉**
