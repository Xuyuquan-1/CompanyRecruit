package com.hrrecruit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 录用实体
 */
@Data
@TableName("offer")
public class Offer {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 应聘记录ID */
    private Long applicationId;

    /** 发送录用通知时间 */
    private LocalDateTime offerTime;

    /** 预计入职日期 */
    private LocalDate expectedJoinDate;

    /** 录用状态：0-待确认，1-已接受，2-已拒绝，3-已入职 */
    private Integer status;

    /** 入职资料提交情况（JSON） */
    private String docsSubmitted;

    /** 薪资待遇 */
    private String salary;

    /** 福利说明 */
    private String benefits;

    /** 备注 */
    private String remark;

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
