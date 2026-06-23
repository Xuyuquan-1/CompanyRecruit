package com.hrrecruit.module.offer.service;

import com.hrrecruit.common.PageResult;
import com.hrrecruit.entity.Offer;
import com.hrrecruit.module.offer.dto.OfferDTO;
import com.hrrecruit.module.offer.dto.OfferQueryDTO;

import java.time.LocalDate;

/**
 * 录用管理服务接口
 */
public interface OfferService {

    /**
     * 分页查询录用列表
     */
    PageResult<Offer> getPageList(OfferQueryDTO queryDTO);

    /**
     * 查询录用详情
     */
    Offer getById(Long id);

    /**
     * 发送录用通知
     */
    void sendOffer(OfferDTO dto);

    /**
     * 确认入职
     */
    void confirmOnboard(Long id, LocalDate joinDate);

    /**
     * 拒绝录用
     */
    void rejectOffer(Long id, String reason);

    /**
     * 应聘者接受Offer
     */
    void acceptOffer(Long id);

    /**
     * 应聘者提交入职资料
     */
    void submitDocuments(Long id, String idCardFrontUrl, String idCardBackUrl, String medicalReportUrl, String contractUrl);
}