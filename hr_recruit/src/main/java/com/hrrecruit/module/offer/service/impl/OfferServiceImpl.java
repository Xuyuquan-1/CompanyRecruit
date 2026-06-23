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
import com.hrrecruit.mapper.ApplicationMapper;
import com.hrrecruit.mapper.EmployeeMapper;
import com.hrrecruit.mapper.JobPostMapper;
import com.hrrecruit.mapper.OfferMapper;
import com.hrrecruit.mapper.ResumeMapper;
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

/**
 * 录用管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OfferServiceImpl implements OfferService {

    private final OfferMapper offerMapper;
    private final ApplicationMapper applicationMapper;
    private final EmployeeMapper employeeMapper;
    private final ResumeMapper resumeMapper;
    private final JobPostMapper jobPostMapper;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

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

        // 更新应聘状态为“待确认Offer”
        application.setStatus(Constants.APP_STATUS_OFFER_PENDING);
        applicationMapper.updateById(application);
        
        // 自动发送录用通知
        if (application.getCandidateId() != null) {
            try {
                // 获取岗位信息
                JobPost jobPost = jobPostMapper.selectById(application.getJobId());
                String jobTitle = jobPost != null ? jobPost.getTitle() : "未知岗位";
                
                notificationService.sendOfferNotification(
                    application.getCandidateId(),
                    jobTitle,
                    dto.getSalary(),
                    dto.getBenefits(),
                    dto.getExpectedJoinDate()
                );
            } catch (Exception e) {
                // 通知失败不影响主流程
                log.error("发送录用通知失败", e);
            }
        }
    }

    @Override
    @Transactional
    public void confirmOnboard(Long id, LocalDate joinDate) {
        Offer offer = getById(id);
        
        // 检查Offer状态：只有已接受的Offer才能确认入职
        if (offer.getStatus() == null || offer.getStatus() != Constants.OFFER_STATUS_ACCEPTED) {
            throw new BusinessException("只能对已接受Offer的应聘者确认入职");
        }
        
        // 检查资料是否齐全
        if (offer.getDocsSubmitted() == null || offer.getDocsSubmitted().isEmpty()) {
            throw new BusinessException("应聘者尚未提交入职资料，无法确认入职");
        }
        
        try {
            JsonNode docs = objectMapper.readTree(offer.getDocsSubmitted());
            
            // 兼容新旧两种数据格式
            boolean hasIdCard;
            boolean hasContract;
            boolean hasMedicalReport;
            
            // 新格式：区分身份证正反面
            if (docs.has("idCardFront") && docs.has("idCardBack")) {
                hasIdCard = docs.get("idCardFront").asBoolean() && docs.get("idCardBack").asBoolean();
                hasContract = docs.has("contract") && docs.get("contract").asBoolean();
                hasMedicalReport = docs.has("medicalReport") && docs.get("medicalReport").asBoolean();
            } 
            // 旧格式：中文键名
            else if (docs.has("身份证") && docs.has("合同") && docs.has("体检报告")) {
                hasIdCard = docs.get("身份证").asBoolean();
                hasContract = docs.get("合同").asBoolean();
                hasMedicalReport = docs.get("体检报告").asBoolean();
                log.warn("检测到旧格式数据，建议重新提交资料以更新为新格式");
            } 
            else {
                throw new BusinessException("资料提交记录格式不正确");
            }
            
            if (!hasIdCard || !hasContract || !hasMedicalReport) {
                throw new BusinessException("入职资料不齐全，请确认应聘者已提交：身份证、劳动合同、体检报告");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("解析资料提交记录失败");
        }
        
        // 获取应聘记录信息
        Application application = applicationMapper.selectById(offer.getApplicationId());
        if (application == null) {
            throw new BusinessException("应聘记录不存在");
        }
        
        // 获取简历信息
        Resume resume = resumeMapper.selectById(application.getResumeId());
        
        String name = "未知";
        String phone = "";
        String email = "";
        String profileData = null;
        
        // 尝试从简历中获取信息，如果简历未解析也不影响入职
        if (resume != null && resume.getParsedJson() != null) {
            try {
                // 解析简历JSON获取个人信息
                JsonNode resumeData = objectMapper.readTree(resume.getParsedJson());
                name = resumeData.has("name") ? resumeData.get("name").asText() : "未知";
                phone = resumeData.has("phone") ? resumeData.get("phone").asText() : "";
                email = resumeData.has("email") ? resumeData.get("email").asText() : "";
                profileData = resume.getParsedJson();
            } catch (Exception e) {
                log.warn("解析简历失败，使用默认信息: {}", e.getMessage());
            }
        } else {
            log.warn("简历未解析，将使用默认信息创建员工档案");
        }
        
        // 获取岗位信息（从Application关联的JobPost）
        String department = "";
        String position = "";
        
        // 创建员工档案
        Employee employee = new Employee();
        employee.setOfferId(offer.getId());
        employee.setName(name);
        employee.setPhone(phone);
        employee.setEmail(email);
        employee.setDepartment(department);
        employee.setPosition(position);
        employee.setExpectedJoinDate(offer.getExpectedJoinDate());
        employee.setActualJoinDate(joinDate);
        employee.setIdCardStatus(1);  // 已提交
        employee.setContractStatus(1); // 已提交
        employee.setMedicalReportStatus(1); // 已提交
        employee.setProfileData(profileData); // 保存简历数据（可能为null）
        employee.setStatus(1); // 在职
        
        employeeMapper.insert(employee);
        log.info("员工档案创建成功: {} - {}", name, position);
        
        // 更新Offer状态为已入职
        offer.setStatus(Constants.OFFER_STATUS_ONBOARDED);
        offerMapper.updateById(offer);
        
        // 更新应聘记录状态为“已入职”
        application.setStatus(Constants.APP_STATUS_ONBOARDED);
        application.setResult(1); // 录用成功
        applicationMapper.updateById(application);
        log.info("应聘记录状态更新为已入职: applicationId={}", application.getId());
    }

    @Override
    @Transactional
    public void rejectOffer(Long id, String reason) {
        Offer offer = getById(id);
        // 只有待确认状态的录用才能被拒绝
        if (offer.getStatus() == null || offer.getStatus() != Constants.OFFER_STATUS_PENDING) {
            throw new BusinessException("只有待确认状态的录用才能被拒绝");
        }
        
        offer.setStatus(Constants.OFFER_STATUS_REJECTED);
        offerMapper.updateById(offer);
        
        // 更新应聘记录状态为不录用，失败原因为“候选人拒Offer”
        Application application = applicationMapper.selectById(offer.getApplicationId());
        if (application != null) {
            application.setStatus(Constants.APP_STATUS_REJECTED);
            application.setResult(2); // 应聘失败
            application.setRefuseType(Constants.REFUSE_TYPE_CANDIDATE_REFUSE);
            applicationMapper.updateById(application);
        }
    }

    @Override
    @Transactional
    public void acceptOffer(Long id) {
        Offer offer = getById(id);
        // 只有待确认状态的Offer才能被接受
        if (offer.getStatus() == null || offer.getStatus() != Constants.OFFER_STATUS_PENDING) {
            throw new BusinessException("只有待确认状态的Offer才能被接受");
        }
        
        offer.setStatus(Constants.OFFER_STATUS_ACCEPTED);
        offerMapper.updateById(offer);
        
        // 更新应聘记录状态为“已接受Offer(待入职)”
        Application application = applicationMapper.selectById(offer.getApplicationId());
        if (application != null) {
            application.setStatus(Constants.APP_STATUS_OFFER_ACCEPTED);
            applicationMapper.updateById(application);
        }
    }

    @Override
    @Transactional
    public void submitDocuments(Long id, String idCardFrontUrl, String idCardBackUrl, String medicalReportUrl, String contractUrl) {
        Offer offer = getById(id);
        // 只有已接受状态的Offer才能提交资料
        if (offer.getStatus() == null || offer.getStatus() != Constants.OFFER_STATUS_ACCEPTED) {
            throw new BusinessException("请先接受Offer后再提交入职资料");
        }
        
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, Object> docsData = new java.util.HashMap<>();
            
            // 如果已有docsSubmitted，先解析出来
            if (offer.getDocsSubmitted() != null && !offer.getDocsSubmitted().isEmpty()) {
                docsData = mapper.readValue(offer.getDocsSubmitted(), java.util.Map.class);
            }
            
            // 更新资料URL和状态
            if (idCardFrontUrl != null && !idCardFrontUrl.isEmpty()) {
                docsData.put("idCardFront", true);
                docsData.put("idCardFrontUrl", idCardFrontUrl);
            }
            if (idCardBackUrl != null && !idCardBackUrl.isEmpty()) {
                docsData.put("idCardBack", true);
                docsData.put("idCardBackUrl", idCardBackUrl);
            }
            if (medicalReportUrl != null && !medicalReportUrl.isEmpty()) {
                docsData.put("medicalReport", true);
                docsData.put("medicalReportUrl", medicalReportUrl);
            }
            if (contractUrl != null && !contractUrl.isEmpty()) {
                docsData.put("contract", true);
                docsData.put("contractUrl", contractUrl);
            }
            
            offer.setDocsSubmitted(mapper.writeValueAsString(docsData));
            offerMapper.updateById(offer);
        } catch (Exception e) {
            throw new BusinessException("提交资料失败: " + e.getMessage());
        }
    }
}