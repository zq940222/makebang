package com.makebang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.makebang.common.exception.BusinessException;
import com.makebang.common.result.PageResult;
import com.makebang.common.result.ResultCode;
import com.makebang.dto.order.CreateMilestoneRequest;
import com.makebang.dto.order.ReviewMilestoneRequest;
import com.makebang.dto.order.SubmitMilestoneRequest;
import com.makebang.entity.*;
import com.makebang.repository.*;
import com.makebang.service.OrderService;
import com.makebang.service.UserService;
import com.makebang.service.WalletService;
import com.makebang.vo.MilestoneVO;
import com.makebang.vo.OrderVO;
import com.makebang.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final MilestoneRepository milestoneRepository;
    private final BidRepository bidRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final WalletService walletService;

    @Override
    @Transactional
    public OrderVO createOrderFromBid(Long bidId) {
        Bid bid = bidRepository.selectById(bidId);
        if (bid == null || bid.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "投标不存在");
        }

        if (bid.getStatus() != Bid.Status.ACCEPTED.code) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "只有已接受的投标才能创建订单");
        }

        Project project = projectRepository.selectById(bid.getProjectId());
        if (project == null) {
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
        }

        // 检查是否已存在订单
        Order existingOrder = orderRepository.findByProjectId(project.getId());
        if (existingOrder != null) {
            throw new BusinessException(ResultCode.CONFLICT, "该项目已有订单");
        }

        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setProjectId(project.getId());
        order.setBidId(bidId);
        order.setEmployerId(project.getUserId());
        order.setDeveloperId(bid.getDeveloperId());
        order.setAmount(bid.getProposedPrice());
        order.setStatus(Order.Status.PENDING_PAYMENT.code);
        order.setMilestoneCount(1);
        order.setDeadline(project.getDeadline());

        orderRepository.insert(order);

        // 创建默认里程碑
        Milestone milestone = new Milestone();
        milestone.setOrderId(order.getId());
        milestone.setTitle("项目交付");
        milestone.setDescription("完成全部项目需求并交付");
        milestone.setAmount(bid.getProposedPrice());
        milestone.setSequence(1);
        milestone.setStatus(Milestone.Status.PENDING.code);
        milestone.setDueDate(project.getDeadline());

        milestoneRepository.insert(milestone);

        log.info("订单创建成功: {}", order.getOrderNo());

        return toVO(order);
    }

    @Override
    public OrderVO getOrderById(Long id) {
        Order order = getOrderEntity(id);
        return toVO(order);
    }

    @Override
    public OrderVO getOrderByNo(String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        return toVO(order);
    }

    @Override
    public PageResult<OrderVO> getMyOrders(String role, Integer status, Integer current, Integer size) {
        UserVO currentUser = userService.getCurrentUser();

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        if ("employer".equals(role)) {
            wrapper.eq(Order::getEmployerId, currentUser.getId());
        } else if ("developer".equals(role)) {
            wrapper.eq(Order::getDeveloperId, currentUser.getId());
        } else {
            // 默认查询所有相关订单
            wrapper.and(w -> w
                    .eq(Order::getEmployerId, currentUser.getId())
                    .or()
                    .eq(Order::getDeveloperId, currentUser.getId()));
        }

        wrapper.isNull(Order::getDeletedAt)
                .orderByDesc(Order::getCreatedAt);

        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }

        Page<Order> page = new Page<>(current, size);
        IPage<Order> result = orderRepository.selectPage(page, wrapper);

        List<OrderVO> voList = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return PageResult.of(result, voList);
    }

    @Override
    @Transactional
    public void cancelOrder(Long id) {
        Order order = getOrderEntity(id);
        UserVO currentUser = userService.getCurrentUser();

        // 验证权限
        if (!order.getEmployerId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权取消此订单");
        }

        // 只有待付款或进行中状态可以取消
        if (order.getStatus() != Order.Status.PENDING_PAYMENT.code &&
            order.getStatus() != Order.Status.IN_PROGRESS.code) {
            throw new BusinessException(ResultCode.ORDER_CANNOT_CANCEL);
        }

        // 如果已付款，需要退还托管资金
        if (order.getStatus() == Order.Status.IN_PROGRESS.code) {
            walletService.refundEscrow(order.getId());
        }

        order.setStatus(Order.Status.CANCELLED.code);
        orderRepository.updateById(order);

        // 恢复项目状态为开放
        Project project = projectRepository.selectById(order.getProjectId());
        if (project != null) {
            project.setStatus(Project.Status.OPEN.code);
            projectRepository.updateById(project);
        }

        log.info("订单已取消: {}", order.getOrderNo());
    }

    @Override
    @Transactional
    public OrderVO confirmPayment(Long id) {
        Order order = getOrderEntity(id);
        UserVO currentUser = userService.getCurrentUser();

        // 验证权限
        if (!order.getEmployerId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此订单");
        }

        if (order.getStatus() != Order.Status.PENDING_PAYMENT.code) {
            throw new BusinessException(ResultCode.ORDER_ALREADY_PAID);
        }

        // 托管支付（冻结雇主资金）
        walletService.escrowPayment(order.getId(), order.getAmount());

        // 更新订单状态
        order.setStatus(Order.Status.IN_PROGRESS.code);
        order.setStartedAt(LocalDateTime.now());
        orderRepository.updateById(order);

        // 激活第一个里程碑
        Milestone firstMilestone = milestoneRepository.findNextPendingByOrderId(order.getId());
        if (firstMilestone != null) {
            firstMilestone.setStatus(Milestone.Status.IN_PROGRESS.code);
            milestoneRepository.updateById(firstMilestone);
        }

        log.info("订单付款确认: {}", order.getOrderNo());

        return toVO(order);
    }

    @Override
    @Transactional
    public MilestoneVO addMilestone(Long orderId, CreateMilestoneRequest request) {
        Order order = getOrderEntity(orderId);
        UserVO currentUser = userService.getCurrentUser();

        // 验证权限
        if (!order.getEmployerId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此订单");
        }

        // 只有进行中的订单可以添加里程碑
        if (order.getStatus() != Order.Status.IN_PROGRESS.code) {
            throw new BusinessException(ResultCode.INVALID_ORDER_STATUS, "当前订单状态不允许添加里程碑");
        }

        // 获取当前最大序号
        List<Milestone> existingMilestones = milestoneRepository.findByOrderId(orderId);
        int maxSequence = existingMilestones.stream()
                .mapToInt(Milestone::getSequence)
                .max()
                .orElse(0);

        Milestone milestone = new Milestone();
        milestone.setOrderId(orderId);
        milestone.setTitle(request.getTitle());
        milestone.setDescription(request.getDescription());
        milestone.setAmount(request.getAmount());
        milestone.setSequence(maxSequence + 1);
        milestone.setStatus(Milestone.Status.PENDING.code);
        milestone.setDueDate(request.getDueDate());

        milestoneRepository.insert(milestone);

        // 更新订单里程碑数量
        order.setMilestoneCount(order.getMilestoneCount() + 1);
        orderRepository.updateById(order);

        return toMilestoneVO(milestone);
    }

    @Override
    public List<MilestoneVO> getOrderMilestones(Long orderId) {
        List<Milestone> milestones = milestoneRepository.findByOrderId(orderId);
        return milestones.stream()
                .map(this::toMilestoneVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MilestoneVO startMilestone(Long milestoneId) {
        Milestone milestone = getMilestoneEntity(milestoneId);
        Order order = getOrderEntity(milestone.getOrderId());
        UserVO currentUser = userService.getCurrentUser();

        // 验证权限
        if (!order.getDeveloperId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此里程碑");
        }

        if (milestone.getStatus() != Milestone.Status.PENDING.code) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该里程碑不是待开始状态");
        }

        milestone.setStatus(Milestone.Status.IN_PROGRESS.code);
        milestoneRepository.updateById(milestone);

        return toMilestoneVO(milestone);
    }

    @Override
    @Transactional
    public MilestoneVO submitMilestone(Long milestoneId, SubmitMilestoneRequest request) {
        Milestone milestone = getMilestoneEntity(milestoneId);
        Order order = getOrderEntity(milestone.getOrderId());
        UserVO currentUser = userService.getCurrentUser();

        // 验证权限
        if (!order.getDeveloperId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此里程碑");
        }

        if (milestone.getStatus() != Milestone.Status.IN_PROGRESS.code &&
            milestone.getStatus() != Milestone.Status.REJECTED.code) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许提交");
        }

        milestone.setStatus(Milestone.Status.SUBMITTED.code);
        milestone.setSubmitNote(request.getSubmitNote());
        milestoneRepository.updateById(milestone);

        // 更新订单状态为已交付
        order.setStatus(Order.Status.DELIVERED.code);
        orderRepository.updateById(order);

        log.info("里程碑已提交: {}", milestoneId);

        return toMilestoneVO(milestone);
    }

    @Override
    @Transactional
    public MilestoneVO reviewMilestone(Long milestoneId, ReviewMilestoneRequest request) {
        Milestone milestone = getMilestoneEntity(milestoneId);
        Order order = getOrderEntity(milestone.getOrderId());
        UserVO currentUser = userService.getCurrentUser();

        // 验证权限
        if (!order.getEmployerId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此里程碑");
        }

        if (milestone.getStatus() != Milestone.Status.SUBMITTED.code) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该里程碑不是已提交状态");
        }

        milestone.setReviewNote(request.getReviewNote());

        if (Boolean.TRUE.equals(request.getApproved())) {
            milestone.setStatus(Milestone.Status.APPROVED.code);
            milestone.setCompletedAt(LocalDateTime.now());

            // 释放该里程碑的托管资金给开发者
            walletService.releaseMilestonePayment(milestoneId);

            // 检查是否所有里程碑都已完成
            int completedCount = milestoneRepository.countCompletedByOrderId(order.getId()) + 1;
            if (completedCount >= order.getMilestoneCount()) {
                // 所有里程碑完成，完成订单
                completeOrder(order.getId());
            } else {
                // 激活下一个里程碑
                order.setStatus(Order.Status.IN_PROGRESS.code);
                orderRepository.updateById(order);

                Milestone nextMilestone = milestoneRepository.findNextPendingByOrderId(order.getId());
                if (nextMilestone != null) {
                    nextMilestone.setStatus(Milestone.Status.IN_PROGRESS.code);
                    milestoneRepository.updateById(nextMilestone);
                }
            }
            log.info("里程碑验收通过: {}", milestoneId);
        } else {
            milestone.setStatus(Milestone.Status.REJECTED.code);
            order.setStatus(Order.Status.IN_PROGRESS.code);
            orderRepository.updateById(order);
            log.info("里程碑验收驳回: {}", milestoneId);
        }

        milestoneRepository.updateById(milestone);

        return toMilestoneVO(milestone);
    }

    @Override
    @Transactional
    public OrderVO completeOrder(Long id) {
        Order order = getOrderEntity(id);

        order.setStatus(Order.Status.COMPLETED.code);
        order.setCompletedAt(LocalDateTime.now());
        orderRepository.updateById(order);

        // 更新项目状态为已完成
        Project project = projectRepository.selectById(order.getProjectId());
        if (project != null) {
            project.setStatus(Project.Status.COMPLETED.code);
            projectRepository.updateById(project);
        }

        log.info("订单完成: {}", order.getOrderNo());

        return toVO(order);
    }

    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    /**
     * 获取订单实体
     */
    private Order getOrderEntity(Long id) {
        Order order = orderRepository.selectById(id);
        if (order == null || order.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        return order;
    }

    /**
     * 获取里程碑实体
     */
    private Milestone getMilestoneEntity(Long id) {
        Milestone milestone = milestoneRepository.selectById(id);
        if (milestone == null || milestone.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "里程碑不存在");
        }
        return milestone;
    }

    /**
     * 订单实体转VO
     */
    private OrderVO toVO(Order order) {
        if (order == null) return null;

        OrderVO vo = OrderVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .projectId(order.getProjectId())
                .bidId(order.getBidId())
                .employerId(order.getEmployerId())
                .developerId(order.getDeveloperId())
                .amount(order.getAmount())
                .status(order.getStatus())
                .statusDesc(Order.Status.getDesc(order.getStatus()))
                .milestoneCount(order.getMilestoneCount())
                .startedAt(order.getStartedAt())
                .deadline(order.getDeadline())
                .completedAt(order.getCompletedAt())
                .remark(order.getRemark())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();

        // 加载项目标题
        Project project = projectRepository.selectById(order.getProjectId());
        if (project != null) {
            vo.setProjectTitle(project.getTitle());
        }

        // 加载用户信息
        User employer = userRepository.selectById(order.getEmployerId());
        if (employer != null) {
            vo.setEmployer(userService.toVO(employer));
        }

        User developer = userRepository.selectById(order.getDeveloperId());
        if (developer != null) {
            vo.setDeveloper(userService.toVO(developer));
        }

        // 加载里程碑
        List<Milestone> milestones = milestoneRepository.findByOrderId(order.getId());
        vo.setMilestones(milestones.stream().map(this::toMilestoneVO).collect(Collectors.toList()));

        // 计算进度
        int completed = milestoneRepository.countCompletedByOrderId(order.getId());
        int total = order.getMilestoneCount();
        vo.setProgress(total > 0 ? (completed * 100 / total) : 0);

        return vo;
    }

    /**
     * 里程碑实体转VO
     */
    private MilestoneVO toMilestoneVO(Milestone milestone) {
        if (milestone == null) return null;

        return MilestoneVO.builder()
                .id(milestone.getId())
                .orderId(milestone.getOrderId())
                .title(milestone.getTitle())
                .description(milestone.getDescription())
                .amount(milestone.getAmount())
                .sequence(milestone.getSequence())
                .status(milestone.getStatus())
                .statusDesc(Milestone.Status.getDesc(milestone.getStatus()))
                .dueDate(milestone.getDueDate())
                .completedAt(milestone.getCompletedAt())
                .submitNote(milestone.getSubmitNote())
                .reviewNote(milestone.getReviewNote())
                .createdAt(milestone.getCreatedAt())
                .updatedAt(milestone.getUpdatedAt())
                .build();
    }
}
