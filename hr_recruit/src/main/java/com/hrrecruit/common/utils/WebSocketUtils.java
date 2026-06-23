package com.hrrecruit.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * WebSocket工具类
 */
@Slf4j
public class WebSocketUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 发送消息
     */
    public static void sendMessage(WebSocketSession session, Object message) {
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));
        } catch (IOException e) {
            log.error("WebSocket发送消息失败", e);
        }
    }

    /**
     * 发送文本消息
     */
    public static void sendTextMessage(WebSocketSession session, String message) {
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            log.error("WebSocket发送文本消息失败", e);
        }
    }

    /**
     * 关闭连接
     */
    public static void closeSession(WebSocketSession session) {
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (IOException e) {
                log.error("关闭WebSocket连接失败", e);
            }
        }
    }
}
