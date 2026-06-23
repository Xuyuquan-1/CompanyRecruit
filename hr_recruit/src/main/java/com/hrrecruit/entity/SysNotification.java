package com.hrrecruit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息通知实体
 */
@Data
@TableName("sys_notification")
public class SysNotification {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收人ID */
    private Long userId;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 类型：1-面试通知，2-录用通知，3-系统消息 */
    private Integer type;

    /** 是否已读：0-否，1-是 */
    private Integer isRead;

    private LocalDateTime sendTime;
}
