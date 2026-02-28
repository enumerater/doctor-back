package com.enumerate.disease_detection.Tools;

import com.enumerate.disease_detection.Annotations.ToolName;
import com.enumerate.disease_detection.Utils.MysqlEmbeddingStore;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RagTool {

    @Autowired
    private MysqlEmbeddingStore mysqlEmbeddingStore;

    @Resource(name = "embeddingModel")
    private OpenAiEmbeddingModel embeddingModel;

    @Tool("用户记忆检索工具：根据查询内容搜索该用户的历史记忆信息，返回与查询最相关的记忆条目。当需要了解用户的个人情况、种植习惯、历史问题等个性化信息时调用。")
    @ToolName("用户记忆检索")
    public String userMemorySearch(@P("查询内容，描述你想了解的用户信息") String query,
                                   @P("用户ID") String userId) {
        log.info("工具调用: 用户记忆检索，参数: query={}, userId={}", query, userId);

        Embedding queryEmbedding = embeddingModel.embed(query).content();
        List<String> memories = mysqlEmbeddingStore.searchTopNForUser(userId, queryEmbedding, 5, 0.5f);

        if (memories.isEmpty()) {
            String result = "暂无该用户的相关记忆信息。";
            log.info("工具结果: 用户记忆检索，结果: {}", result);
            return result;
        }

        StringBuilder sb = new StringBuilder("用户相关记忆：\n");
        for (int i = 0; i < memories.size(); i++) {
            sb.append(i + 1).append(". ").append(memories.get(i)).append("\n");
        }

        String result = sb.toString();
        log.info("工具结果: 用户记忆检索，命中{}条", memories.size());
        return result;
    }
}
