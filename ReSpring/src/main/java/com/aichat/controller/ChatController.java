package com.aichat.controller;

import com.aichat.common.Result;
import com.aichat.entity.ChatMessage;
import com.aichat.entity.ChatSession;
import com.aichat.service.ChatService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * 创建会话
     */
    @PostMapping("/session")
    public Result<Long> createSession(@RequestHeader("userId") Long userId) {
        Long sessionId = chatService.createSession(userId);
        return Result.success(sessionId);
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/session/{sessionId}")
    public Result<Boolean> deleteSession(@RequestHeader("userId") Long userId,
                                         @PathVariable Long sessionId) {
        Boolean result = chatService.deleteSession(sessionId, userId);
        return Result.success(result);
    }

    /**
     * 发送消息
     */
    @PostMapping("/message")
    public Result<ChatMessage> sendMessage(@RequestHeader("userId") Long userId,
                                          @RequestParam Long sessionId,
                                          @RequestParam String content) {
        ChatMessage message = chatService.sendMessage(sessionId, userId, content);
        return Result.success(message);
    }

    /**
     * 获取会话消息列表
     */
    @GetMapping("/session/{sessionId}/messages")
    public Result<Page<ChatMessage>> getSessionMessages(@RequestHeader("userId") Long userId,
                                                       @PathVariable Long sessionId,
                                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<ChatMessage> messages = chatService.getSessionMessages(sessionId, userId, pageNum, pageSize);
        return Result.success(messages);
    }

    /**
     * 获取用户会话列表
     */
    @GetMapping("/sessions")
    public Result<List<ChatSession>> getUserSessions(@RequestHeader("userId") Long userId) {
        List<ChatSession> sessions = chatService.getUserSessions(userId);
        return Result.success(sessions);
    }
}    