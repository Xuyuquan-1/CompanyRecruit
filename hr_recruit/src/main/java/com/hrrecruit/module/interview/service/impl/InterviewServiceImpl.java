package com.hrrecruit.module.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
    public String arrange(InterviewDTO dto) {
        if (dto.getInterviewTime() != null && dto.getInterviewTime().isBefore(java.time.LocalDateTime.now())) {
            throw new BusinessException("面试时间不能早于当前时间");
        }

        if (dto.getInterviewerId() != null && dto.getInterviewTime() != null) {
            java.time.LocalDateTime start = dto.getInterviewTime().minusMinutes(30);
            java.time.LocalDateTime end = dto.getInterviewTime().plusMinutes(30);
            long conflictCount = interviewMapper.selectCount(
                new LambdaQueryWrapper<Interview>()
                    .eq(Interview::getInterviewerId, dto.getInterviewerId())
                    .between(Interview::getInterviewTime, start, end)
                    .ne(Interview::getStatus, Constants.INTERVIEW_STATUS_CANCELLED)
            );
            if (conflictCount > 0) {
                throw new BusinessException("该面试官在所选时间段已有面试");
            }
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
        application.setRemark("已安排面试，面试官：" + dto.getInterviewerName() + "，面试时间：" + dto.getInterviewTime().toString());
        applicationMapper.updateById(application);

        boolean notifySuccess = true;
        if (application.getCandidateId() != null) {
            try {
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
                notifySuccess = false;
                log.error("发送面试通知失败", e);
            }
        }
        return notifySuccess ? "面试安排成功" : "面试安排成功，但通知发送失败，请手动联系";
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        Interview interview = getById(id);
        interview.setStatus(Constants.INTERVIEW_STATUS_CANCELLED);
        interviewMapper.updateById(interview);

        Application application = applicationMapper.selectById(interview.getApplicationId());
        if (application != null) {
            application.setStatus(Constants.APP_STATUS_REJECTED);
            application.setResult(2);
            application.setRefuseType(Constants.REFUSE_TYPE_INTERVIEW);
            application.setRemark("面试已取消，标记为不录用");
            applicationMapper.updateById(application);
        }
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
                application.setStatus(Constants.APP_STATUS_INTERVIEWING);
                application.setRemark("面试评价完成，结果：通过");
            } else {
                application.setStatus(Constants.APP_STATUS_REJECTED);
                application.setResult(2);
                application.setRefuseType(Constants.REFUSE_TYPE_INTERVIEW);
                application.setRemark("面试评价完成，结果：不通过");
            }
            applicationMapper.updateById(application);
        }
    }
}