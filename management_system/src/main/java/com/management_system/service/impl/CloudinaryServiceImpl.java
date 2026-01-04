package com.management_system.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.management_system.service.inter.ICloudinaryService;

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
public class CloudinaryServiceImpl implements ICloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.upload-folder:user-avatars}")
    private String uploadFolder;

    /**
     * Upload ảnh lên Cloudinary
     * 
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
                            .quality("auto"));

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
     * 
     * @param imageUrl URL của ảnh cần xóa
     */
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        try {
            // Extract public_id from URL
            // URL format:
            // https://res.cloudinary.com/{cloud_name}/image/upload/v{version}/{public_id}.{format}
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
            // Example URL:
            // https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg
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