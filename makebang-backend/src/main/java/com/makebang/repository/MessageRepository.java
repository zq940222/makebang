package com.makebang.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.makebang.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 消息Repository
 */
@Mapper
public interface MessageRepository extends BaseMapper<Message> {

    /**
     * 分页查询会话消息
     */
    @Select("SELECT * FROM message WHERE conversation_id = #{conversationId} AND deleted_at IS NULL " +
            "ORDER BY created_at DESC")
    IPage<Message> findByConversationId(Page<Message> page, @Param("conversationId") Long conversationId);

    /**
     * 统计未读消息数
     */
    @Select("SELECT COUNT(*) FROM message WHERE receiver_id = #{userId} AND is_read = false AND deleted_at IS NULL")
    int countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 统计会话未读消息数
     */
    @Select("SELECT COUNT(*) FROM message WHERE conversation_id = #{conversationId} " +
            "AND receiver_id = #{userId} AND is_read = false AND deleted_at IS NULL")
    int countUnreadByConversationAndUser(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    /**
     * 标记会话消息为已读
     */
    @Update("UPDATE message SET is_read = true, read_at = NOW(), updated_at = NOW() " +
            "WHERE conversation_id = #{conversationId} AND receiver_id = #{userId} " +
            "AND is_read = false AND deleted_at IS NULL")
    int markAsReadByConversation(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    /**
     * 标记单条消息为已读
     */
    @Update("UPDATE message SET is_read = true, read_at = NOW(), updated_at = NOW() " +
            "WHERE id = #{id} AND deleted_at IS NULL")
    int markAsRead(@Param("id") Long id);
}
