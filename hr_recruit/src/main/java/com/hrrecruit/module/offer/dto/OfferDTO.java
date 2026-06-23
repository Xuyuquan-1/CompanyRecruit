package com.hrrecruit.module.offer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 录用通知请求参数
 */
@Data
public class OfferDTO {

    @NotNull(message = "应聘记录ID不能为空")
    private Long applicationId;

    /** 预计入职日期 */
    private LocalDate expectedJoinDate;

    /** 薪资待遇 */
    private String salary;

    /** 福利说明 */
    private String benefits;

    /** 备注 */
    private String remark;
}