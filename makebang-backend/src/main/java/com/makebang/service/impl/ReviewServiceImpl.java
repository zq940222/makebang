package com.makebang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.makebang.common.exception.BusinessException;
import com.makebang.common.result.ResultCode;
import com.makebang.dto.review.CreateReviewRequest;
import com.makebang.dto.review.ReplyReviewRequest;
import com.makebang.entity.Order;
import com.makebang.entity.Project;
import com.makebang.entity.Review;
import com.makebang.entity.User;
import com.makebang.repository.OrderRepository;
import com.makebang.repository.ProjectRepository;
import com.makebang.repository.ReviewRepository;
import com.makebang.repository.UserRepository;
import com.makebang.service.MessageService;
import com.makebang.service.ReviewService;
import com.makebang.service.UserService;
import com.makebang.vo.ReviewVO;
import com.makebang.vo.UserStatsVO;
import com.makebang.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评价服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final MessageService messageService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ReviewVO createReview(CreateReviewRequest request) {
        UserVO currentUser = userService.getCurrentUser();
        Long orderId = request.getOrderId();

        // 获取订单
        Order order = orderRepository.selectById(orderId);
        if (order == null || order.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 检查订单状态必须是已完成
        if (order.getStatus() != Order.Status.COMPLETED.code) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "只有已完成的订单才能评价");
        }

        // 检查用户是否是订单参与者
        boolean isEmployer = order.getEmployerId().equals(currentUser.getId());
        boolean isDeveloper = order.getDeveloperId().equals(currentUser.getId());

        if (!isEmployer && !isDeveloper) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您不是该订单的参与者");
        }

        // 检查是否已评价
        Review existingReview = reviewRepository.findByOrderAndReviewer(orderId, currentUser.getId());
        if (existingReview != null) {
            throw new BusinessException(ResultCode.CONFLICT, "您已评价过该订单");
        }

        // 确定评价类型和被评价者
        int reviewType = isEmployer ? Review.Type.EMPLOYER_TO_DEVELOPER.code : Review.Type.DEVELOPER_TO_EMPLOYER.code;
        Long revieweeId = isEmployer ? order.getDeveloperId() : order.getEmployerId();

        // 创建评价
        Review review = new Review();
        review.setOrderId(orderId);
        review.setProjectId(order.getProjectId());
        review.setReviewerId(currentUser.getId());
        review.setRevieweeId(revieweeId);
        review.setType(reviewType);
        review.setRating(request.getRating());
        review.setSkillRating(request.getSkillRating() != null ? request.getSkillRating() : request.getRating());
        review.setCommunicationRating(request.getCommunicationRating() != null ? request.getCommunicationRating() : request.getRating());
        review.setAttitudeRating(request.getAttitudeRating() != null ? request.getAttitudeRating() : request.getRating());
        review.setTimelinessRating(request.getTimelinessRating() != null ? request.getTimelinessRating() : request.getRating());
        review.setContent(request.getContent());
        review.setIsAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : false);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        // 保存标签
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            try {
                review.setTags(objectMapper.writeValueAsString(request.getTags()));
            } catch (JsonProcessingException e) {
                log.error("序列化标签失败", e);
            }
        }

        reviewRepository.insert(review);

        // 发送通知给被评价者
        String notifyTitle = isEmployer ? "收到雇主评价" : "收到开发者评价";
        String notifyContent = String.format("您在订单 %s 中收到了%d星评价", order.getOrderNo(), request.getRating());
        messageService.sendNotification(
                revieweeId,
                com.makebang.entity.Notification.Type.ORDER.code,
                notifyTitle,
                notifyContent,
                "order",
                orderId,
                "/orders/" + orderId
        );

        log.info("用户 {} 评价订单 {} 成功", currentUser.getId(), orderId);

        return toVO(review);
    }

    @Override
    public ReviewVO getReview(Long id) {
        Review review = reviewRepository.selectById(id);
        if (review == null || review.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "评价不存在");
        }
        return toVO(review);
    }

    @Override
    public List<ReviewVO> getOrderReviews(Long orderId) {
        List<Review> reviews = reviewRepository.findByOrderId(orderId);
        return reviews.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public boolean canReviewOrder(Long orderId) {
        UserVO currentUser = userService.getCurrentUser();

        Order order = orderRepository.selectById(orderId);
        if (order == null || order.getStatus() != Order.Status.COMPLETED.code) {
            return false;
        }

        // 检查是否是订单参与者
        if (!order.getEmployerId().equals(currentUser.getId()) &&
            !order.getDeveloperId().equals(currentUser.getId())) {
            return false;
        }

        // 检查是否已评价
        Review existingReview = reviewRepository.findByOrderAndReviewer(orderId, currentUser.getId());
        return existingReview == null;
    }

    @Override
    @Transactional
    public ReviewVO replyReview(Long id, ReplyReviewRequest request) {
        UserVO currentUser = userService.getCurrentUser();

        Review review = reviewRepository.selectById(id);
        if (review == null || review.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "评价不存在");
        }

        // 只有被评价者可以回复
        if (!review.getRevieweeId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权回复此评价");
        }

        // 检查是否已回复
        if (review.getReply() != null && !review.getReply().isEmpty()) {
            throw new BusinessException(ResultCode.CONFLICT, "该评价已回复");
        }

        review.setReply(request.getReply());
        review.setUpdatedAt(LocalDateTime.now());
        reviewRepository.updateById(review);

        log.info("用户 {} 回复评价 {} 成功", currentUser.getId(), id);

        return toVO(review);
    }

    @Override
    public IPage<ReviewVO> getReceivedReviews(Long userId, int current, int size) {
        Page<Review> page = new Page<>(current, size);
        IPage<Review> result = reviewRepository.findReceivedReviews(page, userId);
        return result.convert(this::toVO);
    }

    @Override
    public IPage<ReviewVO> getGivenReviews(int current, int size) {
        UserVO currentUser = userService.getCurrentUser();

        Page<Review> page = new Page<>(current, size);
        IPage<Review> result = reviewRepository.findGivenReviews(page, currentUser.getId());
        return result.convert(this::toVO);
    }

    @Override
    public UserStatsVO getUserStats(Long userId) {
        BigDecimal avgRating = reviewRepository.getAvgRating(userId);
        BigDecimal avgSkillRating = reviewRepository.getAvgSkillRating(userId);
        BigDecimal avgCommunicationRating = reviewRepository.getAvgCommunicationRating(userId);
        BigDecimal avgAttitudeRating = reviewRepository.getAvgAttitudeRating(userId);
        BigDecimal avgTimelinessRating = reviewRepository.getAvgTimelinessRating(userId);

        int reviewCount = reviewRepository.countByReviewee(userId);
        int positiveCount = reviewRepository.countPositiveByReviewee(userId);

        // 计算完成订单数
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.and(w -> w
                .eq(Order::getEmployerId, userId)
                .or()
                .eq(Order::getDeveloperId, userId))
                .eq(Order::getStatus, Order.Status.COMPLETED.code)
                .isNull(Order::getDeletedAt);
        Long completedOrderCount = orderRepository.selectCount(orderWrapper);

        // 计算好评率
        BigDecimal positiveRate = BigDecimal.ZERO;
        if (reviewCount > 0) {
            positiveRate = new BigDecimal(positiveCount)
                    .divide(new BigDecimal(reviewCount), 2, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
        }

        return UserStatsVO.builder()
                .userId(userId)
                .avgRating(avgRating.setScale(1, RoundingMode.HALF_UP))
                .avgSkillRating(avgSkillRating.setScale(1, RoundingMode.HALF_UP))
                .avgCommunicationRating(avgCommunicationRating.setScale(1, RoundingMode.HALF_UP))
                .avgAttitudeRating(avgAttitudeRating.setScale(1, RoundingMode.HALF_UP))
                .avgTimelinessRating(avgTimelinessRating.setScale(1, RoundingMode.HALF_UP))
                .reviewCount(reviewCount)
                .completedOrderCount(completedOrderCount.intValue())
                .positiveCount(positiveCount)
                .positiveRate(positiveRate)
                .build();
    }

    @Override
    public List<ReviewVO> getProjectReviews(Long projectId) {
        List<Review> reviews = reviewRepository.findByProjectId(projectId);
        return reviews.stream().map(this::toVO).collect(Collectors.toList());
    }

    private ReviewVO toVO(Review review) {
        User reviewer = userRepository.selectById(review.getReviewerId());
        User reviewee = userRepository.selectById(review.getRevieweeId());
        Order order = orderRepository.selectById(review.getOrderId());
        Project project = projectRepository.selectById(review.getProjectId());

        // 解析标签
        List<String> tags = Collections.emptyList();
        if (review.getTags() != null && !review.getTags().isEmpty()) {
            try {
                tags = objectMapper.readValue(review.getTags(), new TypeReference<List<String>>() {});
            } catch (JsonProcessingException e) {
                log.error("解析标签失败", e);
            }
        }

        ReviewVO vo = ReviewVO.builder()
                .id(review.getId())
                .orderId(review.getOrderId())
                .orderNo(order != null ? order.getOrderNo() : null)
                .projectId(review.getProjectId())
                .projectTitle(project != null ? project.getTitle() : null)
                .reviewerId(review.getReviewerId())
                .revieweeId(review.getRevieweeId())
                .type(review.getType())
                .typeDesc(Review.Type.getDesc(review.getType()))
                .rating(review.getRating())
                .skillRating(review.getSkillRating())
                .communicationRating(review.getCommunicationRating())
                .attitudeRating(review.getAttitudeRating())
                .timelinessRating(review.getTimelinessRating())
                .content(review.getContent())
                .tags(tags)
                .isAnonymous(review.getIsAnonymous())
                .reply(review.getReply())
                .createdAt(review.getCreatedAt())
                .build();

        // 处理匿名显示
        if (Boolean.TRUE.equals(review.getIsAnonymous())) {
            vo.setReviewer(UserVO.builder()
                    .id(0L)
                    .username("匿名用户")
                    .build());
        } else {
            vo.setReviewer(reviewer != null ? userService.toVO(reviewer) : null);
        }

        vo.setReviewee(reviewee != null ? userService.toVO(reviewee) : null);

        return vo;
    }
}
