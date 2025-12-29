package com.makebang.dto.review;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * 创建评价请求
 */
@Data
public class CreateReviewRequest {

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 综合评分（1-5）
     */
    @NotNull(message = "综合评分不能为空")
    @Min(value = 1, message = "评分最低为1")
    @Max(value = 5, message = "评分最高为5")
    private Integer rating;

    /**
     * 技能/专业评分
     */
    @Min(value = 1, message = "评分最低为1")
    @Max(value = 5, message = "评分最高为5")
    private Integer skillRating;

    /**
     * 沟通评分
     */
    @Min(value = 1, message = "评分最低为1")
    @Max(value = 5, message = "评分最高为5")
    private Integer communicationRating;

    /**
     * 态度评分
     */
    @Min(value = 1, message = "评分最低为1")
    @Max(value = 5, message = "评分最高为5")
    private Integer attitudeRating;

    /**
     * 及时性评分
     */
    @Min(value = 1, message = "评分最低为1")
    @Max(value = 5, message = "评分最高为5")
    private Integer timelinessRating;

    /**
     * 评价内容
     */
    @NotBlank(message = "评价内容不能为空")
    @Size(min = 10, max = 1000, message = "评价内容需要10-1000个字符")
    private String content;

    /**
     * 评价标签
     */
    private List<String> tags;

    /**
     * 是否匿名
     */
    private Boolean isAnonymous = false;
}
