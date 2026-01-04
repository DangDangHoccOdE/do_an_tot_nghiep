package com.management_system.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.management_system.constant.ErrorCode;
import com.management_system.service.inter.IFileStorageService;
import com.management_system.utils.MessageUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements IFileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir; // directory to store uploaded files

    @Value("${file.remove-dir}")
    private String removeDir;

    @Value("${file.upload.max-size}")
    private Long maxFileSize; // directory to store uploaded files

    @Value("${file.size.kb}")
    private Long fileSizeKb;

    @Value("${file.size.mb}")
    private Long fileSizeMb;

    private final MessageUtil messageUtil;

    /* Save a new file to the upload directory */
    public String storeFile(MultipartFile file, Locale locale) throws IOException {
        if (file.getSize() > maxFileSize) {
            long maxFileSizeInMB = maxFileSize / (fileSizeKb * fileSizeMb);
            throw new IllegalArgumentException(
                    messageUtil.getMessage(ErrorCode.ERR005, new Object[] { maxFileSizeInMB }, locale));
        }

        // Get original file name
        String originalName = file.getOriginalFilename();

        // Get file extension
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf('.'));
            originalName = originalName.substring(0, originalName.lastIndexOf('.'));
        }

        // Add timestamp to avoid duplicate file names
        String timestamp = String.valueOf(System.currentTimeMillis());
        String newFileName = originalName + "_" + timestamp + ext;

        // Create directory if it doesn’t exist
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Save file to the directory
        File dest = new File(dir, newFileName);
        file.transferTo(dest);

        // Return only the file name (no base URL)
        return newFileName;
    }

    /* Delete an existing file by file name if it exists */
    public void deleteFileByUrl(String fileName) throws Exception {
        if (fileName == null || fileName.isBlank()) {
            return;
        }

        // Build file path using upload directory and file name
        File file = new File(uploadDir, fileName);

        if (file.exists()) {
            file.delete();
        }
    }

    /* Soft delete file → move to remove folder */
    public void softDeleteFileByUrl(String fileName) throws IOException {
        if (fileName == null || fileName.isBlank()) {
            return;
        }

        File sourceFile = new File(uploadDir, fileName);
        if (!sourceFile.exists()) {
            return;
        }

        File removeFolder = new File(removeDir);
        if (!removeFolder.exists()) {
            removeFolder.mkdirs();
        }

        File destFile = new File(removeFolder, fileName);
        sourceFile.renameTo(destFile);
    }
}
