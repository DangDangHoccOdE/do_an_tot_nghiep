# 📋 Hướng Dẫn Logic Tạo Dự Án & Trạng Thái

## 1️⃣ LOGIC TẠO DỰ ÁN TOÀN BỘ

### 1.1. Flow Tạo Dự Án (ProjectFormPage.vue)

```
User Click "Thêm Mới"
    ↓
Router.push({ name: 'admin-projects-new', query: { bucket: 'current' | 'future' } })
    ↓
ProjectFormPage mounted với props.mode = 'create'
    ↓
Hiển thị Form với các trường:
    - Tên dự án (bắt buộc)
    - Khách hàng (bắt buộc)
    - Ngân sách
    - Đơn vị tiền tệ
    - Trạng thái (Pending mặc định)
    - Timeline
    - Ngày bắt đầu/kết thúc
    - Mô tả
    ↓
Thêm Thành Viên Dự Án (nếu là view mode không được chỉnh sửa)
    ↓
User Submit Form → validate() → submitForm()
    ↓
```

### 1.2. Hàm submitForm() Chi Tiết

```javascript
const submitForm = () => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return; // ← Validate form

    submitting.value = true;
    try {
      // Chuẩn bị dữ liệu gửi server
      const payload = {
        ...form,
        memberIds: selectedMembers.value.map((m) => m.userId), // ← Lấy ID thành viên
      };

      if (isCreate.value) {
        // ← Tạo mới
        // Lấy bucket từ query param
        const bucket = route.query.bucket === "future" ? "future" : "current";

        // Gọi API khác nhau tùy theo loại dự án
        if (bucket === "future") {
          await apiProjects.createFuture(payload); // ← API dự án tương lai
        } else {
          await apiProjects.create(payload); // ← API dự án hiện tại
        }

        // Hiển thị thông báo thành công
        handleSuccess(
          t("admin.messages.created", {
            entity: t("admin.entities.project"),
          })
        );
      } else {
        // ← Cập nhật
        await apiProjects.update(props.id, payload);

        handleSuccess(
          t("admin.messages.updated", {
            entity: t("admin.entities.project"),
          })
        );
      }

      // Redirect về danh sách
      redirectToList();
    } catch (error) {
      handleError(error, t, t("admin.messages.saveFail"));
    } finally {
      submitting.value = false;
    }
  });
};
```

---

## 2️⃣ PHÂN BIỆT DỰ ÁN HIỆN TẠI VS TƯƠNG LAI

| Tiêu Chí        | Dự Án Hiện Tại (Current)                  | Dự Án Tương Lai (Future)                 |
| --------------- | ----------------------------------------- | ---------------------------------------- |
| **Menu**        | "Dự án hiện tại"                          | "Dự án tương lai"                        |
| **Query Param** | `bucket=current` (mặc định)               | `bucket=future`                          |
| **API Create**  | `apiProjects.create()`                    | `apiProjects.createFuture()`             |
| **API List**    | `apiProjects.list({ status: 'current' })` | `apiProjects.list({ status: 'future' })` |
| **Router Name** | `admin-projects-current`                  | `admin-projects-future`                  |
| **Props**       | `status="current"`                        | `status="future"`                        |
| **Ý Nghĩa**     | Dự án đang thực hiện hoặc sắp thực hiện   | Dự án được lên kế hoạch cho tương lai    |

### 2.1. Cách Phân Biệt Trong Redirect

```javascript
const redirectToList = () => {
  const bucket = route.query.from || route.query.bucket || "current";
  const name =
    bucket === "future" ? "admin-projects-future" : "admin-projects-current";
  router.push({ name });
};
```

### 2.2. Cách Tạo Dự Án Từ Các Trang Khác

```javascript
// Từ Dự Án Hiện Tại
router.push({ name: "admin-projects-new", query: { bucket: "current" } });

// Từ Dự Án Tương Lai
router.push({ name: "admin-projects-new", query: { bucket: "future" } });
```

---

## 3️⃣ TRẠNG THÁI DỰ ÁN (5 TRẠNG THÁI)

### 3.1. Liệt Kê 5 Trạng Thái

| Mã Code       | Tên Tiếng Việt    | Tên Tiếng Anh | Badge Type  | Ý Nghĩa                    |
| ------------- | ----------------- | ------------- | ----------- | -------------------------- |
| `PENDING`     | 🟡 Đang duyệt     | Pending       | **warning** | Dự án vừa tạo, chờ duyệt   |
| `APPROVED`    | 🔵 Đã duyệt       | Approved      | **info**    | Dự án được phê duyệt       |
| `IN_PROGRESS` | 🟦 Đang thực hiện | In progress   | **info**    | Dự án đang được triển khai |
| `DONE`        | 🟢 Hoàn thành     | Done          | **success** | Dự án đã hoàn thành        |
| `CANCELLED`   | 🔴 Hủy bỏ         | Cancelled     | **danger**  | Dự án bị hủy               |

