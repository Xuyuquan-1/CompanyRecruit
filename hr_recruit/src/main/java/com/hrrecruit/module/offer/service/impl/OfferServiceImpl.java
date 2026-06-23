package com.hrrecruit.module.offer.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrrecruit.common.Constants;
import com.hrrecruit.common.PageResult;
import com.hrrecruit.common.exception.BusinessException;
import com.hrrecruit.entity.Application;
import com.hrrecruit.entity.Offer;
import com.hrrecruit.mapper.ApplicationMapper;
import com.hrrecruit.mapper.OfferMapper;
import com.hrrecruit.module.offer.dto.OfferDTO;
import com.hrrecruit.module.offer.dto.OfferQueryDTO;
import com.hrrecruit.module.offer.service.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 录用管理服务实现
 */
@Service
@RequiredArgsConstructor
public class OfferServiceImpl implements OfferService {

    private final OfferMapper offerMapper;
    private final ApplicationMapper applicationMapper;

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

        application.setStatus(Constants.APP_STATUS_HIRED);
        applicationMapper.updateById(application);
    }

    @Override
    @Transactional
    public void confirmOnboard(Long id, LocalDate joinDate) {
        Offer offer = getById(id);
        offer.setExpectedJoinDate(joinDate);
        offer.setStatus(Constants.OFFER_STATUS_ONBOARDED);
        offerMapper.updateById(offer);
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
        
        // 更新应聘记录状态为已拒绝
        Application application = applicationMapper.selectById(offer.getApplicationId());
        if (application != null) {
            application.setStatus(Constants.APP_STATUS_REJECTED);
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
    }

    @Override
    @Transactional
    public void submitDocuments(Long id, String idCardUrl, String medicalReportUrl, String contractUrl) {
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
            if (idCardUrl != null && !idCardUrl.isEmpty()) {
                docsData.put("idCard", true);
                docsData.put("idCardUrl", idCardUrl);
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