package com.hrrecruit.module.notification.controller;

import com.hrrecruit.common.Result;
import com.hrrecruit.entity.SysNotification;
import com.hrrecruit.module.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息通知控制器
 */
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 获取当前用户的通知列表
     */
    @GetMapping("/list")
    public Result<List<SysNotification>> getMyNotifications() {
        return Result.success(notificationService.getMyNotifications());
    }

    /**
     * 标记已读
     */
    @PostMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.successMsg("已标记为已读");
    }

    /**
     * 全部标记为已读
     */
    @PostMapping("/read-all")
    public Result<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return Result.successMsg("已全部标记为已读");
    }

    /**
     * 获取未读数量
     */
    @GetMapping("/unread-count")
    public Result<Map<String, Long>> getUnreadCount() {
        Map<String, Long> result = new HashMap<>();
        result.put("count", notificationService.getUnreadCount());
        return Result.success(result);
    }
}