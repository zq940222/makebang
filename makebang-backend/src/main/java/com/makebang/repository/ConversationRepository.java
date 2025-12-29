package com.makebang.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.makebang.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 会话Repository
 */
@Mapper
public interface ConversationRepository extends BaseMapper<Conversation> {

    /**
     * 查询用户的会话列表
     */
    @Select("SELECT * FROM conversation WHERE (participant1_id = #{userId} OR participant2_id = #{userId}) " +
            "AND deleted_at IS NULL ORDER BY last_message_at DESC NULLS LAST, created_at DESC")
    IPage<Conversation> findByUserId(Page<Conversation> page, @Param("userId") Long userId);

    /**
     * 查询两个用户之间的私聊会话
     */
    @Select("SELECT * FROM conversation WHERE type = 1 " +
            "AND ((participant1_id = #{userId1} AND participant2_id = #{userId2}) " +
            "OR (participant1_id = #{userId2} AND participant2_id = #{userId1})) " +
            "AND deleted_at IS NULL LIMIT 1")
    Conversation findPrivateConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * 查询订单会话
     */
    @Select("SELECT * FROM conversation WHERE type = 2 AND order_id = #{orderId} AND deleted_at IS NULL LIMIT 1")
    Conversation findByOrderId(@Param("orderId") Long orderId);

    /**
     * 更新参与者1的未读数
     */
    @Update("UPDATE conversation SET participant1_unread = participant1_unread + 1, updated_at = NOW() " +
            "WHERE id = #{id} AND deleted_at IS NULL")
    int incrementParticipant1Unread(@Param("id") Long id);

    /**
     * 更新参与者2的未读数
     */
    @Update("UPDATE conversation SET participant2_unread = participant2_unread + 1, updated_at = NOW() " +
            "WHERE id = #{id} AND deleted_at IS NULL")
    int incrementParticipant2Unread(@Param("id") Long id);

    /**
     * 清空参与者1的未读数
     */
    @Update("UPDATE conversation SET participant1_unread = 0, updated_at = NOW() " +
            "WHERE id = #{id} AND deleted_at IS NULL")
    int clearParticipant1Unread(@Param("id") Long id);

    /**
     * 清空参与者2的未读数
     */
    @Update("UPDATE conversation SET participant2_unread = 0, updated_at = NOW() " +
            "WHERE id = #{id} AND deleted_at IS NULL")
    int clearParticipant2Unread(@Param("id") Long id);

    /**
     * 更新最后消息
     */
    @Update("UPDATE conversation SET last_message_id = #{messageId}, last_message_content = #{content}, " +
            "last_message_at = NOW(), updated_at = NOW() WHERE id = #{id} AND deleted_at IS NULL")
    int updateLastMessage(@Param("id") Long id, @Param("messageId") Long messageId, @Param("content") String content);
}
