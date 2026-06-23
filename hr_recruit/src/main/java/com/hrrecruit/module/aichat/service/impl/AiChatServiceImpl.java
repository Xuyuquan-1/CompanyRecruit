package com.hrrecruit.module.aichat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrrecruit.entity.AiChatHistory;
import com.hrrecruit.mapper.AiChatHistoryMapper;
import com.hrrecruit.module.aichat.service.AiChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI智能问答服务实现（Spring AI 官方集成）
 * 支持两种LLM，默认使用 OpenAI，切换 DashScope 只改 pom 和 application.yml
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final AiChatHistoryMapper aiChatHistoryMapper;
    private final ChatClient chatClient;

    @Override
    public AiChatHistory chat(Long userId, String question) {
        String answer = generateAiAnswer(userId, question);

        AiChatHistory history = new AiChatHistory();
        history.setUserId(userId);
        history.setQuestion(question);
        history.setAnswer(answer);
        history.setCreateTime(LocalDateTime.now());
        aiChatHistoryMapper.insert(history);

        log.info("AI问答 - 用户: {}, 问题: {}, 调用成功", userId, question);
        return history;
    }

    @Override
    public List<AiChatHistory> getChatHistory(Long userId) {
        return aiChatHistoryMapper.selectList(
                new LambdaQueryWrapper<AiChatHistory>()
                        .eq(AiChatHistory::getUserId, userId)
                        .orderByAsc(AiChatHistory::getCreateTime)
        );
    }

    @Override
    public void clearHistory(Long userId) {
        aiChatHistoryMapper.delete(
                new LambdaQueryWrapper<AiChatHistory>().eq(AiChatHistory::getUserId, userId)
        );
    }

    /**
     * 调用 Spring AI ChatClient 生成回答
     * 系统提示词引导AI成为HR招聘系统的智能助手
     */
    private String generateAiAnswer(Long userId, String question) {
        String systemPrompt =
                "你是「人力资源招聘管理系统」的智能助手，正在与用户进行持续对话。\n" +
                "你需要回答用户关于系统操作流程的问题，包括：\n" +
                "- 岗位发布和管理操作\n" +
                "- 简历上传和解析\n" +
                "- 应聘筛选流程\n" +
                "- 面试安排流程\n" +
                "- 录用和入职手续\n" +
                "- 报表统计和Excel导出\n" +
                "回答要求：\n" +
                "1. 用简洁清晰的步骤化语言回答，适合非技术用户理解\n" +
                "2. 保持友好语气，不要说无关内容\n" +
                "3. 直接回答问题，不要在每次回复中都做自我介绍或重复开场白\n" +
                "4. 如果用户继续提问，基于上下文自然回应，不要每次都重新介绍自己\n" +
                "5. 只在第一次对话时可以简单问候，后续对话直接回答问题";

        ChatClient.CallResponseSpec response = chatClient.prompt()
                .system(systemPrompt)
                .user(question)
                .advisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                .call();

        return response.content();
    }
}