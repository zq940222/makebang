package com.makebang.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.makebang.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 通知Repository
 */
@Mapper
public interface NotificationRepository extends BaseMapper<Notification> {

    /**
     * 分页查询用户通知
     */
    @Select("<script>" +
            "SELECT * FROM notification WHERE user_id = #{userId} AND deleted_at IS NULL " +
            "<if test='type != null'>AND type = #{type}</if> " +
            "ORDER BY created_at DESC" +
            "</script>")
    IPage<Notification> findByUserId(Page<Notification> page, @Param("userId") Long userId, @Param("type") Integer type);

    /**
     * 统计未读通知数
     */
    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = false AND deleted_at IS NULL")
    int countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 标记单条通知为已读
     */
    @Update("UPDATE notification SET is_read = true, read_at = NOW(), updated_at = NOW() " +
            "WHERE id = #{id} AND deleted_at IS NULL")
    int markAsRead(@Param("id") Long id);

    /**
     * 标记所有通知为已读
     */
    @Update("UPDATE notification SET is_read = true, read_at = NOW(), updated_at = NOW() " +
            "WHERE user_id = #{userId} AND is_read = false AND deleted_at IS NULL")
    int markAllAsRead(@Param("userId") Long userId);
}
