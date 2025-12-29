package com.makebang.controller;

import com.makebang.common.result.Result;
import com.makebang.dto.auth.LoginRequest;
import com.makebang.dto.auth.LoginResponse;
import com.makebang.dto.auth.RegisterRequest;
import com.makebang.service.UserService;
import com.makebang.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "用户注册、登录、令牌刷新等接口")
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final UserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(userService.register(request));
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(@RequestParam String refreshToken) {
        return Result.success(userService.refreshToken(refreshToken));
    }

    @Operation(summary = "发送验证码")
    @PostMapping("/verify-code")
    public Result<Void> sendVerifyCode(
            @RequestParam
            @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号")
            String phone
    ) {
        userService.sendVerifyCode(phone);
        return Result.success();
    }

    @Operation(summary = "检查用户名是否可用")
    @GetMapping("/check-username")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        // 返回true表示可用
        return Result.success(!checkUsernameExists(username));
    }

    @Operation(summary = "检查手机号是否已注册")
    @GetMapping("/check-phone")
    public Result<Boolean> checkPhone(
            @RequestParam
            @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号")
            String phone
    ) {
        // 返回true表示可用
        return Result.success(!checkPhoneExists(phone));
    }

    /**
     * 检查用户名是否存在(内部使用)
     */
    private boolean checkUsernameExists(String username) {
        try {
            userService.getCurrentUser();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查手机号是否存在(内部使用)
     */
    private boolean checkPhoneExists(String phone) {
        // TODO: 添加UserService方法
        return false;
    }
}
