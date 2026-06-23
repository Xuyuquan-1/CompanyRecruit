package com.hrrecruit.module.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrrecruit.common.Constants;
import com.hrrecruit.common.PageResult;
import com.hrrecruit.common.exception.BusinessException;
import com.hrrecruit.entity.Application;
import com.hrrecruit.entity.JobPost;
import com.hrrecruit.entity.Resume;
import com.hrrecruit.mapper.ApplicationMapper;
import com.hrrecruit.mapper.JobPostMapper;
import com.hrrecruit.mapper.ResumeMapper;
import com.hrrecruit.module.application.dto.ApplicationQueryDTO;
import com.hrrecruit.module.application.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 应聘管理服务实现
 */
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationMapper applicationMapper;
    private final JobPostMapper jobPostMapper;
    private final ResumeMapper resumeMapper;

    @Override
    public PageResult<Application> getPageList(ApplicationQueryDTO queryDTO) {
        Page<Application> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        Page<Application> result = applicationMapper.selectPageWithFilter(page,
                queryDTO.getKeyword(),
                queryDTO.getStatus(),
                queryDTO.getJobId(),
                queryDTO.getTag(),
                queryDTO.getCandidateId());

        return new PageResult<>(result.getTotal(), result.getRecords(), result.getCurrent(), result.getSize());
    }

    @Override
    public Application getById(Long id) {
        Application application = applicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException("应聘记录不存在");
        }
        return application;
    }

    @Override
    public void pass(Long id) {
        Application application = getById(id);
        if (application.getStatus() != Constants.APP_STATUS_PENDING) {
            throw new BusinessException("只有待筛选状态的应聘记录才能通过");
        }
        application.setStatus(Constants.APP_STATUS_PASSED);
        applicationMapper.updateById(application);
    }

    @Override
    public void reject(Long id, Integer refuseType) {
        Application application = getById(id);
        
        // 状态流转约束：只有非终态才能设置为不录用
        if (application.getStatus() == Constants.APP_STATUS_REJECTED 
            || application.getStatus() == Constants.APP_STATUS_ONBOARDED) {
            throw new BusinessException("该应聘记录已为终态，无法重复操作");
        }
        
        // 强制约束：不录用必须指定失败原因
        if (refuseType == null) {
            throw new BusinessException("不录用操作必须指定失败原因");
        }
        
        application.setStatus(Constants.APP_STATUS_REJECTED);
        application.setResult(2); // 应聘失败
        application.setRefuseType(refuseType);
        applicationMapper.updateById(application);
    }

    @Override
    public void updateRemark(Long id, String tags, String remark) {
        Application application = getById(id);
        if (StringUtils.hasText(tags)) {
            application.setTags(tags);
        }
        if (StringUtils.hasText(remark)) {
            application.setRemark(remark);
        }
        applicationMapper.updateById(application);
    }

    @Override
    @Transactional
    public void submitApplication(Long jobId, Long resumeId, Long candidateId) {
        // 检查岗位是否存在且处于招聘中状态
        JobPost jobPost = jobPostMapper.selectById(jobId);
        if (jobPost == null) {
            throw new BusinessException("岗位不存在");
        }
        if (jobPost.getStatus() != 1) {
            throw new BusinessException("该岗位已关闭，无法投递");
        }

        // 检查简历是否存在
        Resume resume = resumeMapper.selectById(resumeId);
        if (resume == null) {
            throw new BusinessException("简历不存在");
        }

        // 检查是否已经投递过该岗位
        Application existingApp = applicationMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Application>()
                .eq(Application::getJobId, jobId)
                .eq(Application::getResumeId, resumeId)
        );
        if (existingApp != null) {
            throw new BusinessException("您已经投递过该岗位，请勿重复投递");
        }

        // 创建应聘记录
        Application application = new Application();
        application.setJobId(jobId);
        application.setResumeId(resumeId);
        application.setCandidateId(candidateId);
        application.setStatus(Constants.APP_STATUS_PENDING);
        application.setApplyTime(LocalDateTime.now());
        
        applicationMapper.insert(application);
    }
}