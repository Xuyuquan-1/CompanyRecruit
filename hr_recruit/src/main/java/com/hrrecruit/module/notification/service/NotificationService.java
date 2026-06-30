package com.hrrecruit.module.notification.service;

import com.hrrecruit.common.PageResult;
import com.hrrecruit.entity.SysNotification;

import java.util.List;

/**
 * 娑堟伅閫氱煡鏈嶅姟鎺ュ彛
 */
public interface NotificationService {

    /**
     * 鑾峰彇褰撳墠鐢ㄦ埛鐨勯€氱煡鍒楄〃
     */
    List<SysNotification> getMyNotifications();

    /**
     * 鏍囪閫氱煡涓哄凡璇
     */
    void markAsRead(Long id);

    /**
     * 鍏ㄩ儴鏍囪涓哄凡璇
     */
    void markAllAsRead();

    /**
     * 鑾峰彇鏈鏁伴噺
     */
    long getUnreadCount();

    /**
     * 鍙戦€侀潰璇曢€氱煡
     * @param candidateId 搴旇仒鑰匢D
     * @param jobTitle 宀椾綅鍚嶇О
     * @param interviewTime 闈㈣瘯鏃堕棿
     * @param location 闈㈣瘯鍦扮偣
     * @param interviewerName 闈㈣瘯瀹樺鍚
     */
    void sendInterviewNotification(Long candidateId, String jobTitle, java.time.LocalDateTime interviewTime,
                                    String location, String interviewerName);

    /**
     * 鍙戦€佸綍鐢ㄩ€氱煡锛堝惈璧勬枡鎻愪氦璇存槑锛
     * @param candidateId 搴旇仒鑰匢D
     * @param jobTitle 宀椾綅鍚嶇О
     * @param salary 钖祫寰呴亣
     * @param benefits 绂忓埄璇存槑
     * @param expectedJoinDate 棰勮鍏ヨ亴鏃ユ湡
     */
    void sendOfferNotification(Long candidateId, String jobTitle, String salary,
                               String benefits, java.time.LocalDate expectedJoinDate);

    /**
     * 鍙戦€丱ffer鎺ュ彈閫氱煡锛堢粰绠＄悊鍛橈級
     * @param adminId 绠＄悊鍛橈紙鎷涜仒鑰咃級ID
     * @param candidateName 搴旇仒鑰呭鍚
     * @param jobTitle 宀椾綅鍚嶇О
     */
    void sendOfferAcceptedNotification(Long adminId, String candidateName, String jobTitle);
}