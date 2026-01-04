# Quick Reference - User & Staff Management

## Language Switcher - New Location

**Header Component**: `FE/src/layouts/Header.vue`

```vue
<!-- Desktop Navigation -->
<div class="hidden md:flex items-center">
    <LanguageSwitcher />
</div>
```

**Usage**: Available in header for all users (not just admin)

---

## User Management Routes

### Frontend Routes

```javascript
// List users
/admin/users  → UsersPage.vue

// Create user
/admin/users/new  → UserFormPage.vue (mode: create)

// Edit user
/admin/users/:id/edit  → UserFormPage.vue (mode: edit)
```

### API Endpoints

```
GET    /api/v1/users                    // List
POST   /api/v1/users                    // Create
GET    /api/v1/users/:id                // Detail
PUT    /api/v1/users/:id                // Update
DELETE /api/v1/users/:id                // Delete
```

---

## Staff Management Routes

### Frontend Routes

```javascript
// List staff
/admin/staff  → StaffPage.vue

// Create staff
/admin/staff/new  → StaffFormPage.vue (mode: create)

// Edit staff
/admin/staff/:id/edit  → StaffFormPage.vue (mode: edit)
```

### API Endpoints

```
GET    /api/v1/users/staff              // List
POST   /api/v1/users/staff              // Create
GET    /api/v1/users/:id                // Detail (works for staff too)
PUT    /api/v1/users/staff/:id          // Update
DELETE /api/v1/users/staff/:id          // Delete

// Skills
POST   /api/v1/users/staff/:id/skills   // Add skill
GET    /api/v1/users/staff/:id/skills   // Get skills
DELETE /api/v1/users/staff/:id/skills/:skillId  // Remove skill
```

---

## Form Fields Reference

### User Form

```
- Avatar (File, optional, max 5MB)
- Email* (String, required, unique)
- Password* (String, required - create only)
- First Name* (String, required)
- Last Name* (String, required)
- Phone* (String, required)
- Role* (UUID, required, dropdown)
```

### Staff Form

```
- Avatar (File, optional, max 5MB)
- Email* (String, required)
- Password* (String, required - create only)
- First Name* (String, required)
- Last Name* (String, required)
- Phone* (String, required)
- IT Role (Enum, optional)
- CV (String, optional)
- Skills (Array, optional)
  - Skill ID* (UUID)
  - Level* (Enum: INTERN-EXPERT)
  - Years* (Number: 0-50)
```

---

## Common Code Patterns

### Fetch User List

```javascript
const users = await apiUsers.list({ page: 0, size: 10 });
// Returns: { data: { content: [...], totalElements, totalPages } }
```

### Create User

```javascript
const formData = new FormData();
formData.append("email", "user@example.com");
formData.append("password", "password123");
formData.append("firstName", "John");
formData.append("lastName", "Doe");
formData.append("phone", "123456789");
formData.append("roleId", roleUUID);
formData.append("avatar", file); // if present

const user = await apiUsers.create(formData);
```

### Fetch Staff

```javascript
const staff = await apiUsers.listStaff({ page: 0, size: 10 });
// Includes skills in response
```

### Add Skill to Staff

```javascript
const skill = {
  skillId: skillUUID,
  level: "SENIOR", // or JUNIOR, MIDDLE, etc.
  yearsOfExperience: 5,
};

await employeeSkillService.addSkillToEmployee(staffId, skill);
```

---

## Responsive Breakpoints

### CSS Media Query

```css
@media (max-width: 768px) {
  /* Mobile styles */
}
```

### Grid Behavior

**Desktop (>768px)**:

```css
grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
/* 2-3 columns depending on screen */
```

**Mobile (<768px)**:

```css
grid-template-columns: 1fr;
/* 1 column */
```

---

## Validation Rules

### Email

- Must be valid email format
- Must be unique in system
- Case-insensitive comparison

### Password

- Minimum 6 characters (check backend rules)
- Required on create only
- Encrypted with BCrypt

### Phone

- Any format accepted
- Displayed as-is

### Avatar

- Must be image file
- Max 5MB
- Formats: JPG, PNG, GIF, WebP

### Skills

- Level must be from enum
- Years must be 0-50
- Skill ID must exist

---

## IT Role Enums

```
BACKEND_DEVELOPER
FRONTEND_DEVELOPER
FULLSTACK_DEVELOPER
MOBILE_DEVELOPER
DEVOPS_ENGINEER
QA_MANUAL
QA_AUTOMATION
BUSINESS_ANALYST
PRODUCT_OWNER
PROJECT_MANAGER
SCRUM_MASTER
TECH_LEAD
SOLUTION_ARCHITECT
UI_UX_DESIGNER
DATA_ENGINEER
DATA_ANALYST
SECURITY_ENGINEER
DATABASE_ADMINISTRATOR
SYSTEM_ADMINISTRATOR
```

---

## Proficiency Level Enums

