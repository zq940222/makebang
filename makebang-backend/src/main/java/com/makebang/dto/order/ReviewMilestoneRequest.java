package com.makebang.dto.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 验收里程碑请求
 */
@Data
public class ReviewMilestoneRequest {

    /**
     * 是否通过
     */
    @NotNull(message = "请选择是否通过")
    private Boolean approved;

    /**
     * 验收意见
     */
    @Size(max = 2000, message = "验收意见最多2000字符")
    private String reviewNote;
}
