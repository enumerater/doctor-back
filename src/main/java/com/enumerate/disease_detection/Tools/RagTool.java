package com.enumerate.disease_detection.Tools;

import com.enumerate.disease_detection.Annotations.ToolName;
import com.enumerate.disease_detection.ChatModel.MainModel;
import com.enumerate.disease_detection.POJO.PO.VectorStorePO;
import com.enumerate.disease_detection.Utils.MysqlEmbeddingStore;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.service.TokenStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



@Component
public class RagTool {

    @Autowired
    private MainModel mainModel;

    @Autowired
    private MysqlEmbeddingStore mysqlEmbeddingStore;

    @Tool("rag工具")
    @ToolName("rag检索")
    public String ragTool(String prompt) {
        log.info("工具调用: rag工具，参数: prompt={}", prompt);
        
        EmbeddingModel embeddingModel = mainModel.embeddingModel();

        Embedding queryEmbedding = embeddingModel.embed(prompt).content();

        // 5. 相似度搜索（从MySQL中找最相似的向量）
        VectorStorePO mostSimilar = mysqlEmbeddingStore.searchMostSimilar(queryEmbedding);
        String result = mostSimilar.getTextContent() != null ? mostSimilar.getTextContent() : "无知识";
        
        log.info("工具结果: rag工具，结果: {}", result);
        return result;
    }
    
    // 使用Slf4j日志记录
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RagTool.class);
}