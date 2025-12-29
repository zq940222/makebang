package com.makebang.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发送消息请求
 */
@Data
public class SendMessageRequest {

    /**
     * 接收者ID
     */
    @NotNull(message = "接收者不能为空")
    private Long receiverId;

    /**
     * 消息类型：1-文本 2-图片 3-文件
     */
    private Integer type = 1;

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 5000, message = "消息内容不能超过5000字符")
    private String content;

    /**
     * 附件URL
     */
    private String attachmentUrl;

    /**
     * 附件名称
     */
    private String attachmentName;

    /**
     * 关联订单ID（可选，用于订单会话）
     */
    private Long orderId;
}
