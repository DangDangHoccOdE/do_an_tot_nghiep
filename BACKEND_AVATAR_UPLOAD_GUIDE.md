# Hướng dẫn Implement Upload Avatar ở Backend

## 📋 Tổng quan

Frontend đã được cập nhật để gửi file ảnh qua **FormData** (multipart/form-data). Backend cần:

1. Nhận multipart request với file ảnh
2. Upload lên Cloudinary
3. Lưu URL vào database
4. Xóa ảnh cũ khi update/delete user

---

## 🔧 1. Thêm Cloudinary Dependencies

### Maven (pom.xml)

```xml
<dependency>
    <groupId>com.cloudinary</groupId>
    <artifactId>cloudinary-http44</artifactId>
    <version>1.36.0</version>
</dependency>
```

### Gradle (build.gradle)

```gradle
implementation 'com.cloudinary:cloudinary-http44:1.36.0'
```

---

## ⚙️ 2. Cấu hình Cloudinary

### application.properties

```properties
# Cloudinary Configuration
cloudinary.cloud-name=${CLOUDINARY_CLOUD_NAME}
cloudinary.api-key=${CLOUDINARY_API_KEY}
cloudinary.api-secret=${CLOUDINARY_API_SECRET}
cloudinary.upload-folder=user-avatars

# File Upload Configuration
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB
```

### CloudinaryConfig.java

```java
package com.management_system.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret,
            "secure", true
        ));
    }
}
```

---

## 📁 3. Tạo CloudinaryService

### CloudinaryService.java

```java
package com.management_system.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.upload-folder:user-avatars}")
    private String uploadFolder;

    /**
     * Upload ảnh lên Cloudinary
     * @param file File ảnh từ request
     * @return URL của ảnh đã upload
     */
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("File must be an image");
            }

            // Validate file size (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("File size must not exceed 5MB");
            }

            // Tạo public_id unique
            String publicId = uploadFolder + "/" + UUID.randomUUID().toString();

            // Upload options
            Map uploadParams = ObjectUtils.asMap(
                "public_id", publicId,
                "folder", uploadFolder,
                "resource_type", "image",
                "overwrite", false,
                "transformation", new com.cloudinary.Transformation()
                    .width(500)
                    .height(500)
                    .crop("fill")
                    .gravity("face")
                    .quality("auto")
            );

            // Upload to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            // Return secure URL
            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("Successfully uploaded image to Cloudinary: {}", secureUrl);
            return secureUrl;

        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary", e);
            throw new RuntimeException("Failed to upload image", e);
        }
    }

    /**
     * Xóa ảnh từ Cloudinary dựa trên URL
     * @param imageUrl URL của ảnh cần xóa
     */
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        try {
            // Extract public_id from URL
            // URL format: https://res.cloudinary.com/{cloud_name}/image/upload/v{version}/{public_id}.{format}
            String publicId = extractPublicIdFromUrl(imageUrl);
            if (publicId != null) {
                Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("Deleted image from Cloudinary: {}, result: {}", publicId, result.get("result"));
            }
        } catch (Exception e) {
            log.error("Failed to delete image from Cloudinary: {}", imageUrl, e);
            // Don't throw exception, just log error
        }
    }

    /**
     * Trích xuất public_id từ Cloudinary URL
     */
    private String extractPublicIdFromUrl(String imageUrl) {
        try {
            // Example URL: https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg
            // public_id: sample

            // Split by /upload/
            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) {
                return null;
            }

            // Get part after /upload/
            String afterUpload = parts[1];

            // Remove version (v1234567890/)
            if (afterUpload.startsWith("v")) {
                int slashIndex = afterUpload.indexOf('/');
                if (slashIndex > 0) {
                    afterUpload = afterUpload.substring(slashIndex + 1);
                }
            }

            // Remove file extension
            int dotIndex = afterUpload.lastIndexOf('.');
            if (dotIndex > 0) {
                afterUpload = afterUpload.substring(0, dotIndex);
            }

            return afterUpload;
        } catch (Exception e) {
            log.error("Failed to extract public_id from URL: {}", imageUrl, e);
            return null;
        }
    }
}
```

---

## 👤 4. Cập nhật User Entity

### User.java

```java
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ... các field khác

    @Column(name = "avatar", length = 500)
    private String avatar; // URL của ảnh trên Cloudinary

    // Getters & Setters
}
```

---

## 🎯 5. Cập nhật UserController

### UserController.java

