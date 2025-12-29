package com.makebang.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.makebang.vo.*;

/**
 * 管理后台服务接口
 */
public interface AdminService {

    // ========== 统计相关 ==========

    /**
     * 获取仪表盘统计数据
     */
    DashboardStatsVO getDashboardStats();

    // ========== 用户管理 ==========

    /**
     * 分页查询用户列表
     */
    IPage<UserVO> getUserList(String keyword, Integer userType, Integer status, int current, int size);

    /**
     * 禁用用户
     */
    void disableUser(Long userId);

    /**
     * 启用用户
     */
    void enableUser(Long userId);

    /**
     * 设置用户角色
     */
    void setUserRole(Long userId, Integer role);

    // ========== 项目管理 ==========

    /**
     * 分页查询项目列表
     */
    IPage<ProjectVO> getProjectList(String keyword, Integer status, int current, int size);

    /**
     * 审核项目（通过）
     */
    void approveProject(Long projectId);

    /**
     * 审核项目（拒绝）
     */
    void rejectProject(Long projectId, String reason);

    /**
     * 下架项目
     */
    void takedownProject(Long projectId, String reason);

    // ========== 订单管理 ==========

    /**
     * 分页查询订单列表
     */
    IPage<OrderVO> getOrderList(Integer status, int current, int size);

    // ========== 提现管理 ==========

    /**
     * 分页查询提现申请列表
     */
    IPage<TransactionVO> getWithdrawalList(Integer status, int current, int size);

    /**
     * 审核提现（通过）
     */
    void approveWithdrawal(Long transactionId);

    /**
     * 审核提现（拒绝）
     */
    void rejectWithdrawal(Long transactionId, String reason);
}
