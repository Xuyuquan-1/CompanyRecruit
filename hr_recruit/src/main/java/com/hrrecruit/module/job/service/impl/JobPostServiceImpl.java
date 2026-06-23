package com.hrrecruit.module.job.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrrecruit.common.Constants;
import com.hrrecruit.common.PageResult;
import com.hrrecruit.common.exception.BusinessException;
import com.hrrecruit.entity.JobPost;
import com.hrrecruit.mapper.JobPostMapper;
import com.hrrecruit.module.job.dto.JobPostDTO;
import com.hrrecruit.module.job.dto.JobPostQueryDTO;
import com.hrrecruit.module.job.service.JobPostService;
import com.hrrecruit.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 岗位管理服务实现
 */
@Service
@RequiredArgsConstructor
public class JobPostServiceImpl implements JobPostService {

    private final JobPostMapper jobPostMapper;

    @Override
    public PageResult<JobPost> getPageList(JobPostQueryDTO queryDTO) {
        Page<JobPost> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<JobPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getTitle()), JobPost::getTitle, queryDTO.getTitle());
        wrapper.eq(StringUtils.hasText(queryDTO.getDepartment()), JobPost::getDepartment, queryDTO.getDepartment());
        wrapper.eq(queryDTO.getStatus() != null, JobPost::getStatus, queryDTO.getStatus());
        
        // 应聘者只能查看已发布的岗位
        LoginUser loginUser = getCurrentUser();
        if (loginUser.getRoleCodes().contains("ROLE_CANDIDATE")) {
            wrapper.eq(JobPost::getStatus, Constants.JOB_STATUS_PUBLISHED);
        }
        
        wrapper.orderByDesc(JobPost::getCreateTime);

        Page<JobPost> result = jobPostMapper.selectPage(page, wrapper);

        return new PageResult<>(result.getTotal(), result.getRecords(), result.getCurrent(), result.getSize());
    }

    @Override
    public JobPost getById(Long id) {
        JobPost jobPost = jobPostMapper.selectById(id);
        if (jobPost == null) {
            throw new BusinessException("岗位不存在");
        }
        return jobPost;
    }

    @Override
    public void add(JobPostDTO dto) {
        JobPost jobPost = new JobPost();
        jobPost.setTitle(dto.getTitle());
        jobPost.setDepartment(dto.getDepartment());
        jobPost.setDescription(dto.getDescription());
        jobPost.setRequirements(dto.getRequirements());
        jobPost.setHeadcount(dto.getHeadcount());
        jobPost.setStatus(dto.getStatus() != null ? dto.getStatus() : Constants.JOB_STATUS_DRAFT);
        LoginUser loginUser = getCurrentUser();
        jobPost.setCreateUser(loginUser.getUserId());

        jobPostMapper.insert(jobPost);
    }

    @Override
    public void update(Long id, JobPostDTO dto) {
        JobPost jobPost = getById(id);
        jobPost.setTitle(dto.getTitle());
        jobPost.setDepartment(dto.getDepartment());
        jobPost.setDescription(dto.getDescription());
        jobPost.setRequirements(dto.getRequirements());
        jobPost.setHeadcount(dto.getHeadcount());
        if (dto.getStatus() != null) {
            jobPost.setStatus(dto.getStatus());
        }

        jobPostMapper.updateById(jobPost);
    }

    @Override
    public void delete(Long id) {
        JobPost jobPost = getById(id);
        jobPostMapper.deleteById(id);
    }

    @Override
    public void publish(Long id) {
        JobPost jobPost = getById(id);
        jobPost.setStatus(Constants.JOB_STATUS_PUBLISHED);
        jobPostMapper.updateById(jobPost);
    }

    @Override
    public void close(Long id) {
        JobPost jobPost = getById(id);
        jobPost.setStatus(Constants.JOB_STATUS_CLOSED);
        jobPostMapper.updateById(jobPost);
    }

    @Override
    public java.util.List<JobPost> getPublished() {
        LambdaQueryWrapper<JobPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobPost::getStatus, Constants.JOB_STATUS_PUBLISHED);
        wrapper.orderByDesc(JobPost::getCreateTime);
        return jobPostMapper.selectList(wrapper);
    }

    private LoginUser getCurrentUser() {
        return (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}