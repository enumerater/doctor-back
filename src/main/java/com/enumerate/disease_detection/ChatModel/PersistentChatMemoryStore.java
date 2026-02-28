package com.enumerate.disease_detection.ChatModel;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enumerate.disease_detection.MVC.Mapper.MemoryMapper;
import com.enumerate.disease_detection.MVC.POJO.PO.MemoryPO;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class PersistentChatMemoryStore implements ChatMemoryStore {

    @Autowired
    private MemoryMapper memoryMapper;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        // 1. 严格校验参数
        if (memoryId == null || StringUtils.isEmpty(memoryId.toString().trim())) {
            log.warn("memoryId为空，无法获取聊天记忆");
            return Collections.emptyList();
        }

        log.info("查询聊天记忆，unionId: {}", memoryId);

        // 2. 查询记录
        QueryWrapper<MemoryPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("union_id", memoryId);
        MemoryPO memoryPO = memoryMapper.selectOne(queryWrapper);

        // 3. 反序列化
        if (memoryPO != null && StringUtils.hasText(memoryPO.getMessage())) {
            List<ChatMessage> messages = ChatMessageDeserializer.messagesFromJson(memoryPO.getMessage());
            log.info("查询到{}条聊天记忆，unionId: {}", messages.size(), memoryId);
            return messages;
        }

        log.info("未查询到聊天记忆，unionId: {}", memoryId);
        return Collections.emptyList();
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        // 1. 严格校验参数
        if (memoryId == null || StringUtils.isEmpty(memoryId.toString().trim()) || messages == null || messages.isEmpty()) {
            log.warn("参数无效，跳过更新聊天记忆：memoryId={}, messages={}", memoryId, messages);
            return;
        }
        String unionId = memoryId.toString().trim();
        String messagesJson = ChatMessageSerializer.messagesToJson(messages);
        log.info("更新聊天记忆，unionId: {}, 消息数量: {}", unionId, messages.size());

        // 关键优化：先查后更（替代先删后插），减少数据库操作，保留数据一致性
        QueryWrapper<MemoryPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("union_id", unionId);
        MemoryPO existingPO = memoryMapper.selectOne(queryWrapper);

        if (existingPO != null) {
            // 有旧记录则更新
            existingPO.setMessage(messagesJson);
            memoryMapper.updateById(existingPO);
            log.info("更新已有聊天记忆，unionId: {}", unionId);
        } else {
            // 无记录则插入
            MemoryPO memoryPO = new MemoryPO();
            memoryPO.setUnionId(unionId);
            memoryPO.setMessage(messagesJson);
            memoryMapper.insert(memoryPO);
            log.info("插入新的聊天记忆，unionId: {}", unionId);
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        if (memoryId == null || StringUtils.isEmpty(memoryId.toString().trim())) {
            log.warn("memoryId为空，跳过删除聊天记忆");
            return;
        }
        String unionId = memoryId.toString().trim();
        log.info("删除聊天记忆，unionId: {}", unionId);

        QueryWrapper<MemoryPO> deleteWrapper = new QueryWrapper<>();
        deleteWrapper.eq("union_id", unionId);
        int deleteCount = memoryMapper.delete(deleteWrapper);
        log.info("删除聊天记忆完成，unionId: {}, 删除条数: {}", unionId, deleteCount);
    }
}