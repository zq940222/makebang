package com.makebang.common.result;

import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权,请先登录"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    CONFLICT(409, "资源冲突"),

    // 服务端错误 5xx
    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂时不可用"),

    // 用户模块 1xxx
    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_EXISTS(1002, "用户名已存在"),
    PHONE_EXISTS(1003, "手机号已注册"),
    EMAIL_EXISTS(1004, "邮箱已注册"),
    PASSWORD_ERROR(1005, "密码错误"),
    ACCOUNT_DISABLED(1006, "账号已被禁用"),
    VERIFICATION_CODE_ERROR(1007, "验证码错误"),
    VERIFICATION_CODE_EXPIRED(1008, "验证码已过期"),
    USER_NOT_VERIFIED(1009, "用户未实名认证"),

    // 项目模块 2xxx
    PROJECT_NOT_FOUND(2001, "项目不存在"),
    PROJECT_CLOSED(2002, "项目已关闭"),
    PROJECT_ALREADY_ASSIGNED(2003, "项目已分配"),
    INVALID_PROJECT_STATUS(2004, "项目状态不正确"),

    // 投标模块 2500
    BID_NOT_FOUND(2501, "投标不存在"),
    BID_ALREADY_EXISTS(2502, "您已投标该项目"),
    CANNOT_BID_OWN_PROJECT(2503, "不能投标自己的项目"),

    // 订单模块 3xxx
    ORDER_NOT_FOUND(3001, "订单不存在"),
    INVALID_ORDER_STATUS(3002, "订单状态不正确"),
    ORDER_CANNOT_CANCEL(3003, "订单无法取消"),
    ORDER_ALREADY_PAID(3004, "订单已支付"),

    // 支付模块 4xxx
    PAYMENT_FAILED(4001, "支付失败"),
    INSUFFICIENT_BALANCE(4002, "余额不足"),
    WITHDRAWAL_FAILED(4003, "提现失败"),

    // 消息模块 5xxx
    MESSAGE_SEND_FAILED(5001, "消息发送失败"),

    // 文件模块 6xxx
    FILE_UPLOAD_FAILED(6001, "文件上传失败"),
    FILE_TYPE_NOT_ALLOWED(6002, "文件类型不允许"),
    FILE_SIZE_EXCEEDED(6003, "文件大小超出限制");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
