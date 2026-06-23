package com.hrrecruit.module.offer.dto;

import lombok.Data;

/**
 * 录用分页查询参数
 */
@Data
public class OfferQueryDTO {

    /** 录用状态 */
    private Integer status;

    /** 应聘者姓名（模糊搜索） */
    private String candidateName;

    /** 当前页码 */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;

    /** 应聘者用户ID（后端根据角色自动填充，前端不需要传） */
    private Long candidateId;
}