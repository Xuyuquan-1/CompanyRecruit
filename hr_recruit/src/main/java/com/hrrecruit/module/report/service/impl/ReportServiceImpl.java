package com.hrrecruit.module.report.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报表统计服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final JobPostMapper jobPostMapper;
    private final ApplicationMapper applicationMapper;
    private final InterviewMapper interviewMapper;
    private final OfferMapper offerMapper;

    // ==================== 新增：仪表盘接口 ====================

    @Override
    public Map<String, Object> getDashboard(LocalDate startDate, LocalDate endDate, String department, Long jobId) {
        log.info("查询仪表盘，时间：{}~{}, 部门：{}, 岗位ID：{}", startDate, endDate, department, jobId);

        Map<String, Object> result = new LinkedHashMap<>();

        // 1. 查询基础数据
        List<JobPost> jobs = jobPostMapper.selectList(null);
        if (department != null && !department.isEmpty()) {
            jobs = jobs.stream().filter(j -> department.equals(j.getDepartment())).collect(Collectors.toList());
        }
        if (jobId != null) {
            jobs = jobs.stream().filter(j -> j.getId().equals(jobId)).collect(Collectors.toList());
        }
        Set<Long> jobIds = jobs.stream().map(JobPost::getId).collect(Collectors.toSet());

        // 2. 查询应聘记录
        LambdaQueryWrapper<Application> appWrapper = new LambdaQueryWrapper<>();
        if (!jobIds.isEmpty()) {
            appWrapper.in(Application::getJobId, jobIds);
        } else if (department != null && !department.isEmpty()) {
            // 如果没有匹配的岗位，返回空
            appWrapper.in(Application::getJobId, Collections.singleton(-1L));
        }
        if (startDate != null) appWrapper.ge(Application::getApplyTime, startDate.atStartOfDay());
        if (endDate != null) appWrapper.le(Application::getApplyTime, endDate.plusDays(1).atStartOfDay());
        List<Application> applications = applicationMapper.selectList(appWrapper);
        List<Long> appIds = applications.stream().map(Application::getId).collect(Collectors.toList());

        // 3. 查询面试和Offer
        List<Interview> interviews;
        List<Offer> offers;
        if (!appIds.isEmpty()) {
            LambdaQueryWrapper<Interview> iw = new LambdaQueryWrapper<Interview>().in(Interview::getApplicationId, appIds);
            interviews = interviewMapper.selectList(iw);
            LambdaQueryWrapper<Offer> ow = new LambdaQueryWrapper<Offer>().in(Offer::getApplicationId, appIds);
            offers = offerMapper.selectList(ow);
        } else {
            interviews = Collections.emptyList();
            offers = Collections.emptyList();
        }

        // === KPI 指标 ===
        long totalApplications = applications.size();
        long pendingInterviews = applications.stream()
                .filter(a -> a.getStatus() != null && a.getStatus() == Constants.APP_STATUS_INTERVIEWING)
                .count();
        long totalInterviews = interviews.size();
        long interviewPassed = interviews.stream()
                .filter(i -> i.getResult() != null && i.getResult() == Constants.INTERVIEW_RESULT_PASS).count();
        double interviewPassRate = totalInterviews > 0 ? (double) interviewPassed / totalInterviews * 100 : 0;
        long totalOffers = offers.size();
        long offersAccepted = offers.stream()
                .filter(o -> o.getStatus() != null && (o.getStatus() == Constants.OFFER_STATUS_ACCEPTED || o.getStatus() == Constants.OFFER_STATUS_ONBOARDED))
                .count();
        long totalHired = offersAccepted;
        int totalHeadcount = jobs.stream().mapToInt(j -> j.getHeadcount() != null ? j.getHeadcount() : 0).sum();
        double completionRate = totalHeadcount > 0 ? (double) totalHired / totalHeadcount * 100 : 0;

        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("totalApplications", totalApplications);
        kpi.put("pendingInterviews", pendingInterviews);
        kpi.put("interviewPassRate", String.format("%.1f", interviewPassRate));
        kpi.put("totalHired", totalHired);
        kpi.put("recruitmentCompletionRate", String.format("%.1f", completionRate));
        kpi.put("totalHeadcount", totalHeadcount);
        result.put("kpi", kpi);

        // === 图表1：各岗位投递量排行 ===
        Map<Long, Long> appCountByJob = applications.stream()
                .collect(Collectors.groupingBy(Application::getJobId, Collectors.counting()));
        List<Map<String, Object>> jobRanking = jobs.stream()
                .map(j -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("jobTitle", j.getTitle());
                    item.put("applicationCount", appCountByJob.getOrDefault(j.getId(), 0L));
                    return item;
                })
                .sorted((a, b) -> Long.compare((long) b.get("applicationCount"), (long) a.get("applicationCount")))
                .collect(Collectors.toList());
        result.put("jobApplicationRanking", jobRanking);

        // === 图表2：招聘漏斗 ===
        long passedScreening = applications.stream()
                .filter(a -> a.getStatus() != null && a.getStatus() >= Constants.APP_STATUS_PASSED).count();
        List<Map<String, Object>> funnel = new ArrayList<>();
        funnel.add(Map.of("stage", "简历投递", "count", totalApplications));
        funnel.add(Map.of("stage", "筛选通过", "count", passedScreening));
        funnel.add(Map.of("stage", "面试", "count", totalInterviews));
        funnel.add(Map.of("stage", "录用", "count", totalOffers));
        funnel.add(Map.of("stage", "入职", "count", offers.stream()
                .filter(o -> o.getStatus() != null && o.getStatus() == Constants.OFFER_STATUS_ONBOARDED).count()));
        result.put("funnel", funnel);

        // === 图表3：面试状态分布 ===
        Map<String, Long> interviewStatusMap = new LinkedHashMap<>();
        interviewStatusMap.put("待面试", interviews.stream()
                .filter(i -> i.getStatus() != null && i.getStatus() == Constants.INTERVIEW_STATUS_PENDING).count());
        interviewStatusMap.put("已面试", interviews.stream()
                .filter(i -> i.getStatus() != null && i.getStatus() == Constants.INTERVIEW_STATUS_COMPLETED).count());
        interviewStatusMap.put("已取消", interviews.stream()
                .filter(i -> i.getStatus() != null && i.getStatus() == Constants.INTERVIEW_STATUS_CANCELLED).count());
        // 加上录用和不录用
        long hiredCount = applications.stream()
                .filter(a -> a.getStatus() != null && (a.getStatus() == Constants.APP_STATUS_OFFER_PENDING
                        || a.getStatus() == Constants.APP_STATUS_OFFER_ACCEPTED || a.getStatus() == Constants.APP_STATUS_ONBOARDED))
                .count();
        long rejectedCount = applications.stream()
                .filter(a -> a.getStatus() != null && a.getStatus() == Constants.APP_STATUS_REJECTED).count();
        List<Map<String, Object>> interviewDistribution = new ArrayList<>();
        interviewDistribution.add(Map.of("name", "待面试", "value", interviewStatusMap.get("待面试")));
        interviewDistribution.add(Map.of("name", "已面试-通过", "value", interviewPassed));
        interviewDistribution.add(Map.of("name", "已录用", "value", hiredCount));
        interviewDistribution.add(Map.of("name", "不录用", "value", rejectedCount));
        result.put("interviewStatusDistribution", interviewDistribution);

        // === 图表4：投递趋势（按日） ===
        Map<String, Long> trendMap = new TreeMap<>();
        // 确定趋势图日期范围：有参数用参数，无参数从数据推算
        LocalDate trendStart = startDate;
        LocalDate trendEnd = endDate;
        if (trendStart == null || trendEnd == null) {
            // 从 applications 中找出最早和最晚日期
            Optional<LocalDate> minDate = applications.stream()
                    .filter(a -> a.getApplyTime() != null)
                    .map(a -> a.getApplyTime().toLocalDate())
                    .min(LocalDate::compareTo);
            Optional<LocalDate> maxDate = applications.stream()
                    .filter(a -> a.getApplyTime() != null)
                    .map(a -> a.getApplyTime().toLocalDate())
                    .max(LocalDate::compareTo);
            if (minDate.isPresent() && maxDate.isPresent()) {
                trendStart = minDate.get().withDayOfMonth(1); // 从月初开始
                trendEnd = maxDate.get();
            }
        }
        if (trendStart != null && trendEnd != null) {
            LocalDate cursor = trendStart;
            while (!cursor.isAfter(trendEnd)) {
                trendMap.put(cursor.toString(), 0L);
                cursor = cursor.plusDays(1);
            }
        }
        for (Application app : applications) {
            if (app.getApplyTime() != null) {
                String dateKey = app.getApplyTime().toLocalDate().toString();
                trendMap.merge(dateKey, 1L, Long::sum);
            }
        }
        List<Map<String, Object>> trend = trendMap.entrySet().stream()
                .map(e -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("date", e.getKey());
                    item.put("count", e.getValue());
                    return item;
                })
                .collect(Collectors.toList());
        result.put("applicationTrend", trend);

        // === 图表5：各部门招聘进度 ===
        Map<String, List<JobPost>> jobsByDept = jobs.stream()
                .collect(Collectors.groupingBy(j -> j.getDepartment() != null ? j.getDepartment() : "未分配"));
        List<Map<String, Object>> deptProgress = new ArrayList<>();
        for (Map.Entry<String, List<JobPost>> entry : jobsByDept.entrySet()) {
            Set<Long> deptJobIds = entry.getValue().stream().map(JobPost::getId).collect(Collectors.toSet());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("department", entry.getKey());
            long deptApps = applications.stream().filter(a -> deptJobIds.contains(a.getJobId())).count();
            long deptInterviews = 0;
            long deptOffers = 0;
            long deptOnboard = 0;
            List<Long> deptAppIds = applications.stream().filter(a -> deptJobIds.contains(a.getJobId()))
                    .map(Application::getId).collect(Collectors.toList());
            if (!deptAppIds.isEmpty()) {
                deptInterviews = interviews.stream().filter(i -> deptAppIds.contains(i.getApplicationId())).count();
                deptOffers = offers.stream().filter(o -> deptAppIds.contains(o.getApplicationId())
                        && o.getStatus() != null && (o.getStatus() == Constants.OFFER_STATUS_ACCEPTED || o.getStatus() == Constants.OFFER_STATUS_ONBOARDED))
                        .count();
                deptOnboard = offers.stream().filter(o -> deptAppIds.contains(o.getApplicationId())
                        && o.getStatus() != null && o.getStatus() == Constants.OFFER_STATUS_ONBOARDED)
                        .count();
            }
            int deptHeadcount = entry.getValue().stream().mapToInt(j -> j.getHeadcount() != null ? j.getHeadcount() : 0).sum();
            item.put("applicationCount", deptApps);
            item.put("interviewCount", deptInterviews);
            item.put("offerCount", deptOffers);
            item.put("onboardCount", deptOnboard);
            item.put("headcount", deptHeadcount);
            deptProgress.add(item);
        }
        result.put("departmentProgress", deptProgress);

        // === 图表6：岗位录用率TOP5 ===
        List<Map<String, Object>> top5List = jobs.stream().map(j -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("jobTitle", j.getTitle());
            long jobApps = applications.stream().filter(a -> a.getJobId().equals(j.getId())).count();
            List<Long> jobAppIds = applications.stream().filter(a -> a.getJobId().equals(j.getId()))
                    .map(Application::getId).collect(Collectors.toList());
            long jobOffers = 0;
            if (!jobAppIds.isEmpty()) {
                jobOffers = offers.stream().filter(o -> jobAppIds.contains(o.getApplicationId())
                        && o.getStatus() != null && (o.getStatus() == Constants.OFFER_STATUS_ACCEPTED || o.getStatus() == Constants.OFFER_STATUS_ONBOARDED))
                        .count();
            }
            double rate = jobApps > 0 ? (double) jobOffers / jobApps * 100 : 0;
            item.put("hireRate", Math.round(rate * 100.0) / 100.0);
            item.put("offerCount", jobOffers);
            item.put("applicationCount", jobApps);
            return item;
        }).sorted((a, b) -> Double.compare((double) b.get("hireRate"), (double) a.get("hireRate")))
                .limit(5)
                .collect(Collectors.toList());
        result.put("topHireRateJobs", top5List);

        return result;
    }

    // ==================== 新增：效果分析接口 ====================

    @Override
    public Map<String, Object> getAnalysis(LocalDate startDate, LocalDate endDate, String dimension) {
        log.info("查询效果分析，时间：{}~{}, 维度：{}", startDate, endDate, dimension);

        Map<String, Object> result = new LinkedHashMap<>();

        // 通用KPI
        LambdaQueryWrapper<Application> appWrapper = new LambdaQueryWrapper<>();
        if (startDate != null) appWrapper.ge(Application::getApplyTime, startDate.atStartOfDay());
        if (endDate != null) appWrapper.le(Application::getApplyTime, endDate.plusDays(1).atStartOfDay());
        List<Application> applications = applicationMapper.selectList(appWrapper);
        List<Long> appIds = applications.stream().map(Application::getId).collect(Collectors.toList());

        // 计算通用KPI
        long totalApps = applications.size();
        long passedScreening = applications.stream()
                .filter(a -> a.getStatus() != null && a.getStatus() >= Constants.APP_STATUS_PASSED).count();
        double screeningPassRate = totalApps > 0 ? (double) passedScreening / totalApps * 100 : 0;

        List<Offer> offers;
        if (!appIds.isEmpty()) {
            LambdaQueryWrapper<Offer> ow = new LambdaQueryWrapper<Offer>().in(Offer::getApplicationId, appIds);
            offers = offerMapper.selectList(ow);
        } else {
            offers = Collections.emptyList();
        }
        long totalOffers = offers.size();
        long offersAccepted = offers.stream()
                .filter(o -> o.getStatus() != null && (o.getStatus() == Constants.OFFER_STATUS_ACCEPTED || o.getStatus() == Constants.OFFER_STATUS_ONBOARDED))
                .count();
        double offerAcceptRate = totalOffers > 0 ? (double) offersAccepted / totalOffers * 100 : 0;

        // 平均招聘周期（从投递到录用接受的天数）
        double avgCycle = calculateAvgRecruitmentCycle(applications, offers);

        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("avgRecruitmentCycle", String.format("%.1f", avgCycle));
        kpi.put("avgCostPerHire", "暂无数据");
        kpi.put("screeningPassRate", String.format("%.1f", screeningPassRate));
        kpi.put("offerAcceptRate", String.format("%.1f", offerAcceptRate));
        result.put("kpi", kpi);

        // 按维度返回数据
        switch (dimension != null ? dimension : "time") {
            case "job":
                result.putAll(buildJobAnalysis(applications, appIds, offers, startDate, endDate));
                break;
            case "channel":
                result.putAll(buildChannelAnalysis(applications, appIds, offers));
                break;
            case "time":
            default:
                result.putAll(buildTimeAnalysis(applications, appIds, offers, startDate, endDate));
                break;
        }

        return result;
    }

    // ==================== 兼容旧接口 ====================

    @Override
    public Map<String, Object> getRecruitmentProgress(LocalDate startDate, LocalDate endDate) {
        log.info("查询招聘进度（旧接口），时间范围：{} 至 {}", startDate, endDate);

        Map<String, Object> result = new LinkedHashMap<>();
        List<JobPost> jobs = jobPostMapper.selectList(null);
        List<Map<String, Object>> progressList = new ArrayList<>();

        for (JobPost job : jobs) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("jobId", job.getId());
            item.put("jobTitle", job.getTitle());
            item.put("department", job.getDepartment());
            item.put("headcount", job.getHeadcount());

            LambdaQueryWrapper<Application> appWrapper = new LambdaQueryWrapper<Application>()
                    .eq(Application::getJobId, job.getId());
            if (startDate != null) appWrapper.ge(Application::getApplyTime, startDate.atStartOfDay());
            if (endDate != null) appWrapper.le(Application::getApplyTime, endDate.plusDays(1).atStartOfDay());
            List<Application> applications = applicationMapper.selectList(appWrapper);
            long applicationCount = applications.size();
            List<Long> appIds = applications.stream().map(Application::getId).collect(Collectors.toList());

            long interviewCount = 0;
            if (!appIds.isEmpty()) {
                LambdaQueryWrapper<Interview> interviewWrapper = new LambdaQueryWrapper<Interview>()
                        .in(Interview::getApplicationId, appIds);
                if (startDate != null) interviewWrapper.ge(Interview::getInterviewTime, startDate.atStartOfDay());
                if (endDate != null) interviewWrapper.le(Interview::getInterviewTime, endDate.plusDays(1).atStartOfDay());
                interviewCount = interviewMapper.selectCount(interviewWrapper);
            }

            // Offer 由所属 application 决定范围，不再按 offer_time 做日期过滤
            long offerCount = 0;
            if (!appIds.isEmpty()) {
                LambdaQueryWrapper<Offer> offerWrapper = new LambdaQueryWrapper<Offer>()
                        .in(Offer::getApplicationId, appIds)
                        .eq(Offer::getStatus, Constants.OFFER_STATUS_ACCEPTED);
                offerCount = offerMapper.selectCount(offerWrapper);
            }

            long onboardCount = 0;
            if (!appIds.isEmpty()) {
                LambdaQueryWrapper<Offer> offerWrapper = new LambdaQueryWrapper<Offer>()
                        .in(Offer::getApplicationId, appIds)
                        .eq(Offer::getStatus, Constants.OFFER_STATUS_ONBOARDED);
                onboardCount = offerMapper.selectCount(offerWrapper);
            }

            double conversionRate = applicationCount > 0 ? (double) onboardCount / applicationCount * 100 : 0;
            item.put("applicationCount", applicationCount);
            item.put("interviewCount", interviewCount);
            item.put("offerCount", offerCount);
            item.put("onboardCount", onboardCount);
            item.put("conversionRate", String.format("%.2f%%", conversionRate));
            progressList.add(item);
        }

        long totalApplications = progressList.stream().mapToLong(m -> (long) m.get("applicationCount")).sum();
        long totalInterviews = progressList.stream().mapToLong(m -> (long) m.get("interviewCount")).sum();
        long totalOffers = progressList.stream().mapToLong(m -> (long) m.get("offerCount")).sum();
        long totalOnboard = progressList.stream().mapToLong(m -> (long) m.get("onboardCount")).sum();

        result.put("progressList", progressList);
        result.put("summary", Map.of(
                "totalJobs", jobs.size(),
                "totalApplications", totalApplications,
                "totalInterviews", totalInterviews,
                "totalOffers", totalOffers,
                "totalOnboard", totalOnboard,
                "overallConversionRate", totalApplications > 0
                        ? String.format("%.2f%%", (double) totalOnboard / totalApplications * 100)
                        : "0.00%"
        ));
        return result;
    }

    @Override
    public Map<String, Object> getRecruitmentEffect(LocalDate startDate, LocalDate endDate, Long jobId) {
        log.info("查询招聘效果（旧接口），时间范围：{} 至 {}, 岗位ID：{}", startDate, endDate, jobId);

        Map<String, Object> result = new LinkedHashMap<>();
        LambdaQueryWrapper<Application> appWrapper = new LambdaQueryWrapper<>();
        if (jobId != null) appWrapper.eq(Application::getJobId, jobId);
        if (startDate != null) appWrapper.ge(Application::getApplyTime, startDate.atStartOfDay());
        if (endDate != null) appWrapper.le(Application::getApplyTime, endDate.plusDays(1).atStartOfDay());
        List<Application> applications = applicationMapper.selectList(appWrapper);

        long totalApplications = applications.size();
        long passedScreening = applications.stream()
                .filter(a -> a.getStatus() != null && a.getStatus() >= Constants.APP_STATUS_PASSED).count();
        long interviewing = applications.stream()
                .filter(a -> a.getStatus() != null && a.getStatus() == Constants.APP_STATUS_INTERVIEWING).count();
        long hired = applications.stream()
                .filter(a -> a.getStatus() != null && a.getStatus() == Constants.APP_STATUS_OFFER_PENDING).count();
        long rejected = applications.stream()
                .filter(a -> a.getStatus() != null && a.getStatus() == Constants.APP_STATUS_REJECTED).count();

        List<Long> appIds = applications.stream().map(Application::getId).collect(Collectors.toList());
        long totalInterviews = 0;
        long interviewPassed = 0;
        if (!appIds.isEmpty()) {
            LambdaQueryWrapper<Interview> iw = new LambdaQueryWrapper<Interview>().in(Interview::getApplicationId, appIds);
            if (startDate != null) iw.ge(Interview::getInterviewTime, startDate.atStartOfDay());
            if (endDate != null) iw.le(Interview::getInterviewTime, endDate.plusDays(1).atStartOfDay());
            List<Interview> interviews = interviewMapper.selectList(iw);
            totalInterviews = interviews.size();
            interviewPassed = interviews.stream()
                    .filter(i -> i.getResult() != null && i.getResult() == Constants.INTERVIEW_RESULT_PASS).count();
        }

        // Offer 由所属 application 决定范围，不再按 offer_time 做日期过滤
        long totalOffers = 0, offersAccepted = 0, offersRejected = 0, onboarded = 0;
        if (!appIds.isEmpty()) {
            LambdaQueryWrapper<Offer> ow = new LambdaQueryWrapper<Offer>().in(Offer::getApplicationId, appIds);
            List<Offer> offers = offerMapper.selectList(ow);
            totalOffers = offers.size();
            offersAccepted = offers.stream().filter(o -> o.getStatus() != null && o.getStatus() == Constants.OFFER_STATUS_ACCEPTED).count();
            offersRejected = offers.stream().filter(o -> o.getStatus() != null && o.getStatus() == Constants.OFFER_STATUS_REJECTED).count();
            onboarded = offers.stream().filter(o -> o.getStatus() != null && o.getStatus() == Constants.OFFER_STATUS_ONBOARDED).count();
        }

        double screeningPassRate = totalApplications > 0 ? (double) passedScreening / totalApplications * 100 : 0;
        double interviewPassRate = totalInterviews > 0 ? (double) interviewPassed / totalInterviews * 100 : 0;
        double offerAcceptRate = totalOffers > 0 ? (double) offersAccepted / totalOffers * 100 : 0;
        double overallConversionRate = totalApplications > 0 ? (double) onboarded / totalApplications * 100 : 0;

        List<Map<String, Object>> jobAnalysis = new ArrayList<>();
        if (jobId == null) {
            Map<Long, List<Application>> appsByJob = applications.stream().collect(Collectors.groupingBy(Application::getJobId));
            for (Map.Entry<Long, List<Application>> entry : appsByJob.entrySet()) {
                JobPost job = jobPostMapper.selectById(entry.getKey());
                if (job == null) continue;
                List<Application> jobApps = entry.getValue();
                long jobAppCount = jobApps.size();
                List<Long> jobAppIds = jobApps.stream().map(Application::getId).collect(Collectors.toList());
                // Offer 由所属 application 决定范围，不再按 offer_time 做日期过滤
                long jobOnboardCount = 0;
                if (!jobAppIds.isEmpty()) {
                    LambdaQueryWrapper<Offer> offerWrapper = new LambdaQueryWrapper<Offer>()
                            .in(Offer::getApplicationId, jobAppIds)
                            .eq(Offer::getStatus, Constants.OFFER_STATUS_ONBOARDED);
                    jobOnboardCount = offerMapper.selectCount(offerWrapper);
                }
                Map<String, Object> jobItem = new LinkedHashMap<>();
                jobItem.put("jobId", job.getId());
                jobItem.put("jobTitle", job.getTitle());
                jobItem.put("department", job.getDepartment());
                jobItem.put("applicationCount", jobAppCount);
                jobItem.put("onboardCount", jobOnboardCount);
                jobItem.put("conversionRate", jobAppCount > 0
                        ? String.format("%.2f%%", (double) jobOnboardCount / jobAppCount * 100) : "0.00%");
                jobAnalysis.add(jobItem);
            }
        }

        result.put("totalApplications", totalApplications);
        result.put("passedScreening", passedScreening);
        result.put("interviewing", interviewing);
        result.put("hired", hired);
        result.put("rejected", rejected);
        result.put("totalInterviews", totalInterviews);
        result.put("interviewPassed", interviewPassed);
        result.put("totalOffers", totalOffers);
        result.put("offersAccepted", offersAccepted);
        result.put("offersRejected", offersRejected);
        result.put("onboarded", onboarded);
        result.put("screeningPassRate", String.format("%.2f%%", screeningPassRate));
        result.put("interviewPassRate", String.format("%.2f%%", interviewPassRate));
        result.put("offerAcceptRate", String.format("%.2f%%", offerAcceptRate));
        result.put("overallConversionRate", String.format("%.2f%%", overallConversionRate));
        result.put("jobAnalysis", jobAnalysis);
        return result;
    }

    // ==================== 导出 ====================

    @Override
    public void exportExcel(LocalDate startDate, LocalDate endDate, String exportType, HttpServletResponse response) throws IOException {
        log.info("导出招聘报表，时间范围：{}~{}, 类型：{}", startDate, endDate, exportType);

        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "招聘统计报表_" + date + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName);

        try {
            com.alibaba.excel.ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build();
            int sheetIndex = 0;

            if ("all".equals(exportType) || "dashboard".equals(exportType)) {
                Map<String, Object> progressData = getRecruitmentProgress(startDate, endDate);
                WriteSheet progressSheet = EasyExcel.writerSheet(sheetIndex++, "招聘进度统计")
                        .head(buildProgressHead())
                        .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()).build();
                excelWriter.write(convertProgressToRows((List<Map<String, Object>>) progressData.get("progressList")), progressSheet);

                Map<String, Object> effectData = getRecruitmentEffect(startDate, endDate, null);
                WriteSheet effectSheet = EasyExcel.writerSheet(sheetIndex++, "招聘效果分析")
                        .head(buildEffectHead())
                        .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()).build();
                excelWriter.write(convertEffectToRows(effectData), effectSheet);

                WriteSheet jobSheet = EasyExcel.writerSheet(sheetIndex++, "各岗位分析")
                        .head(buildJobAnalysisHead())
                        .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()).build();
                excelWriter.write(convertJobAnalysisToRows((List<Map<String, Object>>) effectData.get("jobAnalysis")), jobSheet);
            }

            if ("all".equals(exportType) || "analysis".equals(exportType)) {
                // 按时间分析
                Map<String, Object> timeData = getAnalysis(startDate, endDate, "time");
                if (timeData.containsKey("tableData")) {
                    List<Map<String, Object>> timeTable = (List<Map<String, Object>>) timeData.get("tableData");
                    WriteSheet timeSheet = EasyExcel.writerSheet(sheetIndex++, "按时间分析")
                            .head(buildTimeAnalysisHead())
                            .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()).build();
                    excelWriter.write(convertTimeAnalysisToRows(timeTable), timeSheet);
                }

                // 按岗位分析
                Map<String, Object> jobData = getAnalysis(startDate, endDate, "job");
                if (jobData.containsKey("tableData")) {
                    List<Map<String, Object>> jobTable = (List<Map<String, Object>>) jobData.get("tableData");
                    WriteSheet jobSheet2 = EasyExcel.writerSheet(sheetIndex++, "按岗位分析")
                            .head(buildJobDimAnalysisHead())
                            .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()).build();
                    excelWriter.write(convertJobDimAnalysisToRows(jobTable), jobSheet2);
                }
            }

            excelWriter.finish();
            log.info("报表导出成功：{}", fileName);
        } catch (Exception e) {
            log.error("报表导出失败", e);
            throw new IOException("报表导出失败：" + e.getMessage());
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * ReportServiceImpl.calculateAvgRecruitmentCycle() - 计算平均招聘周期（天）
     */
    private double calculateAvgRecruitmentCycle(List<Application> applications, List<Offer> offers) {
        if (offers.isEmpty()) return 0;
        Map<Long, Application> appMap = applications.stream().collect(Collectors.toMap(Application::getId, a -> a));
        long totalDays = 0;
        int count = 0;
        for (Offer offer : offers) {
            if (offer.getStatus() != null && (offer.getStatus() == Constants.OFFER_STATUS_ACCEPTED || offer.getStatus() == Constants.OFFER_STATUS_ONBOARDED)) {
                Application app = appMap.get(offer.getApplicationId());
                if (app != null && app.getApplyTime() != null && offer.getOfferTime() != null) {
                    long days = ChronoUnit.DAYS.between(app.getApplyTime().toLocalDate(), offer.getOfferTime().toLocalDate());
                    if (days >= 0) {
                        totalDays += days;
                        count++;
                    }
                }
            }
        }
        return count > 0 ? (double) totalDays / count : 0;
    }

    /**
     * ReportServiceImpl.buildTimeAnalysis() - 按时间维度分析
     */
    private Map<String, Object> buildTimeAnalysis(List<Application> applications, List<Long> appIds,
                                                   List<Offer> offers, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 按月分组
        Map<String, List<Application>> appsByMonth = applications.stream()
                .filter(a -> a.getApplyTime() != null)
                .collect(Collectors.groupingBy(a -> a.getApplyTime().format(DateTimeFormatter.ofPattern("yyyy-MM")), TreeMap::new, Collectors.toList()));

        // 面试按月
        List<Interview> interviews = Collections.emptyList();
        if (!appIds.isEmpty()) {
            LambdaQueryWrapper<Interview> iw = new LambdaQueryWrapper<Interview>().in(Interview::getApplicationId, appIds);
            interviews = interviewMapper.selectList(iw);
        }
        Map<String, Long> interviewByMonth = interviews.stream()
                .filter(i -> i.getInterviewTime() != null)
                .collect(Collectors.groupingBy(i -> i.getInterviewTime().format(DateTimeFormatter.ofPattern("yyyy-MM")), Collectors.counting()));

        // 录用按月
        Map<String, Long> offerByMonth = offers.stream()
                .filter(o -> o.getOfferTime() != null && o.getStatus() != null
                        && (o.getStatus() == Constants.OFFER_STATUS_ACCEPTED || o.getStatus() == Constants.OFFER_STATUS_ONBOARDED))
                .collect(Collectors.groupingBy(o -> o.getOfferTime().format(DateTimeFormatter.ofPattern("yyyy-MM")), Collectors.counting()));

        // 图表数据
        Set<String> allMonths = new TreeSet<>(appsByMonth.keySet());
        allMonths.addAll(interviewByMonth.keySet());
        allMonths.addAll(offerByMonth.keySet());

        List<Map<String, Object>> chartData = new ArrayList<>();
        List<Map<String, Object>> tableData = new ArrayList<>();
        for (String month : allMonths) {
            long appCount = appsByMonth.containsKey(month) ? appsByMonth.get(month).size() : 0;
            long intCount = interviewByMonth.getOrDefault(month, 0L);
            long offCount = offerByMonth.getOrDefault(month, 0L);

            Map<String, Object> chart = new LinkedHashMap<>();
            chart.put("period", month);
            chart.put("applicationCount", appCount);
            chart.put("interviewCount", intCount);
            chart.put("hireCount", offCount);
            chartData.add(chart);

            Map<String, Object> table = new LinkedHashMap<>();
            table.put("period", month);
            table.put("applicationCount", appCount);
            table.put("screeningPassed", appsByMonth.containsKey(month) ? appsByMonth.get(month).stream()
                    .filter(a -> a.getStatus() != null && a.getStatus() >= Constants.APP_STATUS_PASSED).count() : 0);
            table.put("interviewCount", intCount);
            table.put("hireCount", offCount);
            table.put("conversionRate", appCount > 0 ? String.format("%.1f%%", (double) offCount / appCount * 100) : "0.0%");
            tableData.add(table);
        }

        result.put("chartData", chartData);
        result.put("tableData", tableData);
        return result;
    }

    /**
     * ReportServiceImpl.buildJobAnalysis() - 按岗位维度分析
     */
    private Map<String, Object> buildJobAnalysis(List<Application> applications, List<Long> appIds,
                                                  List<Offer> offers, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<JobPost> jobs = jobPostMapper.selectList(null);

        Map<Long, List<Application>> appsByJob = applications.stream().collect(Collectors.groupingBy(Application::getJobId));

        List<Interview> interviews = Collections.emptyList();
        if (!appIds.isEmpty()) {
            LambdaQueryWrapper<Interview> iw = new LambdaQueryWrapper<Interview>().in(Interview::getApplicationId, appIds);
            interviews = interviewMapper.selectList(iw);
        }

        List<Map<String, Object>> comparisonChart = new ArrayList<>();
        List<Map<String, Object>> cycleChart = new ArrayList<>();
        List<Map<String, Object>> tableData = new ArrayList<>();

        for (JobPost job : jobs) {
            List<Application> jobApps = appsByJob.getOrDefault(job.getId(), Collections.emptyList());
            long appCount = jobApps.size();
            List<Long> jobAppIds = jobApps.stream().map(Application::getId).collect(Collectors.toList());

            long intCount = 0;
            long hireCount = 0;
            double avgCycle = 0;
            if (!jobAppIds.isEmpty()) {
                intCount = interviews.stream().filter(i -> jobAppIds.contains(i.getApplicationId())).count();
                List<Offer> jobOffers = offers.stream().filter(o -> jobAppIds.contains(o.getApplicationId())).collect(Collectors.toList());
                hireCount = jobOffers.stream().filter(o -> o.getStatus() != null
                        && (o.getStatus() == Constants.OFFER_STATUS_ACCEPTED || o.getStatus() == Constants.OFFER_STATUS_ONBOARDED)).count();
                // 计算平均周期
                int cycleCount = 0;
                long totalDays = 0;
                Map<Long, Application> appMap = jobApps.stream().collect(Collectors.toMap(Application::getId, a -> a));
                for (Offer offer : jobOffers) {
                    if (offer.getStatus() != null && (offer.getStatus() == Constants.OFFER_STATUS_ACCEPTED || offer.getStatus() == Constants.OFFER_STATUS_ONBOARDED)) {
                        Application app = appMap.get(offer.getApplicationId());
                        if (app != null && app.getApplyTime() != null && offer.getOfferTime() != null) {
                            totalDays += ChronoUnit.DAYS.between(app.getApplyTime().toLocalDate(), offer.getOfferTime().toLocalDate());
                            cycleCount++;
                        }
                    }
                }
                avgCycle = cycleCount > 0 ? (double) totalDays / cycleCount : 0;
            }

            Map<String, Object> comp = new LinkedHashMap<>();
            comp.put("jobTitle", job.getTitle());
            comp.put("applicationCount", appCount);
            comp.put("interviewCount", intCount);
            comp.put("hireCount", hireCount);
            comparisonChart.add(comp);

            Map<String, Object> cycle = new LinkedHashMap<>();
            cycle.put("jobTitle", job.getTitle());
            cycle.put("avgCycle", Math.round(avgCycle * 10.0) / 10.0);
            cycleChart.add(cycle);

            Map<String, Object> table = new LinkedHashMap<>();
            table.put("jobTitle", job.getTitle());
            table.put("department", job.getDepartment());
            table.put("applicationCount", appCount);
            table.put("screeningPassed", jobApps.stream().filter(a -> a.getStatus() != null && a.getStatus() >= Constants.APP_STATUS_PASSED).count());
            table.put("interviewCount", intCount);
            table.put("hireCount", hireCount);
            table.put("conversionRate", appCount > 0 ? String.format("%.1f%%", (double) hireCount / appCount * 100) : "0.0%");
            table.put("avgCycle", String.format("%.1f", avgCycle));
            tableData.add(table);
        }

        result.put("comparisonChart", comparisonChart);
        result.put("cycleChart", cycleChart);
        result.put("tableData", tableData);
        return result;
    }

    /**
     * ReportServiceImpl.buildChannelAnalysis() - 按渠道维度分析
     */
    private Map<String, Object> buildChannelAnalysis(List<Application> applications, List<Long> appIds, List<Offer> offers) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 使用 tags 字段作为渠道代理
        Map<String, List<Application>> appsByChannel = applications.stream()
                .collect(Collectors.groupingBy(a -> {
                    String tag = a.getTags();
                    if (tag != null && !tag.isEmpty()) return tag;
                    return "其他渠道";
                }));

        List<Map<String, Object>> pieData = new ArrayList<>();
        List<Map<String, Object>> tableData = new ArrayList<>();

        for (Map.Entry<String, List<Application>> entry : appsByChannel.entrySet()) {
            String channel = entry.getKey();
            List<Application> chApps = entry.getValue();
            long appCount = chApps.size();
            List<Long> chAppIds = chApps.stream().map(Application::getId).collect(Collectors.toList());

            long intCount = 0;
            long hireCount = 0;
            if (!chAppIds.isEmpty()) {
                LambdaQueryWrapper<Interview> iw = new LambdaQueryWrapper<Interview>().in(Interview::getApplicationId, chAppIds);
                intCount = interviewMapper.selectCount(iw);
                hireCount = offers.stream().filter(o -> chAppIds.contains(o.getApplicationId())
                        && o.getStatus() != null && (o.getStatus() == Constants.OFFER_STATUS_ACCEPTED || o.getStatus() == Constants.OFFER_STATUS_ONBOARDED))
                        .count();
            }

            Map<String, Object> pie = new LinkedHashMap<>();
            pie.put("name", channel);
            pie.put("value", appCount);
            pieData.add(pie);

            Map<String, Object> table = new LinkedHashMap<>();
            table.put("channel", channel);
            table.put("applicationCount", appCount);
            table.put("screeningPassed", chApps.stream().filter(a -> a.getStatus() != null && a.getStatus() >= Constants.APP_STATUS_PASSED).count());
            table.put("interviewCount", intCount);
            table.put("hireCount", hireCount);
            table.put("conversionRate", appCount > 0 ? String.format("%.1f%%", (double) hireCount / appCount * 100) : "0.0%");
            tableData.add(table);
        }

        result.put("pieData", pieData);
        result.put("tableData", tableData);
        return result;
    }

    // ==================== Excel 表头构建 ====================

    private List<List<String>> buildProgressHead() {
        return Arrays.asList(
                Arrays.asList("岗位ID"), Arrays.asList("岗位名称"), Arrays.asList("部门"),
                Arrays.asList("招聘人数"), Arrays.asList("简历投递量"), Arrays.asList("面试人数"),
                Arrays.asList("录用人数"), Arrays.asList("入职人数"), Arrays.asList("转化率"));
    }

    private List<List<Object>> convertProgressToRows(List<Map<String, Object>> progressList) {
        List<List<Object>> rows = new ArrayList<>();
        for (Map<String, Object> item : progressList) {
            rows.add(Arrays.asList(item.get("jobId"), item.get("jobTitle"), item.get("department"),
                    item.get("headcount"), item.get("applicationCount"), item.get("interviewCount"),
                    item.get("offerCount"), item.get("onboardCount"), item.get("conversionRate")));
        }
        return rows;
    }

    private List<List<String>> buildEffectHead() {
        return Arrays.asList(Arrays.asList("统计指标"), Arrays.asList("数值"), Arrays.asList("占比/转化率"));
    }

    private List<List<Object>> convertEffectToRows(Map<String, Object> effectData) {
        List<List<Object>> rows = new ArrayList<>();
        rows.add(Arrays.asList("总应聘数", effectData.get("totalApplications"), "-"));
        rows.add(Arrays.asList("通过筛选", effectData.get("passedScreening"), effectData.get("screeningPassRate")));
        rows.add(Arrays.asList("面试中", effectData.get("interviewing"), "-"));
        rows.add(Arrays.asList("已录用", effectData.get("hired"), "-"));
        rows.add(Arrays.asList("已淘汰", effectData.get("rejected"), "-"));
        rows.add(Arrays.asList("总面试数", effectData.get("totalInterviews"), "-"));
        rows.add(Arrays.asList("面试通过", effectData.get("interviewPassed"), effectData.get("interviewPassRate")));
        rows.add(Arrays.asList("发放Offer", effectData.get("totalOffers"), "-"));
        rows.add(Arrays.asList("接受Offer", effectData.get("offersAccepted"), effectData.get("offerAcceptRate")));
        rows.add(Arrays.asList("拒绝Offer", effectData.get("offersRejected"), "-"));
        rows.add(Arrays.asList("成功入职", effectData.get("onboarded"), effectData.get("overallConversionRate")));
        return rows;
    }

    private List<List<String>> buildJobAnalysisHead() {
        return Arrays.asList(Arrays.asList("岗位ID"), Arrays.asList("岗位名称"), Arrays.asList("部门"),
                Arrays.asList("简历投递量"), Arrays.asList("入职人数"), Arrays.asList("转化率"));
    }

    private List<List<Object>> convertJobAnalysisToRows(List<Map<String, Object>> jobAnalysis) {
        List<List<Object>> rows = new ArrayList<>();
        if (jobAnalysis == null) return rows;
        for (Map<String, Object> item : jobAnalysis) {
            rows.add(Arrays.asList(item.get("jobId"), item.get("jobTitle"), item.get("department"),
                    item.get("applicationCount"), item.get("onboardCount"), item.get("conversionRate")));
        }
        return rows;
    }

    private List<List<String>> buildTimeAnalysisHead() {
        return Arrays.asList(Arrays.asList("月份"), Arrays.asList("投递量"), Arrays.asList("筛选通过"),
                Arrays.asList("面试数"), Arrays.asList("录用数"), Arrays.asList("转化率"));
    }

    private List<List<Object>> convertTimeAnalysisToRows(List<Map<String, Object>> tableData) {
        List<List<Object>> rows = new ArrayList<>();
        if (tableData == null) return rows;
        for (Map<String, Object> item : tableData) {
            rows.add(Arrays.asList(item.get("period"), item.get("applicationCount"),
                    item.get("screeningPassed"), item.get("interviewCount"),
                    item.get("hireCount"), item.get("conversionRate")));
        }
        return rows;
    }

    private List<List<String>> buildJobDimAnalysisHead() {
        return Arrays.asList(Arrays.asList("岗位名称"), Arrays.asList("部门"), Arrays.asList("投递量"),
                Arrays.asList("筛选通过"), Arrays.asList("面试数"), Arrays.asList("录用数"),
                Arrays.asList("转化率"), Arrays.asList("平均周期(天)"));
    }

    private List<List<Object>> convertJobDimAnalysisToRows(List<Map<String, Object>> tableData) {
        List<List<Object>> rows = new ArrayList<>();
        if (tableData == null) return rows;
        for (Map<String, Object> item : tableData) {
            rows.add(Arrays.asList(item.get("jobTitle"), item.get("department"),
                    item.get("applicationCount"), item.get("screeningPassed"),
                    item.get("interviewCount"), item.get("hireCount"),
                    item.get("conversionRate"), item.get("avgCycle")));
        }
        return rows;
    }
}
