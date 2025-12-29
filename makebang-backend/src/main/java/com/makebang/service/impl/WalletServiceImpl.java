package com.makebang.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.makebang.dto.RechargeRequest;
import com.makebang.dto.WithdrawRequest;
import com.makebang.entity.Milestone;
import com.makebang.entity.Order;
import com.makebang.entity.Transaction;
import com.makebang.entity.User;
import com.makebang.entity.Wallet;
import com.makebang.common.exception.BusinessException;
import com.makebang.repository.MilestoneRepository;
import com.makebang.repository.OrderRepository;
import com.makebang.repository.TransactionRepository;
import com.makebang.repository.WalletRepository;
import com.makebang.service.UserService;
import com.makebang.service.WalletService;
import com.makebang.util.SecurityUtils;
import com.makebang.vo.TransactionVO;
import com.makebang.vo.WalletVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 钱包服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final OrderRepository orderRepository;
    private final MilestoneRepository milestoneRepository;
    private final UserService userService;

    // 平台服务费率（5%）
    private static final BigDecimal SERVICE_FEE_RATE = new BigDecimal("0.05");

    @Override
    public WalletVO getMyWallet() {
        Long userId = SecurityUtils.getCurrentUserId();
        return getOrCreateWallet(userId);
    }

    @Override
    @Transactional
    public WalletVO getOrCreateWallet(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId);

        if (wallet == null) {
            wallet = new Wallet();
            wallet.setUserId(userId);
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setFrozenAmount(BigDecimal.ZERO);
            wallet.setTotalIncome(BigDecimal.ZERO);
            wallet.setTotalExpense(BigDecimal.ZERO);
            wallet.setStatus(Wallet.Status.NORMAL.getCode());
            wallet.setCreatedAt(LocalDateTime.now());
            wallet.setUpdatedAt(LocalDateTime.now());
            walletRepository.insert(wallet);
        }

        return toWalletVO(wallet);
    }

    @Override
    @Transactional
    public TransactionVO recharge(RechargeRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();
        Wallet wallet = getOrCreateWalletEntity(currentUser.getId());

        BigDecimal amount = request.getAmount();
        BigDecimal balanceBefore = wallet.getBalance();

        // 增加余额
        walletRepository.addBalance(wallet.getId(), amount);

        // 创建交易记录
        Transaction transaction = new Transaction();
        transaction.setTransactionNo(generateTransactionNo());
        transaction.setWalletId(wallet.getId());
        transaction.setUserId(currentUser.getId());
        transaction.setType(Transaction.Type.RECHARGE.getCode());
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceBefore.add(amount));
        transaction.setStatus(Transaction.Status.SUCCESS.getCode());
        transaction.setRemark("账户充值");
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        transactionRepository.insert(transaction);

        log.info("用户 {} 充值 {} 元成功", currentUser.getId(), amount);

        return toTransactionVO(transaction);
    }

    @Override
    @Transactional
    public TransactionVO withdraw(WithdrawRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();
        Wallet wallet = getOrCreateWalletEntity(currentUser.getId());

        BigDecimal amount = request.getAmount();

        // 检查余额
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("余额不足");
        }

        BigDecimal balanceBefore = wallet.getBalance();

        // 扣减余额
        int updated = walletRepository.deductBalance(wallet.getId(), amount);
        if (updated == 0) {
            throw new BusinessException("余额不足或操作失败");
        }

        // 创建交易记录（状态为处理中，实际需要审核）
        Transaction transaction = new Transaction();
        transaction.setTransactionNo(generateTransactionNo());
        transaction.setWalletId(wallet.getId());
        transaction.setUserId(currentUser.getId());
        transaction.setType(Transaction.Type.WITHDRAW.getCode());
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceBefore.subtract(amount));
        transaction.setStatus(Transaction.Status.PROCESSING.getCode());
        transaction.setRemark("提现到" + (request.getWithdrawMethod() == 1 ? "支付宝" : "银行卡") + " " + maskAccount(request.getAccount()));
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        transactionRepository.insert(transaction);

        log.info("用户 {} 申请提现 {} 元", currentUser.getId(), amount);

        return toTransactionVO(transaction);
    }

    @Override
    @Transactional
    public TransactionVO escrowPayment(Long orderId, BigDecimal amount) {
        Order order = orderRepository.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        Wallet employerWallet = getOrCreateWalletEntity(order.getEmployerId());

        // 检查余额
        if (employerWallet.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("余额不足，请先充值");
        }

        BigDecimal balanceBefore = employerWallet.getBalance();

        // 冻结金额
        int updated = walletRepository.freezeAmount(employerWallet.getId(), amount);
        if (updated == 0) {
            throw new BusinessException("余额不足或操作失败");
        }

        // 创建交易记录
        Transaction transaction = new Transaction();
        transaction.setTransactionNo(generateTransactionNo());
        transaction.setWalletId(employerWallet.getId());
        transaction.setUserId(order.getEmployerId());
        transaction.setType(Transaction.Type.PAYMENT.getCode());
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceBefore.subtract(amount));
        transaction.setOrderId(orderId);
        transaction.setStatus(Transaction.Status.SUCCESS.getCode());
        transaction.setRemark("订单托管支付");
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        transactionRepository.insert(transaction);

        log.info("订单 {} 托管支付 {} 元成功", orderId, amount);

        return toTransactionVO(transaction);
    }

    @Override
    @Transactional
    public TransactionVO releaseMilestonePayment(Long milestoneId) {
        Milestone milestone = milestoneRepository.selectById(milestoneId);
        if (milestone == null) {
            throw new BusinessException("里程碑不存在");
        }

        Order order = orderRepository.selectById(milestone.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        Wallet employerWallet = getOrCreateWalletEntity(order.getEmployerId());
        Wallet developerWallet = getOrCreateWalletEntity(order.getDeveloperId());

        BigDecimal milestoneAmount = milestone.getAmount();

        // 计算平台服务费
        BigDecimal serviceFee = milestoneAmount.multiply(SERVICE_FEE_RATE);
        BigDecimal developerIncome = milestoneAmount.subtract(serviceFee);

        // 从雇主冻结金额中释放
        int updated = walletRepository.releaseFrozenAmount(employerWallet.getId(), milestoneAmount);
        if (updated == 0) {
            throw new BusinessException("托管金额不足或操作失败");
        }

        // 增加开发者收入
        walletRepository.addIncome(developerWallet.getId(), developerIncome);

        // 创建开发者收入交易记录
        BigDecimal devBalanceBefore = developerWallet.getBalance();
        Transaction devTransaction = new Transaction();
        devTransaction.setTransactionNo(generateTransactionNo());
        devTransaction.setWalletId(developerWallet.getId());
        devTransaction.setUserId(order.getDeveloperId());
        devTransaction.setType(Transaction.Type.INCOME.getCode());
        devTransaction.setAmount(developerIncome);
        devTransaction.setBalanceBefore(devBalanceBefore);
        devTransaction.setBalanceAfter(devBalanceBefore.add(developerIncome));
        devTransaction.setOrderId(order.getId());
        devTransaction.setMilestoneId(milestoneId);
        devTransaction.setStatus(Transaction.Status.SUCCESS.getCode());
        devTransaction.setRemark("里程碑验收收入（扣除" + SERVICE_FEE_RATE.multiply(new BigDecimal("100")).intValue() + "%服务费）");
        devTransaction.setCreatedAt(LocalDateTime.now());
        devTransaction.setUpdatedAt(LocalDateTime.now());
        transactionRepository.insert(devTransaction);

        // 创建平台服务费记录（可选，用于平台统计）
        if (serviceFee.compareTo(BigDecimal.ZERO) > 0) {
            Transaction feeTransaction = new Transaction();
            feeTransaction.setTransactionNo(generateTransactionNo());
            feeTransaction.setWalletId(developerWallet.getId());
            feeTransaction.setUserId(order.getDeveloperId());
            feeTransaction.setType(Transaction.Type.SERVICE_FEE.getCode());
            feeTransaction.setAmount(serviceFee);
            feeTransaction.setBalanceBefore(devBalanceBefore.add(developerIncome));
            feeTransaction.setBalanceAfter(devBalanceBefore.add(developerIncome));
            feeTransaction.setOrderId(order.getId());
            feeTransaction.setMilestoneId(milestoneId);
            feeTransaction.setStatus(Transaction.Status.SUCCESS.getCode());
            feeTransaction.setRemark("平台服务费");
            feeTransaction.setCreatedAt(LocalDateTime.now());
            feeTransaction.setUpdatedAt(LocalDateTime.now());
            transactionRepository.insert(feeTransaction);
        }

        log.info("里程碑 {} 支付释放成功，开发者获得 {} 元", milestoneId, developerIncome);

        return toTransactionVO(devTransaction);
    }

    @Override
    @Transactional
    public TransactionVO refundEscrow(Long orderId) {
        Order order = orderRepository.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        Wallet employerWallet = getOrCreateWalletEntity(order.getEmployerId());

        // 计算需要退还的托管金额（未完成的里程碑金额）
        BigDecimal refundAmount = calculateRefundAmount(order);

        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("订单 {} 无需退款", orderId);
            return null;
        }

        // 解冻金额回到余额
        int updated = walletRepository.unfreezeAmount(employerWallet.getId(), refundAmount);
        if (updated == 0) {
            throw new BusinessException("退款失败，托管金额不足");
        }

        // 创建退款交易记录
        BigDecimal balanceBefore = employerWallet.getBalance();
        Transaction transaction = new Transaction();
        transaction.setTransactionNo(generateTransactionNo());
        transaction.setWalletId(employerWallet.getId());
        transaction.setUserId(order.getEmployerId());
        transaction.setType(Transaction.Type.REFUND.getCode());
        transaction.setAmount(refundAmount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceBefore.add(refundAmount));
        transaction.setOrderId(orderId);
        transaction.setStatus(Transaction.Status.SUCCESS.getCode());
        transaction.setRemark("订单取消退款");
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        transactionRepository.insert(transaction);

        log.info("订单 {} 退款 {} 元成功", orderId, refundAmount);

        return toTransactionVO(transaction);
    }

    @Override
    public IPage<TransactionVO> getTransactions(Integer type, int current, int size) {
        User currentUser = SecurityUtils.getCurrentUser();

        Page<Transaction> page = new Page<>(current, size);
        IPage<Transaction> result = transactionRepository.findByUserId(page, currentUser.getId(), type);

        return result.convert(this::toTransactionVO);
    }

    @Override
    public List<TransactionVO> getOrderTransactions(Long orderId) {
        List<Transaction> transactions = transactionRepository.findByOrderId(orderId);
        return transactions.stream()
                .map(this::toTransactionVO)
                .collect(Collectors.toList());
    }

    // ========== 私有方法 ==========

    private Wallet getOrCreateWalletEntity(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId);

        if (wallet == null) {
            wallet = new Wallet();
            wallet.setUserId(userId);
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setFrozenAmount(BigDecimal.ZERO);
            wallet.setTotalIncome(BigDecimal.ZERO);
            wallet.setTotalExpense(BigDecimal.ZERO);
            wallet.setStatus(Wallet.Status.NORMAL.getCode());
            wallet.setCreatedAt(LocalDateTime.now());
            wallet.setUpdatedAt(LocalDateTime.now());
            walletRepository.insert(wallet);
        }

        return wallet;
    }

    private BigDecimal calculateRefundAmount(Order order) {
        // 获取所有未完成的里程碑金额
        List<Milestone> milestones = milestoneRepository.findByOrderId(order.getId());
        return milestones.stream()
                .filter(m -> m.getStatus() != Milestone.Status.APPROVED.code)
                .map(Milestone::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateTransactionNo() {
        return "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String maskAccount(String account) {
        if (account == null || account.length() < 4) {
            return "****";
        }
        return account.substring(0, 4) + "****" + account.substring(account.length() - 4);
    }

    private WalletVO toWalletVO(Wallet wallet) {
        String statusDesc = "";
        if (wallet.getStatus() == Wallet.Status.NORMAL.getCode()) {
            statusDesc = "正常";
        } else if (wallet.getStatus() == Wallet.Status.FROZEN.getCode()) {
            statusDesc = "冻结";
        }

        return WalletVO.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .frozenAmount(wallet.getFrozenAmount())
                .totalIncome(wallet.getTotalIncome())
                .totalExpense(wallet.getTotalExpense())
                .status(wallet.getStatus())
                .statusDesc(statusDesc)
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }

    private TransactionVO toTransactionVO(Transaction transaction) {
        Transaction.Type type = Transaction.Type.fromCode(transaction.getType());
        String typeDesc = type != null ? type.getDesc() : "未知";

        String statusDesc = "";
        if (transaction.getStatus() == Transaction.Status.PROCESSING.getCode()) {
            statusDesc = "处理中";
        } else if (transaction.getStatus() == Transaction.Status.SUCCESS.getCode()) {
            statusDesc = "成功";
        } else if (transaction.getStatus() == Transaction.Status.FAILED.getCode()) {
            statusDesc = "失败";
        }

        // 判断收支方向
        int direction;
        String directionDesc;
        if (transaction.getType() == Transaction.Type.RECHARGE.getCode() ||
            transaction.getType() == Transaction.Type.INCOME.getCode() ||
            transaction.getType() == Transaction.Type.REFUND.getCode()) {
            direction = 1;
            directionDesc = "收入";
        } else {
            direction = 2;
            directionDesc = "支出";
        }

        return TransactionVO.builder()
                .id(transaction.getId())
                .transactionNo(transaction.getTransactionNo())
                .userId(transaction.getUserId())
                .type(transaction.getType())
                .typeDesc(typeDesc)
                .amount(transaction.getAmount())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .orderId(transaction.getOrderId())
                .milestoneId(transaction.getMilestoneId())
                .status(transaction.getStatus())
                .statusDesc(statusDesc)
                .remark(transaction.getRemark())
                .createdAt(transaction.getCreatedAt())
                .direction(direction)
                .directionDesc(directionDesc)
                .build();
    }
}
