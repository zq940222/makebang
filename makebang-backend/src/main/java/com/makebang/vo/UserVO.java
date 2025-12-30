package com.makebang.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {

    private Long id;

    private String username;

    /**
     * 脱敏后的手机号
     */
    private String phone;

    /**
     * 脱敏后的邮箱
     */
    private String email;

    private String avatar;

    /**
     * 用户类型: 0-需求方 1-程序员 2-两者
     */
    private Integer userType;

    /**
     * 用户类型描述
     */
    private String userTypeDesc;

    private String realName;

    /**
     * 是否实名认证
     */
    private Boolean verified;

    private Integer status;

    /**
     * 角色: 0-普通用户 1-管理员 2-超级管理员
     */
    private Integer role;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 获取用户类型描述
     */
    public String getUserTypeDesc() {
        if (userType == null) return null;
        return switch (userType) {
            case 0 -> "需求方";
            case 1 -> "程序员";
            case 2 -> "需求方/程序员";
            default -> "未知";
        };
    }

    /**
     * 脱敏手机号
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 脱敏邮箱
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        int atIndex = email.indexOf("@");
        if (atIndex <= 2) return email;
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }
}
