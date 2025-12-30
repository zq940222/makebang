package com.makebang.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.makebang.dto.user.ChangePasswordRequest;
import com.makebang.dto.user.UpdateUserRequest;
import com.makebang.service.UserService;
import com.makebang.vo.UserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 单元测试
 */
@WebMvcTest(UserController.class)
@DisplayName("用户控制器测试")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserVO testUserVO;

    @BeforeEach
    void setUp() {
        testUserVO = UserVO.builder()
                .id(1L)
                .username("testuser")
                .phone("138****8000")
                .email("te***@example.com")
                .avatar("http://example.com/avatar.png")
                .userType(1)
                .realName("张三")
                .status(1)
                .role(0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("获取当前用户信息 - 成功")
    @WithMockUser(username = "testuser")
    void getCurrentUser_Success() throws Exception {
        when(userService.getCurrentUser()).thenReturn(testUserVO);

        mockMvc.perform(get("/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.phone").value("138****8000"));

        verify(userService).getCurrentUser();
    }

    @Test
    @DisplayName("获取当前用户信息 - 未登录")
    void getCurrentUser_Unauthorized() throws Exception {
        mockMvc.perform(get("/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("更新当前用户信息 - 成功")
    @WithMockUser(username = "testuser")
    void updateCurrentUser_Success() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setAvatar("http://example.com/new-avatar.png");
        request.setRealName("李四");
        request.setEmail("newemail@example.com");

        UserVO updatedUser = UserVO.builder()
                .id(1L)
                .username("testuser")
                .avatar("http://example.com/new-avatar.png")
                .realName("李四")
                .email("ne***@example.com")
                .build();

        when(userService.updateUser(any(UpdateUserRequest.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/v1/users/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.avatar").value("http://example.com/new-avatar.png"))
                .andExpect(jsonPath("$.data.realName").value("李四"));

        verify(userService).updateUser(any(UpdateUserRequest.class));
    }

    @Test
    @DisplayName("更新当前用户信息 - 未登录")
    void updateCurrentUser_Unauthorized() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setAvatar("http://example.com/avatar.png");

        mockMvc.perform(put("/v1/users/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("修改密码 - 成功")
    @WithMockUser(username = "testuser")
    void changePassword_Success() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPassword123");
        request.setNewPassword("newPassword456");
        request.setConfirmPassword("newPassword456");

        doNothing().when(userService).changePassword(any(ChangePasswordRequest.class));

        mockMvc.perform(put("/v1/users/me/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).changePassword(any(ChangePasswordRequest.class));
    }

    @Test
    @DisplayName("修改密码 - 参数验证失败")
    @WithMockUser(username = "testuser")
    void changePassword_ValidationFailed() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword(""); // 空密码
        request.setNewPassword("new");
        request.setConfirmPassword("new");

        mockMvc.perform(put("/v1/users/me/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("根据ID获取用户信息 - 成功")
    @WithMockUser(username = "testuser")
    void getUserById_Success() throws Exception {
        when(userService.getUserById(1L)).thenReturn(testUserVO);

        mockMvc.perform(get("/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        verify(userService).getUserById(1L);
    }

    @Test
    @DisplayName("根据ID获取用户信息 - 未登录")
    void getUserById_Unauthorized() throws Exception {
        mockMvc.perform(get("/v1/users/1"))
                .andExpect(status().isUnauthorized());
    }
}
