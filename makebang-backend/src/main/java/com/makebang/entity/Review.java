package com.makebang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 评价实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("review")
public class Review extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 评价者ID
     */
    private Long reviewerId;

    /**
     * 被评价者ID
     */
    private Long revieweeId;

    /**
     * 评价类型：1-雇主评价开发者 2-开发者评价雇主
     */
    private Integer type;

    /**
     * 综合评分（1-5星）
     */
    private Integer rating;

    /**
     * 技能/专业评分
     */
    private Integer skillRating;

    /**
     * 沟通评分
     */
    private Integer communicationRating;

    /**
     * 态度/配合度评分
     */
    private Integer attitudeRating;

    /**
     * 交付/付款及时性评分
     */
    private Integer timelinessRating;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 评价标签（JSON数组）
     */
    private String tags;

    /**
     * 是否匿名
     */
    private Boolean isAnonymous;

    /**
     * 回复内容
     */
    private String reply;

    /**
     * 评价类型枚举
     */
    public enum Type {
        EMPLOYER_TO_DEVELOPER(1, "雇主评价开发者"),
        DEVELOPER_TO_EMPLOYER(2, "开发者评价雇主");

        public final int code;
        public final String desc;

        Type(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static String getDesc(int code) {
            for (Type type : values()) {
                if (type.code == code) {
                    return type.desc;
                }
            }
            return "未知";
        }
    }
}
