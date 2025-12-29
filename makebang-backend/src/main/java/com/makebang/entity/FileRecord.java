package com.makebang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("file_record")
public class FileRecord extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 上传用户ID
     */
    private Long userId;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 存储文件名
     */
    private String storedName;

    /**
     * 文件存储路径
     */
    private String filePath;

    /**
     * 文件访问URL
     */
    private String fileUrl;

    /**
     * 缩略图URL
     */
    private String thumbnailUrl;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型（后缀）
     */
    private String fileType;

    /**
     * MIME类型
     */
    private String mimeType;

    /**
     * 存储类型
     */
    private String storageType;

    /**
     * 文件MD5哈希
     */
    private String hash;

    /**
     * 图片宽度
     */
    private Integer width;

    /**
     * 图片高度
     */
    private Integer height;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务ID
     */
    private Long businessId;

    /**
     * 状态: 0-已删除 1-正常
     */
    private Integer status;

    /**
     * 业务类型枚举
     */
    public enum BusinessType {
        AVATAR("avatar", "用户头像"),
        PROJECT("project", "项目附件"),
        PORTFOLIO("portfolio", "作品集"),
        MESSAGE("message", "消息附件"),
        OTHER("other", "其他");

        public final String code;
        public final String desc;

        BusinessType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }
}
