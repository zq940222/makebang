package com.makebang.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.makebang.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * 用户数据访问接口
 */
@Mapper
public interface UserRepository extends BaseMapper<User> {

    /**
     * 根据用户名查找用户
     */
    @Select("SELECT * FROM \"user\" WHERE username = #{username} AND deleted_at IS NULL")
    Optional<User> findByUsername(@Param("username") String username);

    /**
     * 根据手机号查找用户
     */
    @Select("SELECT * FROM \"user\" WHERE phone = #{phone} AND deleted_at IS NULL")
    Optional<User> findByPhone(@Param("phone") String phone);

    /**
     * 根据邮箱查找用户
     */
    @Select("SELECT * FROM \"user\" WHERE email = #{email} AND deleted_at IS NULL")
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * 根据用户名、手机号或邮箱查找用户(用于登录)
     */
    @Select("SELECT * FROM \"user\" WHERE (username = #{account} OR phone = #{account} OR email = #{account}) AND deleted_at IS NULL")
    Optional<User> findByAccount(@Param("account") String account);

    /**
     * 检查用户名是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM \"user\" WHERE username = #{username} AND deleted_at IS NULL")
    boolean existsByUsername(@Param("username") String username);

    /**
     * 检查手机号是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM \"user\" WHERE phone = #{phone} AND deleted_at IS NULL")
    boolean existsByPhone(@Param("phone") String phone);

    /**
     * 检查邮箱是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM \"user\" WHERE email = #{email} AND deleted_at IS NULL")
    boolean existsByEmail(@Param("email") String email);
}
