package com.makebang.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.makebang.common.result.Result;
import com.makebang.dto.review.CreateReviewRequest;
import com.makebang.dto.review.ReplyReviewRequest;
import com.makebang.service.ReviewService;
import com.makebang.vo.ReviewVO;
import com.makebang.vo.UserStatsVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评价控制器
 */
@RestController
@RequestMapping("/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 创建评价
     */
    @PostMapping
    public Result<ReviewVO> createReview(@Valid @RequestBody CreateReviewRequest request) {
        return Result.success(reviewService.createReview(request));
    }

    /**
     * 获取评价详情
     */
    @GetMapping("/{id}")
    public Result<ReviewVO> getReview(@PathVariable Long id) {
        return Result.success(reviewService.getReview(id));
    }

    /**
     * 获取订单评价
     */
    @GetMapping("/order/{orderId}")
    public Result<List<ReviewVO>> getOrderReviews(@PathVariable Long orderId) {
        return Result.success(reviewService.getOrderReviews(orderId));
    }

    /**
     * 检查是否可以评价订单
     */
    @GetMapping("/order/{orderId}/can-review")
    public Result<Boolean> canReviewOrder(@PathVariable Long orderId) {
        return Result.success(reviewService.canReviewOrder(orderId));
    }

    /**
     * 回复评价
     */
    @PostMapping("/{id}/reply")
    public Result<ReviewVO> replyReview(@PathVariable Long id, @Valid @RequestBody ReplyReviewRequest request) {
        return Result.success(reviewService.replyReview(id, request));
    }

    /**
     * 获取用户收到的评价
     */
    @GetMapping("/user/{userId}/received")
    public Result<IPage<ReviewVO>> getReceivedReviews(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(reviewService.getReceivedReviews(userId, current, size));
    }

    /**
     * 获取我发出的评价
     */
    @GetMapping("/my/given")
    public Result<IPage<ReviewVO>> getGivenReviews(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(reviewService.getGivenReviews(current, size));
    }

    /**
     * 获取用户评分统计
     */
    @GetMapping("/user/{userId}/stats")
    public Result<UserStatsVO> getUserStats(@PathVariable Long userId) {
        return Result.success(reviewService.getUserStats(userId));
    }

    /**
     * 获取项目评价
     */
    @GetMapping("/project/{projectId}")
    public Result<List<ReviewVO>> getProjectReviews(@PathVariable Long projectId) {
        return Result.success(reviewService.getProjectReviews(projectId));
    }
}
