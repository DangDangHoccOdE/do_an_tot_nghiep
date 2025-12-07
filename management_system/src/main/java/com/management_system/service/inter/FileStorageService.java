package com.management_system.service.inter;

import java.io.IOException;
import java.util.Locale;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String storeFile(MultipartFile file, Locale locale) throws IOException;

    void deleteFileByUrl(String fileName) throws Exception;

    void softDeleteFileByUrl(String fileName) throws IOException;
}


