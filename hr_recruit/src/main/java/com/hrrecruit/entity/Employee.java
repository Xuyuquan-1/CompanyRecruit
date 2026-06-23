package com.hrrecruit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工档案实体
 */
@Data
@TableName("employee")
public class Employee {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联录用记录ID */
    private Long offerId;

    /** 员工姓名 */
    private String name;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 所属部门 */
    private String department;

    /** 岗位 */
    private String position;

    /** 预计入职日期 */
    private LocalDate expectedJoinDate;

    /** 实际入职日期 */
    private LocalDate actualJoinDate;

    /** 入职日期 */
    private LocalDate joinDate;

    /** 身份证：0-未提交，1-已提交 */
    private Integer idCardStatus;

    /** 合同：0-未提交，1-已提交 */
    private Integer contractStatus;

    /** 体检报告：0-未提交，1-已提交 */
    private Integer medicalReportStatus;

    /** 员工详细档案（JSON） */
    private String profileData;

    /** 状态：0-离职，1-在职，2-入职中 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}