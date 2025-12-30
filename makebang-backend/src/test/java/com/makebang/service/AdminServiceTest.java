package com.makebang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.makebang.common.exception.BusinessException;
import com.makebang.entity.*;
import com.makebang.repository.*;
import com.makebang.service.impl.AdminServiceImpl;
import com.makebang.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AdminService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("管理后台服务测试")
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private UserService userService;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private AdminServiceImpl adminService;

    private User testUser;
    private Project testProject;
    private Order testOrder;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        // 初始化测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPhone("13800138000");
        testUser.setEmail("test@example.com");
        testUser.setUserType(1);
        testUser.setStatus(1);
        testUser.setRole(0);
        testUser.setCreatedAt(LocalDateTime.now());

        // 初始化测试项目
        testProject = new Project();
        testProject.setId(1L);
        testProject.setTitle("测试项目");
        testProject.setDescription("项目描述");
        testProject.setUserId(1L);
        testProject.setStatus(0);
        testProject.setCreatedAt(LocalDateTime.now());

        // 初始化测试订单
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderNo("ORD202412300001");
        testOrder.setProjectId(1L);
        testOrder.setEmployerId(1L);
        testOrder.setDeveloperId(2L);
        testOrder.setAmount(new BigDecimal("1000.00"));
        testOrder.setStatus(Order.Status.IN_PROGRESS.code);
        testOrder.setCreatedAt(LocalDateTime.now());

        // 初始化测试交易记录
        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setTransactionNo("TXN202412300001");
        testTransaction.setUserId(1L);
        testTransaction.setWalletId(1L);
        testTransaction.setType(Transaction.Type.WITHDRAW.getCode());
        testTransaction.setAmount(new BigDecimal("500.00"));
        testTransaction.setStatus(Transaction.Status.PROCESSING.getCode());
        testTransaction.setRemark("提现申请");
        testTransaction.setCreatedAt(LocalDateTime.now());
    }

    // ========== 仪表盘统计测试 ==========

    @Test
    @DisplayName("获取仪表盘统计数据 - 成功")
    void getDashboardStats_Success() {
        // Mock 数据
        when(userRepository.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(100L)  // totalUsers
                .thenReturn(5L);   // todayNewUsers

        when(projectRepository.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(50L)   // totalProjects
                .thenReturn(20L);  // activeProjects

        when(orderRepository.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(30L)   // totalOrders
                .thenReturn(10L);  // activeOrders

        when(orderRepository.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        when(transactionRepository.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(3L);   // pendingWithdrawals

        when(transactionRepository.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        // 执行测试
        DashboardStatsVO stats = adminService.getDashboardStats();

        // 验证结果
        assertNotNull(stats);
        assertEquals(100L, stats.getTotalUsers());
        assertEquals(5L, stats.getTodayNewUsers());
        assertEquals(50L, stats.getTotalProjects());
        assertEquals(20L, stats.getActiveProjects());
        assertEquals(30L, stats.getTotalOrders());
        assertEquals(10L, stats.getActiveOrders());
        assertEquals(3L, stats.getPendingWithdrawals());
        assertNotNull(stats.getOrderTrend());
        assertNotNull(stats.getUserTrend());
    }

    // ========== 用户管理测试 ==========

    @Test
    @DisplayName("分页查询用户列表 - 成功")
    void getUserList_Success() {
        // Mock 数据
        Page<User> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testUser));
        page.setTotal(1);

        when(userRepository.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        UserVO userVO = UserVO.builder()
                .id(1L)
                .username("testuser")
                .build();
        when(userService.toVO(any(User.class))).thenReturn(userVO);

        // 执行测试
        IPage<UserVO> result = adminService.getUserList(null, null, null, 1, 10);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertEquals("testuser", result.getRecords().get(0).getUsername());
    }

    @Test
    @DisplayName("分页查询用户列表 - 带关键字")
    void getUserList_WithKeyword() {
        // Mock 数据
        Page<User> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testUser));
        page.setTotal(1);

        when(userRepository.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);
        when(userService.toVO(any(User.class))).thenReturn(UserVO.builder().build());

        // 执行测试
        IPage<UserVO> result = adminService.getUserList("test", null, null, 1, 10);

        // 验证结果
        assertNotNull(result);
        verify(userRepository).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("禁用用户 - 成功")
    void disableUser_Success() {
        // Mock 数据
        when(userRepository.selectById(1L)).thenReturn(testUser);
        when(userRepository.updateById(any(User.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> adminService.disableUser(1L));

        // 验证结果
        verify(userRepository).updateById(argThat(user -> user.getStatus() == 0));
    }

    @Test
    @DisplayName("禁用用户 - 用户不存在")
    void disableUser_UserNotFound() {
        // Mock 数据
        when(userRepository.selectById(999L)).thenReturn(null);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> adminService.disableUser(999L));
    }

    @Test
    @DisplayName("启用用户 - 成功")
    void enableUser_Success() {
        // Mock 数据
        testUser.setStatus(0); // 禁用状态
        when(userRepository.selectById(1L)).thenReturn(testUser);
        when(userRepository.updateById(any(User.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> adminService.enableUser(1L));

        // 验证结果
        verify(userRepository).updateById(argThat(user -> user.getStatus() == 1));
    }

    @Test
    @DisplayName("设置用户角色 - 成功")
    void setUserRole_Success() {
        // Mock 数据
        when(userRepository.selectById(1L)).thenReturn(testUser);
        when(userRepository.updateById(any(User.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> adminService.setUserRole(1L, 1));

        // 验证结果
        verify(userRepository).updateById(argThat(user -> user.getRole() == 1));
    }

    // ========== 项目管理测试 ==========

    @Test
    @DisplayName("分页查询项目列表 - 成功")
    void getProjectList_Success() {
        // Mock 数据
        Page<Project> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testProject));
        page.setTotal(1);

        when(projectRepository.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);
        when(userRepository.selectById(anyLong())).thenReturn(testUser);
        when(userService.toVO(any(User.class))).thenReturn(UserVO.builder().build());

        // 执行测试
        IPage<ProjectVO> result = adminService.getProjectList(null, null, 1, 10);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("审核项目通过 - 成功")
    void approveProject_Success() {
        // Mock 数据
        when(projectRepository.selectById(1L)).thenReturn(testProject);
        when(projectRepository.updateById(any(Project.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> adminService.approveProject(1L));

        // 验证结果
        verify(projectRepository).updateById(argThat(project ->
                project.getStatus().equals(Project.Status.OPEN.code)));
        verify(messageService).sendNotification(anyLong(), anyInt(), anyString(),
                anyString(), anyString(), anyLong(), anyString());
    }

    @Test
    @DisplayName("审核项目通过 - 项目不存在")
    void approveProject_ProjectNotFound() {
        // Mock 数据
        when(projectRepository.selectById(999L)).thenReturn(null);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> adminService.approveProject(999L));
    }

    @Test
    @DisplayName("审核项目拒绝 - 成功")
    void rejectProject_Success() {
        // Mock 数据
        when(projectRepository.selectById(1L)).thenReturn(testProject);
        when(projectRepository.updateById(any(Project.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> adminService.rejectProject(1L, "内容不符合要求"));

        // 验证结果
        verify(projectRepository).updateById(argThat(project ->
                project.getStatus().equals(Project.Status.CLOSED.code)));
        verify(messageService).sendNotification(anyLong(), anyInt(), anyString(),
                contains("内容不符合要求"), anyString(), anyLong(), anyString());
    }

    @Test
    @DisplayName("下架项目 - 成功")
    void takedownProject_Success() {
        // Mock 数据
        testProject.setStatus(Project.Status.OPEN.code);
        when(projectRepository.selectById(1L)).thenReturn(testProject);
        when(projectRepository.updateById(any(Project.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> adminService.takedownProject(1L, "违规内容"));

        // 验证结果
        verify(projectRepository).updateById(argThat(project ->
                project.getStatus().equals(Project.Status.CLOSED.code)));
    }

    // ========== 订单管理测试 ==========

    @Test
    @DisplayName("分页查询订单列表 - 成功")
    void getOrderList_Success() {
        // Mock 数据
        Page<Order> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testOrder));
        page.setTotal(1);

        when(orderRepository.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);
        when(userRepository.selectById(anyLong())).thenReturn(testUser);
        when(projectRepository.selectById(anyLong())).thenReturn(testProject);
        when(userService.toVO(any(User.class))).thenReturn(UserVO.builder().build());

        // 执行测试
        IPage<OrderVO> result = adminService.getOrderList(null, 1, 10);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }

    // ========== 提现管理测试 ==========

    @Test
    @DisplayName("分页查询提现列表 - 成功")
    void getWithdrawalList_Success() {
        // Mock 数据
        Page<Transaction> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testTransaction));
        page.setTotal(1);

        when(transactionRepository.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);
        when(userRepository.selectById(anyLong())).thenReturn(testUser);

        // 执行测试
        IPage<TransactionVO> result = adminService.getWithdrawalList(null, 1, 10);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("审核提现通过 - 成功")
    void approveWithdrawal_Success() {
        // Mock 数据
        when(transactionRepository.selectById(1L)).thenReturn(testTransaction);
        when(transactionRepository.updateById(any(Transaction.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> adminService.approveWithdrawal(1L));

        // 验证结果
        verify(transactionRepository).updateById(argThat(tx ->
                tx.getStatus().equals(Transaction.Status.SUCCESS.getCode())));
        verify(messageService).sendNotification(anyLong(), anyInt(), anyString(),
                anyString(), anyString(), anyLong(), anyString());
    }

    @Test
    @DisplayName("审核提现通过 - 已处理")
    void approveWithdrawal_AlreadyProcessed() {
        // Mock 数据
        testTransaction.setStatus(Transaction.Status.SUCCESS.getCode());
        when(transactionRepository.selectById(1L)).thenReturn(testTransaction);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> adminService.approveWithdrawal(1L));
    }

    @Test
    @DisplayName("审核提现拒绝 - 成功")
    void rejectWithdrawal_Success() {
        // Mock 数据
        when(transactionRepository.selectById(1L)).thenReturn(testTransaction);
        when(transactionRepository.updateById(any(Transaction.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> adminService.rejectWithdrawal(1L, "信息不完整"));

        // 验证结果
        verify(transactionRepository).updateById(argThat(tx ->
                tx.getStatus().equals(Transaction.Status.FAILED.getCode())));
        verify(walletRepository).addBalance(anyLong(), any(BigDecimal.class));
        verify(messageService).sendNotification(anyLong(), anyInt(), anyString(),
                contains("信息不完整"), anyString(), anyLong(), anyString());
    }

    @Test
    @DisplayName("审核提现拒绝 - 交易不存在")
    void rejectWithdrawal_TransactionNotFound() {
        // Mock 数据
        when(transactionRepository.selectById(999L)).thenReturn(null);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> adminService.rejectWithdrawal(999L, "reason"));
    }
}
