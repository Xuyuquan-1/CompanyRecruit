package com.hrrecruit.module.interview.service;

import com.hrrecruit.common.PageResult;
import com.hrrecruit.entity.Interview;
import com.hrrecruit.module.interview.dto.InterviewDTO;
import com.hrrecruit.module.interview.dto.InterviewQueryDTO;

/**
 * 面试管理服务接口
 */
public interface InterviewService {

    /**
     * 分页查询面试列表
     */
    PageResult<Interview> getPageList(InterviewQueryDTO queryDTO);

    /**
     * 查询面试详情
     */
    Interview getById(Long id);

    /**
     * 安排面试
     */
    String arrange(InterviewDTO dto);

    /**
     * 取消面试
     */
    void cancel(Long id);

    /**
     * 填写面试评价
     */
    void evaluate(Long id, String evaluation, Integer result);
}