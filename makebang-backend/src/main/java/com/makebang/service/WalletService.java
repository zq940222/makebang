package com.makebang.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.makebang.dto.RechargeRequest;
import com.makebang.dto.WithdrawRequest;
import com.makebang.vo.TransactionVO;
import com.makebang.vo.WalletVO;

import java.math.BigDecimal;

/**
 * 钱包服务接口
 */
public interface WalletService {

    /**
     * 获取当前用户钱包
     */
    WalletVO getMyWallet();

    /**
     * 获取或创建用户钱包
     */
    WalletVO getOrCreateWallet(Long userId);

    /**
     * 充值（模拟）
     */
    TransactionVO recharge(RechargeRequest request);

    /**
     * 提现申请
     */
    TransactionVO withdraw(WithdrawRequest request);

    /**
     * 托管支付（冻结雇主资金）
     */
    TransactionVO escrowPayment(Long orderId, BigDecimal amount);

    /**
     * 释放托管资金到开发者（里程碑验收通过）
     */
    TransactionVO releaseMilestonePayment(Long milestoneId);

    /**
     * 退还托管资金（订单取消）
     */
    TransactionVO refundEscrow(Long orderId);

    /**
     * 获取交易记录列表
     */
    IPage<TransactionVO> getTransactions(Integer type, int current, int size);

    /**
     * 获取订单相关交易记录
     */
    java.util.List<TransactionVO> getOrderTransactions(Long orderId);
}
