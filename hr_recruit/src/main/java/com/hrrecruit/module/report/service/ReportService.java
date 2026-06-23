package com.hrrecruit.module.report.service;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * 报表统计服务接口
 */
public interface ReportService {

    /** 招聘进度统计（各岗位简历数/面试数/录用数） */
    Map<String, Object> getProgressStats();

    /** 招聘效果分析 - 按时间统计 */
    Map<String, Object> getEffectAnalysis(String startDate, String endDate);

    /** 按岗位维度统计 */
    Map<String, Object> getStatsByJob();

    /** 按渠道维度统计 */
    Map<String, Object> getStatsByChannel();

    /** 导出Excel报表 */
    void exportExcel(HttpServletResponse response) throws IOException;
}