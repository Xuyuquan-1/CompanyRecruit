package com.hrrecruit.module.notification.service;

import com.hrrecruit.common.PageResult;
import com.hrrecruit.entity.SysNotification;

import java.util.List;

/**
 * 消息通知服务接口
 */
public interface NotificationService {

    /**
     * 获取当前用户的通知列表
     */
    List<SysNotification> getMyNotifications();

    /**
     * 标记通知为已读
     */
    void markAsRead(Long id);

    /**
     * 全部标记为已读
     */
    void markAllAsRead();

    /**
     * 获取未读数量
     */
    long getUnreadCount();

    /**
     * 发送面试通知
     * @param candidateId 应聘者ID
     * @param jobTitle 岗位名称
     * @param interviewTime 面试时间
     * @param location 面试地点
     * @param interviewerName 面试官姓名
     */
    void sendInterviewNotification(Long candidateId, String jobTitle, java.time.LocalDateTime interviewTime, 
                                    String location, String interviewerName);

    /**
     * 发送录用通知（含资料提交说明）
     * @param candidateId 应聘者ID
     * @param jobTitle 岗位名称
     * @param salary 薪资待遇
     * @param benefits 福利说明
     * @param expectedJoinDate 预计入职日期
     */
    void sendOfferNotification(Long candidateId, String jobTitle, String salary, 
                               String benefits, java.time.LocalDate expectedJoinDate);
}