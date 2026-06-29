package com.hrrecruit.module.report.service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

/**
 * 报表统计服务接口
 */
public interface ReportService {

    /**
     * 招聘进度统计仪表盘：返回核心KPI + 全部图表数据
     * ReportService.getDashboard()
     */
    Map<String, Object> getDashboard(LocalDate startDate, LocalDate endDate, String department, Long jobId);

    /**
     * 招聘效果分析：按维度返回分析数据（time/job/channel）
     * ReportService.getAnalysis()
     */
    Map<String, Object> getAnalysis(LocalDate startDate, LocalDate endDate, String dimension);

    /**
     * 招聘进度统计（兼容旧接口）
     * ReportService.getRecruitmentProgress()
     */
    Map<String, Object> getRecruitmentProgress(LocalDate startDate, LocalDate endDate);

    /**
     * 招聘效果分析（兼容旧接口）
     * ReportService.getRecruitmentEffect()
     */
    Map<String, Object> getRecruitmentEffect(LocalDate startDate, LocalDate endDate, Long jobId);

    /**
     * 导出Excel报表
     * ReportService.exportExcel()
     */
    void exportExcel(LocalDate startDate, LocalDate endDate, String exportType, HttpServletResponse response) throws IOException;
}
