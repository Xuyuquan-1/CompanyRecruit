package com.hrrecruit.module.interview.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 面试分页查询参数
 */
@Data
public class InterviewQueryDTO {

    /** 面试状态 */
    private Integer status;

    /** 面试官ID */
    private Long interviewerId;

    /** 应聘者姓名（模糊搜索） */
    private String candidateName;

    /** 面试时间范围-开始 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /** 面试时间范围-结束 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 当前页码 */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;

    /** 应聘者用户ID（后端根据角色自动填充，前端不需要传） */
    private Long candidateId;
}