### 3.2. Hiển Thị Trạng Thái

```vue
<!-- ProjectsPage.vue -->
<el-tag :type="statusMeta(scope.row.status).type" effect="dark" size="small">
  {{ statusMeta(scope.row.status).label }}
</el-tag>
```

### 3.3. Logic statusMeta (Ánh xạ Trạng Thái)

```javascript
const statusLookup = computed(() => ({
  PENDING: { label: t("admin.projectStatus.PENDING"), type: "warning" }, // 🟡
  APPROVED: { label: t("admin.projectStatus.APPROVED"), type: "info" }, // 🔵
  IN_PROGRESS: { label: t("admin.projectStatus.IN_PROGRESS"), type: "info" }, // 🟦
  DONE: { label: t("admin.projectStatus.DONE"), type: "success" }, // 🟢
  CANCELLED: { label: t("admin.projectStatus.CANCELLED"), type: "danger" }, // 🔴
}));

const statusMeta = (status) =>
  statusLookup.value[status] ?? { label: status || "--", type: "info" };
```

---

## 4️⃣ CHUYỂN SANG TIẾNG VIỆT BẰNG I18N

### 4.1. File i18n được cập nhật

✅ **FE/src/locales/en/admin.json** - Tiếng Anh
✅ **FE/src/locales/vi/admin.json** - Tiếng Việt

### 4.2. Key i18n Mới

```json
{
  "admin": {
    "projectStatus": {
      "PENDING": "Đang duyệt",
      "APPROVED": "Đã duyệt",
      "IN_PROGRESS": "Đang thực hiện",
      "DONE": "Hoàn thành",
      "CANCELLED": "Hủy bỏ"
    }
  }
}
```

### 4.3. Sử Dụng i18n Trong Code

```javascript
// ProjectFormPage.vue
const statusOptions = computed(() => [
  { label: t("admin.projectStatus.PENDING"), value: "PENDING" },
  { label: t("admin.projectStatus.APPROVED"), value: "APPROVED" },
  { label: t("admin.projectStatus.IN_PROGRESS"), value: "IN_PROGRESS" },
  { label: t("admin.projectStatus.DONE"), value: "DONE" },
  { label: t("admin.projectStatus.CANCELLED"), value: "CANCELLED" },
]);

// ProjectsPage.vue
const statusLookup = computed(() => ({
  PENDING: { label: t("admin.projectStatus.PENDING"), type: "warning" },
  APPROVED: { label: t("admin.projectStatus.APPROVED"), type: "info" },
  IN_PROGRESS: { label: t("admin.projectStatus.IN_PROGRESS"), type: "info" },
  DONE: { label: t("admin.projectStatus.DONE"), type: "success" },
  CANCELLED: { label: t("admin.projectStatus.CANCELLED"), type: "danger" },
}));
```

---

## 5️⃣ FLOW CHUYỂN NGÔN NGỮ ĐỘNG

Khi người dùng chuyển ngôn ngữ (EN ↔ VI):

```
User Change Language (en ↔ vi)
    ↓
i18n.locale.value = 'vi' (hoặc 'en')
    ↓
t() function trả về value mới từ locales/vi/admin.json
    ↓
computed(() => statusOptions) được tính toán lại
    ↓
Vue re-render element-plus select options
    ↓
Dropdown show text tiếng Việt
```

---

## 6️⃣ QƯỚC TRÌNH TẠO DỰ ÁN HOÀN CHỈNH

### Bước 1: User Click Nút "Thêm Mới"

```vue
<el-button type="primary" @click="goCreate">
  {{ t('admin.actions.add') }}
</el-button>
```

### Bước 2: goCreate() Redirect

```javascript
const goCreate = () => {
  router.push({
    name: "admin-projects-new", // ← Route name
    query: { bucket: props.status }, // ← 'current' hoặc 'future'
  });
};
```

### Bước 3: Form Mounted

```javascript
onMounted(() => {
  // Nếu đó là tạo mới, restore form data từ sessionStorage
  const savedData = sessionStorage.getItem(FORM_STORAGE_KEY);
  if (savedData && isCreate.value) {
    restoreFormData();
  } else if (!isCreate.value) {
    loadDetail(); // ← Load dữ liệu dự án cũ để edit
  }

  if (!isView.value) {
    loadStaff(); // ← Load danh sách nhân viên
    loadClients(); // ← Load danh sách khách hàng
  }
});
```

