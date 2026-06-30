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

    /** 状态：0-待筛选，1-通过筛选，2-面试中，3-待确认Offer，4-不录用，5-已接受Offer(待入职)，6-已入职 */
    private Integer status;

    /** 结果归集：0-处理中 1-录用成功 2-应聘失败 */
    private Integer result;

    /** 失败细分：1-简历淘汰 2-面试淘汰 3-候选人拒Offer 4-审批不通过 5-岗位关闭终止 */
    private Integer refuseType;

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

    /** Offer ID */
    @TableField(exist = false)
    private Long offerId;

    /** Offer状态 */
    @TableField(exist = false)
    private Integer offerStatus;

    /** Offer资料提交情况（JSON字符串） */
    @TableField(exist = false)
    private String offerDocsSubmitted;
}