package com.makebang.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 设置角色请求
 */
@Data
public class SetRoleRequest {

    /**
     * 角色: 0-普通用户 1-管理员 2-超级管理员
     */
    @NotNull(message = "角色不能为空")
    @Min(value = 0, message = "角色值无效")
    @Max(value = 2, message = "角色值无效")
    private Integer role;
}
