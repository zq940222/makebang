package com.makebang.service.impl;

import com.makebang.config.StorageConfig;
import com.makebang.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 本地文件存储服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "makebang.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageServiceImpl implements StorageService {

    private final StorageConfig storageConfig;

    @Override
    public String upload(InputStream inputStream, String filePath, String contentType) {
        try {
            Path fullPath = Paths.get(storageConfig.getLocal().getBasePath(), filePath);

            // 确保目录存在
            Files.createDirectories(fullPath.getParent());

            // 复制文件
            Files.copy(inputStream, fullPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("File uploaded to local storage: {}", fullPath);

            return getUrl(filePath);
        } catch (IOException e) {
            log.error("Failed to upload file to local storage: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public boolean delete(String filePath) {
        try {
            Path fullPath = Paths.get(storageConfig.getLocal().getBasePath(), filePath);
            boolean deleted = Files.deleteIfExists(fullPath);
            if (deleted) {
                log.info("File deleted from local storage: {}", fullPath);
            }
            return deleted;
        } catch (IOException e) {
            log.error("Failed to delete file from local storage: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean exists(String filePath) {
        Path fullPath = Paths.get(storageConfig.getLocal().getBasePath(), filePath);
        return Files.exists(fullPath);
    }

    @Override
    public String getUrl(String filePath) {
        String urlPrefix = storageConfig.getLocal().getUrlPrefix();
        if (!urlPrefix.endsWith("/")) {
            urlPrefix += "/";
        }
        return urlPrefix + filePath.replace("\\", "/");
    }

    @Override
    public String getStorageType() {
        return "local";
    }
}
