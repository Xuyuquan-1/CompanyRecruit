package com.hrrecruit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 事件失败记录实体（死信表）
 */
@Data
@TableName("sys_event_failure")
public class SysEventFailure {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 事件类型 */
    private String eventType;

    /** 事件体（JSON） */
    private String eventBody;

    /** 失败原因 */
    private String errorMessage;

    /** 已重试次数 */
    private Integer retryCount;

    /** 状态：0-待重试，1-重试成功，2-最终失败 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
