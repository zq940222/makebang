package com.makebang.task;

import com.makebang.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 向量生成定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmbeddingTask {

    private final SearchService searchService;

    /**
     * 定时生成项目向量
     * 每小时执行一次，为没有向量的项目生成向量
     */
    @Scheduled(fixedRate = 3600000)  // 1小时
    public void generateEmbeddings() {
        log.info("Starting batch embedding generation task");
        try {
            searchService.batchGenerateEmbeddings(100);
            log.info("Batch embedding generation task completed");
        } catch (Exception e) {
            log.error("Batch embedding generation task failed: {}", e.getMessage(), e);
        }
    }
}
