package com.makebang.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.makebang.config.EmbeddingConfig;
import com.makebang.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * OpenAI Embedding 服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "makebang.embedding.provider", havingValue = "openai", matchIfMissing = true)
public class OpenAIEmbeddingServiceImpl implements EmbeddingService {

    private final EmbeddingConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public float[] generateEmbedding(String text) {
        if (!isAvailable()) {
            log.warn("Embedding service is not available");
            return null;
        }

        try {
            List<float[]> embeddings = generateEmbeddings(Collections.singletonList(text));
            return embeddings.isEmpty() ? null : embeddings.get(0);
        } catch (Exception e) {
            log.error("Failed to generate embedding: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public List<float[]> generateEmbeddings(List<String> texts) {
        if (!isAvailable() || texts == null || texts.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModel());
            requestBody.put("input", texts);
            if (config.getDimensions() > 0) {
                requestBody.put("dimensions", config.getDimensions());
            }

            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(config.getApiKey());

            HttpEntity<String> entity = new HttpEntity<>(
                    objectMapper.writeValueAsString(requestBody),
                    headers
            );

            // 发送请求
            String url = config.getBaseUrl() + "/embeddings";
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // 解析响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseEmbeddingsResponse(response.getBody());
            }

            log.error("OpenAI API returned status: {}", response.getStatusCode());
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Failed to generate embeddings: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isAvailable() {
        return config.isEnabled() && StringUtils.hasText(config.getApiKey());
    }

    /**
     * 解析 OpenAI 响应
     */
    private List<float[]> parseEmbeddingsResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode dataArray = root.get("data");

        if (dataArray == null || !dataArray.isArray()) {
            return Collections.emptyList();
        }

        List<float[]> embeddings = new ArrayList<>();
        for (JsonNode dataNode : dataArray) {
            JsonNode embeddingNode = dataNode.get("embedding");
            if (embeddingNode != null && embeddingNode.isArray()) {
                float[] embedding = new float[embeddingNode.size()];
                for (int i = 0; i < embeddingNode.size(); i++) {
                    embedding[i] = (float) embeddingNode.get(i).asDouble();
                }
                embeddings.add(embedding);
            }
        }

        return embeddings;
    }
}
