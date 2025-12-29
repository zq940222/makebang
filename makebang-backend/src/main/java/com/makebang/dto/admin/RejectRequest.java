package com.makebang.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 拒绝/驳回请求
 */
@Data
public class RejectRequest {

    /**
     * 拒绝原因
     */
    @NotBlank(message = "原因不能为空")
    @Size(min = 2, max = 500, message = "原因长度应在2-500字符之间")
    private String reason;
}
