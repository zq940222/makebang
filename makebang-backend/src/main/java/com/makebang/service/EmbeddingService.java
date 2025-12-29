package com.makebang.service;

import java.util.List;

/**
 * 向量嵌入服务接口
 */
public interface EmbeddingService {

    /**
     * 生成单个文本的向量
     *
     * @param text 文本内容
     * @return 向量数组
     */
    float[] generateEmbedding(String text);

    /**
     * 批量生成文本向量
     *
     * @param texts 文本列表
     * @return 向量数组列表
     */
    List<float[]> generateEmbeddings(List<String> texts);

    /**
     * 检查服务是否可用
     *
     * @return 是否可用
     */
    boolean isAvailable();
}
