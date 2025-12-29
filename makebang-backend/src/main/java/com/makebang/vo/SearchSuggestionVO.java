package com.makebang.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 搜索建议VO
 */
@Data
@Builder
public class SearchSuggestionVO {

    /**
     * 热门搜索
     */
    private List<String> hotKeywords;

    /**
     * 用户历史搜索
     */
    private List<String> historyKeywords;

    /**
     * 联想词
     */
    private List<String> completions;

    /**
     * 相关技能标签
     */
    private List<String> relatedSkills;
}
