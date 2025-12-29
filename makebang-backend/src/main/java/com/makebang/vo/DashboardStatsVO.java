package com.makebang.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘统计VO
 */
@Data
@Builder
public class DashboardStatsVO {

    /**
     * 总用户数
     */
    private Long totalUsers;

    /**
     * 今日新增用户
     */
    private Long todayNewUsers;

    /**
     * 总项目数
     */
    private Long totalProjects;

    /**
     * 进行中的项目数
     */
    private Long activeProjects;

    /**
     * 总订单数
     */
    private Long totalOrders;

    /**
     * 进行中订单数
     */
    private Long activeOrders;

    /**
     * 总交易额
     */
    private BigDecimal totalAmount;

    /**
     * 今日交易额
     */
    private BigDecimal todayAmount;

    /**
     * 平台总收入（服务费）
     */
    private BigDecimal platformIncome;

    /**
     * 待审核项目数
     */
    private Long pendingProjects;

    /**
     * 待处理提现数
     */
    private Long pendingWithdrawals;

    /**
     * 最近7天订单趋势
     */
    private List<Map<String, Object>> orderTrend;

    /**
     * 最近7天用户趋势
     */
    private List<Map<String, Object>> userTrend;
}