```java
package com.management_system.controller;

import com.management_system.dto.request.UserCreateRequest;
import com.management_system.dto.request.UserUpdateRequest;
import com.management_system.dto.response.UserResponse;
import com.management_system.service.CloudinaryService;
import com.management_system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    /**
     * Tạo user mới
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("phone") String phone,
            @RequestParam("roleId") Long roleId,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar
    ) {
        // Upload avatar nếu có
        String avatarUrl = null;
        if (avatar != null && !avatar.isEmpty()) {
            avatarUrl = cloudinaryService.uploadImage(avatar);
        }

        // Tạo request object
        UserCreateRequest request = UserCreateRequest.builder()
                .email(email)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .roleId(roleId)
                .avatar(avatarUrl)
                .build();

        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Cập nhật user
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @RequestParam("email") String email,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("phone") String phone,
            @RequestParam("roleId") Long roleId,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar
    ) {
        // Lấy thông tin user hiện tại để xóa ảnh cũ
        UserResponse currentUser = userService.getUserById(id);
        String oldAvatarUrl = currentUser.getAvatar();

        // Upload avatar mới nếu có
        String newAvatarUrl = oldAvatarUrl; // Giữ nguyên ảnh cũ nếu không upload
        if (avatar != null && !avatar.isEmpty()) {
            newAvatarUrl = cloudinaryService.uploadImage(avatar);

            // Xóa ảnh cũ sau khi upload thành công
            if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
                cloudinaryService.deleteImage(oldAvatarUrl);
            }
        }

        // Tạo request object
        UserUpdateRequest request = UserUpdateRequest.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .roleId(roleId)
                .avatar(newAvatarUrl)
                .build();

        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa user
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        // Lấy thông tin user để xóa ảnh
        UserResponse user = userService.getUserById(id);

        // Xóa user trong database
        userService.deleteUser(id);

        // Xóa ảnh trên Cloudinary
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            cloudinaryService.deleteImage(user.getAvatar());
        }

        return ResponseEntity.noContent().build();
    }

    // ... các endpoint khác
}
```

---

## 📝 6. DTO Classes

### UserCreateRequest.java

```java
package com.management_system.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private Long roleId;
    private String avatar; // URL từ Cloudinary
}
```

### UserUpdateRequest.java

```java
package com.management_system.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Long roleId;
    private String avatar; // URL từ Cloudinary
}
```

---

## 🔒 7. Xử lý Error

### GlobalExceptionHandler.java

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage()
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "File size exceeds maximum limit of 5MB"
        );
        return ResponseEntity.badRequest().body(error);
    }
}
```

---

## 🧪 8. Testing với Postman

### Create User Request

```
POST http://localhost:8080/api/v1/users
Content-Type: multipart/form-data

Body (form-data):
- email: user@example.com
- password: Password123
- firstName: John
- lastName: Doe
- phone: 0123456789
- roleId: 2
- avatar: [select file]
```

### Update User Request

```
PUT http://localhost:8080/api/v1/users/1
Content-Type: multipart/form-data

Body (form-data):
- email: user@example.com
- firstName: John Updated
- lastName: Doe
- phone: 0123456789
- roleId: 2
- avatar: [select new file]
```

---

## 📊 9. Database Migration

### SQL Script

```sql
-- Thêm cột avatar vào bảng users (nếu chưa có)
ALTER TABLE users ADD COLUMN avatar VARCHAR(500);

-- Index cho tìm kiếm nhanh
CREATE INDEX idx_users_avatar ON users(avatar);
```

---

## ✅ 10. Checklist Implementation

- [ ] Thêm Cloudinary dependency vào pom.xml/build.gradle
- [ ] Tạo CloudinaryConfig.java với thông tin cloud_name, api_key, api_secret
- [ ] Tạo CloudinaryService.java với uploadImage() và deleteImage()
- [ ] Cập nhật User entity thêm field avatar
- [ ] Cập nhật UserController để nhận MultipartFile
- [ ] Test upload với Postman
- [ ] Test update user với ảnh mới (xóa ảnh cũ)
- [ ] Test delete user (xóa ảnh trên Cloudinary)
- [ ] Thêm error handling cho upload failures
- [ ] Thêm validation cho file type và size

---

## 🎯 11. Ưu điểm của Backend Upload

### ✅ Security

- Ẩn Cloudinary credentials (không expose ra frontend)
- Validate file type/size ở server
- Authentication & Authorization check

### ✅ Data Consistency

- Upload và save database trong cùng transaction
- Rollback nếu có lỗi
- Không có orphaned images

### ✅ Cleanup Management

- Tự động xóa ảnh cũ khi update
- Xóa ảnh khi delete user
- Dễ dàng implement cleanup job

### ✅ Control & Monitoring

- Log upload activities
- Track storage usage
- Monitor errors centrally

---

## 🔍 12. Testing Frontend Integration

Sau khi implement backend, test flow:

1. **Create User với Avatar:**

   - Chọn ảnh trong form
   - Submit form
   - Kiểm tra ảnh hiển thị trong danh sách users
   - Verify ảnh đã lên Cloudinary

2. **Update User Avatar:**

   - Edit user
   - Chọn ảnh mới
   - Submit
   - Verify ảnh cũ đã bị xóa trên Cloudinary
   - Verify ảnh mới hiển thị

3. **Delete User:**

   - Xóa user
   - Verify ảnh đã bị xóa trên Cloudinary

4. **Error Handling:**
   - Upload file quá 5MB → Show error
   - Upload file không phải ảnh → Show error
   - Network error → Show error message

---

## 🚀 Kết luận

Với cách implement này:

- ✅ Toàn bộ logic upload nằm ở backend
- ✅ Không còn orphaned images
- ✅ Tự động cleanup khi update/delete
- ✅ Bảo mật hơn (không expose credentials)
- ✅ Dễ maintain và scale

Frontend chỉ cần:

1. Chọn file
2. Gửi FormData đến backend
3. Nhận và hiển thị URL response
