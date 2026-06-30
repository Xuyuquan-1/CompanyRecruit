package com.hrrecruit.module.offer.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrrecruit.common.Constants;
import com.hrrecruit.common.PageResult;
import com.hrrecruit.common.exception.BusinessException;
import com.hrrecruit.entity.Application;
import com.hrrecruit.entity.Employee;
import com.hrrecruit.entity.JobPost;
import com.hrrecruit.entity.Offer;
import com.hrrecruit.entity.Resume;
import com.hrrecruit.entity.SysUser;
import com.hrrecruit.mapper.ApplicationMapper;
import com.hrrecruit.mapper.EmployeeMapper;
import com.hrrecruit.mapper.JobPostMapper;
import com.hrrecruit.mapper.OfferMapper;
import com.hrrecruit.mapper.ResumeMapper;
import com.hrrecruit.mapper.SysUserMapper;
import com.hrrecruit.module.notification.service.NotificationService;
import com.hrrecruit.module.offer.dto.OfferDTO;
import com.hrrecruit.module.offer.dto.OfferQueryDTO;
import com.hrrecruit.module.offer.service.OfferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 录用管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OfferServiceImpl implements OfferService {

    private final OfferMapper offerMapper;
    private final ApplicationMapper applicationMapper;
    private final JobPostMapper jobPostMapper;
    private final NotificationService notificationService;
    private final EmployeeMapper employeeMapper;
    private final ResumeMapper resumeMapper;
    private final SysUserMapper userMapper;

    @Override
    public PageResult<Offer> getPageList(OfferQueryDTO queryDTO) {
        Page<Offer> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<Offer> result = offerMapper.selectPageWithDetail(page, queryDTO);
        return new PageResult<>(result.getTotal(), result.getRecords(), result.getCurrent(), result.getSize());
    }

    @Override
    public Offer getById(Long id) {
        Offer offer = offerMapper.selectById(id);
        if (offer == null) {
            throw new BusinessException("录用记录不存在");
        }
        return offer;
    }

    @Override
    @Transactional
    public void sendOffer(OfferDTO dto) {
        Application application = applicationMapper.selectById(dto.getApplicationId());
        if (application == null) {
            throw new BusinessException("应聘记录不存在");
        }

        Offer offer = new Offer();
        offer.setApplicationId(dto.getApplicationId());
        offer.setOfferTime(LocalDateTime.now());
        offer.setExpectedJoinDate(dto.getExpectedJoinDate());
        offer.setSalary(dto.getSalary());
        offer.setBenefits(dto.getBenefits());
        offer.setRemark(dto.getRemark());
        offer.setStatus(Constants.OFFER_STATUS_PENDING);
        offerMapper.insert(offer);

        application.setStatus(Constants.APP_STATUS_OFFER_PENDING);
        application.setRemark("已发送录用通知，等待候选人确认");
        application.setOfferId(offer.getId());
        applicationMapper.updateById(application);

        JobPost jobPost = jobPostMapper.selectById(application.getJobId());
        String jobTitle = jobPost != null ? jobPost.getTitle() : "未知岗位";

        try {
            notificationService.sendOfferNotification(
                application.getCandidateId(),
                jobTitle,
                dto.getSalary(),
                dto.getBenefits(),
                dto.getExpectedJoinDate()
            );
        } catch (Exception e) {
            log.error("发送录用通知失败", e);
        }
    }

    @Override
    @Transactional
    public void acceptOffer(Long id) {
        Offer offer = getById(id);
        if (offer.getStatus() == null || offer.getStatus() != Constants.OFFER_STATUS_PENDING) {
            throw new BusinessException("只有待确认状态的Offer才能被接受");
        }

        offer.setStatus(Constants.OFFER_STATUS_ACCEPTED);
        offerMapper.updateById(offer);

        Application application = applicationMapper.selectById(offer.getApplicationId());
        if (application != null) {
            application.setStatus(Constants.APP_STATUS_OFFER_ACCEPTED);
            application.setRemark("候选人已接受Offer，待提交入职资料");
            applicationMapper.updateById(application);

            try {
                JobPost jobPost = jobPostMapper.selectById(application.getJobId());
                String jobTitle = jobPost != null ? jobPost.getTitle() : "未知岗位";
                if (jobPost != null && jobPost.getCreateUser() != null) {
                    notificationService.sendOfferAcceptedNotification(
                        jobPost.getCreateUser(),
                        application.getCandidateName(),
                        jobTitle
                    );
                }
            } catch (Exception e) {
                log.error("发送Offer接受通知给管理员失败", e);
            }
        }
    }

    @Override
    @Transactional
    public void rejectOffer(Long id, String reason) {
        Offer offer = getById(id);
        if (offer.getStatus() == null || offer.getStatus() != Constants.OFFER_STATUS_PENDING) {
            throw new BusinessException("只有待确认状态的Offer才能被拒绝");
        }

        offer.setStatus(Constants.OFFER_STATUS_REJECTED);
        offer.setRemark(reason);
        offerMapper.updateById(offer);

        Application application = applicationMapper.selectById(offer.getApplicationId());
        if (application != null) {
            application.setStatus(Constants.APP_STATUS_REJECTED);
            application.setResult(2);
            application.setRefuseType(Constants.REFUSE_TYPE_CANDIDATE_REFUSE);
            application.setRemark("候选人已拒绝Offer" + (reason != null && !reason.isEmpty() ? "，原因：" + reason : ""));
            applicationMapper.updateById(application);
        }
    }

    @Override
    @Transactional
    public void submitDocuments(Long id, String idCardFrontUrl, String idCardBackUrl, String medicalReportUrl, String contractUrl) {
        Offer offer = getById(id);
        if (offer.getStatus() == null || offer.getStatus() != Constants.OFFER_STATUS_ACCEPTED) {
            throw new BusinessException("只有已接受Offer才能提交入职资料");
        }

        try {
            Map<String, String> docs = new HashMap<>();
            docs.put("idCardFrontUrl", idCardFrontUrl);
            docs.put("idCardBackUrl", idCardBackUrl);
            docs.put("medicalReportUrl", medicalReportUrl);
            docs.put("contractUrl", contractUrl);
            ObjectMapper mapper = new ObjectMapper();
            String docsJson = mapper.writeValueAsString(docs);
            offer.setDocsSubmitted(docsJson);
            offerMapper.updateById(offer);
        } catch (Exception e) {
            throw new BusinessException("资料保存失败");
        }
    }

    @Override
    @Transactional
    public void confirmOnboard(Long id, LocalDate joinDate) {
        Offer offer = getById(id);
        if (offer.getStatus() == null || offer.getStatus() != Constants.OFFER_STATUS_ACCEPTED) {
            throw new BusinessException("只有已接受Offer才能确认入职");
        }
        if (offer.getDocsSubmitted() == null) {
            throw new BusinessException("候选人尚未提交入职资料，无法确认入职");
        }

        offer.setStatus(Constants.OFFER_STATUS_ONBOARDED);
        offer.setExpectedJoinDate(joinDate);
        offerMapper.updateById(offer);

        Application application = applicationMapper.selectById(offer.getApplicationId());
        if (application != null) {
            application.setStatus(Constants.APP_STATUS_ONBOARDED);
            application.setRemark("入职确认完成，已生成员工档案");
            applicationMapper.updateById(application);

            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode docsNode = mapper.readTree(offer.getDocsSubmitted());
                Employee employee = new Employee();
                employee.setName(application.getCandidateName());
                employee.setPosition(application.getJobTitle());
                employee.setOfferId(offer.getId());
                employee.setExpectedJoinDate(joinDate);
                employee.setStatus(1);
                employee.setCreateTime(LocalDateTime.now());
                employee.setUpdateTime(LocalDateTime.now());
                employeeMapper.insert(employee);
            } catch (Exception e) {
                log.error("创建员工档案失败", e);
                throw new BusinessException("创建员工档案失败");
            }
        }
    }

    @Transactional
    public void approveDocuments(Long id) {
        Offer offer = getById(id);
        if (offer.getStatus() == null || offer.getStatus() != Constants.OFFER_STATUS_ACCEPTED) {
            throw new BusinessException("只有已接受Offer才能审核资料");
        }
        if (offer.getDocsSubmitted() == null) {
            throw new BusinessException("候选人尚未提交入职资料");
        }

        offer.setStatus(Constants.OFFER_STATUS_ONBOARDED);
        offerMapper.updateById(offer);

        Application application = applicationMapper.selectById(offer.getApplicationId());
        if (application != null) {
            application.setStatus(Constants.APP_STATUS_ONBOARDED);
            application.setRemark("入职资料审核通过，已办理入职");
            applicationMapper.updateById(application);
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode docsNode = mapper.readTree(offer.getDocsSubmitted());
            Employee employee = new Employee();
            employee.setName(docsNode.has("name") ? docsNode.get("name").asText() : application.getCandidateName());
            employee.setPosition(docsNode.has("jobTitle") ? docsNode.get("jobTitle").asText() : "");
            employee.setOfferId(offer.getId());
            employee.setStatus(1);
            employee.setExpectedJoinDate(offer.getExpectedJoinDate());
            employee.setCreateTime(LocalDateTime.now());
            employee.setUpdateTime(LocalDateTime.now());
            employeeMapper.insert(employee);
        } catch (Exception e) {
            log.error("创建员工记录失败", e);
        }
    }

    @Transactional
    public void rejectDocuments(Long id, String reason) {
        Offer offer = getById(id);
        if (offer.getStatus() == null || offer.getStatus() != Constants.OFFER_STATUS_ACCEPTED) {
            throw new BusinessException("只有已接受Offer才能审核资料");
        }
        if (offer.getDocsSubmitted() == null) {
            throw new BusinessException("候选人尚未提交入职资料");
        }

        offer.setDocsSubmitted(null);
        offer.setRemark(reason != null ? reason : "入职资料审核不通过，请重新提交");
        offerMapper.updateById(offer);
    }
}