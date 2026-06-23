package com.hrrecruit.module.report.controller;

import com.hrrecruit.common.Result;
import com.hrrecruit.module.report.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
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
     * 招聘进度统计：统计各岗位简历投递量、面试人数、录用人数
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     */
    @GetMapping("/progress")
    @PreAuthorize("hasAuthority('report:list')")
    public Result<Map<String, Object>> getRecruitmentProgress(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(reportService.getRecruitmentProgress(startDate, endDate));
    }

    /**
     * 招聘效果分析：按时间、岗位多维度分析
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param jobId 岗位ID（可选，为null时统计全部岗位）
     */
    @GetMapping("/effect")
    @PreAuthorize("hasAuthority('report:list')")
    public Result<Map<String, Object>> getRecruitmentEffect(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) Long jobId) {
        return Result.success(reportService.getRecruitmentEffect(startDate, endDate, jobId));
    }

    /**
     * 导出Excel报表（包含招聘进度、效果分析、各岗位分析3个Sheet）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     */
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('report:export')")
    public void exportExcel(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            HttpServletResponse response) throws IOException {
        reportService.exportExcel(startDate, endDate, response);
    }
}
