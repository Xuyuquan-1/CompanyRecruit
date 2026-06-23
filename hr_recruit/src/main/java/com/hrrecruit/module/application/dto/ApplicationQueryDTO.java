package com.hrrecruit.module.application.dto;

import lombok.Data;

/**
 * 应聘记录分页查询参数
 */
@Data
public class ApplicationQueryDTO {

    /** 岗位ID */
    private Long jobId;

    /** 应聘状态 */
    private Integer status;

    /** 应聘者姓名（模糊搜索） */
    private String candidateName;

    /** 关键词搜索 */
    private String keyword;

    /** 标签筛选 */
    private String tag;

    /** 当前页码 */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;

    /** 应聘者用户ID（后端根据角色自动填充，前端不需要传） */
    private Long candidateId;
}