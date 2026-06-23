package com.hrrecruit.module.job.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 岗位新增/修改请求参数
 */
@Data
public class JobPostDTO {

    @NotBlank(message = "岗位名称不能为空")
    private String title;

    @NotBlank(message = "所属部门不能为空")
    private String department;

    /** 岗位职责 */
    private String description;

    /** 岗位要求 */
    private String requirements;

    @Min(value = 1, message = "招聘人数至少为1")
    private Integer headcount;

    /** 状态：0-草稿，1-已发布，2-已关闭 */
    private Integer status;
}