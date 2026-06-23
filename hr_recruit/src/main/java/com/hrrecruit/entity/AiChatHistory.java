package com.hrrecruit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI对话历史实体
 */
@Data
@TableName("ai_chat_history")
public class AiChatHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 用户问题 */
    private String question;

    /** AI回答 */
    private String answer;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}