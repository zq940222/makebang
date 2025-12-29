package com.makebang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 热门搜索关键词实体
 */
@Data
@TableName("hot_keyword")
public class HotKeyword {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关键词
     */
    private String keyword;

    /**
     * 搜索次数
     */
    private Integer searchCount;

    /**
     * 最后搜索时间
     */
    private LocalDateTime lastSearchedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
