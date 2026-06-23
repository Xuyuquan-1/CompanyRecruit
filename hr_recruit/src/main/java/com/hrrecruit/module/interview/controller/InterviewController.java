package com.hrrecruit.module.interview.controller;

import com.hrrecruit.common.PageResult;
import com.hrrecruit.common.Result;
import com.hrrecruit.entity.Interview;
import com.hrrecruit.module.interview.dto.InterviewDTO;
import com.hrrecruit.module.interview.dto.InterviewQueryDTO;
import com.hrrecruit.module.interview.service.InterviewService;
import com.hrrecruit.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 面试管理控制器
 */
@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    /**
     * 分页查询面试列表
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyAuthority('interview:list', 'ROLE_CANDIDATE')")
    public Result<PageResult<Interview>> page(InterviewQueryDTO queryDTO,
                                              @AuthenticationPrincipal LoginUser loginUser) {
        // 应聘者只能查看自己的面试记录
        if (loginUser.getRoleCodes().contains("ROLE_CANDIDATE")) {
            queryDTO.setCandidateId(loginUser.getUserId());
        }
        return Result.success(interviewService.getPageList(queryDTO));
    }

    /**
     * 查询面试详情
     */
    @GetMapping("/{id}")
    public Result<Interview> getById(@PathVariable Long id) {
        return Result.success(interviewService.getById(id));
    }

    /**
     * 安排面试
     */
    @PostMapping
    @PreAuthorize("hasAuthority('interview:list')")
    public Result<Void> arrange(@Valid @RequestBody InterviewDTO dto) {
        interviewService.arrange(dto);
        return Result.successMsg("面试安排成功");
    }

    /**
     * 取消面试
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('interview:list')")
    public Result<Void> cancel(@PathVariable Long id) {
        interviewService.cancel(id);
        return Result.successMsg("面试已取消");
    }

    /**
     * 填写面试评价
     */
    @PostMapping("/{id}/evaluate")
    @PreAuthorize("hasAuthority('interview:evaluate')")
    public Result<Void> evaluate(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        String evaluation = (String) params.get("evaluation");
        Integer result = params.get("result") != null ? ((Number) params.get("result")).intValue() : null;
        interviewService.evaluate(id, evaluation, result);
        return Result.successMsg("评价提交成功");
    }
}