# Quản Lý Doanh Thu - Revenue Management

Đã tạo hoàn chỉnh hệ thống quản lý doanh thu với các tính năng sau:

## Yêu Cầu 2: Giải Thích Tại Sao Dự Án Mất

### Nguyên Nhân

Khi bạn chuyển dự án sang trạng thái `DONE` (Hoàn thành) hoặc `CANCELLED` (Hủy bỏ), dự án sẽ **không xuất hiện** trong cả 2 danh sách (dự án hiện tại & tương lai) vì:

**Backend Logic** (`ProjectServiceImpl.java` - dòng 62-66):

```java
case "current":
    filtered = all.stream()
            .filter(p -> p.getStatus() == ProjectStatus.IN_PROGRESS
                    || p.getStatus() == ProjectStatus.APPROVED)
            .filter(p -> p.getStartDate() == null || !p.getStartDate().isAfter(today))
            .collect(Collectors.toList());
```

**Dự án hiện tại** chỉ lấy dự án với status:

- `IN_PROGRESS` (Đang thực hiện)
- `APPROVED` (Đã phê duyệt)

**Không lấy dự án DONE/CANCELLED** vì chúng đã kết thúc!

Tương tự, **dự án tương lai** cũng loại bỏ DONE/CANCELLED:

```java
case "future":
    filtered = all.stream()
            .filter(p -> p.getStatus() == ProjectStatus.PENDING
                    || (p.getStatus() != ProjectStatus.DONE && p.getStatus() != ProjectStatus.CANCELLED
                            && p.getStartDate() != null && p.getStartDate().isAfter(today)))
            .collect(Collectors.toList());
```

**Giải pháp**: Dự án DONE/CANCELLED được quản lý ở trang **"Quản Lý Doanh Thu"** mới (yêu cầu 4) để tracking doanh thu và hiệu suất.

---

## Yêu Cầu 3: Action Buttons Trong Form

Form thêm/sửa dự án **đã có** action buttons rõ ràng ở **phía trên** của form trong component `ProjectFormPage.vue`:

```vue
<template #actions>
  <el-space>
    <el-button @click="goBack">{{ t("admin.actions.back") }}</el-button>
    <el-button
      v-if="!isView"
      type="primary"
      :loading="submitting"
      @click="submitForm"
    >
      {{ isCreate ? t("admin.actions.create") : t("admin.actions.save") }}
    </el-button>
  </el-space>
</template>
```

✅ **Đã có**: Back button + Save/Create button rõ ràng

---

## Yêu Cầu 4: Hệ Thống Quản Lý Doanh Thu ✅

### Tính Năng Chính

1. **Biểu Đồ Doanh Thu Hàng Tháng** - Visualize revenue trend
2. **Thống Kê Tổng Hợp** (4 cards):

   - Tổng doanh thu
   - Doanh thu tháng này
   - Số dự án hoàn thành
   - Giá trị trung bình mỗi dự án

3. **Top 5 Dự Án Cao Nhất** - Sắp xếp theo doanh thu
4. **Top 5 Dự Án Hoàn Thành Sớm** - Sắp xếp theo ngày kết thúc
5. **Xuất File Excel** - Download báo cáo doanh thu

### Đường Dẫn

- **FE**: `FE/src/components/admin/pages/RevenueManagementPage.vue`
- **BE**: `management_system/src/main/java/com/management_system/controller/RevenueController.java`
- **BE DTO**: `management_system/src/main/java/com/management_system/dto/response/RevenueProjectResponse.java`
- **FE Service**: `FE/src/services/apiRevenue.js`
- **Router**: Thêm route `admin-revenue` vào `FE/src/router/index.js`
- **Sidebar**: Thêm menu item "💰 Quản Lý Doanh Thu" vào `FE/src/pages/AdminDashboard.vue`

### API Endpoints

```
GET  /projects/revenue/stats                - Lấy thống kê tổng hợp
GET  /projects/revenue/monthly/{year}       - Lấy doanh thu hàng tháng
GET  /projects/revenue/top/revenue          - Top 5 dự án cao nhất
GET  /projects/revenue/top/completed        - Top 5 dự án hoàn thành sớm
POST /projects/revenue/export               - Xuất file Excel
```

### Biểu Đồ

- **Công nghệ**: ECharts (chart library)
- **Loại**: Bar chart (cột)
- **Gradient**: Màu xanh dương gradient chuyên nghiệp
- **Responsive**: Tự động resize khi window thay đổi

### File Export

- **Format**: Excel (.xlsx)
- **Nội dung**: Danh sách tất cả dự án + tổng doanh thu
- **Header**: Bold, tô màu
- **Columns**: Project Name, Client, Revenue, Currency, Status, End Date

---

## Yêu Cầu 1: Tách 2 Cột ✅

Đã sửa `ProjectsPage.vue`:

- **Cột 1**: Tên dự án + Tên khách hàng (260px)
- **Cột 2**: Trạng thái (140px)
- Các cột khác: Ngày bắt đầu, Ngày kết thúc, Ngân sách, Actions

Layout gọn gàng, dễ đọc!

---

## Tóm Tắt Thay Đổi

| Yêu Cầu | Tệp Thay Đổi                                         | Trạng Thái                     |
| ------- | ---------------------------------------------------- | ------------------------------ |
| 1       | ProjectsPage.vue                                     | ✅ Xong - Tách 2 cột rõ ràng   |
| 2       | Giải thích logic                                     | ✅ Xong - Dự án DONE ở Revenue |
| 3       | ProjectFormPage.vue                                  | ✅ Xong - Có buttons rõ ràng   |
| 4       | RevenueManagementPage.vue<br/>RevenueController.java | ✅ Xong - Biểu đồ, export, top |

All set! 🚀
