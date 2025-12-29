package com.makebang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.makebang.common.exception.BusinessException;
import com.makebang.common.result.ResultCode;
import com.makebang.entity.*;
import com.makebang.repository.*;
import com.makebang.service.AdminService;
import com.makebang.service.MessageService;
import com.makebang.service.UserService;
import com.makebang.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理后台服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final OrderRepository orderRepository;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final UserService userService;
    private final MessageService messageService;

    @Override
    public DashboardStatsVO getDashboardStats() {
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        // 用户统计
        Long totalUsers = userRepository.selectCount(
                new LambdaQueryWrapper<User>().isNull(User::getDeletedAt));
        Long todayNewUsers = userRepository.selectCount(
                new LambdaQueryWrapper<User>()
                        .ge(User::getCreatedAt, todayStart)
                        .isNull(User::getDeletedAt));

        // 项目统计
        Long totalProjects = projectRepository.selectCount(
                new LambdaQueryWrapper<Project>().isNull(Project::getDeletedAt));
        Long activeProjects = projectRepository.selectCount(
                new LambdaQueryWrapper<Project>()
                        .eq(Project::getStatus, Project.Status.OPEN.code)
                        .isNull(Project::getDeletedAt));

        // 订单统计
        Long totalOrders = orderRepository.selectCount(
                new LambdaQueryWrapper<Order>().isNull(Order::getDeletedAt));
        Long activeOrders = orderRepository.selectCount(
                new LambdaQueryWrapper<Order>()
                        .in(Order::getStatus, Arrays.asList(
                                Order.Status.PENDING_PAYMENT.code,
                                Order.Status.IN_PROGRESS.code,
                                Order.Status.DELIVERED.code))
                        .isNull(Order::getDeletedAt));

        // 交易额统计
        BigDecimal totalAmount = calculateTotalAmount();
        BigDecimal todayAmount = calculateTodayAmount(todayStart);
        BigDecimal platformIncome = calculatePlatformIncome();

        // 待处理事项
        Long pendingWithdrawals = transactionRepository.selectCount(
                new LambdaQueryWrapper<Transaction>()
                        .eq(Transaction::getType, Transaction.Type.WITHDRAW.getCode())
                        .eq(Transaction::getStatus, Transaction.Status.PROCESSING.getCode())
                        .isNull(Transaction::getDeletedAt));

        // 近7天趋势
        List<Map<String, Object>> orderTrend = getOrderTrend();
        List<Map<String, Object>> userTrend = getUserTrend();

        return DashboardStatsVO.builder()
                .totalUsers(totalUsers)
                .todayNewUsers(todayNewUsers)
                .totalProjects(totalProjects)
                .activeProjects(activeProjects)
                .totalOrders(totalOrders)
                .activeOrders(activeOrders)
                .totalAmount(totalAmount)
                .todayAmount(todayAmount)
                .platformIncome(platformIncome)
                .pendingProjects(0L)
                .pendingWithdrawals(pendingWithdrawals)
                .orderTrend(orderTrend)
                .userTrend(userTrend)
                .build();
    }

    @Override
    public IPage<UserVO> getUserList(String keyword, Integer userType, Integer status, int current, int size) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(User::getDeletedAt);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getPhone, keyword)
                    .or()
                    .like(User::getEmail, keyword));
        }
        if (userType != null) {
            wrapper.eq(User::getUserType, userType);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }

        wrapper.orderByDesc(User::getCreatedAt);

        Page<User> page = new Page<>(current, size);
        IPage<User> result = userRepository.selectPage(page, wrapper);

        return result.convert(userService::toVO);
    }

    @Override
    @Transactional
    public void disableUser(Long userId) {
        User user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        user.setStatus(0);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.updateById(user);

        log.info("用户已禁用: {}", userId);
    }

    @Override
    @Transactional
    public void enableUser(Long userId) {
        User user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        user.setStatus(1);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.updateById(user);

        log.info("用户已启用: {}", userId);
    }

    @Override
    @Transactional
    public void setUserRole(Long userId, Integer role) {
        User user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        user.setRole(role);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.updateById(user);

        log.info("用户角色已设置: {} -> {}", userId, role);
    }

    @Override
    public IPage<ProjectVO> getProjectList(String keyword, Integer status, int current, int size) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Project::getDeletedAt);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Project::getTitle, keyword);
        }
        if (status != null) {
            wrapper.eq(Project::getStatus, status);
        }

        wrapper.orderByDesc(Project::getCreatedAt);

        Page<Project> page = new Page<>(current, size);
        IPage<Project> result = projectRepository.selectPage(page, wrapper);

        return result.convert(this::toProjectVO);
    }

    @Override
    @Transactional
    public void approveProject(Long projectId) {
        Project project = projectRepository.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
        }

        project.setStatus(Project.Status.OPEN.code);
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.updateById(project);

        // 发送通知
        messageService.sendNotification(
                project.getUserId(),
                Notification.Type.SYSTEM.code,
                "项目审核通过",
                "您的项目「" + project.getTitle() + "」已通过审核，已上架展示",
                "project",
                projectId,
                "/projects/" + projectId);

        log.info("项目已审核通过: {}", projectId);
    }

    @Override
    @Transactional
    public void rejectProject(Long projectId, String reason) {
        Project project = projectRepository.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
        }

        project.setStatus(Project.Status.CLOSED.code);
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.updateById(project);

        // 发送通知
        messageService.sendNotification(
                project.getUserId(),
                Notification.Type.SYSTEM.code,
                "项目审核未通过",
                "您的项目「" + project.getTitle() + "」审核未通过，原因：" + reason,
                "project",
                projectId,
                "/projects/" + projectId);

        log.info("项目审核拒绝: {}, 原因: {}", projectId, reason);
    }

    @Override
    @Transactional
    public void takedownProject(Long projectId, String reason) {
        Project project = projectRepository.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
        }

        project.setStatus(Project.Status.CLOSED.code);
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.updateById(project);

        // 发送通知
        messageService.sendNotification(
                project.getUserId(),
                Notification.Type.SYSTEM.code,
                "项目已下架",
                "您的项目「" + project.getTitle() + "」已被管理员下架，原因：" + reason,
                "project",
                projectId,
                "/projects/" + projectId);

        log.info("项目已下架: {}, 原因: {}", projectId, reason);
    }

    @Override
    public IPage<OrderVO> getOrderList(Integer status, int current, int size) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Order::getDeletedAt);

        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }

        wrapper.orderByDesc(Order::getCreatedAt);

        Page<Order> page = new Page<>(current, size);
        IPage<Order> result = orderRepository.selectPage(page, wrapper);

        return result.convert(this::toOrderVO);
    }

    @Override
    public IPage<TransactionVO> getWithdrawalList(Integer status, int current, int size) {
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getType, Transaction.Type.WITHDRAW.getCode())
               .isNull(Transaction::getDeletedAt);

        if (status != null) {
            wrapper.eq(Transaction::getStatus, status);
        }

        wrapper.orderByDesc(Transaction::getCreatedAt);

        Page<Transaction> page = new Page<>(current, size);
        IPage<Transaction> result = transactionRepository.selectPage(page, wrapper);

        return result.convert(this::toTransactionVO);
    }

    @Override
    @Transactional
    public void approveWithdrawal(Long transactionId) {
        Transaction transaction = transactionRepository.selectById(transactionId);
        if (transaction == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "交易记录不存在");
        }

        if (transaction.getStatus() != Transaction.Status.PROCESSING.getCode()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该提现申请已处理");
        }

        transaction.setStatus(Transaction.Status.SUCCESS.getCode());
        transaction.setUpdatedAt(LocalDateTime.now());
        transactionRepository.updateById(transaction);

        // 发送通知
        messageService.sendNotification(
                transaction.getUserId(),
                Notification.Type.PAYMENT.code,
                "提现成功",
                "您的提现申请已通过，金额 ¥" + transaction.getAmount() + " 将在1-3个工作日内到账",
                "transaction",
                transactionId,
                "/wallet");

        log.info("提现审核通过: {}", transactionId);
    }

    @Override
    @Transactional
    public void rejectWithdrawal(Long transactionId, String reason) {
        Transaction transaction = transactionRepository.selectById(transactionId);
        if (transaction == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "交易记录不存在");
        }

        if (transaction.getStatus() != Transaction.Status.PROCESSING.getCode()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该提现申请已处理");
        }

        // 退还金额到用户余额
        walletRepository.addBalance(transaction.getWalletId(), transaction.getAmount());

        transaction.setStatus(Transaction.Status.FAILED.getCode());
        transaction.setRemark(transaction.getRemark() + " | 拒绝原因: " + reason);
        transaction.setUpdatedAt(LocalDateTime.now());
        transactionRepository.updateById(transaction);

        // 发送通知
        messageService.sendNotification(
                transaction.getUserId(),
                Notification.Type.PAYMENT.code,
                "提现申请被拒绝",
                "您的提现申请被拒绝，金额 ¥" + transaction.getAmount() + " 已退还到账户余额，原因：" + reason,
                "transaction",
                transactionId,
                "/wallet");

        log.info("提现审核拒绝: {}, 原因: {}", transactionId, reason);
    }

    // ========== 私有方法 ==========

    private BigDecimal calculateTotalAmount() {
        List<Order> completedOrders = orderRepository.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, Order.Status.COMPLETED.code)
                        .isNull(Order::getDeletedAt));
        return completedOrders.stream()
                .map(Order::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTodayAmount(LocalDateTime todayStart) {
        List<Order> todayOrders = orderRepository.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, Order.Status.COMPLETED.code)
                        .ge(Order::getCompletedAt, todayStart)
                        .isNull(Order::getDeletedAt));
        return todayOrders.stream()
                .map(Order::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculatePlatformIncome() {
        List<Transaction> feeTransactions = transactionRepository.selectList(
                new LambdaQueryWrapper<Transaction>()
                        .eq(Transaction::getType, Transaction.Type.SERVICE_FEE.getCode())
                        .eq(Transaction::getStatus, Transaction.Status.SUCCESS.getCode())
                        .isNull(Transaction::getDeletedAt));
        return feeTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<Map<String, Object>> getOrderTrend() {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);

            Long count = orderRepository.selectCount(
                    new LambdaQueryWrapper<Order>()
                            .ge(Order::getCreatedAt, dayStart)
                            .le(Order::getCreatedAt, dayEnd)
                            .isNull(Order::getDeletedAt));

            Map<String, Object> point = new HashMap<>();
            point.put("date", date.toString());
            point.put("count", count);
            trend.add(point);
        }

        return trend;
    }

    private List<Map<String, Object>> getUserTrend() {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);

            Long count = userRepository.selectCount(
                    new LambdaQueryWrapper<User>()
                            .ge(User::getCreatedAt, dayStart)
                            .le(User::getCreatedAt, dayEnd)
                            .isNull(User::getDeletedAt));

            Map<String, Object> point = new HashMap<>();
            point.put("date", date.toString());
            point.put("count", count);
            trend.add(point);
        }

        return trend;
    }

    private ProjectVO toProjectVO(Project project) {
        User user = userRepository.selectById(project.getUserId());

        return ProjectVO.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .categoryId(project.getCategoryId())
                .budgetMin(project.getBudgetMin())
                .budgetMax(project.getBudgetMax())
                .deadline(project.getDeadline())
                .status(project.getStatus())
                .statusText(Project.Status.getDesc(project.getStatus()))
                .userId(project.getUserId())
                .user(user != null ? userService.toVO(user) : null)
                .bidCount(project.getBidCount())
                .viewCount(project.getViewCount())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    private OrderVO toOrderVO(Order order) {
        User employer = userRepository.selectById(order.getEmployerId());
        User developer = userRepository.selectById(order.getDeveloperId());
        Project project = projectRepository.selectById(order.getProjectId());

        return OrderVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .projectId(order.getProjectId())
                .projectTitle(project != null ? project.getTitle() : null)
                .employerId(order.getEmployerId())
                .developerId(order.getDeveloperId())
                .employer(employer != null ? userService.toVO(employer) : null)
                .developer(developer != null ? userService.toVO(developer) : null)
                .amount(order.getAmount())
                .status(order.getStatus())
                .statusDesc(Order.Status.getDesc(order.getStatus()))
                .createdAt(order.getCreatedAt())
                .build();
    }

    private TransactionVO toTransactionVO(Transaction transaction) {
        User user = userRepository.selectById(transaction.getUserId());

        Transaction.Type type = Transaction.Type.fromCode(transaction.getType());
        String typeDesc = type != null ? type.getDesc() : "未知";

        String statusDesc = "";
        if (transaction.getStatus() == Transaction.Status.PROCESSING.getCode()) {
            statusDesc = "处理中";
        } else if (transaction.getStatus() == Transaction.Status.SUCCESS.getCode()) {
            statusDesc = "成功";
        } else if (transaction.getStatus() == Transaction.Status.FAILED.getCode()) {
            statusDesc = "失败";
        }

        return TransactionVO.builder()
                .id(transaction.getId())
                .transactionNo(transaction.getTransactionNo())
                .userId(transaction.getUserId())
                .type(transaction.getType())
                .typeDesc(typeDesc)
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .statusDesc(statusDesc)
                .remark(transaction.getRemark())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
