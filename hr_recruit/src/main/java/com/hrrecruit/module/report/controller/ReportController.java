package com.hrrecruit.module.report.controller;

import com.hrrecruit.common.Result;
import com.hrrecruit.module.report.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

/**
 * 报表统计控制器
 */
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * 招聘进度统计看板
     * 各岗位简历数量、面试人数、录用人数等
     */
    @GetMapping("/progress")
    @PreAuthorize("hasAuthority('report:list')")
    public Result<Map<String, Object>> getProgressStats() {
        return Result.success(reportService.getProgressStats());
    }

    /**
     * 招聘效果分析 - 按时间范围
     */
    @GetMapping("/effect")
    @PreAuthorize("hasAuthority('report:list')")
    public Result<Map<String, Object>> getEffectAnalysis(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(reportService.getEffectAnalysis(startDate, endDate));
    }

    /**
     * 按岗位维度统计
     */
    @GetMapping("/by-job")
    @PreAuthorize("hasAuthority('report:list')")
    public Result<Map<String, Object>> getStatsByJob() {
        return Result.success(reportService.getStatsByJob());
    }

    /**
     * 按渠道/面试维度统计
     */
    @GetMapping("/by-channel")
    @PreAuthorize("hasAuthority('report:list')")
    public Result<Map<String, Object>> getStatsByChannel() {
        return Result.success(reportService.getStatsByChannel());
    }

    /**
     * 导出Excel报表
     */
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('report:export')")
    public void exportExcel(HttpServletResponse response) throws IOException {
        reportService.exportExcel(response);
    }
}