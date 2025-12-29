package com.makebang.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户信息请求DTO
 */
@Data
public class UpdateUserRequest {

    @Size(max = 500, message = "头像URL过长")
    private String avatar;

    @Email(message = "请输入正确的邮箱地址")
    private String email;

    @Size(max = 50, message = "真实姓名过长")
    private String realName;
}
