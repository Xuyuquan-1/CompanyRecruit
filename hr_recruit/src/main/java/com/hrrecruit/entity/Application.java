package com.hrrecruit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 应聘记录实体
 */
@Data
@TableName("application")
public class Application {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 岗位ID */
    private Long jobId;

    /** 简历ID */
    private Long resumeId;

    /** 应聘者用户ID */
    private Long candidateId;

    /** 状态：0-待筛选，1-通过筛选，2-面试中，3-录用，4-不录用 */
    private Integer status;

    /** 标签 */
    private String tags;

    /** 备注 */
    private String remark;

    /** 智能匹配度分数 */
    private BigDecimal matchScore;

    private LocalDateTime applyTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String resumeName;

    @TableField(exist = false)
    private String jobTitle;

    @TableField(exist = false)
    private String jobDepartment;

    @TableField(exist = false)
    private String candidateName;

    @TableField(exist = false)
    private String phone;
}