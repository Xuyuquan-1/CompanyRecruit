package com.hrrecruit.module.job.dto;

import lombok.Data;

/**
 * 岗位分页查询参数
 */
@Data
public class JobPostQueryDTO {

    /** 岗位名称（模糊搜索） */
    private String title;

    /** 部门 */
    private String department;

    /** 状态筛选 */
    private Integer status;

    /** 当前页码（默认第1页） */
    private Integer pageNum = 1;

    /** 每页条数（默认10条） */
    private Integer pageSize = 10;
}