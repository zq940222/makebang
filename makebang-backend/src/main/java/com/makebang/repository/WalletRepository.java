package com.makebang.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.makebang.entity.Wallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * 钱包Repository
 */
@Mapper
public interface WalletRepository extends BaseMapper<Wallet> {

    /**
     * 根据用户ID查询钱包
     */
    @Select("SELECT * FROM wallet WHERE user_id = #{userId} AND deleted_at IS NULL")
    Wallet findByUserId(@Param("userId") Long userId);

    /**
     * 增加余额
     */
    @Update("UPDATE wallet SET balance = balance + #{amount}, updated_at = NOW() " +
            "WHERE id = #{walletId} AND deleted_at IS NULL")
    int addBalance(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    /**
     * 扣减余额（乐观锁，确保余额足够）
     */
    @Update("UPDATE wallet SET balance = balance - #{amount}, updated_at = NOW() " +
            "WHERE id = #{walletId} AND balance >= #{amount} AND deleted_at IS NULL")
    int deductBalance(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    /**
     * 冻结金额（从余额转到冻结）
     */
    @Update("UPDATE wallet SET balance = balance - #{amount}, frozen_amount = frozen_amount + #{amount}, updated_at = NOW() " +
            "WHERE id = #{walletId} AND balance >= #{amount} AND deleted_at IS NULL")
    int freezeAmount(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    /**
     * 解冻金额（从冻结转回余额）
     */
    @Update("UPDATE wallet SET balance = balance + #{amount}, frozen_amount = frozen_amount - #{amount}, updated_at = NOW() " +
            "WHERE id = #{walletId} AND frozen_amount >= #{amount} AND deleted_at IS NULL")
    int unfreezeAmount(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    /**
     * 从冻结金额释放到指定用户（托管支付完成）
     */
    @Update("UPDATE wallet SET frozen_amount = frozen_amount - #{amount}, total_expense = total_expense + #{amount}, updated_at = NOW() " +
            "WHERE id = #{walletId} AND frozen_amount >= #{amount} AND deleted_at IS NULL")
    int releaseFrozenAmount(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    /**
     * 增加收入
     */
    @Update("UPDATE wallet SET balance = balance + #{amount}, total_income = total_income + #{amount}, updated_at = NOW() " +
            "WHERE id = #{walletId} AND deleted_at IS NULL")
    int addIncome(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);
}
