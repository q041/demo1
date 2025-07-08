package com.aichat.service;

import com.aichat.entity.ChatMessage;
import com.aichat.entity.ChatSession;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public interface ChatService {

    /**
     * 创建会话
     * @param userId 用户ID
     * @return 会话ID
     */
    Long createSession(Long userId);

    /**
     * 删除会话
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean deleteSession(Long sessionId, Long userId);

    /**
     * 发送消息
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param content 消息内容
     * @return 完整消息对象
     */
    ChatMessage sendMessage(Long sessionId, Long userId, String content);

    /**
     * 获取会话消息列表
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 消息分页列表
     */
    Page<ChatMessage> getSessionMessages(Long sessionId, Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户会话列表
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ChatSession> getUserSessions(Long userId);
}    