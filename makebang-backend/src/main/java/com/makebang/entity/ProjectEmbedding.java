package com.makebang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目语义向量实体
 */
@Data
@TableName("project_embedding")
public class ProjectEmbedding {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 语义向量（1536维）
     * 注意：MyBatis-Plus 不直接支持 vector 类型，需要通过自定义 SQL 处理
     */
    @TableField(exist = false)
    private float[] embedding;

    /**
     * 内容哈希（用于检测内容是否变化）
     */
    private String contentHash;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
