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
}