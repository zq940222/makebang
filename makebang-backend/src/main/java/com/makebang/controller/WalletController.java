package com.makebang.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.makebang.common.result.Result;
import com.makebang.dto.RechargeRequest;
import com.makebang.dto.WithdrawRequest;
import com.makebang.service.WalletService;
import com.makebang.vo.TransactionVO;
import com.makebang.vo.WalletVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 钱包控制器
 */
@RestController
@RequestMapping("/v1/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    /**
     * 获取我的钱包
     */
    @GetMapping
    public Result<WalletVO> getMyWallet() {
        return Result.success(walletService.getMyWallet());
    }

    /**
     * 充值（模拟）
     */
    @PostMapping("/recharge")
    public Result<TransactionVO> recharge(@Valid @RequestBody RechargeRequest request) {
        return Result.success(walletService.recharge(request));
    }

    /**
     * 提现申请
     */
    @PostMapping("/withdraw")
    public Result<TransactionVO> withdraw(@Valid @RequestBody WithdrawRequest request) {
        return Result.success(walletService.withdraw(request));
    }

    /**
     * 获取交易记录
     */
    @GetMapping("/transactions")
    public Result<IPage<TransactionVO>> getTransactions(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(walletService.getTransactions(type, current, size));
    }

    /**
     * 获取订单相关交易记录
     */
    @GetMapping("/transactions/order/{orderId}")
    public Result<List<TransactionVO>> getOrderTransactions(@PathVariable Long orderId) {
        return Result.success(walletService.getOrderTransactions(orderId));
    }
}
