package com.makebang.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.makebang.dto.RechargeRequest;
import com.makebang.dto.WithdrawRequest;
import com.makebang.service.WalletService;
import com.makebang.vo.TransactionVO;
import com.makebang.vo.WalletVO;
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
 * WalletController 单元测试
 */
@WebMvcTest(WalletController.class)
@DisplayName("钱包控制器测试")
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WalletService walletService;

    private WalletVO testWalletVO;
    private TransactionVO testTransactionVO;

    @BeforeEach
    void setUp() {
        testWalletVO = WalletVO.builder()
                .id(1L)
                .userId(1L)
                .balance(new BigDecimal("10000.00"))
                .frozenAmount(new BigDecimal("500.00"))
                .totalIncome(new BigDecimal("20000.00"))
                .totalExpense(new BigDecimal("5000.00"))
                .build();

        testTransactionVO = TransactionVO.builder()
                .id(1L)
                .transactionNo("TXN202412300001")
                .userId(1L)
                .type(1)
                .typeDesc("充值")
                .amount(new BigDecimal("1000.00"))
                .status(1)
                .statusDesc("成功")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("获取我的钱包 - 成功")
    @WithMockUser(username = "testuser")
    void getMyWallet_Success() throws Exception {
        when(walletService.getMyWallet()).thenReturn(testWalletVO);

        mockMvc.perform(get("/v1/wallet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.balance").value(10000.00))
                .andExpect(jsonPath("$.data.frozenAmount").value(500.00));

        verify(walletService).getMyWallet();
    }

    @Test
    @DisplayName("获取我的钱包 - 未登录")
    void getMyWallet_Unauthorized() throws Exception {
        mockMvc.perform(get("/v1/wallet"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("充值 - 成功")
    @WithMockUser(username = "testuser")
    void recharge_Success() throws Exception {
        RechargeRequest request = new RechargeRequest();
        request.setAmount(new BigDecimal("1000.00"));

        when(walletService.recharge(any(RechargeRequest.class))).thenReturn(testTransactionVO);

        mockMvc.perform(post("/v1/wallet/recharge")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.type").value(1))
                .andExpect(jsonPath("$.data.amount").value(1000.00));

        verify(walletService).recharge(any(RechargeRequest.class));
    }

    @Test
    @DisplayName("充值 - 未登录")
    void recharge_Unauthorized() throws Exception {
        RechargeRequest request = new RechargeRequest();
        request.setAmount(new BigDecimal("1000.00"));

        mockMvc.perform(post("/v1/wallet/recharge")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("提现申请 - 成功")
    @WithMockUser(username = "testuser")
    void withdraw_Success() throws Exception {
        WithdrawRequest request = new WithdrawRequest();
        request.setAmount(new BigDecimal("500.00"));
        request.setWithdrawMethod(2);
        request.setAccount("622848*********1234");
        request.setAccountName("张三");

        TransactionVO withdrawVO = TransactionVO.builder()
                .id(2L)
                .transactionNo("TXN202412300002")
                .type(3)
                .typeDesc("提现")
                .amount(new BigDecimal("500.00"))
                .status(0)
                .statusDesc("处理中")
                .build();

        when(walletService.withdraw(any(WithdrawRequest.class))).thenReturn(withdrawVO);

        mockMvc.perform(post("/v1/wallet/withdraw")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.type").value(3))
                .andExpect(jsonPath("$.data.statusDesc").value("处理中"));

        verify(walletService).withdraw(any(WithdrawRequest.class));
    }

    @Test
    @DisplayName("提现申请 - 参数验证失败")
    @WithMockUser(username = "testuser")
    void withdraw_ValidationFailed() throws Exception {
        WithdrawRequest request = new WithdrawRequest();
        request.setAmount(new BigDecimal("-100")); // 负数金额

        mockMvc.perform(post("/v1/wallet/withdraw")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("获取交易记录 - 成功")
    @WithMockUser(username = "testuser")
    void getTransactions_Success() throws Exception {
        Page<TransactionVO> page = new Page<>(1, 20);
        page.setRecords(Arrays.asList(testTransactionVO));
        page.setTotal(1);

        when(walletService.getTransactions(any(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/v1/wallet/transactions")
                        .param("current", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());

        verify(walletService).getTransactions(any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("获取交易记录 - 按类型筛选")
    @WithMockUser(username = "testuser")
    void getTransactions_ByType() throws Exception {
        Page<TransactionVO> page = new Page<>(1, 20);
        page.setRecords(Arrays.asList(testTransactionVO));
        page.setTotal(1);

        when(walletService.getTransactions(eq(1), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/v1/wallet/transactions")
                        .param("type", "1")
                        .param("current", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(walletService).getTransactions(eq(1), anyInt(), anyInt());
    }

    @Test
    @DisplayName("获取订单相关交易记录 - 成功")
    @WithMockUser(username = "testuser")
    void getOrderTransactions_Success() throws Exception {
        List<TransactionVO> transactions = Arrays.asList(testTransactionVO);
        when(walletService.getOrderTransactions(1L)).thenReturn(transactions);

        mockMvc.perform(get("/v1/wallet/transactions/order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].transactionNo").value("TXN202412300001"));

        verify(walletService).getOrderTransactions(1L);
    }
}
