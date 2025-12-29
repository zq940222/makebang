package com.makebang.controller;

import com.makebang.common.result.Result;
import com.makebang.service.SearchService;
import com.makebang.vo.ProjectVO;
import com.makebang.vo.SearchResultVO;
import com.makebang.vo.SearchSuggestionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 搜索控制器
 */
@Tag(name = "搜索", description = "项目搜索相关接口")
@RestController
@RequestMapping("/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "综合搜索")
    @GetMapping
    public Result<SearchResultVO> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer minBudget,
            @RequestParam(required = false) Integer maxBudget,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(searchService.search(keyword, categoryId, minBudget, maxBudget, current, size));
    }

    @Operation(summary = "语义搜索")
    @GetMapping("/semantic")
    public Result<List<ProjectVO>> semanticSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(searchService.semanticSearch(query, limit));
    }

    @Operation(summary = "获取搜索建议")
    @GetMapping("/suggestions")
    public Result<SearchSuggestionVO> getSuggestions(
            @RequestParam(required = false) String prefix,
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(searchService.getSuggestions(prefix, limit));
    }

    @Operation(summary = "获取热门搜索关键词")
    @GetMapping("/hot-keywords")
    public Result<List<String>> getHotKeywords(
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(searchService.getHotKeywords(limit));
    }

    @Operation(summary = "获取用户搜索历史")
    @GetMapping("/history")
    public Result<List<String>> getUserSearchHistory(
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(searchService.getUserSearchHistory(limit));
    }

    @Operation(summary = "清除用户搜索历史")
    @DeleteMapping("/history")
    public Result<Void> clearUserSearchHistory() {
        searchService.clearUserSearchHistory();
        return Result.success();
    }
}
