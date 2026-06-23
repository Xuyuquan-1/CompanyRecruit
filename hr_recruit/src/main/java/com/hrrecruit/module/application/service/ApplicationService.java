package com.hrrecruit.module.application.service;

import com.hrrecruit.common.PageResult;
import com.hrrecruit.entity.Application;
import com.hrrecruit.module.application.dto.ApplicationQueryDTO;

/**
 * 应聘管理服务接口
 */
public interface ApplicationService {

    /**
     * 分页查询应聘列表
     */
    PageResult<Application> getPageList(ApplicationQueryDTO queryDTO);

    /**
     * 查询应聘详情
     */
    Application getById(Long id);

    /**
     * 通过筛选（转入面试流程）
     */
    void pass(Long id);

    /**
     * 不录用
     * @param id 应聘记录ID
     * @param refuseType 失败原因类型（必填）
     */
    void reject(Long id, Integer refuseType);

    /**
     * 更新备注/标签
     */
    void updateRemark(Long id, String tags, String remark);

    /**
     * 应聘者提交应聘申请
     */
    void submitApplication(Long jobId, Long resumeId, Long candidateId);
}