package com.makebang.service;

import com.makebang.dto.auth.LoginRequest;
import com.makebang.dto.auth.LoginResponse;
import com.makebang.dto.auth.RegisterRequest;
import com.makebang.dto.user.ChangePasswordRequest;
import com.makebang.dto.user.UpdateUserRequest;
import com.makebang.entity.User;
import com.makebang.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    UserVO register(RegisterRequest request);

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 刷新令牌
     */
    LoginResponse refreshToken(String refreshToken);

    /**
     * 获取当前用户信息
     */
    UserVO getCurrentUser();

    /**
     * 根据ID获取用户信息
     */
    UserVO getUserById(Long userId);

    /**
     * 更新用户信息
     */
    UserVO updateUser(UpdateUserRequest request);

    /**
     * 修改密码
     */
    void changePassword(ChangePasswordRequest request);

    /**
     * 发送验证码
     */
    void sendVerifyCode(String phone);

    /**
     * 验证验证码
     */
    boolean verifyCode(String phone, String code);

    /**
     * 实体转VO
     */
    UserVO toVO(User user);
}
