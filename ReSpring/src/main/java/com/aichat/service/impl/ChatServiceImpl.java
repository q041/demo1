package com.aichat.service.impl;

import com.aichat.entity.ChatMessage;
import com.aichat.entity.ChatSession;
import com.aichat.mapper.ChatMessageMapper;
import com.aichat.mapper.ChatSessionMapper;
import com.aichat.service.ChatService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ChatServiceImpl implements ChatService {

    private static final String SESSION_KEY_PREFIX = "chat:session:";
    private static final String CONTEXT_KEY = "context";
    private static final int CONTEXT_MAX_LENGTH = 10;
    private static final long SESSION_EXPIRE_TIME = 2; // 会话过期时间（小时）

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public Long createSession(Long userId) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setCreateTime(new Date());
        session.setIsDeleted(0);
        
        chatSessionMapper.insert(session);
        
        // 初始化会话上下文
        String contextKey = getContextKey(session.getId());
        redisTemplate.opsForList().trim(contextKey, 0, -1);
        
        // 设置会话过期时间
        redisTemplate.expire(contextKey, SESSION_EXPIRE_TIME, TimeUnit.HOURS);
        
        return session.getId();
    }

    @Override
    @Transactional
    public Boolean deleteSession(Long sessionId, Long userId) {
        // 验证会话是否存在且属于当前用户
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            return false;
        }
        
        // 逻辑删除会话
        session.setIsDeleted(1);
        session.setUpdateTime(new Date());
        chatSessionMapper.updateById(session);
        
        // 删除会话上下文
        String contextKey = getContextKey(sessionId);
        redisTemplate.delete(contextKey);
        
        return true;
    }

    @Override
    @Transactional
    public ChatMessage sendMessage(Long sessionId, Long userId, String content) {
        // 验证会话是否存在且属于当前用户
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null || session.getIsDeleted() == 1 || !session.getUserId().equals(userId)) {
            throw new RuntimeException("会话不存在或已删除");
        }
        
        // 保存用户消息
        ChatMessage userMessage = new ChatMessage();
        userMessage.setSessionId(sessionId);
        userMessage.setUserId(userId);
        userMessage.setContent(content);
        userMessage.setSender("USER");
        userMessage.setSendTime(new Date());
        chatMessageMapper.insert(userMessage);
        
        // 更新会话时间
        session.setUpdateTime(new Date());
        chatSessionMapper.updateById(session);
        
        // 获取历史对话上下文
        String contextKey = getContextKey(sessionId);// 修改第100行为如下代码：
        List<Object> contextMessages = redisTemplate.opsForList().range(contextKey, 0, -1);
        // 调用AI接口获取回复
        String aiResponse = callAiService(contextMessages, content);
        
        // 保存AI回复
        ChatMessage aiMessage = new ChatMessage();
        aiMessage.setSessionId(sessionId);
        aiMessage.setUserId(userId);
        aiMessage.setContent(aiResponse);
        aiMessage.setSender("AI");
        aiMessage.setSendTime(new Date());
        chatMessageMapper.insert(aiMessage);
        
        // 更新会话上下文（限制长度）
        redisTemplate.opsForList().rightPush(contextKey, content);
        redisTemplate.opsForList().rightPush(contextKey, aiResponse);
        
        // 限制上下文长度
        if (redisTemplate.opsForList().size(contextKey) > CONTEXT_MAX_LENGTH * 2) {
            redisTemplate.opsForList().trim(contextKey, -CONTEXT_MAX_LENGTH * 2, -1);
        }
        
        // 重置会话过期时间
        redisTemplate.expire(contextKey, SESSION_EXPIRE_TIME, TimeUnit.HOURS);
        
        return aiMessage;
    }

    @Override
    public Page<ChatMessage> getSessionMessages(Long sessionId, Long userId, Integer pageNum, Integer pageSize) {
        // 验证会话是否存在且属于当前用户
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null || session.getIsDeleted() == 1 || !session.getUserId().equals(userId)) {
            throw new RuntimeException("会话不存在或已删除");
        }
        
        // 分页查询消息
        Page<ChatMessage> page = new Page<>(pageNum, pageSize);
        return chatMessageMapper.selectPage(page, null);
    }

    @Override
    public List<ChatSession> getUserSessions(Long userId) {
        return chatSessionMapper.selectList(null);
    }

    /**
     * 调用AI服务获取回复
     * @param contextMessages 上下文消息
     * @param userMessage 用户当前消息
     * @return AI回复
     */
    private String callAiService(List<Object> contextMessages, String userMessage) {
        // 实际项目中这里会调用具体的AI服务API
        // 此处简化处理，返回固定回复
        return "AI回复：您发送的消息是：" + userMessage;
    }

    /**
     * 获取会话上下文的Redis键
     * @param sessionId 会话ID
     * @return Redis键
     */
    private String getContextKey(Long sessionId) {
        return SESSION_KEY_PREFIX + sessionId + ":" + CONTEXT_KEY;
    }
}    