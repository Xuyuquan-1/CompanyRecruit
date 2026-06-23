package com.hrrecruit.module.job.service;

import com.hrrecruit.common.PageResult;
import com.hrrecruit.entity.JobPost;
import com.hrrecruit.module.job.dto.JobPostDTO;
import com.hrrecruit.module.job.dto.JobPostQueryDTO;

/**
 * 岗位管理服务接口
 */
public interface JobPostService {

    /**
     * 分页查询岗位列表
     */
    PageResult<JobPost> getPageList(JobPostQueryDTO queryDTO);

    /**
     * 根据ID查询岗位详情
     */
    JobPost getById(Long id);

    /**
     * 新增岗位
     */
    void add(JobPostDTO dto);

    /**
     * 修改岗位
     */
    void update(Long id, JobPostDTO dto);

    /**
     * 删除岗位（逻辑删除）
     */
    void delete(Long id);

    /**
     * 发布岗位
     */
    void publish(Long id);

    /**
     * 关闭岗位
     */
    void close(Long id);

    /**
     * 查询所有已发布岗位
     */
    java.util.List<JobPost> getPublished();
}