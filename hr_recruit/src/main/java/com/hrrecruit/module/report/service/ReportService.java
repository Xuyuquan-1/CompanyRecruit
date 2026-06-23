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
     * 招聘进度统计：统计各岗位简历投递量、面试人数、录用人数
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 各岗位统计数据列表
     */
    Map<String, Object> getRecruitmentProgress(LocalDate startDate, LocalDate endDate);

    /**
     * 招聘效果分析：按时间、岗位多维度分析
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param jobId 岗位ID（可选，为null时统计全部）
     * @return 效果分析数据（转化率、平均招聘周期等）
     */
    Map<String, Object> getRecruitmentEffect(LocalDate startDate, LocalDate endDate, Long jobId);

    /**
     * 导出Excel报表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    void exportExcel(LocalDate startDate, LocalDate endDate, HttpServletResponse response) throws IOException;
}
