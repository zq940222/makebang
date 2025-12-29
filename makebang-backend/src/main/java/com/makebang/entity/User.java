package com.makebang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("\"user\"")
public class User extends BaseEntity {

    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 用户类型: 0-需求方 1-程序员 2-两者
     */
    private Integer userType;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 状态: 0-禁用 1-正常
     */
    private Integer status;

    /**
     * 角色: 0-普通用户 1-管理员 2-超级管理员
     */
    private Integer role;

    /**
     * 角色枚举
     */
    public enum Role {
        USER(0, "普通用户"),
        ADMIN(1, "管理员"),
        SUPER_ADMIN(2, "超级管理员");

        public final int code;
        public final String desc;

        Role(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static String getDesc(int code) {
            for (Role role : values()) {
                if (role.code == code) {
                    return role.desc;
                }
            }
            return "未知";
        }
    }
}
