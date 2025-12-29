package com.makebang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 项目/需求实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "project", autoResultMap = true)
public class Project extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发布者ID
     */
    private Long userId;

    /**
     * 项目标题
     */
    private String title;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 最低预算
     */
    private BigDecimal budgetMin;

    /**
     * 最高预算
     */
    private BigDecimal budgetMax;

    /**
     * 截止日期
     */
    private LocalDate deadline;

    /**
     * 技能要求
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> skillRequirements;

    /**
     * 附件URL
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> attachmentUrls;

    /**
     * 状态: 0-草稿 1-开放 2-进行中 3-已完成 4-已取消 5-已关闭
     */
    private Integer status;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 投标数
     */
    private Integer bidCount;

    /**
     * 状态枚举
     */
    public enum Status {
        DRAFT(0, "草稿"),
        OPEN(1, "开放中"),
        IN_PROGRESS(2, "进行中"),
        COMPLETED(3, "已完成"),
        CANCELLED(4, "已取消"),
        CLOSED(5, "已关闭");

        public final int code;
        public final String desc;

        Status(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static String getDesc(int code) {
            for (Status s : values()) {
                if (s.code == code) return s.desc;
            }
            return "未知";
        }
    }
}
