package com.makebang.service;

import com.makebang.common.exception.BusinessException;
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
import com.makebang.service.impl.UserServiceImpl;
import com.makebang.vo.UserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务测试")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // 初始化测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$encoded_password");
        testUser.setPhone("13800138000");
        testUser.setEmail("test@example.com");
        testUser.setUserType(1);
        testUser.setStatus(1);
        testUser.setRole(0);
        testUser.setCreatedAt(LocalDateTime.now());

        // 初始化注册请求
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setPhone("13900139000");
        registerRequest.setEmail("new@example.com");
        registerRequest.setVerifyCode("123456");
        registerRequest.setUserType(1);

        // 初始化登录请求
        loginRequest = new LoginRequest();
        loginRequest.setAccount("testuser");
        loginRequest.setPassword("password123");
    }

    // ========== 注册测试 ==========

    @Test
    @DisplayName("用户注册 - 成功")
    void register_Success() {
        // Mock 数据
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("verify_code:13900139000")).thenReturn("123456");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByPhone("13900139000")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encoded");
        when(userRepository.insert(any(User.class))).thenReturn(1);
        when(walletRepository.insert(any(Wallet.class))).thenReturn(1);
        when(redisTemplate.delete("verify_code:13900139000")).thenReturn(true);

        // 执行测试
        UserVO result = userService.register(registerRequest);

        // 验证结果
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        verify(userRepository).insert(any(User.class));
        verify(walletRepository).insert(any(Wallet.class));
    }

    @Test
    @DisplayName("用户注册 - 验证码错误")
    void register_InvalidVerifyCode() {
        // Mock 数据
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("verify_code:13900139000")).thenReturn("654321");

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> userService.register(registerRequest));
    }

    @Test
    @DisplayName("用户注册 - 用户名已存在")
    void register_UsernameExists() {
        // Mock 数据
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("verify_code:13900139000")).thenReturn("123456");
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> userService.register(registerRequest));
    }

    @Test
    @DisplayName("用户注册 - 手机号已存在")
    void register_PhoneExists() {
        // Mock 数据
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("verify_code:13900139000")).thenReturn("123456");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByPhone("13900139000")).thenReturn(true);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> userService.register(registerRequest));
    }

    // ========== 登录测试 ==========

    @Test
    @DisplayName("用户登录 - 成功")
    void login_Success() {
        // Mock 数据
        when(userRepository.findByAccount("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);
        when(jwtUtils.generateToken(any(UserDetails.class))).thenReturn("access_token");
        when(jwtUtils.generateRefreshToken(any(UserDetails.class))).thenReturn("refresh_token");

        // 执行测试
        LoginResponse result = userService.login(loginRequest);

        // 验证结果
        assertNotNull(result);
        assertEquals("access_token", result.getAccessToken());
        assertEquals("refresh_token", result.getRefreshToken());
        assertEquals("Bearer", result.getTokenType());
        assertNotNull(result.getUser());
    }

    @Test
    @DisplayName("用户登录 - 用户不存在")
    void login_UserNotFound() {
        // Mock 数据
        when(userRepository.findByAccount("testuser")).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> userService.login(loginRequest));
    }

    @Test
    @DisplayName("用户登录 - 密码错误")
    void login_WrongPassword() {
        // Mock 数据
        when(userRepository.findByAccount("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(false);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> userService.login(loginRequest));
    }

    @Test
    @DisplayName("用户登录 - 账户已禁用")
    void login_AccountDisabled() {
        // Mock 数据
        testUser.setStatus(0);
        when(userRepository.findByAccount("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> userService.login(loginRequest));
    }

    // ========== 刷新令牌测试 ==========

    @Test
    @DisplayName("刷新令牌 - 成功")
    void refreshToken_Success() {
        // Mock 数据
        when(jwtUtils.validateToken("valid_refresh_token")).thenReturn(true);
        when(jwtUtils.getUsernameFromToken("valid_refresh_token")).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtUtils.generateToken(any(UserDetails.class))).thenReturn("new_access_token");
        when(jwtUtils.generateRefreshToken(any(UserDetails.class))).thenReturn("new_refresh_token");

        // 执行测试
        LoginResponse result = userService.refreshToken("valid_refresh_token");

        // 验证结果
        assertNotNull(result);
        assertEquals("new_access_token", result.getAccessToken());
        assertEquals("new_refresh_token", result.getRefreshToken());
    }

    @Test
    @DisplayName("刷新令牌 - 令牌无效")
    void refreshToken_InvalidToken() {
        // Mock 数据
        when(jwtUtils.validateToken("invalid_token")).thenReturn(false);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> userService.refreshToken("invalid_token"));
    }

    // ========== 获取当前用户测试 ==========

    @Test
    @DisplayName("获取当前用户 - 成功")
    void getCurrentUser_Success() {
        // Mock Security Context
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // 执行测试
        UserVO result = userService.getCurrentUser();

        // 验证结果
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());

        // 清理
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("获取当前用户 - 未授权")
    void getCurrentUser_Unauthorized() {
        // Mock Security Context
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> userService.getCurrentUser());

        // 清理
        SecurityContextHolder.clearContext();
    }

    // ========== 获取用户测试 ==========

    @Test
    @DisplayName("根据ID获取用户 - 成功")
    void getUserById_Success() {
        // Mock 数据
        when(userRepository.selectById(1L)).thenReturn(testUser);

        // 执行测试
        UserVO result = userService.getUserById(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    @DisplayName("根据ID获取用户 - 用户不存在")
    void getUserById_NotFound() {
        // Mock 数据
        when(userRepository.selectById(999L)).thenReturn(null);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> userService.getUserById(999L));
    }

    @Test
    @DisplayName("根据ID获取用户 - 用户已删除")
    void getUserById_Deleted() {
        // Mock 数据
        testUser.setDeletedAt(LocalDateTime.now());
        when(userRepository.selectById(1L)).thenReturn(testUser);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> userService.getUserById(1L));
    }

    // ========== 更新用户测试 ==========

    @Test
    @DisplayName("更新用户信息 - 成功")
    void updateUser_Success() {
        // Mock Security Context
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.updateById(any(User.class))).thenReturn(1);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setAvatar("http://example.com/avatar.png");
        request.setRealName("张三");

        // 执行测试
        UserVO result = userService.updateUser(request);

        // 验证结果
        assertNotNull(result);
        verify(userRepository).updateById(any(User.class));

        // 清理
        SecurityContextHolder.clearContext();
    }

    // ========== 修改密码测试 ==========

    @Test
    @DisplayName("修改密码 - 成功")
    void changePassword_Success() {
        // Mock Security Context
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("$2a$10$new_encoded");
        when(userRepository.updateById(any(User.class))).thenReturn(1);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPassword");
        request.setNewPassword("newPassword");
        request.setConfirmPassword("newPassword");

        // 执行测试
        assertDoesNotThrow(() -> userService.changePassword(request));

        // 验证结果
        verify(userRepository).updateById(any(User.class));

        // 清理
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("修改密码 - 两次密码不一致")
    void changePassword_PasswordMismatch() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPassword");
        request.setNewPassword("newPassword");
        request.setConfirmPassword("differentPassword");

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> userService.changePassword(request));
    }

    @Test
    @DisplayName("修改密码 - 旧密码错误")
    void changePassword_WrongOldPassword() {
        // Mock Security Context
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongOldPassword", testUser.getPassword())).thenReturn(false);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrongOldPassword");
        request.setNewPassword("newPassword");
        request.setConfirmPassword("newPassword");

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> userService.changePassword(request));

        // 清理
        SecurityContextHolder.clearContext();
    }

    // ========== 验证码测试 ==========

    @Test
    @DisplayName("发送验证码 - 成功")
    void sendVerifyCode_Success() {
        // Mock 数据
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 执行测试
        assertDoesNotThrow(() -> userService.sendVerifyCode("13800138000"));

        // 验证结果
        verify(valueOperations).set(eq("verify_code:13800138000"), anyString(), eq(5L), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("验证验证码 - 成功")
    void verifyCode_Success() {
        // Mock 数据
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("verify_code:13800138000")).thenReturn("123456");

        // 执行测试
        boolean result = userService.verifyCode("13800138000", "123456");

        // 验证结果
        assertTrue(result);
    }

    @Test
    @DisplayName("验证验证码 - 失败")
    void verifyCode_Failed() {
        // Mock 数据
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("verify_code:13800138000")).thenReturn("654321");

        // 执行测试
        boolean result = userService.verifyCode("13800138000", "123456");

        // 验证结果
        assertFalse(result);
    }

    // ========== toVO 测试 ==========

    @Test
    @DisplayName("转换为VO - 成功")
    void toVO_Success() {
        // 执行测试
        UserVO result = userService.toVO(testUser);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("138****8000", result.getPhone()); // 脱敏后的手机号
        assertEquals("te***@example.com", result.getEmail()); // 脱敏后的邮箱
        assertEquals(1, result.getUserType());
        assertEquals(1, result.getStatus());
        assertEquals(0, result.getRole());
    }

    @Test
    @DisplayName("转换为VO - null输入")
    void toVO_NullInput() {
        // 执行测试
        UserVO result = userService.toVO(null);

        // 验证结果
        assertNull(result);
    }
}
