package com.hrrecruit.module.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrrecruit.common.Constants;
import com.hrrecruit.common.exception.BusinessException;
import com.hrrecruit.entity.SysNotification;
import com.hrrecruit.mapper.SysNotificationMapper;
import com.hrrecruit.module.notification.service.NotificationService;
import com.hrrecruit.security.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 消息通知服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final SysNotificationMapper sysNotificationMapper;

    @Override
    public List<SysNotification> getMyNotifications() {
        LoginUser loginUser = getCurrentUser();
        return sysNotificationMapper.selectList(
                new LambdaQueryWrapper<SysNotification>()
                        .eq(SysNotification::getUserId, loginUser.getUserId())
                        .orderByDesc(SysNotification::getSendTime)
        );
    }

    @Override
    public void markAsRead(Long id) {
        SysNotification notification = sysNotificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        notification.setIsRead(Constants.NOTIFY_READ);
        sysNotificationMapper.updateById(notification);
    }

    @Override
    public void markAllAsRead() {
        LoginUser loginUser = getCurrentUser();
        List<SysNotification> list = sysNotificationMapper.selectList(
                new LambdaQueryWrapper<SysNotification>()
                        .eq(SysNotification::getUserId, loginUser.getUserId())
                        .eq(SysNotification::getIsRead, Constants.NOTIFY_UNREAD)
        );
        for (SysNotification n : list) {
            n.setIsRead(Constants.NOTIFY_READ);
            sysNotificationMapper.updateById(n);
        }
    }

    @Override
    public long getUnreadCount() {
        LoginUser loginUser = getCurrentUser();
        return sysNotificationMapper.selectCount(
                new LambdaQueryWrapper<SysNotification>()
                        .eq(SysNotification::getUserId, loginUser.getUserId())
                        .eq(SysNotification::getIsRead, Constants.NOTIFY_UNREAD)
        );
    }

    private LoginUser getCurrentUser() {
        return (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public void sendInterviewNotification(Long candidateId, String jobTitle, LocalDateTime interviewTime,
                                          String location, String interviewerName) {
        SysNotification notification = new SysNotification();
        notification.setUserId(candidateId);
        notification.setType(Constants.NOTIFY_TYPE_INTERVIEW);
        notification.setTitle("面试通知");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String content = String.format(
            "您应聘的【%s】岗位已安排面试！\n" +
            "面试时间：%s\n" +
            "面试地点：%s\n" +
            "面试官：%s\n\n" +
            "请准时参加面试，祝您顺利！",
            jobTitle,
            interviewTime.format(formatter),
            location != null ? location : "待定",
            interviewerName != null ? interviewerName : "待定"
        );
        
        notification.setContent(content);
        notification.setIsRead(Constants.NOTIFY_UNREAD);
        notification.setSendTime(LocalDateTime.now());
        
        sysNotificationMapper.insert(notification);
        log.info("面试通知已发送给候选人ID: {}, 岗位: {}", candidateId, jobTitle);
    }

    @Override
    public void sendOfferNotification(Long candidateId, String jobTitle, String salary,
                                     String benefits, java.time.LocalDate expectedJoinDate) {
        SysNotification notification = new SysNotification();
        notification.setUserId(candidateId);
        notification.setType(Constants.NOTIFY_TYPE_OFFER);
        notification.setTitle("录用通知");
        
        String content = String.format(
            "恭喜您！您已被录用为【%s】岗位的正式员工！\n\n" +
            "薪资待遇：%s\n" +
            "福利说明：%s\n" +
            "预计入职日期：%s\n\n" +
            "【重要】请在确认Offer后提交以下入职资料：\n" +
            "1. 身份证正反面照片\n" +
            "2. 劳动合同（将发送给您签署）\n" +
            "3. 体检报告（近三个月内）\n\n" +
            "资料齐全后，我们将为您办理入职手续。",
            jobTitle,
            salary != null ? salary : "面议",
            benefits != null ? benefits : "按公司规定",
            expectedJoinDate != null ? expectedJoinDate.toString() : "待定"
        );
        
        notification.setContent(content);
        notification.setIsRead(Constants.NOTIFY_UNREAD);
        notification.setSendTime(LocalDateTime.now());
        
        sysNotificationMapper.insert(notification);
        log.info("录用通知已发送给候选人ID: {}, 岗位: {}", candidateId, jobTitle);
    }

    @Override
    public void sendOfferAcceptedNotification(Long adminId, String candidateName, String jobTitle) {
        SysNotification notification = new SysNotification();
        notification.setUserId(adminId);
        notification.setType(Constants.NOTIFY_TYPE_SYSTEM);
        notification.setTitle("Offer被接受");
        notification.setContent(String.format(
            "应聘者【%s】已接受了【%s】岗位的录用通知，将进入入职资料提交阶段。",
            candidateName, jobTitle
        ));
        notification.setIsRead(Constants.NOTIFY_UNREAD);
        notification.setSendTime(LocalDateTime.now());
        sysNotificationMapper.insert(notification);
        log.info("Offer接受通知已发送给管理员 adminId={}, candidate={}, job={}", adminId, candidateName, jobTitle);
    }

}