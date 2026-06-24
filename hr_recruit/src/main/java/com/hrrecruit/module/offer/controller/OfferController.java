package com.hrrecruit.module.offer.controller;

import com.hrrecruit.common.PageResult;
import com.hrrecruit.common.Result;
import com.hrrecruit.entity.Offer;
import com.hrrecruit.module.offer.dto.OfferDTO;
import com.hrrecruit.module.offer.dto.OfferQueryDTO;
import com.hrrecruit.module.offer.service.OfferService;
import com.hrrecruit.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * 录用管理控制器
 */
@RestController
@RequestMapping("/api/offer")
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;

    /**
     * 分页查询录用列表
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyAuthority('offer:list', 'ROLE_CANDIDATE')")
    public Result<PageResult<Offer>> page(OfferQueryDTO queryDTO,
                                          @AuthenticationPrincipal LoginUser loginUser) {
        // 应聘者只能查看自己的录用记录
        if (loginUser.getRoleCodes().contains("ROLE_CANDIDATE")) {
            queryDTO.setCandidateId(loginUser.getUserId());
        }
        return Result.success(offerService.getPageList(queryDTO));
    }

    /**
     * 查询录用详情
     */
    @GetMapping("/{id}")
    public Result<Offer> getById(@PathVariable Long id) {
        return Result.success(offerService.getById(id));
    }

    /**
     * 发送录用通知
     */
    @PostMapping
    @PreAuthorize("hasAuthority('offer:list')")
    public Result<Void> sendOffer(@Valid @RequestBody OfferDTO dto) {
        offerService.sendOffer(dto);
        return Result.successMsg("录用通知已发送");
    }

    /**
     * 确认入职
     */
    @PostMapping("/{id}/onboard")
    @PreAuthorize("hasAuthority('offer:list')")
    public Result<Void> confirmOnboard(@PathVariable Long id, @RequestBody Map<String, String> params) {
        LocalDate joinDate = LocalDate.parse(params.get("joinDate"));
        offerService.confirmOnboard(id, joinDate);
        return Result.successMsg("已确认入职");
    }

    /**
     * 拒绝录用
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('offer:list', 'ROLE_CANDIDATE')")
    public Result<Void> rejectOffer(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String reason = params.get("reason");
        offerService.rejectOffer(id, reason);
        return Result.successMsg("已拒绝录用");
    }

    /**
     * 应聘者接受Offer
     */
    @PostMapping("/{id}/accept")
    @PreAuthorize("hasAnyAuthority('offer:list', 'ROLE_CANDIDATE')")
    public Result<Void> acceptOffer(@PathVariable Long id) {
        offerService.acceptOffer(id);
        return Result.successMsg("已接受Offer，请提交入职资料");
    }

    /**
     * 应聘者提交入职资料
     */
    @PostMapping("/{id}/submit-documents")
    @PreAuthorize("hasAnyAuthority('offer:list', 'ROLE_CANDIDATE')")
    public Result<Void> submitDocuments(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String idCardFrontUrl = params.get("idCardFrontUrl");
        String idCardBackUrl = params.get("idCardBackUrl");
        String medicalReportUrl = params.get("medicalReportUrl");
        String contractUrl = params.get("contractUrl");
        offerService.submitDocuments(id, idCardFrontUrl, idCardBackUrl, medicalReportUrl, contractUrl);
        return Result.successMsg("资料提交成功，等待招聘者审核");
    }
}