package com.hrrecruit.module.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrrecruit.common.Constants;
import com.hrrecruit.entity.Application;
import com.hrrecruit.entity.Interview;
import com.hrrecruit.entity.JobPost;
import com.hrrecruit.entity.Offer;
import com.hrrecruit.mapper.ApplicationMapper;
import com.hrrecruit.mapper.InterviewMapper;
import com.hrrecruit.mapper.JobPostMapper;
import com.hrrecruit.mapper.OfferMapper;
import com.hrrecruit.module.report.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报表统计服务实现
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final JobPostMapper jobPostMapper;
    private final ApplicationMapper applicationMapper;
    private final InterviewMapper interviewMapper;
    private final OfferMapper offerMapper;

    @Override
    public Map<String, Object> getProgressStats() {
        Map<String, Object> result = new LinkedHashMap<>();

        long totalJobs = jobPostMapper.selectCount(null);
        long activeJobs = jobPostMapper.selectCount(
                new LambdaQueryWrapper<JobPost>().eq(JobPost::getStatus, Constants.JOB_STATUS_PUBLISHED)
        );
        long totalApplications = applicationMapper.selectCount(null);
        long totalInterviews = interviewMapper.selectCount(null);
        long totalOffers = offerMapper.selectCount(null);
        long hiredCount = offerMapper.selectCount(
                new LambdaQueryWrapper<Offer>().eq(Offer::getStatus, Constants.OFFER_STATUS_ONBOARDED)
        );

        result.put("totalJobs", totalJobs);
        result.put("activeJobs", activeJobs);
        result.put("totalApplications", totalApplications);
        result.put("totalInterviews", totalInterviews);
        result.put("totalOffers", totalOffers);
        result.put("hiredCount", hiredCount);

        return result;
    }

    @Override
    public Map<String, Object> getEffectAnalysis(String startDate, String endDate) {
        Map<String, Object> result = new LinkedHashMap<>();

        long applications = applicationMapper.selectCount(null);
        long passed = applicationMapper.selectCount(
                new LambdaQueryWrapper<Application>().eq(Application::getStatus, Constants.APP_STATUS_HIRED)
        );

        result.put("totalApplications", applications);
        result.put("hiredCount", passed);
        result.put("conversionRate",
                applications > 0 ? String.format("%.2f%%", (double) passed / applications * 100) : "0.00%");

        return result;
    }

    @Override
    public Map<String, Object> getStatsByJob() {
        Map<String, Object> result = new LinkedHashMap<>();

        List<JobPost> jobs = jobPostMapper.selectList(null);
        List<Map<String, Object>> details = new ArrayList<>();

        for (JobPost job : jobs) {
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("jobId", job.getId());
            detail.put("title", job.getTitle());
            detail.put("department", job.getDepartment());
            detail.put("appCount",
                    applicationMapper.selectCount(
                            new LambdaQueryWrapper<Application>().eq(Application::getJobId, job.getId())
                    )
            );
            details.add(detail);
        }

        result.put("list", details);
        return result;
    }

    @Override
    public Map<String, Object> getStatsByChannel() {
        Map<String, Object> result = new LinkedHashMap<>();

        List<Interview> allInterviews = interviewMapper.selectList(null);
        long pendingCount = allInterviews.stream()
                .filter(i -> Integer.valueOf(Constants.INTERVIEW_STATUS_PENDING).equals(i.getStatus()))
                .count();
        long completedCount = allInterviews.stream()
                .filter(i -> Integer.valueOf(Constants.INTERVIEW_STATUS_COMPLETED).equals(i.getStatus()))
                .count();
        long passCount = allInterviews.stream()
                .filter(i -> Integer.valueOf(Constants.INTERVIEW_RESULT_PASS).equals(i.getResult()))
                .count();

        result.put("pendingInterviews", pendingCount);
        result.put("completedInterviews", completedCount);
        result.put("passRate", allInterviews.size() > 0
                ? String.format("%.2f%%", (double) passCount / allInterviews.size() * 100) : "0.00%");

        return result;
    }

    @Override
    public void exportExcel(HttpServletResponse response) throws IOException {
        List<JobPost> jobs = jobPostMapper.selectList(null);
        List<Application> allApplications = applicationMapper.selectList(null);
        Map<Long, List<Application>> appByJob = allApplications.stream()
                .collect(Collectors.groupingBy(Application::getJobId));

        List<Interview> allInterviews = interviewMapper.selectList(null);
        Map<Long, List<Interview>> interviewByApp = allInterviews.stream()
                .collect(Collectors.groupingBy(Interview::getApplicationId));

        List<Offer> allOffers = offerMapper.selectList(null);
        Map<Long, List<Offer>> offerByApp = allOffers.stream()
                .collect(Collectors.groupingBy(Offer::getApplicationId));

        List<Map<String, Object>> data = new ArrayList<>();
        for (JobPost job : jobs) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("岗位ID", job.getId());
            row.put("岗位名称", job.getTitle());
            row.put("部门", job.getDepartment());
            row.put("招聘人数", job.getHeadcount());
            row.put("状态", getJobStatusName(job.getStatus()));

            List<Application> jobApps = appByJob.getOrDefault(job.getId(), Collections.emptyList());
            long appCount = jobApps.size();
            long interviewCount = jobApps.stream()
                    .mapToLong(a -> interviewByApp.getOrDefault(a.getId(), Collections.emptyList()).size())
                    .sum();
            long offerCount = jobApps.stream()
                    .mapToLong(a -> offerByApp.getOrDefault(a.getId(), Collections.emptyList()).size())
                    .sum();
            long hiredCount = jobApps.stream()
                    .flatMap(a -> offerByApp.getOrDefault(a.getId(), Collections.emptyList()).stream())
                    .filter(o -> o.getStatus() != null && Constants.OFFER_STATUS_ONBOARDED == o.getStatus())
                    .count();

            row.put("应聘数", appCount);
            row.put("面试数", interviewCount);
            row.put("录用数", offerCount);
            row.put("到岗数", hiredCount);
            row.put("转化率", appCount > 0 ? String.format("%.2f%%", (double) hiredCount / appCount * 100) : "-");

            data.add(row);
        }

        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "招聘统计报表_" + date + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName);

        com.alibaba.excel.EasyExcel.write(response.getOutputStream())
                .head(buildHead(data))
                .sheet("招聘统计")
                .doWrite(convertToRows(data));
    }

    private String getJobStatusName(Integer status) {
        if (status == null) return "-";
        return switch (status) {
            case Constants.JOB_STATUS_DRAFT -> "草稿";
            case Constants.JOB_STATUS_PUBLISHED -> "已发布";
            case Constants.JOB_STATUS_CLOSED -> "已关闭";
            default -> "-";
        };
    }

    private List<List<String>> buildHead(List<Map<String, Object>> data) {
        List<List<String>> head = new ArrayList<>();
        if (data.isEmpty()) {
            head.add(Collections.singletonList("暂无数据"));
            return head;
        }
        for (String key : data.get(0).keySet()) {
            head.add(Collections.singletonList(key));
        }
        return head;
    }

    private List<List<Object>> convertToRows(List<Map<String, Object>> data) {
        List<List<Object>> rows = new ArrayList<>();
        for (Map<String, Object> rowMap : data) {
            rows.add(new ArrayList<>(rowMap.values()));
        }
        if (rows.isEmpty()) {
            rows.add(Collections.singletonList("-"));
        }
        return rows;
    }
}