package com.enumerate.disease_detection.Service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.enumerate.disease_detection.ChatModel.MainModel;
import com.enumerate.disease_detection.POJO.PO.VectorStorePO;
import com.enumerate.disease_detection.Utils.MysqlEmbeddingStore;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class TestService {

    @Autowired
    private MainModel mainModel;

    @Autowired
    private MysqlEmbeddingStore mysqlEmbeddingStore;

    public String test() {
        log.info("=== test service start ===");
        // 1. 加载文档
        List<Document> documents = FileSystemDocumentLoader.loadDocuments("./src/main/resources/static");

        // 2. 获取嵌入模型
        EmbeddingModel embeddingModel = mainModel.embeddingModel();

        // 3. 加载/生成向量（优先查库，无则生成并存库，避免浪费token）
        mysqlEmbeddingStore.loadOrSaveEmbedding(embeddingModel, documents);

        // 4. 生成查询向量
        Embedding queryEmbedding = embeddingModel.embed("玉米锈病").content();

        // 5. 相似度搜索（从MySQL中找最相似的向量）
        VectorStorePO mostSimilar = mysqlEmbeddingStore.searchMostSimilar(queryEmbedding);

        // 6. 输出结果
        log.info("最相似文档路径：{}", mostSimilar.getDocumentPath());
        log.info("最相似文本内容：{}", mostSimilar.getTextContent());

        return "test service end";
    }

    public String dashScope() throws NoApiKeyException, InputRequiredException {
        log.info("=== dashScope service start ===");
        Generation gen = new Generation();

        Message userMsg = Message.builder().role(Role.USER.getValue()).content("你是谁？").build();

        GenerationParam param = GenerationParam.builder()
                // 若没有配置环境变量，请用阿里云百炼API Key将下行替换为：.apiKey("sk-xxx")
                .apiKey("sk-9c6b5f80e77b4beaaf299d06308d7f9d")
                .model("qwen-plus")
                .enableThinking(true)
                .messages(Arrays.asList(userMsg))
                .resultFormat("message") // ✅ 必须设置为message（强制约束
                .build();

        GenerationResult result = gen.call(param);
        log.info("dashScope result: {}", result);


        return "dashScope service end";
    }
}