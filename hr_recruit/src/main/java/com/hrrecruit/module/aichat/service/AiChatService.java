package com.hrrecruit.module.aichat.service;

import com.hrrecruit.entity.AiChatHistory;

import java.util.List;

/**
 * AI智能问答服务接口
 */
public interface AiChatService {

    /**
     * 发送问题，AI自动回复
     */
    AiChatHistory chat(Long userId, String question);

    /**
     * 查询指定用户的对话历史
     */
    List<AiChatHistory> getChatHistory(Long userId);

    /**
     * 清除对话历史
     */
    void clearHistory(Long userId);
}