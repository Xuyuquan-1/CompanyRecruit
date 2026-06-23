package com.hrrecruit.module.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrrecruit.common.Constants;
import com.hrrecruit.common.exception.BusinessException;
import com.hrrecruit.entity.SysNotification;
import com.hrrecruit.mapper.SysNotificationMapper;
import com.hrrecruit.module.notification.service.NotificationService;
import com.hrrecruit.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 消息通知服务实现
 */
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
}