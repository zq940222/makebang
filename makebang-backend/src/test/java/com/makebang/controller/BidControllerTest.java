package com.makebang.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.makebang.common.result.PageResult;
import com.makebang.dto.bid.CreateBidRequest;
import com.makebang.dto.bid.UpdateBidRequest;
import com.makebang.service.BidService;
import com.makebang.vo.BidVO;
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
 * BidController 单元测试
 */
@WebMvcTest(BidController.class)
@DisplayName("投标控制器测试")
class BidControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BidService bidService;

    private BidVO testBidVO;
    private CreateBidRequest createRequest;
    private UpdateBidRequest updateRequest;

    @BeforeEach
    void setUp() {
        testBidVO = BidVO.builder()
                .id(1L)
                .projectId(1L)
                .developerId(2L)
                .amount(new BigDecimal("3000"))
                .deliveryDays(14)
                .proposal("我可以完成这个项目")
                .status(0)
                .statusDesc("待处理")
                .createdAt(LocalDateTime.now())
                .build();

        createRequest = new CreateBidRequest();
        createRequest.setProjectId(1L);
        createRequest.setAmount(new BigDecimal("3000"));
        createRequest.setDeliveryDays(14);
        createRequest.setProposal("我可以完成这个项目");

        updateRequest = new UpdateBidRequest();
        updateRequest.setAmount(new BigDecimal("2500"));
        updateRequest.setDeliveryDays(10);
        updateRequest.setProposal("更新后的方案");
    }

    @Test
    @DisplayName("创建投标 - 成功")
    @WithMockUser(username = "developer")
    void createBid_Success() throws Exception {
        when(bidService.createBid(any(CreateBidRequest.class))).thenReturn(testBidVO);

        mockMvc.perform(post("/v1/bids")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.projectId").value(1))
                .andExpect(jsonPath("$.data.amount").value(3000));

        verify(bidService).createBid(any(CreateBidRequest.class));
    }

    @Test
    @DisplayName("创建投标 - 未登录")
    void createBid_Unauthorized() throws Exception {
        mockMvc.perform(post("/v1/bids")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("更新投标 - 成功")
    @WithMockUser(username = "developer")
    void updateBid_Success() throws Exception {
        when(bidService.updateBid(eq(1L), any(UpdateBidRequest.class))).thenReturn(testBidVO);

        mockMvc.perform(put("/v1/bids/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(bidService).updateBid(eq(1L), any(UpdateBidRequest.class));
    }

    @Test
    @DisplayName("撤回投标 - 成功")
    @WithMockUser(username = "developer")
    void withdrawBid_Success() throws Exception {
        doNothing().when(bidService).withdrawBid(1L);

        mockMvc.perform(post("/v1/bids/1/withdraw")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(bidService).withdrawBid(1L);
    }

    @Test
    @DisplayName("接受投标（雇主操作）- 成功")
    @WithMockUser(username = "employer")
    void acceptBid_Success() throws Exception {
        testBidVO.setStatus(1);
        testBidVO.setStatusDesc("已接受");
        when(bidService.acceptBid(1L)).thenReturn(testBidVO);

        mockMvc.perform(post("/v1/bids/1/accept")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(1));

        verify(bidService).acceptBid(1L);
    }

    @Test
    @DisplayName("拒绝投标（雇主操作）- 成功")
    @WithMockUser(username = "employer")
    void rejectBid_Success() throws Exception {
        doNothing().when(bidService).rejectBid(1L);

        mockMvc.perform(post("/v1/bids/1/reject")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(bidService).rejectBid(1L);
    }

    @Test
    @DisplayName("获取投标详情 - 成功")
    @WithMockUser(username = "testuser")
    void getBid_Success() throws Exception {
        when(bidService.getBidById(1L)).thenReturn(testBidVO);

        mockMvc.perform(get("/v1/bids/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.proposal").value("我可以完成这个项目"));

        verify(bidService).getBidById(1L);
    }

    @Test
    @DisplayName("获取项目的投标列表 - 成功")
    @WithMockUser(username = "employer")
    void getProjectBids_Success() throws Exception {
        List<BidVO> bids = Arrays.asList(testBidVO);
        when(bidService.getProjectBids(1L)).thenReturn(bids);

        mockMvc.perform(get("/v1/bids/project/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].projectId").value(1));

        verify(bidService).getProjectBids(1L);
    }

    @Test
    @DisplayName("获取我的投标列表 - 成功")
    @WithMockUser(username = "developer")
    void getMyBids_Success() throws Exception {
        PageResult<BidVO> pageResult = new PageResult<>();
        pageResult.setRecords(Arrays.asList(testBidVO));
        pageResult.setTotal(1L);

        when(bidService.getMyBids(any(), anyInt(), anyInt())).thenReturn(pageResult);

        mockMvc.perform(get("/v1/bids/my")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());

        verify(bidService).getMyBids(any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("检查是否已投标 - 已投标")
    @WithMockUser(username = "developer")
    void hasBid_True() throws Exception {
        when(bidService.hasBid(1L)).thenReturn(true);

        mockMvc.perform(get("/v1/bids/check/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(bidService).hasBid(1L);
    }

    @Test
    @DisplayName("检查是否已投标 - 未投标")
    @WithMockUser(username = "developer")
    void hasBid_False() throws Exception {
        when(bidService.hasBid(1L)).thenReturn(false);

        mockMvc.perform(get("/v1/bids/check/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(false));

        verify(bidService).hasBid(1L);
    }
}
