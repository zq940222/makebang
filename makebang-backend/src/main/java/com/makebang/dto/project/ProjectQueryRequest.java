package com.makebang.dto.project;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 项目查询请求DTO
 */
@Data
public class ProjectQueryRequest {

    /**
     * 关键词搜索
     */
    private String keyword;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 技能要求
     */
    private List<String> skills;

    /**
     * 最低预算(下限)
     */
    private BigDecimal budgetMinFrom;

    /**
     * 最低预算(上限)
     */
    private BigDecimal budgetMinTo;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 发布者ID
     */
    private Long userId;

    /**
     * 排序字段: created_at, budget_max, view_count, bid_count
     */
    private String sortBy = "created_at";

    /**
     * 排序方向: asc, desc
     */
    private String sortOrder = "desc";

    /**
     * 当前页
     */
    private Integer current = 1;

    /**
     * 每页数量
     */
    private Integer size = 10;
}
