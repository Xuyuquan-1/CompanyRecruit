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

    @Override
    public Map<String, Object> getRecruitmentProgress(LocalDate startDate, LocalDate endDate) {
        log.info("查询招聘进度，时间范围：{} 至 {}", startDate, endDate);
        
        Map<String, Object> result = new LinkedHashMap<>();

        // 查询所有岗位
        List<JobPost> jobs = jobPostMapper.selectList(null);
        log.info("查询到岗位数量：{}", jobs.size());

        List<Map<String, Object>> progressList = new ArrayList<>();
        
        for (JobPost job : jobs) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("jobId", job.getId());
            item.put("jobTitle", job.getTitle());
            item.put("department", job.getDepartment());
            item.put("headcount", job.getHeadcount());
            
            // 统计该岗位的应聘数量
            LambdaQueryWrapper<Application> appWrapper = new LambdaQueryWrapper<Application>()
                    .eq(Application::getJobId, job.getId());
            
            // 添加时间筛选
            if (startDate != null) {
                appWrapper.ge(Application::getApplyTime, startDate.atStartOfDay());
            }
            if (endDate != null) {
                appWrapper.le(Application::getApplyTime, endDate.plusDays(1).atStartOfDay());
            }
            
            List<Application> applications = applicationMapper.selectList(appWrapper);
            long applicationCount = applications.size();
            
            log.debug("岗位[{}] 投递数：{}", job.getTitle(), applicationCount);
            
            // 统计面试人数
            List<Long> appIds = applications.stream().map(Application::getId).collect(Collectors.toList());
            long interviewCount = 0;
            if (!appIds.isEmpty()) {
                interviewCount = interviewMapper.selectCount(
                        new LambdaQueryWrapper<Interview>().in(Interview::getApplicationId, appIds)
                );
            }
            
            // 统计录用人数
            long offerCount = 0;
            if (!appIds.isEmpty()) {
                offerCount = offerMapper.selectCount(
                        new LambdaQueryWrapper<Offer>()
                                .in(Offer::getApplicationId, appIds)
                                .eq(Offer::getStatus, Constants.OFFER_STATUS_ACCEPTED)
                );
            }
            
            // 统计入职人数
            long onboardCount = 0;
            if (!appIds.isEmpty()) {
                onboardCount = offerMapper.selectCount(
                        new LambdaQueryWrapper<Offer>()
                                .in(Offer::getApplicationId, appIds)
                                .eq(Offer::getStatus, Constants.OFFER_STATUS_ONBOARDED)
                );
            }
            
            // 计算转化率
            double conversionRate = applicationCount > 0 ? (double) onboardCount / applicationCount * 100 : 0;
            
            item.put("applicationCount", applicationCount);
            item.put("interviewCount", interviewCount);
            item.put("offerCount", offerCount);
            item.put("onboardCount", onboardCount);
            item.put("conversionRate", String.format("%.2f%%", conversionRate));
            
            progressList.add(item);
        }
        
        // 汇总统计
        long totalApplications = progressList.stream().mapToLong(m -> (long) m.get("applicationCount")).sum();
        long totalInterviews = progressList.stream().mapToLong(m -> (long) m.get("interviewCount")).sum();
        long totalOffers = progressList.stream().mapToLong(m -> (long) m.get("offerCount")).sum();
        long totalOnboard = progressList.stream().mapToLong(m -> (long) m.get("onboardCount")).sum();
        
        log.info("汇总统计 - 总投递：{}, 总面试：{}, 总录用：{}, 总入职：{}", 
                totalApplications, totalInterviews, totalOffers, totalOnboard);
        
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
        log.info("查询招聘效果，时间范围：{} 至 {}, 岗位ID：{}", startDate, endDate, jobId);
        
        Map<String, Object> result = new LinkedHashMap<>();

        // 构建查询条件
        LambdaQueryWrapper<Application> appWrapper = new LambdaQueryWrapper<>();
        if (jobId != null) {
            appWrapper.eq(Application::getJobId, jobId);
        }
        if (startDate != null) {
            appWrapper.ge(Application::getApplyTime, startDate.atStartOfDay());
        }
        if (endDate != null) {
            appWrapper.le(Application::getApplyTime, endDate.plusDays(1).atStartOfDay());
        }
        
        List<Application> applications = applicationMapper.selectList(appWrapper);
        log.info("查询到应聘记录数：{}", applications.size());
        
        // 基础统计
        long totalApplications = applications.size();
        long passedScreening = applications.stream()
                .filter(a -> a.getStatus() != null && a.getStatus() >= Constants.APP_STATUS_PASSED)
                .count();
        long interviewing = applications.stream()
                .filter(a -> a.getStatus() != null && a.getStatus() == Constants.APP_STATUS_INTERVIEWING)
                .count();
        long hired = applications.stream()
                .filter(a -> a.getStatus() != null && a.getStatus() == Constants.APP_STATUS_OFFER_PENDING)
                .count();
        long rejected = applications.stream()
                .filter(a -> a.getStatus() != null && a.getStatus() == Constants.APP_STATUS_REJECTED)
                .count();
        
        // 面试统计
        List<Long> appIds = applications.stream().map(Application::getId).collect(Collectors.toList());
        long totalInterviews = 0;
        long interviewPassed = 0;
        if (!appIds.isEmpty()) {
            List<Interview> interviews = interviewMapper.selectList(
                    new LambdaQueryWrapper<Interview>().in(Interview::getApplicationId, appIds)
            );
            totalInterviews = interviews.size();
            interviewPassed = interviews.stream()
                    .filter(i -> i.getResult() != null && i.getResult() == Constants.INTERVIEW_RESULT_PASS)
                    .count();
        }
        
        // 录用统计
        long totalOffers = 0;
        long offersAccepted = 0;
        long offersRejected = 0;
        long onboarded = 0;
        if (!appIds.isEmpty()) {
            List<Offer> offers = offerMapper.selectList(
                    new LambdaQueryWrapper<Offer>().in(Offer::getApplicationId, appIds)
            );
            totalOffers = offers.size();
            offersAccepted = offers.stream()
                    .filter(o -> o.getStatus() != null && o.getStatus() == Constants.OFFER_STATUS_ACCEPTED)
                    .count();
            offersRejected = offers.stream()
                    .filter(o -> o.getStatus() != null && o.getStatus() == Constants.OFFER_STATUS_REJECTED)
                    .count();
            onboarded = offers.stream()
                    .filter(o -> o.getStatus() != null && o.getStatus() == Constants.OFFER_STATUS_ONBOARDED)
                    .count();
        }
        
        // 计算各阶段转化率
        double screeningPassRate = totalApplications > 0 ? (double) passedScreening / totalApplications * 100 : 0;
        double interviewPassRate = totalInterviews > 0 ? (double) interviewPassed / totalInterviews * 100 : 0;
        double offerAcceptRate = totalOffers > 0 ? (double) offersAccepted / totalOffers * 100 : 0;
        double overallConversionRate = totalApplications > 0 ? (double) onboarded / totalApplications * 100 : 0;
        
        // 按岗位统计（如果没指定岗位）
        List<Map<String, Object>> jobAnalysis = new ArrayList<>();
        if (jobId == null) {
            Map<Long, List<Application>> appsByJob = applications.stream()
                    .collect(Collectors.groupingBy(Application::getJobId));
            
            for (Map.Entry<Long, List<Application>> entry : appsByJob.entrySet()) {
                JobPost job = jobPostMapper.selectById(entry.getKey());
                if (job == null) continue;
                
                List<Application> jobApps = entry.getValue();
                long jobAppCount = jobApps.size();
                
                // 该岗位录用数
                List<Long> jobAppIds = jobApps.stream().map(Application::getId).collect(Collectors.toList());
                long jobOnboardCount = 0;
                if (!jobAppIds.isEmpty()) {
                    jobOnboardCount = offerMapper.selectCount(
                            new LambdaQueryWrapper<Offer>()
                                    .in(Offer::getApplicationId, jobAppIds)
                                    .eq(Offer::getStatus, Constants.OFFER_STATUS_ONBOARDED)
                    );
                }
                
                Map<String, Object> jobItem = new LinkedHashMap<>();
                jobItem.put("jobId", job.getId());
                jobItem.put("jobTitle", job.getTitle());
                jobItem.put("department", job.getDepartment());
                jobItem.put("applicationCount", jobAppCount);
                jobItem.put("onboardCount", jobOnboardCount);
                jobItem.put("conversionRate", jobAppCount > 0 
                        ? String.format("%.2f%%", (double) jobOnboardCount / jobAppCount * 100) 
                        : "0.00%");
                
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

    @Override
    public void exportExcel(LocalDate startDate, LocalDate endDate, HttpServletResponse response) throws IOException {
        log.info("导出招聘报表，时间范围：{} 至 {}", startDate, endDate);
        
        // 获取数据
        Map<String, Object> progressData = getRecruitmentProgress(startDate, endDate);
        Map<String, Object> effectData = getRecruitmentEffect(startDate, endDate, null);
        
        // 设置响应头
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "招聘统计报表_" + date + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName);
        
        try {
            // 使用EasyExcel写入，包含多个Sheet
            com.alibaba.excel.ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build();
            
            // Sheet1: 招聘进度统计
            WriteSheet progressSheet = EasyExcel.writerSheet(0, "招聘进度统计")
                    .head(buildProgressHead())
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .build();
            excelWriter.write(convertProgressToRows((List<Map<String, Object>>) progressData.get("progressList")), progressSheet);
            
            // Sheet2: 招聘效果分析
            WriteSheet effectSheet = EasyExcel.writerSheet(1, "招聘效果分析")
                    .head(buildEffectHead())
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .build();
            excelWriter.write(convertEffectToRows(effectData), effectSheet);
            
            // Sheet3: 各岗位分析
            WriteSheet jobSheet = EasyExcel.writerSheet(2, "各岗位分析")
                    .head(buildJobAnalysisHead())
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .build();
            excelWriter.write(convertJobAnalysisToRows((List<Map<String, Object>>) effectData.get("jobAnalysis")), jobSheet);
            
            excelWriter.finish();
            log.info("报表导出成功：{}", fileName);
            
        } catch (Exception e) {
            log.error("报表导出失败", e);
            throw new IOException("报表导出失败：" + e.getMessage());
        }
    }
    
    // 构建招聘进度表头
    private List<List<String>> buildProgressHead() {
        return Arrays.asList(
                Arrays.asList("岗位ID"),
                Arrays.asList("岗位名称"),
                Arrays.asList("部门"),
                Arrays.asList("招聘人数"),
                Arrays.asList("简历投递量"),
                Arrays.asList("面试人数"),
                Arrays.asList("录用人数"),
                Arrays.asList("入职人数"),
                Arrays.asList("转化率")
        );
    }
    
    // 转换招聘进度数据为行
    private List<List<Object>> convertProgressToRows(List<Map<String, Object>> progressList) {
        List<List<Object>> rows = new ArrayList<>();
        for (Map<String, Object> item : progressList) {
            rows.add(Arrays.asList(
                    item.get("jobId"),
                    item.get("jobTitle"),
                    item.get("department"),
                    item.get("headcount"),
                    item.get("applicationCount"),
                    item.get("interviewCount"),
                    item.get("offerCount"),
                    item.get("onboardCount"),
                    item.get("conversionRate")
            ));
        }
        return rows;
    }
    
    // 构建招聘效果表头
    private List<List<String>> buildEffectHead() {
        return Arrays.asList(
                Arrays.asList("统计指标"),
                Arrays.asList("数值"),
                Arrays.asList("占比/转化率")
        );
    }
    
    // 转换招聘效果数据为行
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
    
    // 构建各岗位分析表头
    private List<List<String>> buildJobAnalysisHead() {
        return Arrays.asList(
                Arrays.asList("岗位ID"),
                Arrays.asList("岗位名称"),
                Arrays.asList("部门"),
                Arrays.asList("简历投递量"),
                Arrays.asList("入职人数"),
                Arrays.asList("转化率")
        );
    }
    
    // 转换各岗位分析数据为行
    private List<List<Object>> convertJobAnalysisToRows(List<Map<String, Object>> jobAnalysis) {
        List<List<Object>> rows = new ArrayList<>();
        for (Map<String, Object> item : jobAnalysis) {
            rows.add(Arrays.asList(
                    item.get("jobId"),
                    item.get("jobTitle"),
                    item.get("department"),
                    item.get("applicationCount"),
                    item.get("onboardCount"),
                    item.get("conversionRate")
            ));
        }
        return rows;
    }
}
