package com.makebang.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 提交里程碑请求
 */
@Data
public class SubmitMilestoneRequest {

    @NotBlank(message = "提交说明不能为空")
    @Size(min = 10, max = 2000, message = "提交说明长度需在10-2000字符之间")
    private String submitNote;
}
