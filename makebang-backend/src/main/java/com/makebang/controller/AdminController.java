package com.makebang.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.makebang.common.result.Result;
import com.makebang.dto.admin.SetRoleRequest;
import com.makebang.dto.admin.RejectRequest;
import com.makebang.service.AdminService;
import com.makebang.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 管理后台控制器
 */
@Tag(name = "管理后台", description = "管理后台相关接口")
@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // ========== 统计相关 ==========

    @Operation(summary = "获取仪表盘统计数据")
    @GetMapping("/dashboard/stats")
    public Result<DashboardStatsVO> getDashboardStats() {
        return Result.success(adminService.getDashboardStats());
    }

    // ========== 用户管理 ==========

    @Operation(summary = "分页查询用户列表")
    @GetMapping("/users")
    public Result<IPage<UserVO>> getUserList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer userType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(adminService.getUserList(keyword, userType, status, current, size));
    }

    @Operation(summary = "禁用用户")
    @PostMapping("/users/{userId}/disable")
    public Result<Void> disableUser(@PathVariable Long userId) {
        adminService.disableUser(userId);
        return Result.success();
    }

    @Operation(summary = "启用用户")
    @PostMapping("/users/{userId}/enable")
    public Result<Void> enableUser(@PathVariable Long userId) {
        adminService.enableUser(userId);
        return Result.success();
    }

    @Operation(summary = "设置用户角色")
    @PostMapping("/users/{userId}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> setUserRole(@PathVariable Long userId, @Valid @RequestBody SetRoleRequest request) {
        adminService.setUserRole(userId, request.getRole());
        return Result.success();
    }

    // ========== 项目管理 ==========

    @Operation(summary = "分页查询项目列表")
    @GetMapping("/projects")
    public Result<IPage<ProjectVO>> getProjectList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(adminService.getProjectList(keyword, status, current, size));
    }

    @Operation(summary = "审核项目通过")
    @PostMapping("/projects/{projectId}/approve")
    public Result<Void> approveProject(@PathVariable Long projectId) {
        adminService.approveProject(projectId);
        return Result.success();
    }

    @Operation(summary = "审核项目拒绝")
    @PostMapping("/projects/{projectId}/reject")
    public Result<Void> rejectProject(@PathVariable Long projectId, @Valid @RequestBody RejectRequest request) {
        adminService.rejectProject(projectId, request.getReason());
        return Result.success();
    }

    @Operation(summary = "下架项目")
    @PostMapping("/projects/{projectId}/takedown")
    public Result<Void> takedownProject(@PathVariable Long projectId, @Valid @RequestBody RejectRequest request) {
        adminService.takedownProject(projectId, request.getReason());
        return Result.success();
    }

    // ========== 订单管理 ==========

    @Operation(summary = "分页查询订单列表")
    @GetMapping("/orders")
    public Result<IPage<OrderVO>> getOrderList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(adminService.getOrderList(status, current, size));
    }

    // ========== 提现管理 ==========

    @Operation(summary = "分页查询提现申请列表")
    @GetMapping("/withdrawals")
    public Result<IPage<TransactionVO>> getWithdrawalList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(adminService.getWithdrawalList(status, current, size));
    }

    @Operation(summary = "审核提现通过")
    @PostMapping("/withdrawals/{transactionId}/approve")
    public Result<Void> approveWithdrawal(@PathVariable Long transactionId) {
        adminService.approveWithdrawal(transactionId);
        return Result.success();
    }

    @Operation(summary = "审核提现拒绝")
    @PostMapping("/withdrawals/{transactionId}/reject")
    public Result<Void> rejectWithdrawal(@PathVariable Long transactionId, @Valid @RequestBody RejectRequest request) {
        adminService.rejectWithdrawal(transactionId, request.getReason());
        return Result.success();
    }
}
