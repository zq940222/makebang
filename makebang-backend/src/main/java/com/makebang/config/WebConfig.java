package com.makebang.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web 配置
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final StorageConfig storageConfig;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 本地文件存储的静态资源映射
        if ("local".equals(storageConfig.getType())) {
            String basePath = storageConfig.getLocal().getBasePath();
            String urlPrefix = storageConfig.getLocal().getUrlPrefix();

            // 确保路径格式正确
            if (!urlPrefix.startsWith("/")) {
                urlPrefix = "/" + urlPrefix;
            }
            if (!urlPrefix.endsWith("/")) {
                urlPrefix = urlPrefix + "/";
            }

            // 获取绝对路径
            String absolutePath = Paths.get(basePath).toAbsolutePath().toString();
            if (!absolutePath.endsWith("/") && !absolutePath.endsWith("\\")) {
                absolutePath = absolutePath + "/";
            }

            registry.addResourceHandler(urlPrefix + "**")
                    .addResourceLocations("file:" + absolutePath);
        }
    }
}
