package com.makebang.service;

import com.makebang.common.result.PageResult;
import com.makebang.dto.order.CreateMilestoneRequest;
import com.makebang.dto.order.ReviewMilestoneRequest;
import com.makebang.dto.order.SubmitMilestoneRequest;
import com.makebang.vo.MilestoneVO;
import com.makebang.vo.OrderVO;

import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 根据投标创建订单（接受投标时调用）
     */
    OrderVO createOrderFromBid(Long bidId);

    /**
     * 获取订单详情
     */
    OrderVO getOrderById(Long id);

    /**
     * 获取订单详情（通过订单编号）
     */
    OrderVO getOrderByNo(String orderNo);

    /**
     * 获取我的订单列表（作为雇主或开发者）
     */
    PageResult<OrderVO> getMyOrders(String role, Integer status, Integer current, Integer size);

    /**
     * 取消订单（雇主操作，仅待付款状态可取消）
     */
    void cancelOrder(Long id);

    /**
     * 确认付款（模拟支付）
     */
    OrderVO confirmPayment(Long id);

    // ========== 里程碑相关 ==========

    /**
     * 添加里程碑（雇主操作）
     */
    MilestoneVO addMilestone(Long orderId, CreateMilestoneRequest request);

    /**
     * 获取订单的里程碑列表
     */
    List<MilestoneVO> getOrderMilestones(Long orderId);

    /**
     * 开始里程碑（开发者操作）
     */
    MilestoneVO startMilestone(Long milestoneId);

    /**
     * 提交里程碑（开发者操作）
     */
    MilestoneVO submitMilestone(Long milestoneId, SubmitMilestoneRequest request);

    /**
     * 验收里程碑（雇主操作）
     */
    MilestoneVO reviewMilestone(Long milestoneId, ReviewMilestoneRequest request);

    /**
     * 完成订单（所有里程碑验收后自动调用或手动完成）
     */
    OrderVO completeOrder(Long id);
}
