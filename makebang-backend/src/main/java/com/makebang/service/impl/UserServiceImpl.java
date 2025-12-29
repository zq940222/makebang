package com.makebang.service.impl;

import com.makebang.common.exception.BusinessException;
import com.makebang.common.result.ResultCode;
import com.makebang.dto.auth.LoginRequest;
import com.makebang.dto.auth.LoginResponse;
import com.makebang.dto.auth.RegisterRequest;
import com.makebang.dto.user.ChangePasswordRequest;
import com.makebang.dto.user.UpdateUserRequest;
import com.makebang.entity.User;
import com.makebang.entity.Wallet;
import com.makebang.repository.UserRepository;
import com.makebang.repository.WalletRepository;
import com.makebang.security.JwtUtils;
import com.makebang.service.UserService;
import com.makebang.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;

    private static final String VERIFY_CODE_KEY = "verify_code:";
    private static final long VERIFY_CODE_EXPIRE = 5; // 5分钟

    @Override
    @Transactional
    public UserVO register(RegisterRequest request) {
        // 验证验证码
        if (!verifyCode(request.getPhone(), request.getVerifyCode())) {
            throw new BusinessException(ResultCode.VERIFICATION_CODE_ERROR);
        }

        // 检查用户名是否存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        // 检查手机号是否存在
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException(ResultCode.PHONE_EXISTS);
        }

        // 检查邮箱是否存在
        if (StringUtils.hasText(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ResultCode.EMAIL_EXISTS);
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setUserType(request.getUserType());
        user.setStatus(1);

        userRepository.insert(user);

        // 创建钱包
        Wallet wallet = new Wallet();
        wallet.setUserId(user.getId());
        walletRepository.insert(wallet);

        log.info("用户注册成功: {}", user.getUsername());

        // 删除验证码
        redisTemplate.delete(VERIFY_CODE_KEY + request.getPhone());

        return toVO(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // 查找用户
        User user = userRepository.findByAccount(request.getAccount())
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 检查状态
        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // 生成Token
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities("ROLE_USER")
                .build();

        String accessToken = jwtUtils.generateToken(userDetails);
        String refreshToken = jwtUtils.generateRefreshToken(userDetails);

        log.info("用户登录成功: {}", user.getUsername());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(toVO(user))
                .build();
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "刷新令牌无效或已过期");
        }

        String username = jwtUtils.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities("ROLE_USER")
                .build();

        String newAccessToken = jwtUtils.generateToken(userDetails);
        String newRefreshToken = jwtUtils.generateRefreshToken(userDetails);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(toVO(user))
                .build();
    }

    @Override
    public UserVO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        return toVO(user);
    }

    @Override
    public UserVO getUserById(Long userId) {
        User user = userRepository.selectById(userId);
        if (user == null || user.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return toVO(user);
    }

    @Override
    @Transactional
    public UserVO updateUser(UpdateUserRequest request) {
        User currentUser = getCurrentUserEntity();

        if (StringUtils.hasText(request.getAvatar())) {
            currentUser.setAvatar(request.getAvatar());
        }
        if (StringUtils.hasText(request.getEmail())) {
            // 检查邮箱是否被其他人使用
            userRepository.findByEmail(request.getEmail()).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(currentUser.getId())) {
                    throw new BusinessException(ResultCode.EMAIL_EXISTS);
                }
            });
            currentUser.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getRealName())) {
            currentUser.setRealName(request.getRealName());
        }

        userRepository.updateById(currentUser);

        return toVO(currentUser);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        // 验证确认密码
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "两次输入的密码不一致");
        }

        User currentUser = getCurrentUserEntity();

        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR, "旧密码错误");
        }

        // 更新密码
        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.updateById(currentUser);

        log.info("用户修改密码成功: {}", currentUser.getUsername());
    }

    @Override
    public void sendVerifyCode(String phone) {
        // 生成6位验证码
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));

        // 存储到Redis
        redisTemplate.opsForValue().set(
                VERIFY_CODE_KEY + phone,
                code,
                VERIFY_CODE_EXPIRE,
                TimeUnit.MINUTES
        );

        // TODO: 调用短信服务发送验证码
        log.info("发送验证码: {} -> {}", phone, code);
    }

    @Override
    public boolean verifyCode(String phone, String code) {
        String savedCode = redisTemplate.opsForValue().get(VERIFY_CODE_KEY + phone);
        return code.equals(savedCode);
    }

    @Override
    public UserVO toVO(User user) {
        if (user == null) return null;

        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .phone(UserVO.maskPhone(user.getPhone()))
                .email(UserVO.maskEmail(user.getEmail()))
                .avatar(user.getAvatar())
                .userType(user.getUserType())
                .realName(user.getRealName())
                .verified(StringUtils.hasText(user.getIdCard()))
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * 获取当前用户实体
     */
    private User getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));
    }
}
