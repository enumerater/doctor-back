package com.enumerate.disease_detection.Utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enumerate.disease_detection.MVC.Mapper.VectorStoreMapper;
import com.enumerate.disease_detection.MVC.POJO.PO.VectorStorePO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于MySQL的用户记忆向量存储服务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MysqlEmbeddingStore {

    private final ObjectMapper objectMapper;

    @Autowired
    private VectorStoreMapper vectorStoreMapper;

    @Resource(name = "embeddingModel")
    private OpenAiEmbeddingModel embeddingModel;

    /**
     * 向量化一条用户记忆并存储到MySQL
     *
     * @param userId          用户ID
     * @param memoryText      记忆文本
     * @param sourceSessionId 来源会话ID
     * @param memoryType      记忆类型
     */
    public void saveUserMemory(Long userId, String memoryText, String sourceSessionId, String memoryType) {
        Embedding embedding = embeddingModel.embed(memoryText).content();

        String embeddingJson;
        try {
            embeddingJson = objectMapper.writeValueAsString(embedding.vector());
        } catch (JsonProcessingException e) {
            log.error("向量转JSON失败", e);
            throw new RuntimeException("向量序列化失败", e);
        }

        VectorStorePO po = VectorStorePO.builder()
                .userId(userId)
                .textContent(memoryText)
                .embedding(embeddingJson)
                .memoryType(memoryType)
                .sourceSessionId(sourceSessionId)
                .createdAt(LocalDateTime.now())
                .build();
        vectorStoreMapper.insert(po);
        log.info("用户{}记忆已入库: {}", userId, memoryText.substring(0, Math.min(50, memoryText.length())));
    }

    /**
     * 按用户过滤，返回Top-N相似记忆文本
     *
     * @param userId         用户ID
     * @param queryEmbedding 查询向量
     * @param topN           返回条数
     * @param minScore       最低相似度阈值
     * @return 相似记忆文本列表
     */
    public List<String> searchTopNForUser(String userId, Embedding queryEmbedding, int topN, float minScore) {
        List<VectorStorePO> userVectors = vectorStoreMapper.selectList(
                new QueryWrapper<VectorStorePO>().eq("user_id", userId)
        );

        if (userVectors.isEmpty()) {
            return List.of();
        }

        float[] queryVector = queryEmbedding.vector();

        // 计算相似度并排序
        List<ScoredMemory> scoredList = new ArrayList<>();
        for (VectorStorePO entity : userVectors) {
            float[] docVector;
            try {
                docVector = objectMapper.readValue(entity.getEmbedding(), float[].class);
            } catch (JsonProcessingException e) {
                log.error("向量反序列化失败", e);
                continue;
            }

            float score = (float) cosineSimilarity(queryVector, docVector);
            if (score >= minScore) {
                scoredList.add(new ScoredMemory(entity.getTextContent(), score));
            }
        }

        scoredList.sort((a, b) -> Float.compare(b.score, a.score));

        List<String> results = new ArrayList<>();
        for (int i = 0; i < Math.min(topN, scoredList.size()); i++) {
            results.add(scoredList.get(i).text);
        }

        log.info("用户{}记忆检索完成，命中{}条（总{}条）", userId, results.size(), userVectors.size());
        return results;
    }

    /**
     * 余弦相似度计算
     */
    private double cosineSimilarity(float[] vec1, float[] vec2) {
        if (vec1.length != vec2.length) {
            throw new IllegalArgumentException("向量长度不一致");
        }
        double dotProduct = 0;
        double norm1 = 0;
        double norm2 = 0;
        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += Math.pow(vec1[i], 2);
            norm2 += Math.pow(vec2[i], 2);
        }
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private record ScoredMemory(String text, float score) {}
}
