package com.hrrecruit.module.application.controller;

import com.hrrecruit.common.PageResult;
import com.hrrecruit.common.Result;
import com.hrrecruit.entity.Application;
import com.hrrecruit.module.application.dto.ApplicationQueryDTO;
import com.hrrecruit.module.application.service.ApplicationService;
import com.hrrecruit.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 应聘管理控制器
 */
@RestController
@RequestMapping("/api/application")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    /**
     * 分页查询应聘列表
     */
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('application:list')")
    public Result<PageResult<Application>> page(ApplicationQueryDTO queryDTO,
                                                @AuthenticationPrincipal LoginUser loginUser) {
        // 应聘者只能查看自己的应聘记录
        if (loginUser.getRoleCodes().contains("ROLE_CANDIDATE")) {
            queryDTO.setCandidateId(loginUser.getUserId());
        }
        return Result.success(applicationService.getPageList(queryDTO));
    }

    /**
     * 查询应聘详情
     */
    @GetMapping("/{id}")
    public Result<Application> getById(@PathVariable Long id) {
        return Result.success(applicationService.getById(id));
    }

    /**
     * 通过筛选
     */
    @PostMapping("/{id}/pass")
    @PreAuthorize("hasAuthority('application:interview')")
    public Result<Void> pass(@PathVariable Long id) {
        applicationService.pass(id);
        return Result.successMsg("已通过筛选，进入面试流程");
    }

    /**
     * 不录用
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('application:interview')")
    public Result<Void> reject(@PathVariable Long id) {
        applicationService.reject(id);
        return Result.successMsg("已标记为不录用");
    }

    /**
     * 更新备注/标签
     */
    @PutMapping("/{id}/remark")
    @PreAuthorize("hasAuthority('application:interview')")
    public Result<Void> updateRemark(@PathVariable Long id, @RequestBody Map<String, String> params) {
        applicationService.updateRemark(id, params.get("tags"), params.get("remark"));
        return Result.successMsg("更新成功");
    }

    /**
     * 应聘者提交应聘申请
     */
    @PostMapping("/submit")
    @PreAuthorize("hasAnyAuthority('application:list', 'ROLE_CANDIDATE')")
    public Result<Void> submit(@RequestBody Map<String, Long> params,
                               @AuthenticationPrincipal LoginUser loginUser) {
        Long jobId = params.get("jobId");
        Long resumeId = params.get("resumeId");
        applicationService.submitApplication(jobId, resumeId, loginUser.getUserId());
        return Result.successMsg("投递成功");
    }
}