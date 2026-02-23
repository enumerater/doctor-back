package com.enumerate.disease_detection.Service;

import com.enumerate.disease_detection.ChatModel.MainModel;
import com.enumerate.disease_detection.Tools.DatabaseTool;
import com.enumerate.disease_detection.Tools.RagTool;
import com.enumerate.disease_detection.Tools.VisioTool;
import com.enumerate.disease_detection.Tools.WebSearchTool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.tool.DefaultToolExecutor;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Agent工作流服务 - 基于ReAct模式的单一智能体循环
 *
 * <p>采用标准 ReAct (Reasoning + Acting) 架构，单一智能体在循环中自主思考（Thought）、
 * 选择工具（Action）、观察结果（Observation），利用 LangChain4j 原生 Function Calling。</p>
 *
 * <p>工作流程：UserMessage → [LLM思考 → 调用工具 → 观察结果] × N → 最终回答</p>
 */
@Service
@Slf4j
public class AgentWorkflowService {

    private static final int MAX_ITERATIONS = 10;

    @Autowired
    private MainModel mainModel;

    @Autowired
    private ToolLoaderService toolLoaderService;

    @Autowired
    private VisioTool visioTool;

    @Autowired
    private RagTool ragTool;

    @Autowired
    private DatabaseTool databaseTool;

    @Autowired
    private WebSearchTool webSearchTool;

    /** 内置 @Tool Bean 提取的 ToolSpecification 列表 */
    private final List<ToolSpecification> builtinToolSpecs = new ArrayList<>();

    /** 内置工具执行器：toolName -> DefaultToolExecutor */
    private final Map<String, DefaultToolExecutor> builtinExecutors = new HashMap<>();

    @PostConstruct
    public void init() {
        registerToolBean(visioTool);
        registerToolBean(ragTool);
        registerToolBean(databaseTool);
        registerToolBean(webSearchTool);
        log.info("ReAct Agent 初始化完成，已注册 {} 个内置工具: {}",
                builtinToolSpecs.size(),
                builtinExecutors.keySet());
    }

