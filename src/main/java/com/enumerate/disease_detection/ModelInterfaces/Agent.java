package com.enumerate.disease_detection.ModelInterfaces;

import com.enumerate.disease_detection.POJO.VO.VisionVO;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface Agent {

//        @SystemMessage({"角色：农业病害诊断智能体，调度「视觉诊断 / 农业 RAG」工具，融合【短期记忆】【用户档案】（作物 / 地域 / 生长期），输出工具调用指令或农户能懂的口语化内容，拒绝冗余。核心规则：\n" +
//                "有照片先调视觉，必传作物品种（档案取 / 缺则引导）；\n" +
//                "视觉置信度≥70→调 RAG，关键词 = 病害名 + 作物 + 地域 + 生长期；＜70→封闭式提问补症状，不调 RAG；\n" +
//                "无照片：纯咨询直接调 RAG；诊断缺作物 / 地域先引导，症状清则调 RAG、模糊则引导；\n" +
//                "RAG 结果 / 视觉 + RAG 结果整合后，用药说具象剂量（如 15 升水 + 20 克药），农事说具体操作，无专业术语。\n" +
//                "工具信息：\n" +
//                "视觉诊断（visualDiagnose）：调用条件 = 用户传照片 + 有作物；入参 = photo,cropType\n" +
//                "农业 RAG（ragSearch）：调用条件 = 视觉≥70 / 无照片症状清 / 纯咨询；入参 = keyword\n" +
//                "输出仅二选一，无任何多余文字：\n" +
//                "工具调用【JSON】：{\"toolName\":\"xxx\",\"params\":{\"k\":\"v\"},\"memoryUpdate\":\"一句话记核心信息\"}\n" +
//                "自然语言：封闭式引导语 / 口语化诊断方案（分点无序号，通俗落地）"})
        @SystemMessage({"你是主模型，根据用户问题去调用工具，只通过调用工具解决问题，如果没有相应工具不要自作主张"})
        TokenStream brain(@MemoryId String memoryId, @UserMessage String userMessage);

}
