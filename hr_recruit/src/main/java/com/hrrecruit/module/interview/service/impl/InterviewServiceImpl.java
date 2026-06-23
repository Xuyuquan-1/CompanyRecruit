package com.hrrecruit.module.interview.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrrecruit.common.Constants;
import com.hrrecruit.common.PageResult;
import com.hrrecruit.common.exception.BusinessException;
import com.hrrecruit.entity.Application;
import com.hrrecruit.entity.Interview;
import com.hrrecruit.mapper.ApplicationMapper;
import com.hrrecruit.mapper.InterviewMapper;
import com.hrrecruit.module.interview.dto.InterviewDTO;
import com.hrrecruit.module.interview.dto.InterviewQueryDTO;
import com.hrrecruit.module.interview.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 面试管理服务实现
 */
@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewMapper interviewMapper;
    private final ApplicationMapper applicationMapper;

    @Override
    public PageResult<Interview> getPageList(InterviewQueryDTO queryDTO) {
        Page<Interview> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<Interview> result = interviewMapper.selectPageWithDetail(page, queryDTO);
        return new PageResult<>(result.getTotal(), result.getRecords(), result.getCurrent(), result.getSize());
    }

    @Override
    public Interview getById(Long id) {
        Interview interview = interviewMapper.selectById(id);
        if (interview == null) {
            throw new BusinessException("面试记录不存在");
        }
        return interview;
    }

    @Override
    @Transactional
    public void arrange(InterviewDTO dto) {
        Application application = applicationMapper.selectById(dto.getApplicationId());
        if (application == null) {
            throw new BusinessException("应聘记录不存在");
        }

        Interview interview = new Interview();
        interview.setApplicationId(dto.getApplicationId());
        interview.setInterviewTime(dto.getInterviewTime());
        interview.setLocation(dto.getLocation());
        interview.setInterviewerId(dto.getInterviewerId());
        interview.setInterviewerName(dto.getInterviewerName());
        interview.setStatus(Constants.INTERVIEW_STATUS_PENDING);

        interviewMapper.insert(interview);

        application.setStatus(Constants.APP_STATUS_INTERVIEWING);
        applicationMapper.updateById(application);
    }

    @Override
    public void cancel(Long id) {
        Interview interview = getById(id);
        interview.setStatus(Constants.INTERVIEW_STATUS_CANCELLED);
        interviewMapper.updateById(interview);
    }

    @Override
    @Transactional
    public void evaluate(Long id, String evaluation, Integer result) {
        Interview interview = getById(id);
        interview.setEvaluation(evaluation);
        interview.setStatus(Constants.INTERVIEW_STATUS_COMPLETED);
        interviewMapper.updateById(interview);

        Application application = applicationMapper.selectById(interview.getApplicationId());
        if (application != null) {
            if (result == Constants.INTERVIEW_RESULT_PASS) {
                application.setStatus(Constants.APP_STATUS_PASSED);
            } else {
                application.setStatus(Constants.APP_STATUS_REJECTED);
            }
            applicationMapper.updateById(application);
        }
    }
}