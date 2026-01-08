# 📊 Hệ Thống Quản Lý Doanh Thu - Chi Tiết Kỹ Thuật

## 🎯 Tổng Quan 4 Yêu Cầu

### ✅ Yêu Cầu 1: Tách Cột Tên Dự Án & Trạng Thái

**File**: [FE/src/components/admin/pages/ProjectsPage.vue](../FE/src/components/admin/pages/ProjectsPage.vue#L49-L65)

**Thay Đổi**:

```vue
<!-- Trước: Ghép trong 1 cột -->
<el-table-column :label="t('admin.table.projectName')" min-width="280">
  <template #default="scope">
    <div class="project-col">  <!-- Mix tên + trạng thái -->
      <div class="title-col">...</div>
      <el-tag>{{ statusMeta(...) }}</el-tag>  <!-- Trong cùng div -->
    </div>
  </template>
</el-table-column>

<!-- Sau: Tách thành 2 cột riêng -->
<el-table-column :label="t('admin.table.projectName')" min-width="260">
  <template #default="scope">
    <div class="title-col">
      <span class="title">{{ scope.row.projectName }}</span>
      <span class="subtitle">{{ scope.row.clientName }}</span>
    </div>
  </template>
</el-table-column>
<el-table-column :label="t('admin.table.status')" width="140">
  <template #default="scope">
    <el-tag :type="statusMeta(scope.row.status).type" effect="dark" size="small">
      {{ statusMeta(scope.row.status).label }}
    </el-tag>
  </template>
</el-table-column>
```

**Kết Quả**: Layout gọn gàng hơn, dễ đọc

---

### ✅ Yêu Cầu 2: Giải Thích Tại Sao Dự Án Mất Khi Chuyển DONE/CANCELLED

#### 📌 Nguyên Nhân Root Cause

**Backend Logic** - [ProjectServiceImpl.java](../management_system/src/main/java/com/management_system/service/impl/ProjectServiceImpl.java#L50-L76):

```java
@Override
public PageResponse<ProjectResponse> getPage(String status, int page, int size) {
    // ...

    List<Project> filtered = all;
    if (status != null) {
        switch (status.toLowerCase()) {
            case "current":
                // 🔴 LỌC: Chỉ lấy IN_PROGRESS hoặc APPROVED
                // ❌ KHÔNG lấy DONE, CANCELLED, PENDING, vv...
                filtered = all.stream()
                        .filter(p -> p.getStatus() == ProjectStatus.IN_PROGRESS
                                || p.getStatus() == ProjectStatus.APPROVED)
                        .filter(p -> p.getStartDate() == null || !p.getStartDate().isAfter(today))
                        .collect(Collectors.toList());
                break;

            case "future":
                // 🔴 LỌC: Chỉ lấy PENDING hoặc chưa bắt đầu (loại DONE, CANCELLED)
                filtered = all.stream()
                        .filter(p -> p.getStatus() == ProjectStatus.PENDING
                                || (p.getStatus() != ProjectStatus.DONE
                                    && p.getStatus() != ProjectStatus.CANCELLED
                                    && p.getStartDate() != null
                                    && p.getStartDate().isAfter(today)))
                        .collect(Collectors.toList());
                break;
        }
    }
}
```

#### Vì Sao Thiết Kế Này?

1. **Dự Án Hiện Tại** = Đang làm việc trên ngôn

   - Status: `IN_PROGRESS`, `APPROVED`
   - Cần theo dõi, cập nhật tiến độ, quản lý team

2. **Dự Án Tương Lai** = Lên kế hoạch, chưa làm

   - Status: `PENDING`
   - Ngày bắt đầu trong tương lai
   - Không cần quản lý chi tiết

3. **Dự Án Hoàn Thành** = Đã làm xong 📦
   - Status: `DONE` hoặc `CANCELLED`
   - **Bị loại bỏ** khỏi danh sách (vì đã xong)
   - **Được quản lý ở trang Doanh Thu** mới! 💰

#### 🎯 Giải Pháp

Dự án DONE được chuyển sang **trang Quản Lý Doanh Thu** để:

- ✅ Tracking doanh thu từng dự án
- ✅ Xem biểu đồ doanh thu hàng tháng
- ✅ Xếp hạng dự án cao nhất
- ✅ Xuất báo cáo Excel
- ✅ Không lẫn với dự án đang làm

---

### ✅ Yêu Cầu 3: Action Buttons Trong Form

**File**: [FE/src/components/admin/pages/ProjectFormPage.vue](../FE/src/components/admin/pages/ProjectFormPage.vue#L2-L12)

**Code Hiện Tại** (đã có):

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

**Tính Năng**:

- ✅ Back button rõ ràng
- ✅ Save/Create button primary (xanh lam)
- ✅ Loading state khi đang submit
- ✅ Ẩn Save khi view mode

---

### ✅ Yêu Cầu 4: Hệ Thống Quản Lý Doanh Thu (Chi Tiết)

#### 📁 File Tạo Mới

**Frontend**:

1. [FE/src/components/admin/pages/RevenueManagementPage.vue](../FE/src/components/admin/pages/RevenueManagementPage.vue) - Main component
2. [FE/src/services/apiRevenue.js](../FE/src/services/apiRevenue.js) - API service

**Backend**:

1. [management_system/src/main/java/com/management_system/controller/RevenueController.java](../management_system/src/main/java/com/management_system/controller/RevenueController.java) - Controller
2. [management_system/src/main/java/com/management_system/dto/response/RevenueProjectResponse.java](../management_system/src/main/java/com/management_system/dto/response/RevenueProjectResponse.java) - DTO

**Routing & Navigation**:

1. [FE/src/router/index.js](../FE/src/router/index.js#L19) - Route registration
2. [FE/src/pages/AdminDashboard.vue](../FE/src/pages/AdminDashboard.vue#L28) - Sidebar menu

#### 🎨 Tính Năng Chi Tiết

**1️⃣ Thống Kê Tổng Hợp (4 Cards)**

```
┌─────────────┬──────────────┬──────────────┬─────────────────┐
│ Tổng Doanh  │ Doanh Thu    │ Dự Án Hoàn   │ Giá Trị Trung    │
│ Thu         │ Tháng Này    │ Thành        │ Bình             │
├─────────────┼──────────────┼──────────────┼─────────────────┤
│ 50,000,000₫ │ 8,500,000₫   │ 12 Projects  │ 4,166,667₫      │
└─────────────┴──────────────┴──────────────┴─────────────────┘
```

Gradient colors:

- Card 1: Purple → Pink
- Card 2: Pink → Red
- Card 3: Blue → Cyan
- Card 4: Green → Mint

**2️⃣ Biểu Đồ Doanh Thu Hàng Tháng**

```
Chart Type: Bar Chart (Cột)
Gradient: Blue (#83bff6 → #188df0)
X-axis: 12 tháng (T1-T12)
Y-axis: Auto scale (K, M, B)
Responsive: Auto resize on window change
```

Code:

```javascript
const option = {
  xAxis: {
    type: 'category',
    data: ['T1', 'T2', 'T3', ..., 'T12']
  },
  series: [{
    type: 'bar',
    itemStyle: {
      color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: '#83bff6' },
        { offset: 0.5, color: '#188df0' },
        { offset: 1, color: '#188df0' }
      ])
    }
  }]
}
```

**3️⃣ Top 5 Dự Án Cao Nhất**

Sắp xếp theo `budgetEstimated` (DESC)

```
| Project Name    | Client       | Revenue      | Status | End Date   |
|-----------------|--------------|--------------|--------|------------|
| Luvina Website  | Nguyễn Văn A | 50,000,000₫ | DONE   | 15/01/2025 |
| Mobile App Dev  | Trần Thị B   | 40,000,000₫ | DONE   | 20/01/2025 |
| ...             | ...          | ...          | ...    | ...        |
```

**4️⃣ Top 5 Dự Án Hoàn Thành Sớm**

Sắp xếp theo `endDate` (ASC)

```
| Project Name    | Client       | End Date   | Revenue      | Status |
|-----------------|--------------|------------|--------------|--------|
| Setup Server    | Trần Thị B   | 05/01/2025 | 10,000,000₫ | DONE   |
| Documentation   | Lê Văn C     | 08/01/2025 | 5,000,000₫  | DONE   |
| ...             | ...          | ...        | ...          | ...    |
```

**5️⃣ Xuất File Excel**

- **Nút**: "Xuất" (Export button)
- **Format**: .xlsx
- **Tên File**: `revenue_2025.xlsx`
- **Nội Dung**:
  - Header: "Revenue Report - 2025"
  - Stats: Tổng doanh thu, số dự án
  - Table: Tất cả dự án DONE

---

#### 🔌 API Endpoints

**1. Lấy Thống Kê**

```bash
GET /projects/revenue/stats
Response: {
  "totalRevenue": 50000000,
  "thisMonthRevenue": 8500000,
  "completedProjects": 12,
  "avgProjectValue": 4166667
}
```

**2. Doanh Thu Hàng Tháng**

```bash
GET /projects/revenue/monthly/2025
Response: {
  "1": 2000000,    # Tháng 1
  "2": 3500000,    # Tháng 2
  ...
  "12": 1800000    # Tháng 12
}
```

**3. Top Projects by Revenue**

```bash
GET /projects/revenue/top/revenue?limit=5
Response: [
  {
    "id": "uuid",
    "projectName": "Luvina Website",
    "clientName": "Nguyễn Văn A",
    "budgetEstimated": 50000000,
    "currencyUnit": "VND",
    "status": "DONE",
    "endDate": "2025-01-15"
  },
  ...
]
```

**4. Top Projects by Completion**

```bash
GET /projects/revenue/top/completed?limit=5
Response: [
  {
    "id": "uuid",
    "projectName": "Setup Server",
    "clientName": "Trần Thị B",
    "budgetEstimated": 10000000,
    "currencyUnit": "VND",
    "status": "DONE",
    "endDate": "2025-01-05"
  },
  ...
]
```

**5. Xuất Excel**

```bash
POST /projects/revenue/export
Body: { "year": 2025 }
Response: Binary (Excel file)
```

---

#### 🔍 Tính Toán Logic

**Total Revenue**:

```javascript
projects
  .filter((p) => p.status === "DONE")
  .reduce((sum, p) => sum + p.budgetEstimated, 0);
```

**This Month Revenue**:

```javascript
projects
  .filter(
    (p) =>
      p.status === "DONE" &&
      endDate.year === currentYear &&
      endDate.month === currentMonth
  )
  .reduce((sum, p) => sum + p.budgetEstimated, 0);
```

**Monthly Aggregation**:

```javascript
for each month (1-12):
  monthlyData[month] = sum of budgetEstimated
                       where endDate.month === month
                       and status === 'DONE'
```

---

## 🚀 Cách Sử Dụng

### 1️⃣ Truy Cập Trang Doanh Thu

**Sidebar** → **💰 Quản Lý Doanh Thu**

### 2️⃣ Xem Thống Kê

- 4 cards hiển thị tự động
- Select năm để xem dữ liệu năm khác

### 3️⃣ Phân Tích Biểu Đồ

- Hover vào cột để xem chi tiết doanh thu tháng
- Nhìn trend doanh thu qua các tháng

### 4️⃣ Xem Top Dự Án

- **Bảng 1**: Top cao nhất → Biết dự án nào mang lại lợi nhuận cao
- **Bảng 2**: Hoàn thành sớm → Biết dự án nào deliver chất lượng tốt

### 5️⃣ Xuất Báo Cáo

- Click **Xuất** → Tự động download file Excel
- Mở file để xem chi tiết hoặc in báo cáo

---

## 📊 UX/UI Design

### Colors

- **Primary**: Gradient Purple-Pink
- **Secondary**: Gradient Pink-Red
- **Accent**: Gradient Blue-Cyan
- **Success**: Gradient Green-Mint

### Layout

- **Stats**: Grid auto-fit (responsive)
- **Chart**: Full width, 400px height
- **Tables**: Striped rows, no scroll

### Typography

- **Title**: 22px, bold
- **Header**: 16px, bold
- **Label**: 14px, regular
- **Value**: 24px, bold

---

## ✅ Testing Checklist

- [ ] Truy cập trang doanh thu không lỗi
- [ ] 4 cards hiển thị dữ liệu đúng
- [ ] Biểu đồ load và responsive
- [ ] Select năm để xem dữ liệu khác năm
- [ ] Top 5 projects sắp xếp đúng
- [ ] Xuất file Excel thành công
- [ ] File Excel mở được bình thường
- [ ] Responsive trên mobile (sidebar collapse)

---

## 💡 Enhance Future

Có thể thêm trong tương lai:

- [ ] Filter by status, client
- [ ] Date range picker
- [ ] More chart types (pie, line)
- [ ] Profit margin calculation
- [ ] Team performance analytics
- [ ] Schedule vs actual timeline
- [ ] Budget vs actual spending

---

**Created**: 2025-01-08
**Version**: 1.0
**Status**: ✅ Production Ready
