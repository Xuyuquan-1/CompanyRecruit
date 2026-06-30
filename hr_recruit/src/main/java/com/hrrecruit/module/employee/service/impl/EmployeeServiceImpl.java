package com.hrrecruit.module.employee.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrrecruit.common.Constants;
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
import com.hrrecruit.module.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工管理服务实现
 */
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final OfferMapper offerMapper;
    private final ApplicationMapper applicationMapper;
    private final ResumeMapper resumeMapper;
    private final JobPostMapper jobPostMapper;

    @Override
    public List<Employee> getList() {
        return employeeMapper.selectListWithDetail();
    }

    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }
        return employee;
    }

    @Override
    @Transactional
    public Employee createFromOffer(Long offerId, Employee employee) {
        Offer offer = offerMapper.selectById(offerId);
        if (offer == null) {
            throw new BusinessException("录用记录不存在");
        }

        // 从应聘记录中获取简历和岗位信息
        Application application = applicationMapper.selectById(offer.getApplicationId());
        if (application == null) {
            throw new BusinessException("应聘记录不存在");
        }

        Resume resume = resumeMapper.selectById(application.getResumeId());
        JobPost jobPost = jobPostMapper.selectById(application.getJobId());

        // 自动填充员工基本信息
        if (resume != null && resume.getParsedJson() != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> resumeData = mapper.readValue(resume.getParsedJson(), Map.class);
                
                // 如果前端没有传姓名，从简历中获取
                if (employee.getName() == null || employee.getName().isEmpty()) {
                    employee.setName((String) resumeData.get("name"));
                }
                // 如果前端没有传电话，从简历中获取
                if (employee.getPhone() == null || employee.getPhone().isEmpty()) {
                    employee.setPhone((String) resumeData.get("phone"));
                }
                // 如果前端没有传邮箱，从简历中获取
                if (employee.getEmail() == null || employee.getEmail().isEmpty()) {
                    employee.setEmail((String) resumeData.get("email"));
                }
            } catch (Exception e) {
                // 解析失败不影响创建流程
            }
        }

        // 从岗位信息中获取部门和职位
        if (jobPost != null) {
            if (employee.getDepartment() == null || employee.getDepartment().isEmpty()) {
                employee.setDepartment(jobPost.getDepartment());
            }
            if (employee.getPosition() == null || employee.getPosition().isEmpty()) {
                employee.setPosition(jobPost.getTitle());
            }
        }

        // 设置预计入职日期（从Offer）
        if (offer.getExpectedJoinDate() != null) {
            employee.setExpectedJoinDate(offer.getExpectedJoinDate());
        }

        // 构建员工档案数据
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("source", "offer_" + offerId);
            profileData.put("applicationId", application.getId());
            profileData.put("resumeId", application.getResumeId());
            profileData.put("offerSalary", offer.getSalary());
            profileData.put("offerBenefits", offer.getBenefits());
            
            if (resume != null && resume.getParsedJson() != null) {
                Map<String, Object> resumeInfo = mapper.readValue(resume.getParsedJson(), Map.class);
                profileData.put("education", resumeInfo.get("education"));
                profileData.put("workExperience", resumeInfo.get("work_experience"));
                profileData.put("skills", resumeInfo.get("skills"));
            }
            
            employee.setProfileData(mapper.writeValueAsString(profileData));
        } catch (Exception e) {
            // JSON处理失败不影响创建流程
        }

        employee.setOfferId(offerId);
        employee.setStatus(Constants.EMPLOYEE_STATUS_ONBOARDING);
        employeeMapper.insert(employee);

        // 更新Offer状态为已入职
        offer.setStatus(Constants.OFFER_STATUS_ONBOARDED);
        offerMapper.updateById(offer);

        return employee;
    }

    @Override
    public void update(Long id, Employee employee) {
        Employee exist = getById(id);
        exist.setName(employee.getName());
        exist.setPhone(employee.getPhone());
        exist.setEmail(employee.getEmail());
        exist.setPosition(employee.getPosition());
        exist.setDepartment(employee.getDepartment());
        exist.setExpectedJoinDate(employee.getExpectedJoinDate());
        exist.setActualJoinDate(employee.getActualJoinDate());
        employeeMapper.updateById(exist);
    }

    @Override
    @Transactional
    public void updateDocuments(Long id, String documentType, Integer status) {
        Employee employee = getById(id);
        switch (documentType) {
            case "idCard":
                employee.setIdCardStatus(status);
                break;
            case "contract":
                employee.setContractStatus(status);
                break;
            case "medicalReport":
                employee.setMedicalReportStatus(status);
                break;
            default:
                throw new BusinessException("未知的资料类型: " + documentType);
        }
        employeeMapper.updateById(employee);
        
        if (status != null && status == 0) {
            Offer offer = offerMapper.selectById(employee.getOfferId());
            if (offer != null && offer.getStatus() != Constants.OFFER_STATUS_REJECTED 
                && offer.getStatus() != Constants.OFFER_STATUS_ONBOARDED) {
                offer.setStatus(Constants.OFFER_STATUS_REJECTED);
                offer.setRemark("入职资料审核不通过（" + documentType + "），已拒绝");
                offerMapper.updateById(offer);
                
                Application application = applicationMapper.selectById(offer.getApplicationId());
                if (application != null) {
                    application.setStatus(Constants.APP_STATUS_REJECTED);
                    application.setResult(2);
                    application.setRefuseType(Constants.REFUSE_TYPE_APPROVAL);
                    application.setRemark("入职资料审核不通过（" + documentType + "），审批拒绝");
                    applicationMapper.updateById(application);
                }
            }
        }
    }

    @Override
    @Transactional
    public void submitDocument(Long id, String documentType, String fileUrl) {
        Employee employee = getById(id);
        
        // 根据资料类型更新对应的状态字段和保存文件URL
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, Object> profile = new java.util.HashMap<>();
            
            // 如果已有profileData，先解析出来
            if (employee.getProfileData() != null && !employee.getProfileData().isEmpty()) {
                profile = mapper.readValue(employee.getProfileData(), java.util.Map.class);
            }
            
            // 根据资料类型设置状态和保存URL
            switch (documentType) {
                case "idCard":
                    employee.setIdCardStatus(1); // 已提交
                    profile.put("idCardUrl", fileUrl);
                    break;
                case "contract":
                    employee.setContractStatus(1); // 已提交
                    profile.put("contractUrl", fileUrl);
                    break;
                case "medicalReport":
                    employee.setMedicalReportStatus(1); // 已提交
                    profile.put("medicalReportUrl", fileUrl);
                    break;
                default:
                    throw new BusinessException("未知的资料类型: " + documentType);
            }
            
            // 保存更新后的profileData
            employee.setProfileData(mapper.writeValueAsString(profile));
        } catch (Exception e) {
            throw new BusinessException("提交资料失败: " + e.getMessage());
        }
        
        employeeMapper.updateById(employee);
    }

    @Override
    public void delete(Long id) {
        getById(id);
        employeeMapper.deleteById(id);
    }
}