package com.makebang.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.makebang.dto.review.CreateReviewRequest;
import com.makebang.dto.review.ReplyReviewRequest;
import com.makebang.vo.ReviewVO;
import com.makebang.vo.UserStatsVO;

import java.util.List;

/**
 * 评价服务接口
 */
public interface ReviewService {

    /**
     * 创建评价
     */
    ReviewVO createReview(CreateReviewRequest request);

    /**
     * 获取评价详情
     */
    ReviewVO getReview(Long id);

    /**
     * 获取订单的评价列表
     */
    List<ReviewVO> getOrderReviews(Long orderId);

    /**
     * 检查是否可以评价订单
     */
    boolean canReviewOrder(Long orderId);

    /**
     * 回复评价
     */
    ReviewVO replyReview(Long id, ReplyReviewRequest request);

    /**
     * 获取用户收到的评价
     */
    IPage<ReviewVO> getReceivedReviews(Long userId, int current, int size);

    /**
     * 获取用户发出的评价
     */
    IPage<ReviewVO> getGivenReviews(int current, int size);

    /**
     * 获取用户评分统计
     */
    UserStatsVO getUserStats(Long userId);

    /**
     * 获取项目评价
     */
    List<ReviewVO> getProjectReviews(Long projectId);
}
