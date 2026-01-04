package com.management_system.service.inter;

import org.springframework.web.multipart.MultipartFile;

public interface ICloudinaryService {
    public String uploadImage(MultipartFile file);

    public void deleteImage(String imageUrl);
}
