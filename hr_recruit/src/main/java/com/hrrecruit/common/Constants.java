package com.hrrecruit.common;

/**
 * 系统常量定义
 * 集中管理所有固定不变的值，避免硬编码
 */
public final class Constants {

    private Constants() {}

    // ============ 用户状态 ============
    /** 用户状态：启用 */
    public static final int USER_STATUS_ENABLED  = 1;
    /** 用户状态：禁用 */
    public static final int USER_STATUS_DISABLED = 0;

    // ============ 岗位状态 ============
    /** 岗位状态：草稿 */
    public static final int JOB_STATUS_DRAFT    = 0;
    /** 岗位状态：已发布 */
    public static final int JOB_STATUS_PUBLISHED = 1;
    /** 岗位状态：已关闭 */
    public static final int JOB_STATUS_CLOSED   = 2;

    // ============ 应聘状态 ============
    /** 应聘状态：待筛选 */
    public static final int APP_STATUS_PENDING          = 0;
    /** 应聘状态：通过筛选 */
    public static final int APP_STATUS_PASSED           = 1;
    /** 应聘状态：面试中 */
    public static final int APP_STATUS_INTERVIEWING     = 2;
    /** 应聘状态：待确认Offer（已发录用通知，等待候选人反馈） */
    public static final int APP_STATUS_OFFER_PENDING    = 3;
    /** 应聘状态：不录用 */
    public static final int APP_STATUS_REJECTED         = 4;
    /** 应聘状态：已接受Offer（待入职） */
    public static final int APP_STATUS_OFFER_ACCEPTED   = 5;
    /** 应聘状态：已入职（最终终态） */
    public static final int APP_STATUS_ONBOARDED        = 6;

    // ============ 失败原因类型 ============
    /** 失败原因：简历淘汰 */
    public static final int REFUSE_TYPE_RESUME          = 1;
    /** 失败原因：面试淘汰 */
    public static final int REFUSE_TYPE_INTERVIEW       = 2;
    /** 失败原因：候选人拒Offer */
    public static final int REFUSE_TYPE_CANDIDATE_REFUSE = 3;
    /** 失败原因：审批不通过 */
    public static final int REFUSE_TYPE_APPROVAL        = 4;
    /** 失败原因：岗位关闭终止 */
    public static final int REFUSE_TYPE_JOB_CLOSED      = 5;

    // ============ 面试状态 ============
    /** 面试状态：待面试 */
    public static final int INTERVIEW_STATUS_PENDING    = 0;
    /** 面试状态：已面试 */
    public static final int INTERVIEW_STATUS_COMPLETED  = 1;
    /** 面试状态：已取消 */
    public static final int INTERVIEW_STATUS_CANCELLED  = 2;

    // ============ 录用状态 ============
    /** 录用状态：待确认 */
    public static final int OFFER_STATUS_PENDING   = 0;
    /** 录用状态：已接受 */
    public static final int OFFER_STATUS_ACCEPTED  = 1;
    /** 录用状态：已拒绝 */
    public static final int OFFER_STATUS_REJECTED  = 2;
    /** 录用状态：已入职 */
    public static final int OFFER_STATUS_ONBOARDED = 3;

    // ============ 简历解析状态 ============
    /** 解析状态：待解析 */
    public static final int RESUME_PARSE_PENDING  = 0;
    /** 解析状态：已解析 */
    public static final int RESUME_PARSE_SUCCESS  = 1;
    /** 解析状态：解析失败 */
    public static final int RESUME_PARSE_FAILED   = 2;
    /** 解析状态：解析失败，字段缺失 */
    public static final int RESUME_PARSE_MISSING_FIELD = 3;

    // ============ 菜单类型 ============
    /** 菜单类型：目录 */
    public static final int MENU_TYPE_DIR  = 1;
    /** 菜单类型：菜单 */
    public static final int MENU_TYPE_MENU = 2;
    /** 菜单类型：按钮 */
    public static final int MENU_TYPE_BTN  = 3;

    // ============ 通知类型 ============
    /** 通知类型：面试通知 */
    public static final int NOTIFY_TYPE_INTERVIEW = 1;
    /** 通知类型：录用通知 */
    public static final int NOTIFY_TYPE_OFFER = 2;
    /** 通知类型：系统消息 */
    public static final int NOTIFY_TYPE_SYSTEM = 3;

    // ============ 通知已读状态 ============
    /** 未读 */
    public static final int NOTIFY_UNREAD = 0;
    /** 已读 */
    public static final int NOTIFY_READ = 1;

    // ============ 面试结果 ============
    /** 面试结果：未定 */
    public static final int INTERVIEW_RESULT_PENDING = 0;
    /** 面试结果：通过 */
    public static final int INTERVIEW_RESULT_PASS = 1;
    /** 面试结果：不通过 */
    public static final int INTERVIEW_RESULT_FAIL = 2;

    // ============ 员工状态 ============
    /** 员工状态：在职 */
    public static final int EMPLOYEE_STATUS_ACTIVE = 1;
    public static final int EMPLOYEE_STATUS_ONBOARDING = 2;
    public static final int EMPLOYEE_STATUS_LEFT = 0;

    // ============ Redis Key 前缀 ============
    public static final String REDIS_TOKEN_PREFIX  = "token:";
    public static final String REDIS_USER_PREFIX   = "user:";
    public static final String REDIS_MENU_PREFIX   = "menu:";
}
