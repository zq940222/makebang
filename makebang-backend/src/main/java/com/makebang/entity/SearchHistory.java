package com.makebang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 搜索历史实体
 */
@Data
@TableName("search_history")
public class SearchHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（可为空，匿名搜索）
     */
    private Long userId;

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 搜索类型
     */
    private String searchType;

    /**
     * 搜索结果数量
     */
    private Integer resultCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 搜索类型枚举
     */
    public enum Type {
        KEYWORD("keyword", "关键词搜索"),
        SEMANTIC("semantic", "语义搜索");

        public final String code;
        public final String desc;

        Type(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }
}
