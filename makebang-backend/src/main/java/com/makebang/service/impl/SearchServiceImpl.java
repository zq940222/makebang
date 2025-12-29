package com.makebang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.makebang.config.EmbeddingConfig;
import com.makebang.entity.HotKeyword;
import com.makebang.entity.Project;
import com.makebang.entity.SearchHistory;
import com.makebang.repository.HotKeywordRepository;
import com.makebang.repository.ProjectEmbeddingRepository;
import com.makebang.repository.ProjectRepository;
import com.makebang.repository.SearchHistoryRepository;
import com.makebang.service.EmbeddingService;
import com.makebang.service.SearchService;
import com.makebang.util.SecurityUtils;
import com.makebang.vo.ProjectVO;
import com.makebang.vo.SearchResultVO;
import com.makebang.vo.SearchSuggestionVO;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 搜索服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ProjectRepository projectRepository;
    private final ProjectEmbeddingRepository embeddingRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final HotKeywordRepository hotKeywordRepository;
    private final EmbeddingService embeddingService;
    private final EmbeddingConfig embeddingConfig;

    @Override
    public SearchResultVO search(String keyword, Long categoryId, Integer minBudget, Integer maxBudget,
                                  int current, int size) {
        long startTime = System.currentTimeMillis();
        String searchType = "keyword";
        List<ProjectVO> projects;
        long total;

        // 记录搜索历史
        recordSearchHistory(keyword, searchType);

        // 判断是否使用语义搜索
        boolean useSemanticSearch = embeddingService.isAvailable()
                && StringUtils.hasText(keyword)
                && keyword.length() > 5;  // 较长的查询更适合语义搜索

        if (useSemanticSearch) {
            // 混合搜索：语义 + 关键词
            searchType = "hybrid";
            projects = hybridSearch(keyword, categoryId, minBudget, maxBudget, current, size);
            total = projects.size() < size ? (long) (current - 1) * size + projects.size()
                    : countHybridResults(keyword, categoryId, minBudget, maxBudget);
        } else {
            // 纯关键词搜索
            IPage<Project> page = keywordSearch(keyword, categoryId, minBudget, maxBudget, current, size);
            projects = page.getRecords().stream()
                    .map(this::toProjectVO)
                    .collect(Collectors.toList());
            total = page.getTotal();
        }

        long costTime = System.currentTimeMillis() - startTime;

        return SearchResultVO.builder()
                .projects(projects)
                .total(total)
                .current(current)
                .size(size)
                .pages((int) Math.ceil((double) total / size))
                .searchType(searchType)
                .costTime(costTime)
                .suggestions(projects.isEmpty() ? getRelatedSuggestions(keyword) : null)
                .build();
    }

    @Override
    public List<ProjectVO> semanticSearch(String query, int limit) {
        if (!embeddingService.isAvailable() || !StringUtils.hasText(query)) {
            return Collections.emptyList();
        }

        try {
            // 生成查询向量
            float[] queryVector = embeddingService.generateEmbedding(query);
            if (queryVector == null) {
                return Collections.emptyList();
            }

            // 向量搜索
            String vectorStr = vectorToString(queryVector);
            List<ProjectEmbeddingRepository.SimilarityResult> results =
                    embeddingRepository.searchByVectorWithThreshold(
                            vectorStr,
                            embeddingConfig.getSimilarityThreshold(),
                            limit
                    );

            // 获取项目详情
            List<Long> projectIds = results.stream()
                    .map(ProjectEmbeddingRepository.SimilarityResult::getProjectId)
                    .collect(Collectors.toList());

            if (projectIds.isEmpty()) {
                return Collections.emptyList();
            }

            List<Project> projects = projectRepository.selectBatchIds(projectIds);

            // 按相似度排序
            Map<Long, Double> similarityMap = results.stream()
                    .collect(Collectors.toMap(
                            ProjectEmbeddingRepository.SimilarityResult::getProjectId,
                            ProjectEmbeddingRepository.SimilarityResult::getSimilarity
                    ));

            return projects.stream()
                    .sorted((p1, p2) -> Double.compare(
                            similarityMap.getOrDefault(p2.getId(), 0.0),
                            similarityMap.getOrDefault(p1.getId(), 0.0)
                    ))
                    .map(this::toProjectVO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Semantic search failed: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public SearchSuggestionVO getSuggestions(String prefix, int limit) {
        Long userId = SecurityUtils.getCurrentUserIdOrNull();

        // 获取热门关键词
        List<String> hotKeywords = hotKeywordRepository.getHotKeywords(10).stream()
                .map(HotKeyword::getKeyword)
                .collect(Collectors.toList());

        // 获取用户历史
        List<String> historyKeywords = Collections.emptyList();
        if (userId != null) {
            historyKeywords = searchHistoryRepository.getUserRecentSearches(userId, 10).stream()
                    .map(SearchHistory::getKeyword)
                    .collect(Collectors.toList());
        }

        // 获取联想词
        List<String> completions = Collections.emptyList();
        if (StringUtils.hasText(prefix)) {
            completions = hotKeywordRepository.getHotSuggestions(prefix, limit);
            if (userId != null && completions.size() < limit) {
                List<String> userSuggestions = searchHistoryRepository.getSuggestions(
                        userId, prefix, limit - completions.size()
                );
                Set<String> uniqueCompletions = new LinkedHashSet<>(completions);
                uniqueCompletions.addAll(userSuggestions);
                completions = new ArrayList<>(uniqueCompletions);
            }
        }

        // 获取相关技能标签
        List<String> relatedSkills = projectRepository.getHotSkills(10);

        return SearchSuggestionVO.builder()
                .hotKeywords(hotKeywords)
                .historyKeywords(historyKeywords)
                .completions(completions)
                .relatedSkills(relatedSkills)
                .build();
    }

    @Override
    public List<String> getHotKeywords(int limit) {
        return hotKeywordRepository.getHotKeywords(limit).stream()
                .map(HotKeyword::getKeyword)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getUserSearchHistory(int limit) {
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            return Collections.emptyList();
        }
        return searchHistoryRepository.getUserRecentSearches(userId, limit).stream()
                .map(SearchHistory::getKeyword)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void clearUserSearchHistory() {
        Long userId = SecurityUtils.getCurrentUserId();
        searchHistoryRepository.clearUserHistory(userId);
    }

    @Override
    @Async
    public void generateProjectEmbedding(Long projectId) {
        if (!embeddingService.isAvailable()) {
            return;
        }

        try {
            Project project = projectRepository.selectById(projectId);
            if (project == null || project.getDeletedAt() != null) {
                return;
            }

            // 生成内容文本
            String content = buildProjectContent(project);
            String contentHash = DigestUtils.md5DigestAsHex(content.getBytes(StandardCharsets.UTF_8));

            // 检查是否需要更新
            String existingHash = embeddingRepository.getContentHash(projectId);
            if (contentHash.equals(existingHash)) {
                log.debug("Project {} embedding is up to date", projectId);
                return;
            }

            // 生成向量
            float[] embedding = embeddingService.generateEmbedding(content);
            if (embedding == null) {
                log.warn("Failed to generate embedding for project {}", projectId);
                return;
            }

            // 保存向量
            String vectorStr = vectorToString(embedding);
            embeddingRepository.upsertEmbedding(projectId, vectorStr, contentHash);

            log.info("Generated embedding for project {}", projectId);

        } catch (Exception e) {
            log.error("Failed to generate embedding for project {}: {}", projectId, e.getMessage());
        }
    }

    @Override
    @Transactional
    public void batchGenerateEmbeddings(int batchSize) {
        if (!embeddingService.isAvailable()) {
            log.info("Embedding service is not available, skipping batch generation");
            return;
        }

        List<Long> projectIds = embeddingRepository.findProjectsWithoutEmbedding(batchSize);
        if (projectIds.isEmpty()) {
            log.debug("No projects need embedding generation");
            return;
        }

        log.info("Generating embeddings for {} projects", projectIds.size());
        for (Long projectId : projectIds) {
            generateProjectEmbedding(projectId);
        }
    }

    @Override
    @Async
    public void refreshProjectEmbedding(Long projectId) {
        generateProjectEmbedding(projectId);
    }

    // ========== Private Methods ==========

    /**
     * 关键词搜索
     */
    private IPage<Project> keywordSearch(String keyword, Long categoryId,
                                          Integer minBudget, Integer maxBudget,
                                          int current, int size) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Project::getDeletedAt);
        wrapper.in(Project::getStatus, List.of(1, 2));  // 招标中或进行中

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Project::getTitle, keyword)
                    .or()
                    .like(Project::getDescription, keyword)
            );
        }

        if (categoryId != null) {
            wrapper.eq(Project::getCategoryId, categoryId);
        }

        if (minBudget != null) {
            wrapper.ge(Project::getBudgetMax, minBudget);
        }

        if (maxBudget != null) {
            wrapper.le(Project::getBudgetMin, maxBudget);
        }

        wrapper.orderByDesc(Project::getCreatedAt);

        return projectRepository.selectPage(new Page<>(current, size), wrapper);
    }

    /**
     * 混合搜索（语义 + 关键词）
     */
    private List<ProjectVO> hybridSearch(String keyword, Long categoryId,
                                          Integer minBudget, Integer maxBudget,
                                          int current, int size) {
        // 语义搜索获取候选项目
        List<ProjectVO> semanticResults = semanticSearch(keyword, size * 3);
        Set<Long> semanticIds = semanticResults.stream()
                .map(ProjectVO::getId)
                .collect(Collectors.toSet());

        // 关键词搜索
        IPage<Project> keywordPage = keywordSearch(keyword, categoryId, minBudget, maxBudget, 1, size * 2);
        List<ProjectVO> keywordResults = keywordPage.getRecords().stream()
                .map(this::toProjectVO)
                .collect(Collectors.toList());

        // 合并结果，去重
        Map<Long, ProjectVO> mergedMap = new LinkedHashMap<>();

        // 语义搜索结果优先
        for (ProjectVO vo : semanticResults) {
            if (matchesFilters(vo, categoryId, minBudget, maxBudget)) {
                mergedMap.put(vo.getId(), vo);
            }
        }

        // 补充关键词搜索结果
        for (ProjectVO vo : keywordResults) {
            mergedMap.putIfAbsent(vo.getId(), vo);
        }

        // 分页
        List<ProjectVO> allResults = new ArrayList<>(mergedMap.values());
        int start = (current - 1) * size;
        int end = Math.min(start + size, allResults.size());

        if (start >= allResults.size()) {
            return Collections.emptyList();
        }

        return allResults.subList(start, end);
    }

    /**
     * 统计混合搜索结果总数
     */
    private long countHybridResults(String keyword, Long categoryId,
                                     Integer minBudget, Integer maxBudget) {
        // 简化处理：返回关键词搜索的总数
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Project::getDeletedAt);
        wrapper.in(Project::getStatus, List.of(1, 2));

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Project::getTitle, keyword)
                    .or()
                    .like(Project::getDescription, keyword)
            );
        }

        if (categoryId != null) {
            wrapper.eq(Project::getCategoryId, categoryId);
        }

        return projectRepository.selectCount(wrapper);
    }

    /**
     * 检查项目是否匹配筛选条件
     */
    private boolean matchesFilters(ProjectVO vo, Long categoryId,
                                    Integer minBudget, Integer maxBudget) {
        if (categoryId != null && !categoryId.equals(vo.getCategoryId())) {
            return false;
        }
        if (minBudget != null && vo.getBudgetMax() != null && vo.getBudgetMax().compareTo(BigDecimal.valueOf(minBudget)) < 0) {
            return false;
        }
        if (maxBudget != null && vo.getBudgetMin() != null && vo.getBudgetMin().compareTo(BigDecimal.valueOf(maxBudget)) > 0) {
            return false;
        }
        return true;
    }

    /**
     * 记录搜索历史
     */
    private void recordSearchHistory(String keyword, String searchType) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }

        try {
            Long userId = SecurityUtils.getCurrentUserIdOrNull();

            // 保存搜索历史
            SearchHistory history = new SearchHistory();
            history.setUserId(userId);
            history.setKeyword(keyword.trim());
            history.setSearchType(searchType);
            searchHistoryRepository.insert(history);

            // 更新热门关键词
            hotKeywordRepository.updateHotKeyword(keyword.trim());

        } catch (Exception e) {
            log.warn("Failed to record search history: {}", e.getMessage());
        }
    }

    /**
     * 构建项目内容文本（用于生成向量）
     */
    private String buildProjectContent(Project project) {
        StringBuilder sb = new StringBuilder();
        sb.append(project.getTitle()).append("\n");
        sb.append(project.getDescription()).append("\n");

        if (project.getSkillRequirements() != null) {
            sb.append("技能要求: ").append(String.join(", ", project.getSkillRequirements()));
        }

        return sb.toString();
    }

    /**
     * 向量数组转字符串（PostgreSQL vector格式）
     */
    private String vectorToString(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 获取相关建议（当搜索无结果时）
     */
    private List<String> getRelatedSuggestions(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return getHotKeywords(5);
        }

        // 返回热门关键词作为建议
        return getHotKeywords(5);
    }

    /**
     * 转换为VO
     */
    private ProjectVO toProjectVO(Project project) {
        ProjectVO vo = new ProjectVO();
        vo.setId(project.getId());
        vo.setTitle(project.getTitle());
        vo.setDescription(project.getDescription());
        vo.setCategoryId(project.getCategoryId());
        vo.setBudgetMin(project.getBudgetMin());
        vo.setBudgetMax(project.getBudgetMax());
        vo.setDeadline(project.getDeadline());
        vo.setSkillRequirements(project.getSkillRequirements());
        vo.setStatus(project.getStatus());
        vo.setViewCount(project.getViewCount());
        vo.setBidCount(project.getBidCount());
        vo.setCreatedAt(project.getCreatedAt());
        return vo;
    }
}
