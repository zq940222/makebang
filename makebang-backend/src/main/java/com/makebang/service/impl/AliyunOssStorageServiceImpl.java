package com.makebang.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.makebang.config.StorageConfig;
import com.makebang.service.StorageService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * 阿里云 OSS 存储服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "makebang.storage.type", havingValue = "aliyun-oss")
public class AliyunOssStorageServiceImpl implements StorageService {

    private final StorageConfig storageConfig;
    private OSS ossClient;

    @PostConstruct
    public void init() {
        StorageConfig.AliyunOssConfig ossConfig = storageConfig.getAliyunOss();
        this.ossClient = new OSSClientBuilder().build(
                ossConfig.getEndpoint(),
                ossConfig.getAccessKeyId(),
                ossConfig.getAccessKeySecret()
        );
        log.info("Aliyun OSS client initialized");
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
            log.info("Aliyun OSS client shutdown");
        }
    }

    @Override
    public String upload(InputStream inputStream, String filePath, String contentType) {
        try {
            StorageConfig.AliyunOssConfig ossConfig = storageConfig.getAliyunOss();

            ObjectMetadata metadata = new ObjectMetadata();
            if (contentType != null) {
                metadata.setContentType(contentType);
            }

            ossClient.putObject(ossConfig.getBucketName(), filePath, inputStream, metadata);

            log.info("File uploaded to Aliyun OSS: {}", filePath);

            return getUrl(filePath);
        } catch (Exception e) {
            log.error("Failed to upload file to Aliyun OSS: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public boolean delete(String filePath) {
        try {
            StorageConfig.AliyunOssConfig ossConfig = storageConfig.getAliyunOss();
            ossClient.deleteObject(ossConfig.getBucketName(), filePath);
            log.info("File deleted from Aliyun OSS: {}", filePath);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete file from Aliyun OSS: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean exists(String filePath) {
        try {
            StorageConfig.AliyunOssConfig ossConfig = storageConfig.getAliyunOss();
            return ossClient.doesObjectExist(ossConfig.getBucketName(), filePath);
        } catch (Exception e) {
            log.error("Failed to check file existence in Aliyun OSS: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getUrl(String filePath) {
        StorageConfig.AliyunOssConfig ossConfig = storageConfig.getAliyunOss();
        String urlPrefix = ossConfig.getUrlPrefix();
        if (urlPrefix != null && !urlPrefix.isEmpty()) {
            if (!urlPrefix.endsWith("/")) {
                urlPrefix += "/";
            }
            return urlPrefix + filePath;
        }
        // 默认使用 bucket 域名
        return String.format("https://%s.%s/%s",
                ossConfig.getBucketName(),
                ossConfig.getEndpoint().replace("https://", "").replace("http://", ""),
                filePath);
    }

    @Override
    public String getStorageType() {
        return "aliyun-oss";
    }
}
