package com.makebang.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 搜索结果VO
 */
@Data
@Builder
public class SearchResultVO {

    /**
     * 项目列表
     */
    private List<ProjectVO> projects;

    /**
     * 总数
     */
    private Long total;

    /**
     * 当前页
     */
    private Integer current;

    /**
     * 每页大小
     */
    private Integer size;

    /**
     * 总页数
     */
    private Integer pages;

    /**
     * 搜索类型: keyword/semantic/hybrid
     */
    private String searchType;

    /**
     * 搜索耗时（毫秒）
     */
    private Long costTime;

    /**
     * 相关建议（如果没有结果）
     */
    private List<String> suggestions;
}
