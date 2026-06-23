package com.hrrecruit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 招聘岗位实体
 */
@Data
@TableName("job_post")
public class JobPost {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 岗位名称 */
    private String title;

    /** 部门 */
    private String department;

    /** 岗位职责 */
    private String description;

    /** 岗位要求 */
    private String requirements;

    /** 招聘人数 */
    private Integer headcount;

    /** 状态：0-草稿，1-已发布，2-已关闭 */
    private Integer status;

    /** 创建人ID */
    private Long createUser;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;
}
