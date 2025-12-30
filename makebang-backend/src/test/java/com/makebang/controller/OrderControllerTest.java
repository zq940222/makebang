package com.makebang.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.makebang.common.result.PageResult;
import com.makebang.dto.order.CreateMilestoneRequest;
import com.makebang.dto.order.ReviewMilestoneRequest;
import com.makebang.dto.order.SubmitMilestoneRequest;
import com.makebang.service.OrderService;
import com.makebang.vo.MilestoneVO;
import com.makebang.vo.OrderVO;
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
 * OrderController 单元测试
 */
@WebMvcTest(OrderController.class)
@DisplayName("订单控制器测试")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private OrderVO testOrderVO;
    private MilestoneVO testMilestoneVO;

    @BeforeEach
    void setUp() {
        testOrderVO = OrderVO.builder()
                .id(1L)
                .orderNo("ORD202412300001")
                .projectId(1L)
                .projectTitle("测试项目")
                .employerId(1L)
                .developerId(2L)
                .amount(new BigDecimal("5000"))
                .status(2)
                .statusDesc("进行中")
                .createdAt(LocalDateTime.now())
                .build();

        testMilestoneVO = MilestoneVO.builder()
                .id(1L)
                .orderId(1L)
                .title("阶段1: 需求分析")
                .description("完成需求分析文档")
                .amount(new BigDecimal("1000"))
                .status(0)
                .statusDesc("待开始")
                .sortOrder(1)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("获取订单详情 - 成功")
    @WithMockUser(username = "testuser")
    void getOrder_Success() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(testOrderVO);

        mockMvc.perform(get("/v1/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.orderNo").value("ORD202412300001"));

        verify(orderService).getOrderById(1L);
    }

    @Test
    @DisplayName("获取订单详情 - 未登录")
    void getOrder_Unauthorized() throws Exception {
        mockMvc.perform(get("/v1/orders/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("根据订单编号获取订单 - 成功")
    @WithMockUser(username = "testuser")
    void getOrderByNo_Success() throws Exception {
        when(orderService.getOrderByNo("ORD202412300001")).thenReturn(testOrderVO);

        mockMvc.perform(get("/v1/orders/no/ORD202412300001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderNo").value("ORD202412300001"));

        verify(orderService).getOrderByNo("ORD202412300001");
    }

    @Test
    @DisplayName("获取我的订单列表 - 成功")
    @WithMockUser(username = "testuser")
    void getMyOrders_Success() throws Exception {
        PageResult<OrderVO> pageResult = new PageResult<>();
        pageResult.setRecords(Arrays.asList(testOrderVO));
        pageResult.setTotal(1L);

        when(orderService.getMyOrders(any(), any(), anyInt(), anyInt())).thenReturn(pageResult);

        mockMvc.perform(get("/v1/orders/my")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());

        verify(orderService).getMyOrders(any(), any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("获取我的订单列表 - 按角色筛选")
    @WithMockUser(username = "testuser")
    void getMyOrders_ByRole() throws Exception {
        PageResult<OrderVO> pageResult = new PageResult<>();
        pageResult.setRecords(Arrays.asList(testOrderVO));
        pageResult.setTotal(1L);

        when(orderService.getMyOrders(eq("employer"), any(), anyInt(), anyInt())).thenReturn(pageResult);

        mockMvc.perform(get("/v1/orders/my")
                        .param("role", "employer")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(orderService).getMyOrders(eq("employer"), any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("取消订单 - 成功")
    @WithMockUser(username = "testuser")
    void cancelOrder_Success() throws Exception {
        doNothing().when(orderService).cancelOrder(1L);

        mockMvc.perform(post("/v1/orders/1/cancel")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(orderService).cancelOrder(1L);
    }

    @Test
    @DisplayName("确认付款 - 成功")
    @WithMockUser(username = "employer")
    void confirmPayment_Success() throws Exception {
        testOrderVO.setStatus(2);
        when(orderService.confirmPayment(1L)).thenReturn(testOrderVO);

        mockMvc.perform(post("/v1/orders/1/pay")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(2));

        verify(orderService).confirmPayment(1L);
    }

    @Test
    @DisplayName("完成订单 - 成功")
    @WithMockUser(username = "employer")
    void completeOrder_Success() throws Exception {
        testOrderVO.setStatus(5);
        testOrderVO.setStatusDesc("已完成");
        when(orderService.completeOrder(1L)).thenReturn(testOrderVO);

        mockMvc.perform(post("/v1/orders/1/complete")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(5));

        verify(orderService).completeOrder(1L);
    }

    // ========== 里程碑相关 ==========

    @Test
    @DisplayName("获取订单的里程碑列表 - 成功")
    @WithMockUser(username = "testuser")
    void getOrderMilestones_Success() throws Exception {
        List<MilestoneVO> milestones = Arrays.asList(testMilestoneVO);
        when(orderService.getOrderMilestones(1L)).thenReturn(milestones);

        mockMvc.perform(get("/v1/orders/1/milestones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].title").value("阶段1: 需求分析"));

        verify(orderService).getOrderMilestones(1L);
    }

    @Test
    @DisplayName("添加里程碑 - 成功")
    @WithMockUser(username = "testuser")
    void addMilestone_Success() throws Exception {
        CreateMilestoneRequest request = new CreateMilestoneRequest();
        request.setTitle("新里程碑");
        request.setDescription("里程碑描述");
        request.setAmount(new BigDecimal("2000"));

        when(orderService.addMilestone(eq(1L), any(CreateMilestoneRequest.class))).thenReturn(testMilestoneVO);

        mockMvc.perform(post("/v1/orders/1/milestones")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(orderService).addMilestone(eq(1L), any(CreateMilestoneRequest.class));
    }

    @Test
    @DisplayName("开始里程碑 - 成功")
    @WithMockUser(username = "developer")
    void startMilestone_Success() throws Exception {
        testMilestoneVO.setStatus(1);
        testMilestoneVO.setStatusDesc("进行中");
        when(orderService.startMilestone(1L)).thenReturn(testMilestoneVO);

        mockMvc.perform(post("/v1/orders/milestones/1/start")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(1));

        verify(orderService).startMilestone(1L);
    }

    @Test
    @DisplayName("提交里程碑 - 成功")
    @WithMockUser(username = "developer")
    void submitMilestone_Success() throws Exception {
        SubmitMilestoneRequest request = new SubmitMilestoneRequest();
        request.setDeliverable("交付物链接");
        request.setRemark("已完成开发");

        testMilestoneVO.setStatus(2);
        testMilestoneVO.setStatusDesc("待验收");
        when(orderService.submitMilestone(eq(1L), any(SubmitMilestoneRequest.class))).thenReturn(testMilestoneVO);

        mockMvc.perform(post("/v1/orders/milestones/1/submit")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(2));

        verify(orderService).submitMilestone(eq(1L), any(SubmitMilestoneRequest.class));
    }

    @Test
    @DisplayName("验收里程碑 - 通过")
    @WithMockUser(username = "employer")
    void reviewMilestone_Approved() throws Exception {
        ReviewMilestoneRequest request = new ReviewMilestoneRequest();
        request.setApproved(true);
        request.setComment("验收通过");

        testMilestoneVO.setStatus(3);
        testMilestoneVO.setStatusDesc("已完成");
        when(orderService.reviewMilestone(eq(1L), any(ReviewMilestoneRequest.class))).thenReturn(testMilestoneVO);

        mockMvc.perform(post("/v1/orders/milestones/1/review")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(3));

        verify(orderService).reviewMilestone(eq(1L), any(ReviewMilestoneRequest.class));
    }

    @Test
    @DisplayName("验收里程碑 - 拒绝")
    @WithMockUser(username = "employer")
    void reviewMilestone_Rejected() throws Exception {
        ReviewMilestoneRequest request = new ReviewMilestoneRequest();
        request.setApproved(false);
        request.setComment("需要修改");

        testMilestoneVO.setStatus(1);
        testMilestoneVO.setStatusDesc("进行中");
        when(orderService.reviewMilestone(eq(1L), any(ReviewMilestoneRequest.class))).thenReturn(testMilestoneVO);

        mockMvc.perform(post("/v1/orders/milestones/1/review")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(orderService).reviewMilestone(eq(1L), any(ReviewMilestoneRequest.class));
    }
}
