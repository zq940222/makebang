package com.makebang.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.makebang.dto.auth.LoginRequest;
import com.makebang.dto.auth.LoginResponse;
import com.makebang.dto.auth.RegisterRequest;
import com.makebang.service.UserService;
import com.makebang.vo.UserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 单元测试
 */
@WebMvcTest(AuthController.class)
@DisplayName("认证控制器测试")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserVO testUserVO;
    private LoginResponse loginResponse;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUserVO = UserVO.builder()
                .id(1L)
                .username("testuser")
                .phone("138****8000")
                .email("te***@example.com")
                .userType(1)
                .status(1)
                .role(0)
                .createdAt(LocalDateTime.now())
                .build();

        loginResponse = LoginResponse.builder()
                .accessToken("access_token_xxx")
                .refreshToken("refresh_token_xxx")
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(testUserVO)
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setPhone("13900139000");
        registerRequest.setVerifyCode("123456");
        registerRequest.setUserType(1);

        loginRequest = new LoginRequest();
        loginRequest.setAccount("testuser");
        loginRequest.setPassword("password123");
    }

    @Test
    @DisplayName("用户注册 - 成功")
    void register_Success() throws Exception {
        when(userService.register(any(RegisterRequest.class))).thenReturn(testUserVO);

        mockMvc.perform(post("/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        verify(userService).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("用户注册 - 参数验证失败")
    void register_ValidationFailed() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setUsername(""); // 空用户名

        mockMvc.perform(post("/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("用户登录 - 成功")
    void login_Success() throws Exception {
        when(userService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("access_token_xxx"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh_token_xxx"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));

        verify(userService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("用户登录 - 参数验证失败")
    void login_ValidationFailed() throws Exception {
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setAccount(""); // 空账号

        mockMvc.perform(post("/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("刷新令牌 - 成功")
    void refreshToken_Success() throws Exception {
        when(userService.refreshToken("valid_refresh_token")).thenReturn(loginResponse);

        mockMvc.perform(post("/v1/auth/refresh")
                        .with(csrf())
                        .param("refreshToken", "valid_refresh_token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("access_token_xxx"));

        verify(userService).refreshToken("valid_refresh_token");
    }

    @Test
    @DisplayName("发送验证码 - 成功")
    void sendVerifyCode_Success() throws Exception {
        doNothing().when(userService).sendVerifyCode("13800138000");

        mockMvc.perform(post("/v1/auth/verify-code")
                        .with(csrf())
                        .param("phone", "13800138000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).sendVerifyCode("13800138000");
    }

    @Test
    @DisplayName("发送验证码 - 手机号格式错误")
    void sendVerifyCode_InvalidPhone() throws Exception {
        mockMvc.perform(post("/v1/auth/verify-code")
                        .with(csrf())
                        .param("phone", "12345"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("检查用户名是否可用")
    void checkUsername_Success() throws Exception {
        mockMvc.perform(get("/v1/auth/check-username")
                        .param("username", "newuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("检查手机号是否已注册")
    void checkPhone_Success() throws Exception {
        mockMvc.perform(get("/v1/auth/check-phone")
                        .param("phone", "13800138000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
