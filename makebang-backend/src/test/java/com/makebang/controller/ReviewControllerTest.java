package com.makebang.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.makebang.dto.review.CreateReviewRequest;
import com.makebang.dto.review.ReplyReviewRequest;
import com.makebang.service.ReviewService;
import com.makebang.vo.ReviewVO;
import com.makebang.vo.UserStatsVO;
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
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ReviewController 单元测试
 */
@WebMvcTest(ReviewController.class)
@DisplayName("评价控制器测试")
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;

    private ReviewVO testReviewVO;
    private UserStatsVO testUserStatsVO;

    @BeforeEach
    void setUp() {
        testReviewVO = ReviewVO.builder()
                .id(1L)
                .orderId(1L)
                .reviewerId(1L)
                .revieweeId(2L)
                .rating(5)
                .content("非常专业，按时完成，代码质量很高！")
                .reply("感谢您的好评！")
                .createdAt(LocalDateTime.now())
                .build();

        testUserStatsVO = UserStatsVO.builder()
                .userId(1L)
                .averageRating(new BigDecimal("4.8"))
                .totalReviews(50)
                .completedOrders(45)
                .completionRate(new BigDecimal("0.95"))
                .build();
    }

    @Test
    @DisplayName("创建评价 - 成功")
    @WithMockUser(username = "employer")
    void createReview_Success() throws Exception {
        CreateReviewRequest request = new CreateReviewRequest();
        request.setOrderId(1L);
        request.setRating(5);
        request.setContent("非常满意！");

        when(reviewService.createReview(any(CreateReviewRequest.class))).thenReturn(testReviewVO);

        mockMvc.perform(post("/v1/reviews")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.rating").value(5));

        verify(reviewService).createReview(any(CreateReviewRequest.class));
    }

    @Test
    @DisplayName("创建评价 - 未登录")
    void createReview_Unauthorized() throws Exception {
        CreateReviewRequest request = new CreateReviewRequest();
        request.setOrderId(1L);
        request.setRating(5);

        mockMvc.perform(post("/v1/reviews")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("获取评价详情 - 成功")
    @WithMockUser(username = "testuser")
    void getReview_Success() throws Exception {
        when(reviewService.getReview(1L)).thenReturn(testReviewVO);

        mockMvc.perform(get("/v1/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.rating").value(5));

        verify(reviewService).getReview(1L);
    }

    @Test
    @DisplayName("获取订单评价 - 成功")
    @WithMockUser(username = "testuser")
    void getOrderReviews_Success() throws Exception {
        List<ReviewVO> reviews = Arrays.asList(testReviewVO);
        when(reviewService.getOrderReviews(1L)).thenReturn(reviews);

        mockMvc.perform(get("/v1/reviews/order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].orderId").value(1));

        verify(reviewService).getOrderReviews(1L);
    }

    @Test
    @DisplayName("检查是否可以评价订单 - 可以评价")
    @WithMockUser(username = "employer")
    void canReviewOrder_True() throws Exception {
        when(reviewService.canReviewOrder(1L)).thenReturn(true);

        mockMvc.perform(get("/v1/reviews/order/1/can-review"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(reviewService).canReviewOrder(1L);
    }

    @Test
    @DisplayName("检查是否可以评价订单 - 不能评价")
    @WithMockUser(username = "employer")
    void canReviewOrder_False() throws Exception {
        when(reviewService.canReviewOrder(1L)).thenReturn(false);

        mockMvc.perform(get("/v1/reviews/order/1/can-review"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(false));

        verify(reviewService).canReviewOrder(1L);
    }

    @Test
    @DisplayName("回复评价 - 成功")
    @WithMockUser(username = "developer")
    void replyReview_Success() throws Exception {
        ReplyReviewRequest request = new ReplyReviewRequest();
        request.setReply("感谢您的好评，期待下次合作！");

        when(reviewService.replyReview(eq(1L), any(ReplyReviewRequest.class))).thenReturn(testReviewVO);

        mockMvc.perform(post("/v1/reviews/1/reply")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.reply").isNotEmpty());

        verify(reviewService).replyReview(eq(1L), any(ReplyReviewRequest.class));
    }

    @Test
    @DisplayName("获取用户收到的评价 - 成功")
    @WithMockUser(username = "testuser")
    void getReceivedReviews_Success() throws Exception {
        Page<ReviewVO> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testReviewVO));
        page.setTotal(1);

        when(reviewService.getReceivedReviews(eq(2L), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/v1/reviews/user/2/received")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());

        verify(reviewService).getReceivedReviews(eq(2L), anyInt(), anyInt());
    }

    @Test
    @DisplayName("获取我发出的评价 - 成功")
    @WithMockUser(username = "testuser")
    void getGivenReviews_Success() throws Exception {
        Page<ReviewVO> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testReviewVO));
        page.setTotal(1);

        when(reviewService.getGivenReviews(anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/v1/reviews/my/given")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());

        verify(reviewService).getGivenReviews(anyInt(), anyInt());
    }

    @Test
    @DisplayName("获取用户评分统计 - 成功")
    @WithMockUser(username = "testuser")
    void getUserStats_Success() throws Exception {
        when(reviewService.getUserStats(1L)).thenReturn(testUserStatsVO);

        mockMvc.perform(get("/v1/reviews/user/1/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.averageRating").value(4.8))
                .andExpect(jsonPath("$.data.totalReviews").value(50));

        verify(reviewService).getUserStats(1L);
    }

    @Test
    @DisplayName("获取项目评价 - 成功")
    @WithMockUser(username = "testuser")
    void getProjectReviews_Success() throws Exception {
        List<ReviewVO> reviews = Arrays.asList(testReviewVO);
        when(reviewService.getProjectReviews(1L)).thenReturn(reviews);

        mockMvc.perform(get("/v1/reviews/project/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(reviewService).getProjectReviews(1L);
    }
}
