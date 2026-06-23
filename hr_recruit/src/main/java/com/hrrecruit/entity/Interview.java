package com.hrrecruit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 面试实体
 */
@Data
@TableName("interview")
public class Interview {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 应聘记录ID */
    private Long applicationId;

    /** 面试时间 */
    private LocalDateTime interviewTime;

    /** 面试地点 */
    private String location;

    /** 面试官ID */
    private Long interviewerId;

    /** 面试官姓名 */
    private String interviewerName;

    /** 状态：0-待面试，1-已面试，2-已取消 */
    private Integer status;

    /** 面试评价 */
    private String evaluation;

    /** 面试结果：0-未定，1-通过，2-不通过（数据库无此字段，仅内存使用） */
    @TableField(exist = false)
    private Integer result;

    /** 应聘者姓名（关联查询） */
    @TableField(exist = false)
    private String candidateName;

    /** 岗位名称（关联查询） */
    @TableField(exist = false)
    private String jobTitle;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}