### Bước 4: Fill Form & Thêm Thành Viên

User nhập:

- projectName (bắt buộc)
- clientId (bắt buộc)
- description
- budgetEstimated
- currencyUnit
- status (mặc định "PENDING")
- startDate
- endDate
- timelineEstimated

Thêm thành viên từ danh sách staff

### Bước 5: Submit Form

```javascript
submitForm() → validate() → {
  if (isCreate) {
    if (bucket === 'future') {
      apiProjects.createFuture(payload)
    } else {
      apiProjects.create(payload)
    }
  } else {
    apiProjects.update(props.id, payload)
  }
} → redirectToList()
```

---

## 7️⃣ THÔNG TIN THÊM

### 7.1. sessionStorage Dùng Để Gì?

Khi user nhấp vào "Xem Chi Tiết Nhân Viên" từ dự án, form data được lưu:

```javascript
const saveFormData = () => {
  const formData = {
    ...form,
    memberIds: selectedMembers.value.map((m) => m.userId),
    selectedMembers: selectedMembers.value,
  };
  sessionStorage.setItem(FORM_STORAGE_KEY, JSON.stringify(formData));
};
```

Sau khi quay lại, dữ liệu được restore:

```javascript
const restoreFormData = () => {
  const savedData = sessionStorage.getItem(FORM_STORAGE_KEY);
  if (savedData) {
    const data = JSON.parse(savedData);
    Object.assign(form, { ...data });
    selectedMembers.value = data.selectedMembers;
    sessionStorage.removeItem(FORM_STORAGE_KEY);
  }
};
```

### 7.2. Normalizeman Member

Hàm này chuẩn hóa dữ liệu user thành member object:

```javascript
const normalizeMember = (user) => {
  const userId = user?.userId || user?.id;
  const firstName = user?.firstName?.trim() || "";
  const lastName = user?.lastName?.trim() || "";
  const fullName = user?.fullName?.trim() || `${firstName} ${lastName}`.trim();

  return {
    userId,
    fullName: fullName || null,
    email: user?.email || "",
    phone: user?.phone || "",
    avatar: user?.avatar || "",
    itRole: user?.itRole || null,
  };
};
```

### 7.3. Form Validation

Validation rules được định nghĩa trong `FE/src/validations/projectRules.js`:

```javascript
const rules = computed(() => createProjectRules(t, form));
```

---

## 📊 BIỂU ĐỒ TRẠNG THÁI DỰ ÁN

```
┌─────────────────────────────────────────────────┐
│        TRẠNG THÁI DỰ ÁN (PROJECT STATUS)       │
└─────────────────────────────────────────────────┘

   Tạo Dự Án
      ↓
   PENDING (Đang duyệt) 🟡
      ↓
   APPROVED (Đã duyệt) 🔵
      ↓
   IN_PROGRESS (Đang thực hiện) 🟦
      ↓
   DONE (Hoàn thành) 🟢

   Hoặc:
   CANCELLED (Hủy bỏ) 🔴 ← Có thể hủy ở bất kỳ trạng thái
```

---

## ✅ NHỮNG THAY ĐỔI ĐÃ THỰC HIỆN

1. ✅ Thêm `projectStatus` object vào `locales/en/admin.json`
2. ✅ Thêm `projectStatus` object vào `locales/vi/admin.json`
3. ✅ Cập nhật `statusOptions` trong `ProjectFormPage.vue` để dùng `computed()` + i18n
4. ✅ Cập nhật `statusLookup` trong `ProjectsPage.vue` để dùng `computed()` + i18n
5. ✅ Đổi `statusMeta()` để dùng `statusLookup.value[status]` (vì nó giờ là computed)

---

## 🎯 TÓMT TẮT

- **Dự Án Hiện Tại (Current)**: Dự án đang/sắp thực hiện - API `create()`, route `admin-projects-current`
- **Dự Án Tương Lai (Future)**: Dự án lên kế hoạch tương lai - API `createFuture()`, route `admin-projects-future`
- **5 Trạng Thái**: PENDING → APPROVED → IN_PROGRESS → DONE (hoặc CANCELLED bất kỳ lúc)
- **i18n**: Dùng `t('admin.projectStatus.STATUS_CODE')` để lấy tên trạng thái theo ngôn ngữ
- **Ngôn Ngữ**: Tự động thay đổi khi user chuyển EN ↔ VI
