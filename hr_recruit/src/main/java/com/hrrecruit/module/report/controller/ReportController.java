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
     * 招聘进度统计仪表盘：返回核心KPI指标 + 全部图表数据
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param department 部门筛选（可选）
     * @param jobId 岗位ID筛选（可选）
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('report:list')")
    public Result<Map<String, Object>> getDashboard(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Long jobId) {
        return Result.success(reportService.getDashboard(startDate, endDate, department, jobId));
    }

    /**
     * 招聘效果分析：按维度（time/job/channel）返回分析数据
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param dimension 维度：time-按时间, job-按岗位, channel-按渠道
     */
    @GetMapping("/analysis")
    @PreAuthorize("hasAuthority('report:list')")
    public Result<Map<String, Object>> getAnalysis(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "time") String dimension) {
        return Result.success(reportService.getAnalysis(startDate, endDate, dimension));
    }

    /**
     * 招聘进度统计（兼容旧接口）
     */
    @GetMapping("/progress")
    @PreAuthorize("hasAuthority('report:list')")
    public Result<Map<String, Object>> getRecruitmentProgress(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(reportService.getRecruitmentProgress(startDate, endDate));
    }

    /**
     * 招聘效果分析（兼容旧接口）
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
     * 导出Excel报表
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param exportType 导出类型：dashboard-进度统计, analysis-效果分析, all-全部
     */
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('report:export')")
    public void exportExcel(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "all") String exportType,
            HttpServletResponse response) throws IOException {
        reportService.exportExcel(startDate, endDate, exportType, response);
    }
}
