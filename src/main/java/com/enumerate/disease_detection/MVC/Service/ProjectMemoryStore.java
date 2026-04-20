package com.enumerate.disease_detection.MVC.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.enumerate.disease_detection.MVC.Mapper.BotSoulMapper;
import com.enumerate.disease_detection.MVC.Mapper.CompressedHistoryMapper;
import com.enumerate.disease_detection.MVC.Mapper.UserLongTermMemoryMapper;
import com.enumerate.disease_detection.MVC.POJO.PO.CompressedHistoryPO;
import com.enumerate.disease_detection.MVC.POJO.PO.UserLongTermMemoryPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class ProjectMemoryStore {

    @Autowired
    private UserLongTermMemoryMapper userMemoryMapper;
    @Autowired
    private BotSoulMapper botSoulMapper;
    @Autowired
    private CompressedHistoryMapper compressedHistoryMapper;

    // 1. 读取用户长期记忆
    public String getUserLongTermMemory(Long userId) {
        UserLongTermMemoryPO po = userMemoryMapper.selectOne(
                new QueryWrapper<UserLongTermMemoryPO>().eq("user_id", userId)
        );
        return po == null ? "无用户记忆" : po.getContent();
    }

    // 2. 更新用户长期记忆
    public void updateUserLongTermMemory(Long userId, String content) {
        QueryWrapper<UserLongTermMemoryPO> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        UserLongTermMemoryPO exist = userMemoryMapper.selectOne(query);

        if (exist != null) {
            exist.setContent(content);
            exist.setUpdatedAt(LocalDateTime.now());
            userMemoryMapper.updateById(exist);
            log.info("更新用户[{}]长期记忆", userId);
        } else {
            UserLongTermMemoryPO po = new UserLongTermMemoryPO();
            po.setUserId(userId);
            po.setContent(content);
            po.setDreamCursor(0L);
            po.setUpdatedAt(LocalDateTime.now());
            userMemoryMapper.insert(po);
            log.info("新增用户[{}]长期记忆", userId);
        }
    }

    // 3. 追加压缩历史（修复版：无cursor，id自动当游标）
    public void appendCompressedHistory(Long userId, String sessionId, String summary) {
        CompressedHistoryPO po = new CompressedHistoryPO();
        po.setUserId(userId);
        po.setSessionId(sessionId);
        po.setContent(summary);
        po.setCreatedAt(LocalDateTime.now());
        compressedHistoryMapper.insert(po);
        log.info("用户[{}] 会话[{}] 写入压缩历史", userId, sessionId);
    }

    // 4. 读取未处理历史（修复版：用id作为游标查询）
    public List<CompressedHistoryPO> getUnprocessedHistory(Long userId, long lastCursor) {
        QueryWrapper<CompressedHistoryPO> query = new QueryWrapper<>();
        query.eq("user_id", userId)
                .gt("id", lastCursor) // 用主键id替代cursor，完美有序
                .orderByAsc("id")
                .last("LIMIT 20");
        return compressedHistoryMapper.selectList(query);
    }

    // 5. 更新Dream游标
    public void updateDreamCursor(Long userId, long maxCursor) {
        UpdateWrapper<UserLongTermMemoryPO> wrapper = new UpdateWrapper<>();
        wrapper.eq("user_id", userId).set("dream_cursor", maxCursor);
        userMemoryMapper.update(null, wrapper);
    }

    // 6. 读取AI人设
    public String getBotSoul() {
        return botSoulMapper.selectOne(null).getContent();
    }
}