package com.makebang.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Embedding 配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "makebang.embedding")
public class EmbeddingConfig {

    /**
     * 是否启用语义搜索
     */
    private boolean enabled = true;

    /**
     * API 提供商: openai, azure, local
     */
    private String provider = "openai";

    /**
     * OpenAI API Key
     */
    private String apiKey;

    /**
     * API 基础URL（可选，用于代理或Azure）
     */
    private String baseUrl = "https://api.openai.com/v1";

    /**
     * 模型名称
     */
    private String model = "text-embedding-3-small";

    /**
     * 向量维度
     */
    private int dimensions = 1536;

    /**
     * 请求超时时间（秒）
     */
    private int timeout = 30;

    /**
     * 批量处理大小
     */
    private int batchSize = 100;

    /**
     * 最小相似度阈值
     */
    private double similarityThreshold = 0.5;
}
