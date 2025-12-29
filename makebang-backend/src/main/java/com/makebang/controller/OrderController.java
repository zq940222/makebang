package com.makebang.controller;

import com.makebang.common.result.PageResult;
import com.makebang.common.result.Result;
import com.makebang.dto.order.CreateMilestoneRequest;
import com.makebang.dto.order.ReviewMilestoneRequest;
import com.makebang.dto.order.SubmitMilestoneRequest;
import com.makebang.service.OrderService;
import com.makebang.vo.MilestoneVO;
import com.makebang.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器
 */
@Tag(name = "订单管理", description = "订单的创建、查询、里程碑管理等接口")
@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "获取订单详情")
    @GetMapping("/{id}")
    public Result<OrderVO> getOrder(@PathVariable Long id) {
        return Result.success(orderService.getOrderById(id));
    }

    @Operation(summary = "根据订单编号获取订单")
    @GetMapping("/no/{orderNo}")
    public Result<OrderVO> getOrderByNo(@PathVariable String orderNo) {
        return Result.success(orderService.getOrderByNo(orderNo));
    }

    @Operation(summary = "获取我的订单列表")
    @GetMapping("/my")
    public Result<PageResult<OrderVO>> getMyOrders(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return Result.success(orderService.getMyOrders(role, status, current, size));
    }

    @Operation(summary = "取消订单")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return Result.success();
    }

    @Operation(summary = "确认付款")
    @PostMapping("/{id}/pay")
    public Result<OrderVO> confirmPayment(@PathVariable Long id) {
        return Result.success(orderService.confirmPayment(id));
    }

    @Operation(summary = "完成订单")
    @PostMapping("/{id}/complete")
    public Result<OrderVO> completeOrder(@PathVariable Long id) {
        return Result.success(orderService.completeOrder(id));
    }

    // ========== 里程碑相关 ==========

    @Operation(summary = "获取订单的里程碑列表")
    @GetMapping("/{orderId}/milestones")
    public Result<List<MilestoneVO>> getOrderMilestones(@PathVariable Long orderId) {
        return Result.success(orderService.getOrderMilestones(orderId));
    }

    @Operation(summary = "添加里程碑")
    @PostMapping("/{orderId}/milestones")
    public Result<MilestoneVO> addMilestone(
            @PathVariable Long orderId,
            @Valid @RequestBody CreateMilestoneRequest request
    ) {
        return Result.success(orderService.addMilestone(orderId, request));
    }

    @Operation(summary = "开始里程碑")
    @PostMapping("/milestones/{milestoneId}/start")
    public Result<MilestoneVO> startMilestone(@PathVariable Long milestoneId) {
        return Result.success(orderService.startMilestone(milestoneId));
    }

    @Operation(summary = "提交里程碑")
    @PostMapping("/milestones/{milestoneId}/submit")
    public Result<MilestoneVO> submitMilestone(
            @PathVariable Long milestoneId,
            @Valid @RequestBody SubmitMilestoneRequest request
    ) {
        return Result.success(orderService.submitMilestone(milestoneId, request));
    }

    @Operation(summary = "验收里程碑")
    @PostMapping("/milestones/{milestoneId}/review")
    public Result<MilestoneVO> reviewMilestone(
            @PathVariable Long milestoneId,
            @Valid @RequestBody ReviewMilestoneRequest request
    ) {
        return Result.success(orderService.reviewMilestone(milestoneId, request));
    }
}
