# User & Staff Management Guide

## Overview

This document provides a comprehensive guide for the User and Staff Management system in the Luvina Management Platform. The system supports complete CRUD operations for users, customers, and staff with responsive UI and advanced feature support.

---

## Table of Contents

1. [Frontend Implementation](#frontend-implementation)
2. [Backend API Endpoints](#backend-api-endpoints)
3. [Features](#features)
4. [User Management](#user-management)
5. [Staff Management](#staff-management)
6. [Mobile Responsiveness](#mobile-responsiveness)
7. [Language Switcher](#language-switcher)

---

## Frontend Implementation

### Improvements Made to Frontend

#### 1. **Language Switcher Relocation**

- **Previous**: Located inside AdminSidebar dropdown menu
- **Current**: Moved to main Header navigation for better visibility
- **Benefit**: Accessible to all users without needing to access admin dropdown
- **Files Modified**:
  - `FE/src/layouts/Header.vue` - Added language switcher to desktop navigation
  - `FE/src/components/admin/AdminSidebar.vue` - Removed from admin sidebar

#### 2. **User Management Pages**

##### UsersPage.vue (List View)

- **Location**: `FE/src/components/admin/pages/UsersPage.vue`
- **Features**:
  - Responsive grid layout with auto-fit columns
  - Real-time search functionality
  - Role-based filtering
  - Statistics dashboard showing:
    - Total users
    - Admin count
    - Staff count
    - User count
  - Avatar display with fallback
  - Role status indicators with color coding
  - Pagination support
  - Quick actions (Edit, Delete)

**Responsive Breakpoints**:

- Desktop (>768px): Multi-column layout, full features
- Mobile (≤768px): Single column, optimized touch targets, scrollable tables

##### UserFormPage.vue (Create/Edit)

- **Location**: `FE/src/components/admin/pages/UserFormPage.vue`
- **Features**:
  - Auto-fit responsive grid (2-3 columns on desktop)
  - Avatar upload with preview and validation
  - File size validation (max 5MB)
  - Drag-and-drop support
  - Email field (read-only in edit mode)
  - Password field (create mode only)
  - Role selection dropdown
  - Form validation with error messages
  - Progress indicator during upload

**Form Fields**:

```
- Avatar (image upload with preview)
- Email* (required, unique, disabled on edit)
- Password* (required on create only)
- First Name* (required)
- Last Name* (required)
- Phone* (required)
- Role* (required, dropdown selection)
```

#### 3. **Staff Management Pages**

##### StaffPage.vue (List View)

- **Location**: `FE/src/components/admin/pages/StaffPage.vue`
- **Features**:
  - Responsive grid layout
  - Real-time search functionality
  - Statistics dashboard
  - Avatar display
  - IT Role display with formatting
  - Expandable skills preview (shows 2 skills + count if more)
  - Pagination support
  - Quick actions (Edit, Delete)

**Responsive Design**:

- Compresses skills display to 2 items on small screens
- Adaptive table padding and font sizes
- Touch-friendly button spacing

##### StaffFormPage.vue (Create/Edit)

- **Location**: `FE/src/components/admin/pages/StaffFormPage.vue`
- **Features**:
  - 2-3 column responsive grid
  - Skills management section:
    - Select skill from available skills list
    - Choose proficiency level (Intern to Expert)
    - Input years of experience
    - Add button with validation
    - Scrollable skills list with remove buttons
  - IT Role selection with 19+ options:
    - Frontend/Backend/Fullstack Developer
    - Mobile Developer
    - DevOps Engineer
    - QA (Manual & Automation)
    - Business Analyst
    - Product Owner, Project Manager, Scrum Master
    - Tech Lead, Solution Architect
    - UI/UX Designer
    - Data Engineer/Analyst
    - Security Engineer
    - Database/System Administrator
  - CV field for document link/URL
  - Avatar upload with preview (120px × 120px on desktop, responsive on mobile)
  - Professional layout with separated sections

**Form Fields**:

```
- Avatar (image upload with preview)
- Email* (required)
- Password* (required on create only)
- First Name* (required)
- Last Name* (required)
- Phone* (required)
- IT Role (dropdown with 19 options)
- Skills (dynamic add/remove)
  - Skill Selection*
  - Proficiency Level* (Intern-Expert)
  - Years of Experience (0-50)
- CV (text field for URL/path)
```

---

## Backend API Endpoints

### Base URL

```
http://localhost:8080/api/v1/users
```

### Authentication

All endpoints require `Authorization: Bearer <token>` header with ADMIN role

### General User Endpoints

#### 1. List All Users (Paginated)

```
GET /api/v1/users
Parameters:
  - page: int (default: 0, 0-indexed)
  - size: int (default: 10)

Response: 200 OK
{
  "data": {
    "content": [
      {
        "id": "uuid",
        "email": "user@example.com",
        "firstName": "John",
        "lastName": "Doe",
        "phone": "123456789",
        "roleId": "role-uuid",
        "avatar": "url-to-avatar",
        "active": true,
        "deleteFlag": false,
        "createdAt": "2024-01-01T00:00:00Z"
      }
    ],
    "totalElements": 100,
    "totalPages": 10,
    "currentPage": 0,
    "pageSize": 10
  }
}
```

#### 2. Get User Details

```
GET /api/v1/users/{id}
Parameters:
  - id: UUID (user ID)

Response: 200 OK
{
  "data": {
    "id": "uuid",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "123456789",
    "roleId": "role-uuid",
    "avatar": "url-to-avatar",
    "active": true,
    "dateOfBirth": "1990-01-01",
    "gender": "MALE"
  }
}
```

#### 3. Create User

```
POST /api/v1/users
Content-Type: multipart/form-data

Parameters:
  - email: string (required, unique)
  - password: string (required)
  - firstName: string (required)
  - lastName: string (required)
  - phone: string (required)
  - roleId: UUID (required)
  - avatar: File (optional, max 5MB)

Response: 201 Created
{
  "data": {
    "id": "new-user-uuid",
    "email": "user@example.com",
    ...
  }
}
```

#### 4. Update User

```
PUT /api/v1/users/{id}
Content-Type: multipart/form-data

Parameters:
  - id: UUID (path)
  - email: string
  - firstName: string
  - lastName: string
  - phone: string
  - roleId: UUID
  - avatar: File (optional)

Response: 200 OK
{
  "data": {
    "id": "uuid",
    ...
  }
}
```

#### 5. Delete User

```
DELETE /api/v1/users/{id}
Parameters:
  - id: UUID (path)

Response: 204 No Content
```

### Customer Endpoints (ROLE_USER)

#### List Customers

```
GET /api/v1/users/customers
Parameters:
  - page: int (default: 0)
  - size: int (default: 10)

Response: 200 OK (same structure as user list)
```

#### Create Customer

```
POST /api/v1/users/customers
Content-Type: multipart/form-data

Parameters:
  - email: string
  - password: string
  - firstName: string
  - lastName: string
  - phone: string
  - avatar: File (optional)

Response: 201 Created
```

#### Update Customer

```
PUT /api/v1/users/customers/{id}
Content-Type: multipart/form-data

Parameters:
  - id: UUID (path)
  - email: string
  - firstName: string
  - lastName: string
  - phone: string
  - avatar: File (optional)

Response: 200 OK
```

#### Delete Customer

```
DELETE /api/v1/users/customers/{id}

Response: 204 No Content
```

### Staff Endpoints (ROLE_STAFF)

#### List Staff

```
GET /api/v1/users/staff
Parameters:
  - page: int (default: 0)
  - size: int (default: 10)

Response: 200 OK
{
  "data": {
    "content": [
      {
        "id": "uuid",
        "email": "staff@example.com",
        "firstName": "Jane",
        "lastName": "Smith",
        "phone": "987654321",
        "itRole": "BACKEND_DEVELOPER",
        "cv": "url-to-cv",
        "avatar": "url-to-avatar",
        "skills": [
          {
            "id": "skill-uuid",
            "skillName": "Java",
            "level": "SENIOR",
            "yearsOfExperience": 5
          },
          {
            "id": "skill-uuid",
            "skillName": "Spring Boot",
            "level": "SENIOR",
            "yearsOfExperience": 4
          }
        ]
      }
    ],
    "totalElements": 50,
    "totalPages": 5,
    "currentPage": 0,
    "pageSize": 10
  }
}
```

#### Get Staff Details

```
GET /api/v1/users/{id}
(Returns staff user with skills information)

Response: 200 OK
```

#### Create Staff

```
POST /api/v1/users/staff
Content-Type: multipart/form-data

Parameters:
  - email: string (required)
  - password: string (required)
  - firstName: string (required)
  - lastName: string (required)
  - phone: string (required)
  - itRole: string (optional, IT role enum)
  - cv: string (optional, URL or path)
  - skills: JSON (optional, array of skill objects)
  - avatar: File (optional)

Skills JSON Format:
[
  {
    "skillId": "skill-uuid",
    "level": "SENIOR",
    "yearsOfExperience": 5
  },
  {
    "skillId": "another-skill-uuid",
    "level": "JUNIOR",
    "yearsOfExperience": 2
  }
]

Response: 201 Created
```

#### Update Staff

```
PUT /api/v1/users/staff/{id}
Content-Type: multipart/form-data

Parameters:
  - id: UUID (path)
  - email: string
  - firstName: string
  - lastName: string
  - phone: string
  - itRole: string (optional)
  - cv: string (optional)
  - skills: JSON (optional)
  - avatar: File (optional)

Response: 200 OK
```

#### Delete Staff

```
DELETE /api/v1/users/staff/{id}

Response: 204 No Content
```

### Skills Management Endpoints

#### Add Skill to Staff

```
POST /api/v1/users/staff/{staffId}/skills
Content-Type: application/json

Parameters:
  - staffId: UUID (path)
  - Body:
    {
      "skillId": "skill-uuid",
      "level": "SENIOR",
      "yearsOfExperience": 5
    }

Response: 201 Created
```

#### Get Staff Skills

```
GET /api/v1/users/staff/{staffId}/skills

Response: 200 OK
[
  {
    "id": "employee-skill-uuid",
    "skillId": "skill-uuid",
    "skillName": "Java",
    "level": "SENIOR",
    "yearsOfExperience": 5
  }
]
```

#### Remove Skill from Staff

```
DELETE /api/v1/users/staff/{staffId}/skills/{skillId}

Response: 204 No Content
```

---

## Features

### 1. **User Management**

- Create, read, update, and delete users
- Role-based assignment (Admin, PM, Staff, User)
- Avatar upload with validation
- Batch listing with pagination
- Search and filter capabilities
- Status tracking (active/inactive)

### 2. **Staff Management**

- All user features plus:
- IT role assignment (19+ positions)
- Skills tracking with proficiency levels
- Years of experience per skill
- CV document management
- Enhanced profile information

### 3. **Customer Management**

- Simplified user management for customers
- No IT role or skills required
- Basic profile information
- Quick customer creation and updates

### 4. **Image Handling**

- Cloudinary integration for avatar uploads
- Automatic image optimization
- URL-based image storage
- Fallback to default avatar
- Client-side validation (type, size)

### 5. **Responsive Design**

- Mobile-first approach
- Adaptive grid layouts
- Touch-friendly interface
- Optimized for screens < 768px
- Flexible table layouts

---

## User Management

### Workflow

#### Creating a User

1. Navigate to Admin Dashboard → Users
2. Click "Add User" button
3. Fill required fields:
   - Email (must be unique)
   - Password
   - First Name
   - Last Name
   - Phone
   - Role (select from dropdown)
4. Optionally upload avatar
5. Click "Create" button
6. System sends activation email

#### Editing a User

1. Navigate to User list
2. Click "Edit" button on target user
3. Update fields (email is read-only)
4. Update avatar if needed
5. Click "Update" button

#### Deleting a User

1. Navigate to User list
2. Click "Delete" button on target user
3. Confirm deletion in popup
4. User is soft-deleted (marked inactive)

#### Filtering Users

1. Use "Filter by role" dropdown to filter by role
2. Use search box for name/email search
3. Pagination controls at bottom of list

---

## Staff Management

### Workflow

#### Creating Staff Member

1. Navigate to Admin Dashboard → Staff
2. Click "Add Staff" button
3. Fill required fields:
   - Email, Password, Name, Phone
4. Select IT Role from dropdown (optional but recommended)
5. Add Skills:
   - Select skill from list
   - Choose proficiency level
   - Input years of experience
   - Click "Add"
6. Add CV link/URL (optional)
7. Upload avatar (optional)
8. Click "Create" button

#### Managing Skills

- **Add Skill**: Use the skill input section
- **Remove Skill**: Click delete button next to skill
- **Update Skill**: Remove and re-add with new values

#### Proficiency Levels

- **INTERN**: 0-1 years, beginner
- **FRESHER**: 0-1 years, fresh graduate
- **JUNIOR**: 1-2 years
- **MIDDLE**: 2-4 years
- **SENIOR**: 4-7 years
- **LEAD**: 7-10 years
- **PRINCIPAL**: 10+ years
- **ARCHITECT**: 10+ years, system design focus
- **EXPERT**: 15+ years

#### IT Roles Available

- Backend Developer
- Frontend Developer
- Fullstack Developer
- Mobile Developer
- DevOps Engineer
- QA Manual Tester
- QA Automation Tester
- Business Analyst
- Product Owner
- Project Manager
- Scrum Master
- Tech Lead
- Solution Architect
- UI/UX Designer
- Data Engineer
- Data Analyst
- Security Engineer
- Database Administrator
- System Administrator

---

## Mobile Responsiveness

### Breakpoint Strategy

**Desktop (>768px)**

- Multi-column grid layouts
- Full feature display
- Horizontal scrolling tables
- All action buttons visible

**Mobile (≤768px)**

- Single column layouts
- Stacked form fields
- Vertical table layout
- Collapsible sections
- Touch-optimized button sizes (min 44px height)

### Mobile Optimizations

1. **Forms**: Auto-fit to 1 column
2. **Tables**: Reduced padding, smaller fonts
3. **Action Rows**: Stack vertically
4. **Avatars**: Smaller preview (100px)
5. **Stats**: 2-column grid instead of 4
6. **Modals**: Full-width on mobile
7. **Menus**: Touch-optimized dropdown

---

## Language Switcher

### Location

- **Header**: Top navigation bar, next to login button
- **Visible**: All users (authenticated and guests)
- **Removed from**: Admin sidebar dropdown

### Supported Languages

- **VI**: Tiếng Việt (Vietnamese)
- **EN**: English
- **JA**: 日本語 (Japanese)

### Features

- Flag icons for quick recognition
- Dropdown menu with language options
- Current language indicator
- Session persistence
- No page reload required

### Usage

1. Click language switcher in header
2. Select desired language from dropdown
3. Content updates immediately
4. Language preference saved in session

---

## Error Handling

### Common Error Messages

| Error                  | Cause                | Solution                       |
| ---------------------- | -------------------- | ------------------------------ |
| Email already exists   | Duplicate email      | Use unique email               |
| Required field missing | Empty required field | Fill all marked fields         |
| Invalid image type     | Wrong file format    | Use image file (jpg, png, etc) |
| Image too large        | Exceeds 5MB          | Reduce image size              |
| Invalid role           | Non-existent role    | Select from available roles    |
| Unauthorized           | Missing auth header  | Log in first                   |
| Invalid skill          | Non-existent skill   | Select from available skills   |

---

## API Error Response Format

```json
{
  "status": "ERROR",
  "code": "ERR001",
  "message": "Error description",
  "data": null
}
```

## Success Response Format

```json
{
  "status": "SUCCESS",
  "code": "SUCCESS",
  "message": "Operation successful",
  "data": {
    // Response data
  }
}
```

---

## Best Practices

### For Administrators

1. **User Creation**

   - Use strong passwords
   - Verify email addresses
   - Assign appropriate roles

2. **Staff Management**

   - Complete IT role assignment
   - Add relevant skills
   - Keep CV links updated

3. **Data Quality**
   - Use consistent naming
   - Verify phone numbers
   - Keep avatars professional

### For Developers

1. **API Usage**

   - Handle pagination properly
   - Validate file uploads
   - Implement error handling

2. **Frontend**

   - Test mobile responsiveness
   - Use form validation
   - Provide user feedback

3. **Images**
   - Compress before upload
   - Use appropriate formats
   - Provide meaningful names

---

## Troubleshooting

### Issue: Avatar not uploading

- **Check**: File size < 5MB
- **Check**: File format is image
- **Check**: Network connection
- **Solution**: Try smaller/different file

### Issue: Form validation errors

- **Check**: All required fields filled
- **Check**: Email format is valid
- **Check**: Phone format is correct
- **Solution**: Review error message

### Issue: Skills not saving

- **Check**: Skill selected from list
- **Check**: Level is set
- **Check**: Skills JSON format valid
- **Solution**: Remove and re-add skill

### Issue: Pagination not working

- **Check**: Page number is valid
- **Check**: Page size > 0
- **Check**: Total pages calculation
- **Solution**: Reload page

---

## Summary

The User and Staff Management system provides:

✅ Complete CRUD operations for users, staff, and customers
✅ Responsive UI optimized for all devices
✅ Advanced staff profile with skills tracking
✅ Professional avatar upload and management
✅ Flexible role and permission system
✅ Improved UX with language switcher in header
✅ Comprehensive API with full documentation
✅ Error handling and validation

For questions or issues, refer to the API documentation or contact the development team.
