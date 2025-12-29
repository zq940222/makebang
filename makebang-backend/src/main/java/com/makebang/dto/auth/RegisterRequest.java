package com.makebang.dto.auth;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 注册请求DTO
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度为3-20个字符")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号")
    private String phone;

    @Email(message = "请输入正确的邮箱地址")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20个字符")
    private String password;

    @NotBlank(message = "验证码不能为空")
    private String verifyCode;

    @NotNull(message = "用户类型不能为空")
    @Min(value = 0, message = "用户类型无效")
    @Max(value = 2, message = "用户类型无效")
    private Integer userType;
}
