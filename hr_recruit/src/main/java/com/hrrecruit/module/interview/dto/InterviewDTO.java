package com.hrrecruit.module.interview.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 面试安排请求参数
 */
@Data
public class InterviewDTO {

    @NotNull(message = "应聘记录ID不能为空")
    private Long applicationId;

    @NotNull(message = "面试时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime interviewTime;

    /** 面试地点 */
    private String location;

    /** 面试官ID */
    private Long interviewerId;

    /** 面试官姓名 */
    private String interviewerName;
}