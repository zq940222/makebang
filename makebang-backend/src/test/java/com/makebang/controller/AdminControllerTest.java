package com.makebang.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.makebang.dto.admin.RejectRequest;
import com.makebang.dto.admin.SetRoleRequest;
import com.makebang.service.AdminService;
import com.makebang.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AdminController 单元测试
 */
@WebMvcTest(AdminController.class)
@DisplayName("管理后台控制器测试")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminService adminService;

    private DashboardStatsVO dashboardStats;
    private UserVO testUserVO;
    private ProjectVO testProjectVO;
    private OrderVO testOrderVO;
    private TransactionVO testTransactionVO;

    @BeforeEach
    void setUp() {
        // 初始化仪表盘统计数据
        List<java.util.Map<String, Object>> trend = new ArrayList<>();
        java.util.Map<String, Object> point = new HashMap<>();
        point.put("date", "2024-12-30");
        point.put("count", 10);
        trend.add(point);

        dashboardStats = DashboardStatsVO.builder()
                .totalUsers(100L)
                .todayNewUsers(5L)
                .totalProjects(50L)
                .activeProjects(20L)
                .totalOrders(30L)
                .activeOrders(10L)
                .totalAmount(new BigDecimal("50000.00"))
                .todayAmount(new BigDecimal("2000.00"))
                .platformIncome(new BigDecimal("5000.00"))
                .pendingProjects(3L)
                .pendingWithdrawals(2L)
                .orderTrend(trend)
                .userTrend(trend)
                .build();

        // 初始化用户VO
        testUserVO = UserVO.builder()
                .id(1L)
                .username("testuser")
                .phone("138****8000")
                .email("te***@example.com")
                .userType(1)
                .status(1)
                .role(0)
                .createdAt(LocalDateTime.now())
                .build();

        // 初始化项目VO
        testProjectVO = ProjectVO.builder()
                .id(1L)
                .title("测试项目")
                .description("项目描述")
                .status(1)
                .createdAt(LocalDateTime.now())
                .build();

        // 初始化订单VO
        testOrderVO = OrderVO.builder()
                .id(1L)
                .orderNo("ORD202412300001")
                .projectId(1L)
                .projectTitle("测试项目")
                .amount(new BigDecimal("1000.00"))
                .status(2)
                .createdAt(LocalDateTime.now())
                .build();

        // 初始化交易VO
        testTransactionVO = TransactionVO.builder()
                .id(1L)
                .transactionNo("TXN202412300001")
                .userId(1L)
                .type(3)
                .typeDesc("提现")
                .amount(new BigDecimal("500.00"))
                .status(0)
                .statusDesc("处理中")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ========== 仪表盘统计测试 ==========

    @Test
    @DisplayName("获取仪表盘统计数据 - 成功")
    @WithMockUser(roles = "ADMIN")
    void getDashboardStats_Success() throws Exception {
        when(adminService.getDashboardStats()).thenReturn(dashboardStats);

        mockMvc.perform(get("/v1/admin/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalUsers").value(100))
                .andExpect(jsonPath("$.data.todayNewUsers").value(5))
                .andExpect(jsonPath("$.data.totalProjects").value(50));

        verify(adminService).getDashboardStats();
    }

    @Test
    @DisplayName("获取仪表盘统计数据 - 未授权")
    void getDashboardStats_Unauthorized() throws Exception {
        mockMvc.perform(get("/v1/admin/dashboard/stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("获取仪表盘统计数据 - 普通用户禁止访问")
    @WithMockUser(roles = "USER")
    void getDashboardStats_Forbidden() throws Exception {
        mockMvc.perform(get("/v1/admin/dashboard/stats"))
                .andExpect(status().isForbidden());
    }

    // ========== 用户管理测试 ==========

    @Test
    @DisplayName("分页查询用户列表 - 成功")
    @WithMockUser(roles = "ADMIN")
    void getUserList_Success() throws Exception {
        Page<UserVO> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testUserVO));
        page.setTotal(1);

        when(adminService.getUserList(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/v1/admin/users")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].username").value("testuser"));
    }

    @Test
    @DisplayName("分页查询用户列表 - 带筛选条件")
    @WithMockUser(roles = "ADMIN")
    void getUserList_WithFilters() throws Exception {
        Page<UserVO> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testUserVO));
        page.setTotal(1);

        when(adminService.getUserList(eq("test"), eq(1), eq(1), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/v1/admin/users")
                        .param("keyword", "test")
                        .param("userType", "1")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(adminService).getUserList(eq("test"), eq(1), eq(1), anyInt(), anyInt());
    }

    @Test
    @DisplayName("禁用用户 - 成功")
    @WithMockUser(roles = "ADMIN")
    void disableUser_Success() throws Exception {
        doNothing().when(adminService).disableUser(1L);

        mockMvc.perform(post("/v1/admin/users/1/disable")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(adminService).disableUser(1L);
    }

    @Test
    @DisplayName("启用用户 - 成功")
    @WithMockUser(roles = "ADMIN")
    void enableUser_Success() throws Exception {
        doNothing().when(adminService).enableUser(1L);

        mockMvc.perform(post("/v1/admin/users/1/enable")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(adminService).enableUser(1L);
    }

    @Test
    @DisplayName("设置用户角色 - 成功 (超级管理员)")
    @WithMockUser(roles = "SUPER_ADMIN")
    void setUserRole_Success() throws Exception {
        SetRoleRequest request = new SetRoleRequest();
        request.setRole(1);

        doNothing().when(adminService).setUserRole(1L, 1);

        mockMvc.perform(post("/v1/admin/users/1/role")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(adminService).setUserRole(1L, 1);
    }

    @Test
    @DisplayName("设置用户角色 - 普通管理员禁止")
    @WithMockUser(roles = "ADMIN")
    void setUserRole_ForbiddenForAdmin() throws Exception {
        SetRoleRequest request = new SetRoleRequest();
        request.setRole(1);

        mockMvc.perform(post("/v1/admin/users/1/role")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ========== 项目管理测试 ==========

    @Test
    @DisplayName("分页查询项目列表 - 成功")
    @WithMockUser(roles = "ADMIN")
    void getProjectList_Success() throws Exception {
        Page<ProjectVO> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testProjectVO));
        page.setTotal(1);

        when(adminService.getProjectList(any(), any(), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/v1/admin/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].title").value("测试项目"));
    }

    @Test
    @DisplayName("审核项目通过 - 成功")
    @WithMockUser(roles = "ADMIN")
    void approveProject_Success() throws Exception {
        doNothing().when(adminService).approveProject(1L);

        mockMvc.perform(post("/v1/admin/projects/1/approve")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(adminService).approveProject(1L);
    }

    @Test
    @DisplayName("审核项目拒绝 - 成功")
    @WithMockUser(roles = "ADMIN")
    void rejectProject_Success() throws Exception {
        RejectRequest request = new RejectRequest();
        request.setReason("内容不符合要求");

        doNothing().when(adminService).rejectProject(1L, "内容不符合要求");

        mockMvc.perform(post("/v1/admin/projects/1/reject")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(adminService).rejectProject(1L, "内容不符合要求");
    }

    @Test
    @DisplayName("下架项目 - 成功")
    @WithMockUser(roles = "ADMIN")
    void takedownProject_Success() throws Exception {
        RejectRequest request = new RejectRequest();
        request.setReason("违规内容");

        doNothing().when(adminService).takedownProject(1L, "违规内容");

        mockMvc.perform(post("/v1/admin/projects/1/takedown")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(adminService).takedownProject(1L, "违规内容");
    }

    // ========== 订单管理测试 ==========

    @Test
    @DisplayName("分页查询订单列表 - 成功")
    @WithMockUser(roles = "ADMIN")
    void getOrderList_Success() throws Exception {
        Page<OrderVO> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testOrderVO));
        page.setTotal(1);

        when(adminService.getOrderList(any(), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/v1/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].orderNo").value("ORD202412300001"));
    }

    // ========== 提现管理测试 ==========

    @Test
    @DisplayName("分页查询提现列表 - 成功")
    @WithMockUser(roles = "ADMIN")
    void getWithdrawalList_Success() throws Exception {
        Page<TransactionVO> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testTransactionVO));
        page.setTotal(1);

        when(adminService.getWithdrawalList(any(), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/v1/admin/withdrawals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].transactionNo").value("TXN202412300001"));
    }

    @Test
    @DisplayName("审核提现通过 - 成功")
    @WithMockUser(roles = "ADMIN")
    void approveWithdrawal_Success() throws Exception {
        doNothing().when(adminService).approveWithdrawal(1L);

        mockMvc.perform(post("/v1/admin/withdrawals/1/approve")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(adminService).approveWithdrawal(1L);
    }

    @Test
    @DisplayName("审核提现拒绝 - 成功")
    @WithMockUser(roles = "ADMIN")
    void rejectWithdrawal_Success() throws Exception {
        RejectRequest request = new RejectRequest();
        request.setReason("信息不完整");

        doNothing().when(adminService).rejectWithdrawal(1L, "信息不完整");

        mockMvc.perform(post("/v1/admin/withdrawals/1/reject")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(adminService).rejectWithdrawal(1L, "信息不完整");
    }
}
