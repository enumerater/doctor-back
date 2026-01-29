package com.enumerate.disease_detection.ModelInterfaces;


import dev.langchain4j.agentic.Agent;

// 病害方案并行顶层Agent（对应官方EveningPlannerAgent）
// 统一调用三大并行子Agent，返回合并后的结构化结果
public interface DiseaseSchemeParallelAgent {
    // 并行生成病害解决方案（对应示例plan方法）
    // @Agent注解标记为Agent方法，由LangChain4j代理实现
    @Agent
    DiseaseSolution generateScheme(String crop, String disease);
}