package com.makebang.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 文件存储配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "makebang.storage")
public class StorageConfig {

    /**
     * 存储类型: local, aliyun-oss, tencent-cos, minio
     */
    private String type = "local";

    /**
     * 本地存储配置
     */
    private LocalConfig local = new LocalConfig();

    /**
     * 阿里云 OSS 配置
     */
    private AliyunOssConfig aliyunOss = new AliyunOssConfig();

    /**
     * 腾讯云 COS 配置
     */
    private TencentCosConfig tencentCos = new TencentCosConfig();

    /**
     * MinIO 配置
     */
    private MinioConfig minio = new MinioConfig();

    /**
     * 文件大小限制（MB）
     */
    private int maxFileSize = 50;

    /**
     * 图片最大尺寸（MB）
     */
    private int maxImageSize = 10;

    /**
     * 允许的文件类型
     */
    private String[] allowedTypes = {
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/zip", "application/x-rar-compressed",
            "text/plain"
    };

    /**
     * 缩略图宽度
     */
    private int thumbnailWidth = 200;

    /**
     * 缩略图高度
     */
    private int thumbnailHeight = 200;

    @Data
    public static class LocalConfig {
        /**
         * 本地存储根目录
         */
        private String basePath = "./uploads";

        /**
         * 访问URL前缀
         */
        private String urlPrefix = "/uploads";
    }

    @Data
    public static class AliyunOssConfig {
        private String endpoint;
        private String accessKeyId;
        private String accessKeySecret;
        private String bucketName;
        private String urlPrefix;
    }

    @Data
    public static class TencentCosConfig {
        private String region;
        private String secretId;
        private String secretKey;
        private String bucketName;
        private String urlPrefix;
    }

    @Data
    public static class MinioConfig {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucketName;
    }
}
