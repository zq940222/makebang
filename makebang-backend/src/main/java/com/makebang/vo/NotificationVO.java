package com.makebang.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知VO
 */
@Data
@Builder
public class NotificationVO {

    private Long id;

    /**
     * 通知类型
     */
    private Integer type;

    /**
     * 类型描述
     */
    private String typeDesc;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 关联业务类型
     */
    private String bizType;

    /**
     * 关联业务ID
     */
    private Long bizId;

    /**
     * 跳转链接
     */
    private String link;

    /**
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 阅读时间
     */
    private LocalDateTime readAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
