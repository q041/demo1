package com.aichat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aichat.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}    