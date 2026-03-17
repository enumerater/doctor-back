package com.enumerate.disease_detection.MVC.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enumerate.disease_detection.MVC.Mapper.VectorStoreMapper;
import com.enumerate.disease_detection.MVC.POJO.PO.VectorStorePO;
import com.enumerate.disease_detection.Utils.MysqlEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 记忆浓缩服务：定期清理旧的零碎记忆，并将其浓缩为长期记忆
 */
@Service
@Slf4j
public class MemoryCondensationService {

    @Autowired
    private VectorStoreMapper vectorStoreMapper;

    @Autowired
    private MemoryExtractionService memoryExtractionService;

    @Autowired
    private MysqlEmbeddingStore mysqlEmbeddingStore;

    /**
     * 执行记忆浓缩：处理一周前的原子记忆
     */
    @Transactional(rollbackFor = Exception.class)
    public void condenseOldMemories() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        log.info("开始执行记忆浓缩任务，处理 {} 之前的记忆", oneWeekAgo);

        // 1. 查询一周前且类型为原子记忆的记录
        List<VectorStorePO> oldMemories = vectorStoreMapper.selectList(
                new QueryWrapper<VectorStorePO>()
                        .eq("memory_type", "conversation_extract")
                        .le("created_at", oneWeekAgo)
        );

        if (oldMemories.isEmpty()) {
            log.info("没有需要浓缩的旧记忆");
            return;
        }

        // 2. 按用户分组处理
        Map<Long, List<VectorStorePO>> userMemoryGroups = oldMemories.stream()
                .collect(Collectors.groupingBy(VectorStorePO::getUserId));

        int totalCondensed = 0;
        int totalDeleted = 0;

        for (Map.Entry<Long, List<VectorStorePO>> entry : userMemoryGroups.entrySet()) {
            Long userId = entry.getKey();
            List<VectorStorePO> userOldMemories = entry.getValue();

            log.info("正在为用户 {} 浓缩 {} 条记忆", userId, userOldMemories.size());

            // 提取纯文本列表
            List<String> texts = userOldMemories.stream()
                    .map(VectorStorePO::getTextContent)
                    .collect(Collectors.toList());

            // 3. 调用LLM进行浓缩
            List<String> condensedTexts = memoryExtractionService.condenseMemories(texts);

            if (!condensedTexts.isEmpty()) {
                // 4. 保存浓缩后的新记忆
                for (String condensedText : condensedTexts) {
                    mysqlEmbeddingStore.saveUserMemory(userId, condensedText, "condensed_task", "condensed_summary");
                    totalCondensed++;
                }

                // 5. 删除旧记忆
                List<String> idsToDelete = userOldMemories.stream()
                        .map(VectorStorePO::getId)
                        .collect(Collectors.toList());
                vectorStoreMapper.deleteBatchIds(idsToDelete);
                totalDeleted += idsToDelete.size();
                
                log.info("用户 {} 浓缩完成: 新增 {} 条浓缩记忆, 删除 {} 条旧记忆", userId, condensedTexts.size(), idsToDelete.size());
            } else {
                log.warn("用户 {} 的记忆浓缩结果为空，跳过删除操作", userId);
            }
        }

        log.info("===== 记忆浓缩任务结束: 总计新增 {} 条浓缩记忆, 清理 {} 条旧记录 =====", totalCondensed, totalDeleted);
    }
}


//✦ 我已完成多级记忆浓缩功能的开发。
//
//
//变更汇总：
//        1. MemoryExtractionService: 增加了 condenseMemories 方法，利用 tongYiModel 将零碎的记忆点合并为精炼的陈述句。
//        2. MemoryCondensationService (新类):
//        * 每周自动筛选出超过 7 天的 conversation_extract 类型记忆。
//        * 按用户分组进行浓缩处理。
//        * 将浓缩后的结果存入 vector_store，类型标记为 condensed_summary。
//        * 物理删除已被浓缩的原始原子记忆，确保数据库不臃肿。
//        3. MemoryScheduledService: 注册了每周日凌晨 2 点执行的定时任务。
//
//
//建议：
//        - 手动触发测试：由于定时任务是每周执行，如果您想立即看到效果，可以临时将 @Scheduled(cron = "0 0 2 ? * SUN") 改为 @Scheduled(initialDelay = 5000, fixedDelay = Long.MAX_VALUE) 运行一次进行验证。
//        - 检索逻辑：目前的检索逻辑会检索用户的所有记忆。由于 condensed_summary 也是存放在 vector_store 中且带有向量，现有的 RagTool 不需要修改即可直接搜到这些浓缩后的知识。
//
//
//这样，您的系统不仅能“记得细”，还能通过每周的“复盘”保持记忆的精炼和高效。
