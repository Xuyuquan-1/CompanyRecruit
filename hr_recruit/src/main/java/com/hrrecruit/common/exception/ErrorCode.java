package com.hrrecruit.common.exception;

import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
public enum ErrorCode {

    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 业务错误码 1000-1999
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "密码错误"),
    USER_DISABLED(1003, "用户已被禁用"),
    TOKEN_EXPIRED(1004, "Token已过期"),
    TOKEN_INVALID(1005, "Token无效"),

    // 课程相关 2000-2999
    COURSE_NOT_FOUND(2001, "课程不存在"),
    COURSE_NOT_ENROLLABLE(2002, "课程不可报名"),
    COURSE_FULL(2003, "课程已满员"),
    COURSE_ALREADY_ENROLLED(2004, "已报名该课程"),
    COURSE_ENROLLMENT_FAILED(2005, "报名失败"),

    // 考试相关 3000-3999
    EXAM_NOT_FOUND(3001, "考试不存在"),
    EXAM_NOT_STARTED(3002, "考试未开始"),
    EXAM_ALREADY_ENDED(3003, "考试已结束"),
    EXAM_ALREADY_SUBMITTED(3004, "考试已提交"),
    EXAM_TIME_UP(3005, "考试时间已到"),
    EXAM_RECORD_NOT_FOUND(3006, "考试记录不存在"),
    EXAM_PAPER_NOT_FOUND(3007, "试卷不存在"),

    // 工单相关 4000-4999
    WORK_ORDER_NOT_FOUND(4001, "工单不存在"),
    WORK_ORDER_STATUS_ERROR(4002, "工单状态错误"),
    WORK_ORDER_NOT_PROCESSABLE(4003, "工单不可处理"),
    WORK_ORDER_EXISTS(4004, "工单已存在"),

    // 文件相关 5000-5999
    FILE_UPLOAD_ERROR(5001, "文件上传失败"),
    FILE_NOT_FOUND(5002, "文件不存在"),
    FILE_TYPE_NOT_SUPPORTED(5003, "不支持的文件类型"),
    FILE_SIZE_EXCEEDED(5004, "文件大小超出限制"),

    // 讲师相关 6000-6999
    TEACHER_NOT_FOUND(6001, "讲师不存在"),
    TEACHER_NAME_EXISTS(6002, "讲师姓名已存在");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
