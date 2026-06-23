package com.hrrecruit.module.aichat.controller;

import com.hrrecruit.common.Result;
import com.hrrecruit.entity.AiChatHistory;
import com.hrrecruit.module.aichat.service.AiChatService;
import com.hrrecruit.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 智能问答控制器
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    /**
     * 发送问题，获取AI回复
     */
    @PostMapping("/chat")
    public Result<AiChatHistory> chat(@RequestBody Map<String, String> params) {
        LoginUser loginUser = getCurrentUser();
        String question = params.get("question");
        AiChatHistory history = aiChatService.chat(loginUser.getUserId(), question);
        return Result.success(history);
    }

    /**
     * 获取当前用户的对话历史
     */
    @GetMapping("/history")
    public Result<List<AiChatHistory>> getChatHistory() {
        LoginUser loginUser = getCurrentUser();
        return Result.success(aiChatService.getChatHistory(loginUser.getUserId()));
    }

    /**
     * 清除对话历史
     */
    @DeleteMapping("/history")
    public Result<Void> clearHistory() {
        LoginUser loginUser = getCurrentUser();
        aiChatService.clearHistory(loginUser.getUserId());
        return Result.successMsg("对话历史已清除");
    }

    private LoginUser getCurrentUser() {
        return (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}