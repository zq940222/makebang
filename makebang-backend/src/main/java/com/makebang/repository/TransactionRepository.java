package com.makebang.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.makebang.entity.Transaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 交易记录Repository
 */
@Mapper
public interface TransactionRepository extends BaseMapper<Transaction> {

    /**
     * 分页查询用户交易记录
     */
    @Select("<script>" +
            "SELECT * FROM transaction WHERE user_id = #{userId} AND deleted_at IS NULL " +
            "<if test='type != null'>AND type = #{type}</if> " +
            "ORDER BY created_at DESC" +
            "</script>")
    IPage<Transaction> findByUserId(Page<Transaction> page, @Param("userId") Long userId, @Param("type") Integer type);

    /**
     * 根据订单ID查询交易记录
     */
    @Select("SELECT * FROM transaction WHERE order_id = #{orderId} AND deleted_at IS NULL ORDER BY created_at DESC")
    List<Transaction> findByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据里程碑ID查询交易记录
     */
    @Select("SELECT * FROM transaction WHERE milestone_id = #{milestoneId} AND deleted_at IS NULL ORDER BY created_at DESC")
    List<Transaction> findByMilestoneId(@Param("milestoneId") Long milestoneId);

    /**
     * 根据交易编号查询
     */
    @Select("SELECT * FROM transaction WHERE transaction_no = #{transactionNo} AND deleted_at IS NULL")
    Transaction findByTransactionNo(@Param("transactionNo") String transactionNo);
}
