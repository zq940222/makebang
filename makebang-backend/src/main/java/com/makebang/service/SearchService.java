package com.makebang.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.makebang.vo.ProjectVO;
import com.makebang.vo.SearchResultVO;
import com.makebang.vo.SearchSuggestionVO;

import java.util.List;

/**
 * 搜索服务接口
 */
public interface SearchService {

    /**
     * 综合搜索（关键词 + 语义）
     *
     * @param keyword    搜索关键词
     * @param categoryId 分类ID（可选）
     * @param minBudget  最小预算（可选）
     * @param maxBudget  最大预算（可选）
     * @param current    当前页
     * @param size       每页大小
     * @return 搜索结果
     */
    SearchResultVO search(String keyword, Long categoryId, Integer minBudget, Integer maxBudget,
                          int current, int size);

    /**
     * 语义搜索
     *
     * @param query 查询文本
     * @param limit 返回数量
     * @return 项目列表
     */
    List<ProjectVO> semanticSearch(String query, int limit);

    /**
     * 获取搜索建议
     *
     * @param prefix 输入前缀
     * @param limit  返回数量
     * @return 建议列表
     */
    SearchSuggestionVO getSuggestions(String prefix, int limit);

    /**
     * 获取热门搜索关键词
     *
     * @param limit 返回数量
     * @return 热门关键词列表
     */
    List<String> getHotKeywords(int limit);

    /**
     * 获取用户搜索历史
     *
     * @param limit 返回数量
     * @return 搜索历史列表
     */
    List<String> getUserSearchHistory(int limit);

    /**
     * 清除用户搜索历史
     */
    void clearUserSearchHistory();

    /**
     * 为项目生成向量（异步）
     *
     * @param projectId 项目ID
     */
    void generateProjectEmbedding(Long projectId);

    /**
     * 批量生成项目向量（定时任务）
     *
     * @param batchSize 批量大小
     */
    void batchGenerateEmbeddings(int batchSize);

    /**
     * 刷新项目向量（内容变化时调用）
     *
     * @param projectId 项目ID
     */
    void refreshProjectEmbedding(Long projectId);
}