```
INTERN              → 0-1 years
FRESHER             → 0-1 years (recent graduate)
JUNIOR              → 1-2 years
MIDDLE              → 2-4 years
SENIOR              → 4-7 years
LEAD                → 7-10 years
PRINCIPAL           → 10+ years
ARCHITECT           → 10+ years (system design)
EXPERT              → 15+ years
```

---

## Error Handling

### Common Errors

```
400 Bad Request      → Validation failed
401 Unauthorized     → Not authenticated
403 Forbidden        → Insufficient permissions
404 Not Found        → Resource not found
409 Conflict         → Email already exists
500 Server Error     → Backend error
```

### Display Error

```javascript
try {
  await apiUsers.create(formData);
} catch (error) {
  ElMessage.error(error.response?.data?.message || "Failed to create");
}
```

---

## File Locations

### Frontend Components

- **Pages**: `FE/src/components/admin/pages/`
  - UsersPage.vue
  - UserFormPage.vue
  - StaffPage.vue
  - StaffFormPage.vue
- **Common**: `FE/src/components/common/`
  - LanguageSwitcher.vue
- **Layout**: `FE/src/layouts/`
  - Header.vue
  - AdminSidebar.vue

### Frontend Services

- **API**: `FE/src/services/`
  - apiUsers.js
  - apiRoles.js
  - apiSkills.js

### Frontend Stores

- **Auth**: `FE/src/stores/auth/`
  - useAuthStore.js

### Backend Controller

```
management_system/src/main/java/
  com/management_system/controller/
    UserController.java
```

---

## Authentication

### Header Required

```
Authorization: Bearer <access_token>
```

### Roles Required

- `ROLE_ADMIN` - Full access
- `ROLE_PM` - Project manager access
- `ROLE_STAFF` - Staff access
- `ROLE_USER` - User/customer access

### Check Permission

```javascript
const hasAdminAccess = auth.role === "ROLE_ADMIN";
```

---

## Pagination

### Request

```javascript
{
  page: 0,      // 0-indexed
  size: 10      // items per page
}
```

### Response

```json
{
  "data": {
    "content": [...],
    "totalElements": 100,
    "totalPages": 10,
    "currentPage": 0,
    "pageSize": 10
  }
}
```

### Frontend Pagination

```vue
<el-pagination
  :current-page="page"
  :page-size="size"
  :total="total"
  @current-change="handlePageChange"
/>
```

---

## Image Upload

### Client Validation

```javascript
// Type check
if (!file.type.startsWith("image/")) {
  ElMessage.error("Must be image");
}

// Size check (5MB)
if (file.size > 5 * 1024 * 1024) {
  ElMessage.error("Max 5MB");
}
```

### Submit with FormData

```javascript
const formData = new FormData();
formData.append("avatar", file);
formData.append("...other fields...");
```

### Server Processing

- Uploaded to Cloudinary
- Returns public URL
- Stored in database

---

## i18n Translation Keys

### User Management

```
admin.entities.user
admin.actions.create
admin.actions.edit
admin.actions.delete
admin.actions.update
admin.form.email
admin.form.firstName
admin.form.lastName
admin.form.phone
admin.form.memberRole
admin.form.selectRole
admin.form.avatar
admin.table.name
admin.empty
```

### Staff Management

```
admin.menu.staff
admin.form.itRole
admin.form.skills
admin.form.cv
```

---

## Useful Tips

### 1. Reset Form

```javascript
formRef.value?.resetFields();
```

### 2. Clear Validation

```javascript
formRef.value?.clearValidate();
```

### 3. Get Current Language

```javascript
const { locale } = useI18n();
console.log(locale.value); // 'en', 'vi', 'ja'
```

### 4. Use Router

```javascript
router.push({ name: "admin-users" });
router.push({
  name: "admin-users-edit",
  params: { id: userId },
});
```

### 5. Show Loading

```javascript
isLoading.value = true;
// ... do work ...
isLoading.value = false;
```

---

## Performance Tips

1. **Pagination**: Don't load all users at once
2. **Search**: Debounce search input (300ms)
3. **Images**: Compress before upload
4. **Caching**: Browser caches avatars
5. **Lazy Load**: Load skills on demand

---

## Testing Checklist

- [ ] Form validates required fields
- [ ] Email uniqueness is checked
- [ ] Avatar upload shows preview
- [ ] Skills can be added/removed
- [ ] Pagination works correctly
- [ ] Mobile layout is responsive
- [ ] Language switcher changes UI
- [ ] Error messages display properly
- [ ] Loading states show
- [ ] API calls include auth header

---

## Related Documentation

📄 **USER_STAFF_MANAGEMENT_GUIDE.md** - Comprehensive guide
📄 **IMPLEMENTATION_SUMMARY.md** - Summary of changes
📋 This file - Quick reference

---

## Support

For issues or questions:

1. Check the comprehensive guide
2. Review API documentation
3. Check error messages
4. Review browser console
5. Check server logs

---

Last Updated: January 4, 2026
