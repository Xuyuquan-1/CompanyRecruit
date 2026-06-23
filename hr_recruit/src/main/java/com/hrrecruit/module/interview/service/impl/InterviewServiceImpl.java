package com.hrrecruit.module.interview.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrrecruit.common.Constants;
import com.hrrecruit.common.PageResult;
import com.hrrecruit.common.exception.BusinessException;
import com.hrrecruit.entity.Application;
import com.hrrecruit.entity.Interview;
import com.hrrecruit.entity.JobPost;
import com.hrrecruit.mapper.ApplicationMapper;
import com.hrrecruit.mapper.InterviewMapper;
import com.hrrecruit.mapper.JobPostMapper;
import com.hrrecruit.module.interview.dto.InterviewDTO;
import com.hrrecruit.module.interview.dto.InterviewQueryDTO;
import com.hrrecruit.module.interview.service.InterviewService;
import com.hrrecruit.module.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 面试管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewMapper interviewMapper;
    private final ApplicationMapper applicationMapper;
    private final JobPostMapper jobPostMapper;
    private final NotificationService notificationService;

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
        if (dto.getInterviewTime() != null && dto.getInterviewTime().isBefore(java.time.LocalDateTime.now())) {
            throw new BusinessException("面试时间必须晚于当前时间");
        }

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
        
        // 自动发送面试通知
        if (application.getCandidateId() != null) {
            try {
                // 获取岗位信息
                JobPost jobPost = jobPostMapper.selectById(application.getJobId());
                String jobTitle = jobPost != null ? jobPost.getTitle() : "未知岗位";
                
                notificationService.sendInterviewNotification(
                    application.getCandidateId(),
                    jobTitle,
                    dto.getInterviewTime(),
                    dto.getLocation(),
                    dto.getInterviewerName()
                );
            } catch (Exception e) {
                // 通知失败不影响主流程
                log.error("发送面试通知失败", e);
            }
        }
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
        interview.setResult(result);
        interview.setStatus(Constants.INTERVIEW_STATUS_COMPLETED);
        interviewMapper.updateById(interview);

        Application application = applicationMapper.selectById(interview.getApplicationId());
        if (application != null) {
            if (result == Constants.INTERVIEW_RESULT_PASS) {
                // 面试通过：保持status=2（面试中），表示面试已完成，可以发放Offer
                // HR会根据面试结果决定是否发放Offer
                application.setStatus(Constants.APP_STATUS_INTERVIEWING);
            } else {
                // 面试不通过：设置为不录用
                application.setStatus(Constants.APP_STATUS_REJECTED);
            }
            applicationMapper.updateById(application);
        }
    }
}