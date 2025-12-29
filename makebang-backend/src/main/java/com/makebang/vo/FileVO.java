package com.makebang.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件VO
 */
@Data
@Builder
public class FileVO {

    /**
     * 文件ID
     */
    private Long id;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 文件URL
     */
    private String url;

    /**
     * 缩略图URL（图片类型）
     */
    private String thumbnailUrl;

    /**
     * 文件大小（字节）
     */
    private Long size;

    /**
     * 文件类型（后缀）
     */
    private String type;

    /**
     * MIME类型
     */
    private String mimeType;

    /**
     * 图片宽度
     */
    private Integer width;

    /**
     * 图片高度
     */
    private Integer height;

    /**
     * 上传时间
     */
    private LocalDateTime createdAt;
}
