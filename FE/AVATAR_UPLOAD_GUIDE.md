# Avatar Upload Feature - Cloudinary Integration

## 📋 Overview

Hệ thống avatar upload được tích hợp với **Cloudinary** - dịch vụ lưu trữ file trên cloud.

**Ưu điểm:**

- ✅ Upload trực tiếp từ frontend (không qua backend)
- ✅ Tự động resize & optimize ảnh
- ✅ Miễn phí (500MB/tháng)
- ✅ Công khai URL ảnh, dễ chia sẻ
- ✅ CDN tốc độ cao

## 🔧 Cài đặt Cloudinary

### 1. Tạo tài khoản Cloudinary

1. Truy cập https://cloudinary.com/users/register/free
2. Đăng ký tài khoản miễn phí
3. Xác nhận email

### 2. Lấy Cloud Name

1. Vào Dashboard: https://cloudinary.com/console
2. Sao chép **Cloud Name** (ví dụ: `dvl3k4ydi`)

### 3. Tạo Upload Preset

1. Vào Settings > Upload
2. Scroll xuống "Upload presets" section
3. Click "Create upload preset"
4. Điền:
   - **Name**: `do_an` (hoặc tên bất kỳ)
   - **Upload type**: `Unsigned`
   - **Folder**: `do_an_management/avatars` (optional)
5. Click "Save"
6. Sao chép tên Preset

### 4. Cấu hình .env

Tạo file `.env.local` (hoặc cập nhật `.env` hiện tại):

```env
VITE_CLOUDINARY_CLOUD_NAME=your_cloud_name
VITE_CLOUDINARY_UPLOAD_PRESET=do_an
```

## 📁 Files tạo/sửa

### Tạo mới:

- `src/utils/cloudinary.js` - Utility functions for Cloudinary upload
- `.env.cloudinary.example` - Cấu hình example

### Sửa:

- `src/components/admin/pages/UserFormPage.vue` - Thêm avatar upload UI
- `src/locales/vi/vi.json` - Thêm i18n messages
- `src/locales/en/en.json` - Thêm i18n messages (English)
- `src/services/apiUsers.js` - API endpoints

## 🎨 UI Components

### Upload UI

```vue
<el-upload
  action="#"
  :auto-upload="false"
  :on-change="handleAvatarSelect"
  accept="image/*"
  drag
>
    <div>Kéo ảnh vào đây hoặc nhấp để chọn</div>
</el-upload>
```

### Avatar Preview

- Hiển thị ảnh preview (150x150px)
- Nút "Xóa ảnh" để remove
- Progress bar theo dõi upload

## 🔄 Upload Flow

```
User select image
    ↓
uploadToCloudinary(file)
    ↓
FormData request → Cloudinary API
    ↓
Upload progress → UI update
    ↓
Response: {url, publicId}
    ↓
form.avatar = url
    ↓
Save form
    ↓
POST /api/v1/users (with avatar URL)
    ↓
Backend lưu avatar URL vào database
```

## 📝 API Contract

### Frontend gửi:

```json
{
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "0123456789",
  "roleId": "uuid",
  "avatar": "https://res.cloudinary.com/...jpg"
}
```

### Backend lưu:

- Lưu URL vào User.avatar field (String, nvarchar(500))

### User response:

```json
{
  "id": "uuid",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "0123456789",
  "roleId": "uuid",
  "avatar": "https://res.cloudinary.com/...jpg"
}
```

## 🛡️ Security

**Unsigned Upload (Frontend):**

- ✅ Uploadable từ browser
- ✅ Không cần backend involvement
- ⚠️ Cần cấu hình Upload Preset công khai

**Best Practice:**

1. Restrict file types (image only)
2. Max file size: 5MB
3. Restrict folder: `do_an_management/avatars`
4. Enable Auto tagging/moderation (Cloudinary settings)

## 📊 Image Optimization

Cloudinary tự động tối ưu:

- Format: WebP (modern browsers), JPEG (fallback)
- Quality: Auto-adjusted
- Size: Responsive using `c_fill,w_150,h_150`

URL examples:

```
Original: https://res.cloudinary.com/dvl3k4ydi/image/upload/v1234567890/do_an_management/avatars/abc123.jpg

Optimized: https://res.cloudinary.com/dvl3k4ydi/image/fetch/c_fill,w_150,h_150/https://...
```

## 🐛 Troubleshooting

### Upload fails with 401

- Kiểm tra Cloud Name có đúng không
- Kiểm tra Upload Preset có tồn tại không
- Kiểm tra Upload Preset có "Unsigned" không

### Image URL 404

- Kiểm tra public_id có đúng không
- Kiểm tra folder path có đúng không

### Image không display

- Kiểm tra CORS settings trong Cloudinary
- Kiểm tra URL là https (không http)

## 📦 Dependencies

Không cần thêm dependencies!

- Sử dụng native XMLHttpRequest
- HTML5 File API
- Cloudinary REST API

## 🔍 Testing

### Test upload:

1. Vào admin/users/new
2. Click upload avatar
3. Chọn ảnh (max 5MB)
4. Check progress bar
5. Fill form và submit

### Test edit:

1. Vào admin/users
2. Click Edit user
3. Avatar preview hiển thị
4. Click remove để delete
5. Upload ảnh mới

## 📚 References

- Cloudinary Docs: https://cloudinary.com/documentation/cloudinary_fundamentals
- Upload API: https://cloudinary.com/documentation/upload_images
- Upload Presets: https://cloudinary.com/documentation/upload_presets
