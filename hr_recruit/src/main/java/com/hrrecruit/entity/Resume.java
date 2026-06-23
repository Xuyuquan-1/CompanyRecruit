package com.hrrecruit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简历实体
 */
@Data
@TableName("resume")
public class Resume {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 原始文件名 */
    private String originalFilename;

    /** 文件存储路径 */
    private String filePath;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 上传人ID */
    private Long uploadBy;

    /** 解析状态：0-待解析，1-已解析，2-解析失败 */
    private Integer parseStatus;

    /** 解析后结构化数据（JSON格式） */
    private String parsedJson;

    /** 对应向量库文档ID */
    private String vectorId;

    /** 简历技能标签（逗号分隔） */
    private String tags;

    private LocalDateTime uploadTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 关联岗位ID（来自application表，非数据库字段） */
    @TableField(exist = false)
    private Long jobId;

    /** 关联岗位名称（来自job_post表，非数据库字段） */
    @TableField(exist = false)
    private String jobTitle;

    /** 应聘筛选状态（来自application表，非数据库字段）：0-待筛选，1-通过筛选，2-面试中，3-录用，4-不录用 */
    @TableField(exist = false)
    private Integer applicationStatus;

    /** 应聘记录ID（来自application表，非数据库字段） */
    @TableField(exist = false)
    private Long applicationId;
}