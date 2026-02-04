package com.enumerate.disease_detection.ModelInterfaces;

import com.enumerate.disease_detection.POJO.VO.MergeTemHum;
import dev.langchain4j.service.*;

public interface Assistant {

    @SystemMessage({"你是一位专业的农业病害检测专家，具有丰富的农业知识和病害诊断经验。" +
            "你的主要职责是帮助用户检测和诊断农业作物的病害问题。" +
            "请根据用户的问题，提供专业、准确的回答和实用的建议。" +
            "请使用友好的语言，确保用户能够理解诊断结果和建议。"})
    TokenStream chatStream(String userMessage);


    /**
     * RAG聊天核心方法：基于知识库回答用户问题，不知道则明确说明
     *
     * @param knowledgeContext 知识
     * @param userMessage      用户输入的问题
     * @return 基于知识库的准确回答（或“不知道”）
     */
    @SystemMessage({
            "你是一个基于农业知识库的问答助手，必须严格遵守以下规则：",
            "1. 你的所有回答必须完全基于提供的【知识库上下文】，不得使用任何知识库外的信息；",
            "2. 如果【知识库上下文】中没有与用户问题相关的内容，直接回复“对不起，知识库中并没有相关内容”，绝对不要编造答案；",
            "3. 回答要简洁、准确，只回答用户问题相关的内容，不要额外扩展；",
            "4. 仅使用中文回答，语气友好且专业。",
            "",
            "【知识库上下文】：{{knowledgeContext}}"
    })
    TokenStream ragChat(@V("knowledgeContext") String knowledgeContext, @UserMessage String userMessage);

    @SystemMessage({"你现在是一位农学专家，回答用户的农业问题,回答尽量简洁易懂"})
    TokenStream streamChat(@UserMessage String userMessage);


    @SystemMessage({"你现在是一位农学专家，回答用户的农业问题,回答尽量简洁易懂"})
    TokenStream chatMemory(@MemoryId String memoryId, @UserMessage String userMessage);









}