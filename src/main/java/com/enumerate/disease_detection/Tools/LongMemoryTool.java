package com.enumerate.disease_detection.Tools;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enumerate.disease_detection.ChatModel.MainModel;
import com.enumerate.disease_detection.Local.UserContextHolder;
import com.enumerate.disease_detection.Mapper.LongMemoryMapper;
import com.enumerate.disease_detection.POJO.PO.LongMemory;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LongMemoryTool {

    @Autowired
    private MainModel mainModel;

    @Autowired
    private LongMemoryMapper longMemoryMapper;



    @Tool("得到用户长期记忆")
    public LongMemory getLongMemory() {
        Long userId = UserContextHolder.getUserId();
        log.info("工具调用: 得到用户长期记忆，参数: userId={}", userId);
        LongMemory result = longMemoryMapper.selectOne(new QueryWrapper<LongMemory>().eq("user_id", userId));
        log.info("工具结果: 得到用户长期记忆，结果: {}", result);
        return result;
    }

    @Tool("更新用户长期记忆")
    public void updateLongMemory(LongMemory longMemoryNow) {
        Long userId = UserContextHolder.getUserId();
        log.info("工具调用: 更新用户长期记忆，参数: userId={}, longMemoryNow={}", userId, longMemoryNow);
        LongMemory longMemoryPast = longMemoryMapper.selectOne(new QueryWrapper<LongMemory>().eq("user_id", userId));
        BeanUtils.copyProperties(longMemoryNow, longMemoryPast);
        longMemoryMapper.update(longMemoryPast,new QueryWrapper<LongMemory>().eq("user_id", userId));
        log.info("工具结果: 更新用户长期记忆，完成");
    }

    // 使用Slf4j日志记录
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LongMemoryTool.class);
}