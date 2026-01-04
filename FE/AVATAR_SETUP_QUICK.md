## 🚀 Avatar Upload Setup - Quick Start

### ✅ Step 1: Cấu hình Cloudinary

1. **Tạo tài khoản:**

   - Truy cập: https://cloudinary.com/users/register/free
   - Đăng ký → Xác nhận email

2. **Lấy Cloud Name:**

   - Vào Dashboard: https://cloudinary.com/console
   - Sao chép **Cloud Name** (VD: `dvl3k4ydi`)

3. **Tạo Upload Preset:**

   - Settings → Upload
   - "Create upload preset"
   - Name: `do_an`
   - Upload type: **Unsigned**
   - Folder: `do_an_management/avatars` (optional)
   - Save → Copy name

4. **Cập nhật .env:**
   ```env
   VITE_CLOUDINARY_CLOUD_NAME=your_cloud_name
   VITE_CLOUDINARY_UPLOAD_PRESET=do_an
   ```

### ✅ Step 2: Files đã tạo

✅ **Utility:**

- `src/utils/cloudinary.js` - Upload function

✅ **Component:**

- `src/components/admin/pages/UserFormPage.vue` - Avatar upload UI

✅ **Services:**

- `src/services/apiUsers.js` - API endpoints

✅ **Validation:**

- `src/validations/userRules.js` - Form validation

✅ **i18n:**

- `src/locales/vi/vi.json` - Vietnamese messages
- `src/locales/en/en.json` - English messages

✅ **Documentation:**

- `AVATAR_UPLOAD_GUIDE.md` - Chi tiết setup
- `.env.cloudinary.example` - Config example

### ✅ Step 3: Backend Updates

**User entity cần thêm:**

```java
@Column(name = "avatar", length = 500)
private String avatar;
```

**API endpoints cần implement:**

```
POST /api/v1/users - Create user (với avatar URL)
PUT /api/v1/users/{id} - Update user (với avatar URL)
GET /api/v1/users/{id} - Get user detail (trả avatar)
GET /api/v1/users - List users
DELETE /api/v1/users/{id} - Delete user
GET /api/v1/roles - Get all roles
```

### ✅ Step 4: Test

1. **Login vào admin**
2. **Menu → Người dùng**
3. **Thêm mới người dùng:**
   - Click "Upload avatar"
   - Kéo ảnh vào hoặc click để chọn
   - Progress bar update → Xong
   - Fill email, name, role
   - Click "Tạo mới"
4. **Edit người dùng:**
   - Click "Chỉnh sửa"
   - Avatar preview hiển thị
   - Click "Xóa ảnh" để thay đổi
   - Submit

### 🎯 Key Features

✨ **Upload:**

- Drag & drop hoặc click to select
- Max 5MB
- Auto validate image type
- Progress tracking

✨ **Preview:**

- 150x150px thumbnail
- Optimized via Cloudinary
- Can remove & reupload

✨ **i18n:**

- Vietnamese & English support
- All labels translated
- Error messages localized

✨ **No Backend Dependency:**

- Upload trực tiếp Cloudinary
- Frontend lưu URL
- Backend chỉ lưu string

### 🔐 Security Notes

1. **Unsigned Upload:** Không cần API key backend
2. **Folder Restriction:** Tất cả upload vào `do_an_management/avatars`
3. **File Type:** Chỉ image/\*, max 5MB
4. **URL:** Lưu secure HTTPS URL

### ❓ Troubleshooting

**Upload fails?**
→ Check `.env` config có đúng Cloud Name & Preset

**Image 404?**
→ Check Cloudinary account còn active

**CORS error?**
→ Cloudinary API có CORS support, check browser console

### 📞 Support

- Cloudinary Docs: https://cloudinary.com/documentation
- Avatar Setup: Xem `AVATAR_UPLOAD_GUIDE.md`
