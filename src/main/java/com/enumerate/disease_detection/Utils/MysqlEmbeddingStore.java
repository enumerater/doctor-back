package com.enumerate.disease_detection.Utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enumerate.disease_detection.MVC.Mapper.VectorStoreMapper;
import com.enumerate.disease_detection.MVC.POJO.PO.VectorStorePO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 基于MySQL的向量存储服务（替代InMemoryEmbeddingStore）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MysqlEmbeddingStore {

    private final ObjectMapper objectMapper; // Jackson工具，用于JSON和数组互转

    @Autowired
    private VectorStoreMapper vectorStoreMapper;

    /**
     * 加载/生成向量：优先查库，无则生成并存库
     */
    public void loadOrSaveEmbedding(EmbeddingModel embeddingModel, List<dev.langchain4j.data.document.Document> documents) {
         // 1. 创建分词器：
         DocumentByParagraphSplitter splitter = new DocumentByParagraphSplitter(800, 20);

        for (dev.langchain4j.data.document.Document doc : documents) {
            String docPath = doc.metadata().getString("file_name"); // 获取文档路径（唯一标识）
            // 1. 查数据库：判断该文档是否已生成过向量
            boolean exists = vectorStoreMapper.exists(new QueryWrapper<VectorStorePO>().eq("document_path", docPath));
            if (exists) {
                log.info("文档{}已存在向量，无需重复生成", docPath);
                continue;
            }

            // 2. 未存在：生成向量并入库
            log.info("文档{}未生成向量，开始生成并入库", docPath);
            List<TextSegment> split = splitter.split(doc);
            for (TextSegment textSegment : split){

                Embedding embedding = embeddingModel.embed(textSegment).content();

                // 3. 向量转JSON字符串（存入数据库）
                String embeddingJson;
                try {
                    embeddingJson = objectMapper.writeValueAsString(embedding.vector());
                } catch (JsonProcessingException e) {
                    log.error("向量转JSON失败", e);
                    throw new RuntimeException("向量序列化失败", e);
                }

                // 4. 保存到MySQL
                log.info("文档{}生成向量成功，开始入库", docPath);
                VectorStorePO vectorStorePO = VectorStorePO.builder()
                        .documentPath(docPath)
                        .textContent(textSegment.text())
                        .embedding(embeddingJson)
                        .build();
                vectorStoreMapper.insert(vectorStorePO);
            }
        }
    }

    /**
     * 相似度搜索：计算查询向量与库中所有向量的余弦相似度，返回最相似的结果
     * （注：MySQL 8.0.31+可直接用内置函数计算，这里是通用实现）
     */
    public VectorStorePO searchMostSimilar(Embedding queryEmbedding) {
        List<VectorStorePO> allVectors = vectorStoreMapper.selectList(new QueryWrapper<>());
        if (allVectors.isEmpty()) {
            throw new RuntimeException("向量库为空，请先加载文档");
        }

        VectorStorePO mostSimilar = null;
        float maxScore = -1;
        float[] queryVector =  queryEmbedding.vector();

        for (VectorStorePO entity : allVectors) {
            // 1. 数据库中的JSON向量转double数组
            float[] docVector;
            try {
                docVector = objectMapper.readValue(entity.getEmbedding(), float[].class);
            } catch (JsonProcessingException e) {
                log.error("向量反序列化失败", e);
                continue;
            }

            // 2. 计算余弦相似度（核心：值越大，相似度越高）
            float score = (float) cosineSimilarity(queryVector, docVector);
            if (score > maxScore) {
                maxScore =  score;
                mostSimilar = entity;
            }
        }

        if (mostSimilar == null) {
            throw new RuntimeException("未找到相似向量");
        }
        log.info("最相似文档相似度：{}", maxScore);
        return mostSimilar;
    }

    /**
     * 余弦相似度计算（核心算法：衡量两个向量的相似程度）
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
}