    private void registerToolBean(Object toolBean) {
        List<ToolSpecification> specs = ToolSpecifications.toolSpecificationsFrom(toolBean);
        builtinToolSpecs.addAll(specs);

        for (Method method : toolBean.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(dev.langchain4j.agent.tool.Tool.class)) {
                String toolName = method.getName();
                builtinExecutors.put(toolName, new DefaultToolExecutor(toolBean, method));
                log.info("注册内置工具: {}", toolName);
            }
        }
    }

    /**
     * 执行ReAct Agent工作流
     *
     * @param emitter SSE事件发射器
     * @param input   用户输入（文本，可能包含图片URL标记）
     * @param userId  用户ID
     */
    @Async
    public void execute(SseEmitter emitter, String input, Long userId) {
        int msgId = 1;

        try {
            OpenAiChatModel model = mainModel.tongYiModel();

            // 收集所有工具规范
            List<ToolSpecification> allToolSpecs = new ArrayList<>(builtinToolSpecs);
            Map<String, ToolLoaderService.DynamicToolEntry> dynamicTools = new HashMap<>();

            try {
                dynamicTools = toolLoaderService.loadDynamicToolSpecs();
                for (ToolLoaderService.DynamicToolEntry entry : dynamicTools.values()) {
                    allToolSpecs.add(entry.getToolSpecification());
                }
                log.info("工具加载完成: {} 个内置 + {} 个动态, 共 {} 个",
                        builtinToolSpecs.size(), dynamicTools.size(), allToolSpecs.size());
            } catch (Exception e) {
                log.error("加载动态工具失败，继续使用内置工具", e);
            }

            // 构建消息列表
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(SystemMessage.from(buildSystemPrompt(userId)));
            messages.add(UserMessage.from(input));

            sendStatusEvent(emitter, msgId++, "thinking", "正在分析您的问题...");

            // ReAct 循环
            for (int iteration = 1; iteration <= MAX_ITERATIONS; iteration++) {
                log.info("===== ReAct 迭代 {}/{} =====", iteration, MAX_ITERATIONS);

                ChatRequest request = ChatRequest.builder()
                        .messages(messages)
                        .toolSpecifications(allToolSpecs)
                        .build();

                ChatResponse response = model.chat(request);
                AiMessage aiMessage = response.aiMessage();
                messages.add(aiMessage);

                // 发送思考内容
                if (aiMessage.text() != null && !aiMessage.text().isBlank()) {
                    if (aiMessage.hasToolExecutionRequests()) {
                        sendDataEvent(emitter, msgId++, "thought", aiMessage.text());
                    } else {
                        // 没有工具调用请求 → 这是最终回答
                        sendStatusEvent(emitter, msgId++, "completed", "回答完成");
                        sendDataEvent(emitter, msgId++, "final_result", aiMessage.text());
                        emitter.complete();
                        return;
                    }
                }

                // 处理工具调用
                if (aiMessage.hasToolExecutionRequests()) {
                    for (ToolExecutionRequest toolRequest : aiMessage.toolExecutionRequests()) {
                        String toolName = toolRequest.name();
                        log.info("调用工具: {} | 参数: {}", toolName, toolRequest.arguments());

                        sendStatusEvent(emitter, msgId++, "tool_calling",
                                String.format("正在调用工具: %s", toolName));

                        String result;
                        try {
                            if (builtinExecutors.containsKey(toolName)) {
                                result = builtinExecutors.get(toolName).execute(toolRequest, null);
                            } else if (dynamicTools.containsKey(toolName)) {
                                result = dynamicTools.get(toolName).execute(toolRequest.arguments());
                            } else {
                                result = "未找到工具: " + toolName;
                                log.warn("未找到工具: {}", toolName);
                            }
                        } catch (Exception e) {
                            log.error("工具执行失败: {}", toolName, e);
                            result = "工具执行失败: " + e.getMessage();
                        }

                        sendDataEvent(emitter, msgId++, "observation",
                                String.format("[%s] %s", toolName, truncateResult(result)));

                        ToolExecutionResultMessage resultMessage =
                                ToolExecutionResultMessage.from(toolRequest, result);
                        messages.add(resultMessage);
                    }
                } else {
                    // 既没有文本也没有工具调用（不太可能但做保护）
                    log.warn("AI响应既无文本也无工具调用，结束循环");
                    sendStatusEvent(emitter, msgId++, "completed", "回答完成");
                    sendDataEvent(emitter, msgId++, "final_result", "抱歉，无法生成有效回答，请重试。");
                    emitter.complete();
                    return;
                }
            }

            // 达到最大迭代次数
            log.warn("达到最大迭代次数 {}，强制结束", MAX_ITERATIONS);
            sendStatusEvent(emitter, msgId++, "completed", "已达到最大推理步数");

            // 尝试获取最后的AI消息作为结果
            String lastText = extractLastAiText(messages);
            if (lastText != null && !lastText.isBlank()) {
                sendDataEvent(emitter, msgId++, "final_result", lastText);
            } else {
                sendDataEvent(emitter, msgId++, "final_result",
                        "抱歉，经过多轮推理仍未能得出满意答案，请尝试简化您的问题。");
            }
            emitter.complete();

        } catch (Exception e) {
            log.error("ReAct Agent执行失败", e);
            sendStatusEvent(emitter, msgId++, "error", "执行出错: " + e.getMessage());
            emitter.completeWithError(e);
        }
    }

    private String buildSystemPrompt(Long userId) {

        return """
                你是一个专业的农业AI助手，专注于农作物病害诊断、防治建议和农业知识咨询。

                ## 工作模式
                你采用 ReAct（Reasoning + Acting）模式工作：
                1. 先思考用户的问题需要什么信息
                2. 如果需要，调用合适的工具获取信息
                3. 根据工具返回的结果进行分析
                4. 给出最终的专业回答

                ## 工具使用指南
                - **visionTool**: 当用户提供了图片URL时，调用此工具进行病害图像识别
                - **ragTool**: 当需要查询农业知识库中的专业知识时调用
                - **queryDiagnosisHistory**: 当用户询问自己的历史诊断记录时调用
                - **queryUserFarmInfo**: 当用户询问自己的农场、地块信息时调用
                - **searchDiseaseKnowledge**: 当需要查找特定病害的详细信息和防治方法时调用
                - **querySeasonalRisks**: 当用户询问季节性病害风险时调用
                - **webSearch**: 当需要最新的实时信息（政策、新闻、市场等）时调用

                ## 当前用户信息
                - 用户ID: %d（调用数据库相关工具时使用此ID,一定不要错了）

                ## 回答规范
                ### 病害诊断类问题
                请按以下结构组织回答：
                1. **诊断结果**: 明确病害名称和判断依据
                2. **病害描述**: 简要说明该病害的特征和危害
                3. **防治建议**: 包括农业防治、化学防治、生物防治
                4. **安全注意事项**: 用药安全、采收间隔等
                5. **田间管理建议**: 预防复发的管理措施

                ### 一般咨询类问题
                简洁、准确地回答，必要时引用工具查询到的数据支撑观点。

                ### 通用要求
                - 回答使用中文
                - 保持专业但易懂的语言风格
                - 如果信息不足以做出准确判断，如实告知用户并建议补充信息
                """.formatted(userId);
    }

    private String extractLastAiText(List<ChatMessage> messages) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i) instanceof AiMessage ai && ai.text() != null) {
                return ai.text();
            }
        }
        return null;
    }

    private String truncateResult(String result) {
        if (result == null) return "";
        return result.length() > 500 ? result.substring(0, 500) + "..." : result;
    }

    // ========== SSE 事件发送 ==========

    private void sendStatusEvent(SseEmitter emitter, int id, String status, String message) {
        try {
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .id(String.valueOf(id))
                    .name("status")
                    .data(Map.of(
                            "status", status,
                            "message", message,
                            "timestamp", System.currentTimeMillis()
                    ));
            emitter.send(event);
        } catch (IOException e) {
            log.error("发送状态事件失败", e);
        }
    }

    private void sendDataEvent(SseEmitter emitter, int id, String type, String content) {
        try {
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .id(String.valueOf(id))
                    .name("data")
                    .data(Map.of(
                            "type", type,
                            "content", content,
                            "timestamp", System.currentTimeMillis()
                    ));
            emitter.send(event);
        } catch (IOException e) {
            log.error("发送数据事件失败", e);
        }
    }
}
