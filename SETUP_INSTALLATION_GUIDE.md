# Setup & Installation Guide

## 1. Backend Setup

### 1.1 Maven Dependencies

Apache POI đã được thêm vào `pom.xml`:

```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.4</version>
</dependency>
```

**Action**: Run Maven clean build

```bash
cd management_system
mvn clean install
```

### 1.2 Database Migration

File migration đã được tạo: `V3__Add_Revenue_Tracking.sql`

**Location**: `management_system/src/main/resources/db/migration/V3__Add_Revenue_Tracking.sql`

**What it does**:

- Adds indexes for revenue queries (status, end_date, budget)
- Creates view `v_completed_projects_revenue` for analytics
- Ensures required columns exist (budget_estimated, currency_unit)

**Action**: Flyway sẽ tự động chạy khi app start

---

## 2. Frontend Setup

### 2.1 NPM Dependencies

ECharts đã được thêm vào `package.json`:

```json
"echarts": "^5.4.3"
```

**Action**: Install dependencies

```bash
cd FE
npm install
```

### 2.2 Verify Installation

```bash
npm list echarts
# Output should show: echarts@5.4.3
```

---

## 3. Verify Installation Checklist

### Backend

- [ ] Maven build successful (no errors)
- [ ] POI dependencies resolved
- [ ] Application starts
- [ ] Database migration runs (check logs for Flyway)
- [ ] `/projects/revenue/*` endpoints available

### Frontend

- [ ] `npm install` completes successfully
- [ ] `npm run dev` starts without errors
- [ ] No console warnings about echarts
- [ ] Revenue page loads (navigate to /admin/revenue)
- [ ] Charts render properly
- [ ] Export button works

### Database

- [ ] Table `projects` has all required columns
- [ ] Indexes created (check with \d projects)
- [ ] View `v_completed_projects_revenue` created

---

## 4. Troubleshooting

### Issue: "Failed to resolve import echarts"

**Solution**:

```bash
cd FE
rm -rf node_modules package-lock.json
npm install
npm run dev
```

### Issue: POI dependencies not found in Maven

**Solution**:

```bash
cd management_system
mvn clean install -U
```

### Issue: Migration fails at startup

**Solution**:

1. Check `application.properties` has correct DB config
2. Verify `db/migration` folder exists
3. Check file names start with `V1`, `V2`, `V3`

### Issue: Charts not displaying

**Solution**:

1. Check browser console for errors
2. Verify `echarts` is in node_modules
3. Check `chartContainer` ref is valid
4. Ensure data is being fetched

---

## 5. Running the Application

### Development Mode

```bash
# Terminal 1: Backend
cd management_system
mvn spring-boot:run

# Terminal 2: Frontend
cd FE
npm run dev
```

### Access

- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- Revenue Page: http://localhost:5173/admin/revenue

---

## 6. File Locations Summary

### Backend Files Added/Modified

- ✅ `management_system/pom.xml` - Added POI dependency
- ✅ `management_system/src/main/resources/db/migration/V3__Add_Revenue_Tracking.sql` - NEW
- ✅ `management_system/src/main/java/com/management_system/controller/RevenueController.java` - NEW
- ✅ `management_system/src/main/java/com/management_system/dto/response/RevenueProjectResponse.java` - NEW

### Frontend Files Added/Modified

- ✅ `FE/package.json` - Added echarts dependency
- ✅ `FE/src/components/admin/pages/RevenueManagementPage.vue` - NEW
- ✅ `FE/src/services/apiRevenue.js` - NEW
- ✅ `FE/src/router/index.js` - Updated with revenue route
- ✅ `FE/src/pages/AdminDashboard.vue` - Added sidebar menu item

---

## 7. Post-Deployment

After deployment, verify:

1. Database migration completed successfully
2. No 500 errors on /projects/revenue/\* endpoints
3. Charts load and display correctly
4. Export feature creates valid Excel file
5. Tables populate with correct data

---

## Support

For any issues, check:

1. Backend logs for Flyway migration errors
2. Browser console for frontend errors
3. Network tab for API response errors
4. pom.xml for dependency versions
5. package.json for echarts installation
