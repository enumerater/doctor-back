package com.enumerate.disease_detection.MVC.Service;

import com.enumerate.disease_detection.Annotations.ToolName;
import com.enumerate.disease_detection.ChatModel.MainModel;
import com.enumerate.disease_detection.MVC.Mapper.UserMapper;
import com.enumerate.disease_detection.Tools.*;
import com.enumerate.disease_detection.MVC.Mapper.ChatMessageMapper;

import com.enumerate.disease_detection.Utils.SendMessagesUtils;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.*;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.tool.DefaultToolExecutor;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    // 最后信息缓存
    final Map<String, Integer> LAST_CONSOLIDATED_MAP = new ConcurrentHashMap<>();

    private static final int MAX_ITERATIONS = 10;

    @Autowired
    private MainModel mainModel;

    @Autowired
    private VisioTool visioTool;

    @Autowired
    private RagTool ragTool;

    @Autowired
    private DatabaseTool databaseTool;

    @Autowired
    private WebSearchTool webSearchTool;

    @Autowired
    private InteractionTool interactionTool;

    @Autowired
    private ReminderTool reminderTool;


    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private com.enumerate.disease_detection.ChatModel.PersistentChatMemoryStore persistentChatMemoryStore;

    @Resource
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    /** 内置 @Tool Bean 提取的 Tool Specification 列表 */
    private final List<ToolSpecification> builtinToolSpecs = new ArrayList<>();

    /** 内置工具执行器：toolName -> DefaultToolExecutor */
    private final Map<String, DefaultToolExecutor> builtinExecutors = new HashMap<>();

    @PostConstruct
    public void init() {
        registerToolBean(visioTool);
        registerToolBean(ragTool);
        registerToolBean(databaseTool);
        registerToolBean(webSearchTool);
        registerToolBean(interactionTool);
        registerToolBean(reminderTool);
        log.info("ReAct Agent 初始化完成，已注册 {} 个内置工具: {}",
                builtinToolSpecs.size(),
                builtinExecutors.keySet());
    }

    private void registerToolBean(Object toolBean) {
        // 1. 生成默认的工具规格列表
        List<ToolSpecification> specs = ToolSpecifications.toolSpecificationsFrom(toolBean);
        Class<?> beanClass = toolBean.getClass();

        // 2. 遍历所有带@Tool注解的方法，更新spec的名称
        for (Method method : beanClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Tool.class)) {
                // 获取注解指定的工具名称
                String toolName;
                if (method.isAnnotationPresent(ToolName.class)) {
                    ToolName toolNameAnnotation = method.getDeclaredAnnotation(ToolName.class);
                    toolName = toolNameAnnotation.value();
                } else {
                    toolName = method.getName(); // 兜底使用方法名
                }

                // 找到该方法对应的ToolSpecification并更新名称
                for (ToolSpecification spec : specs) {
                    // 匹配规则：根据方法名匹配（默认spec的名称就是方法名）
                    if (spec.name().equals(method.getName())) {
                        // 关键修复：使用Builder模式创建新的ToolSpecification
                        ToolSpecification updatedSpec = ToolSpecification.builder()
                                .name(toolName)          // 替换为注解指定的名称
                                .description(spec.description())// 保留原有描述
                                .parameters(spec.parameters())  // 保留原有参数
                                .build(); // 构建新的实例

                        // 替换列表中的旧spec
                        int index = specs.indexOf(spec);
                        specs.set(index, updatedSpec);
                        break;
                    }
                }

                // 注册执行器（保持原有逻辑）
                builtinExecutors.put(toolName, new DefaultToolExecutor(toolBean, method));
                log.info("注册内置工具: {}", toolName);
            }
        }

        // 3. 将更新后的specs添加到内置规格列表
        builtinToolSpecs.addAll(specs);
    }

    @Resource(name = "tongYiModel")
    private OpenAiChatModel model;

//    /**
//     * 执行ReAct Agent工作流
//     *
//     * @param emitter SSE事件发射器
//     * @param input   用户输入（文本，可能包含图片URL标记）
//     * @param userId  用户ID
//     */
//    @Async
//    public void execute(SseEmitter emitter, String input, Long userId) {
//        int msgId = 1;
//
//        try {
//
//            // 收集所有工具规范
//            List<ToolSpecification> allToolSpecs = new ArrayList<>(builtinToolSpecs);
//
//            log.info("内置工具加载完成: {}",
//                    builtinToolSpecs);
//
//            try {
//                log.info("工具加载完成: {} 个",
//                        builtinToolSpecs.size());
//            } catch (Exception e) {
//                log.error("加载动态工具失败，继续使用内置工具", e);
//            }
//
//            // 构建消息列表
//            List<ChatMessage> messages = new ArrayList<>();
//            messages.add(SystemMessage.from(buildSystemPrompt(userId)));
//            messages.add(UserMessage.from(input));
//
//            sendStatusEvent(emitter, msgId++, "thinking", "正在分析您的问题...");
//
//            // ReAct 循环
//            for (int iteration = 1; iteration <= MAX_ITERATIONS; iteration++) {
//                log.info("===== ReAct 迭代 {}/{} =====", iteration, MAX_ITERATIONS);
//
//                ChatRequest request = ChatRequest.builder()
//                        .messages(messages)
//                        .toolSpecifications(allToolSpecs)
//                        .build();
//
//                ChatResponse response = model.chat(request);
//                AiMessage aiMessage = response.aiMessage();
//                messages.add(aiMessage);
//
//                // 发送思考内容
//                if (aiMessage.text() != null && !aiMessage.text().isBlank()) {
//                    if (aiMessage.hasToolExecutionRequests()) {
//                        sendDataEvent(emitter, msgId++, "thought", aiMessage.text());
//                    } else {
//                        // 没有工具调用请求 → 这是最终回答
//                        sendStatusEvent(emitter, msgId++, "completed", "回答完成");
//                        sendDataEvent(emitter, msgId++, "final_result", aiMessage.text());
//                        emitter.complete();
//                        return;
//                    }
//                }
//
//                // 处理工具调用
//                if (aiMessage.hasToolExecutionRequests()) {
//                    for (ToolExecutionRequest toolRequest : aiMessage.toolExecutionRequests()) {
//                        String toolName = toolRequest.name();
//                        log.info("调用工具: {} | 参数: {}", toolName, toolRequest.arguments());
//
//                        sendStatusEvent(emitter, msgId++, "tool_calling",
//                                String.format("正在调用工具: %s", toolName));
//
//                        String result;
//                        try {
//                            if (builtinExecutors.containsKey(toolName)) {
//                                result = builtinExecutors.get(toolName).execute(toolRequest, null);
//
//                            } else {
//                                result = "未找到工具: " + toolName;
//                                log.warn("未找到工具: {}", toolName);
//                            }
//                        } catch (Exception e) {
//                            log.error("工具执行失败: {}", toolName, e);
//                            result = "工具执行失败: " + e.getMessage();
//                        }
//
//                        sendDataEvent(emitter, msgId++, "observation",
//                                String.format("%s", truncateResult(result)));
//
//                        ToolExecutionResultMessage resultMessage =
//                                ToolExecutionResultMessage.from(toolRequest, result);
//                        messages.add(resultMessage);
//                    }
//                } else {
//                    // 既没有文本也没有工具调用（不太可能但做保护）
//                    log.warn("AI响应既无文本也无工具调用，结束循环");
//                    sendStatusEvent(emitter, msgId++, "completed", "回答完成");
//                    sendDataEvent(emitter, msgId++, "final_result", "抱歉，无法生成有效回答，请重试。");
//                    emitter.complete();
//                    return;
//                }
//            }
//
//            // 达到最大迭代次数
//            log.warn("达到最大迭代次数 {}，强制结束", MAX_ITERATIONS);
//            sendStatusEvent(emitter, msgId++, "completed", "已达到最大推理步数");
//
//            // 尝试获取最后的AI消息作为结果
//            String lastText = extractLastAiText(messages);
//            if (lastText != null && !lastText.isBlank()) {
//                sendDataEvent(emitter, msgId++, "final_result", lastText);
//            } else {
//                sendDataEvent(emitter, msgId++, "final_result",
//                        "抱歉，经过多轮推理仍未能得出满意答案，请尝试简化您的问题。");
//            }
//            emitter.complete();
//
//        } catch (Exception e) {
//            log.error("ReAct Agent执行失败", e);
//            sendStatusEvent(emitter, msgId++, "error", "执行出错: " + e.getMessage());
//            emitter.completeWithError(e);
//        }
//    }

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
                - **vision_analysis**: 当用户提供了图片URL时，调用此工具进行病害图像识别
                - **user_memory_search**: 当需要了解用户的个人情况、种植习惯、历史问题等个性化信息时调用，可检索该用户的历史记忆
                - **diagnosis_history**: 当用户询问自己的历史诊断记录时调用
                - **farm_info**: 当用户询问自己的农场、地块信息时调用
                - **disease_knowledge**: 当需要查找特定病害的详细信息和防治方法时调用
                - **knowledge_graph**: 当需要了解农作物、病虫害、农药、节气之间的关联关系或进行深度知识发现时调用
                - **web_search**: 当需要最新的实时信息（政策、新闻、市场等）时调用
                - **confirm_action**: 当你想执行某项敏感或高危操作（如：创建农场、更新数据、删除记录等）时，必须先调用此工具向用户发起询问确认，返回"true"表示用户同意，"false"表示用户拒绝
                - **ask_user**: 当执行某个操作缺少必要信息时（例如：用户想创建农场但没给面积），调用此工具向用户索取缺失的信息
                - **create_farm**: 创建新农场。调用前需确保已获得用户确认。
                - **update_farm**: 更新农场信息。调用前需确保已获得用户确认。
                - **delete_farm**: 删除农场。调用前需确保已获得用户确认。
                - **create_plot**: 创建新地块。调用前需确保已获得用户确认。
                - **update_plot**: 更新地块信息。调用前需确保已获得用户确认。
                - **delete_plot**: 删除地块。调用前需确保已获得用户确认。
                - **query_reminders**: 当用户询问自己的定时任务、提醒信息时调用
                - **delete_reminders**: 删除用户的定时任务、提醒信息。调用前需确保已获得用户确认。
                
                "1. **主动决策**：当用户提问需要实时信息（如天气、新闻、作物价格）时，请主动调用 `web_search` 工具，不要等待用户下指令。",
                "2. **智能补全**：如果用户提问缺少关键背景（例如问‘今天天气怎么样’但没说地点），请先调用 `user_memory_search` 检索用户以往的背景信息，或调用 `farm_info` 查看其农场位置。",
                "3. **身份意识**：当前用户的ID是 %d。在调用任何需要 userId 参数的工具时，请务必直接使用这个ID。",
                "4. **交互确认**：对于所有涉及数据的'增加'、'修改'或'删除'操作，你必须首先使用 `confirm_action` 工具征得用户同意。如果用户拒绝，则停止操作并告知用户。",
                "5. **信息搜集**：如果用户发出的指令（如'帮我建个农场'）缺少关键参数，请先调用 `ask_user` 工具询问用户，搜集齐全后再请求确认并执行。",
                "6. **专业风格**：你依然是一名农学专家，回答应简洁、专业、易懂。对于不确定的信息，优先查工具，工具查不到再询问用户。",
                "7. **多步思考**：如果需要，你可以连续调用多个工具（例如：先追问信息，再请求确认，最后执行创建）。"
    

                ## 个性化服务
                - 当用户的问题涉及其具体情况（如种植作物、地区、历史病害等）时，主动调用"user_memory_search"工具获取用户的个性化信息
                - 结合用户记忆提供更有针对性的建议和回答

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
                """.formatted(userId, userId);
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
        return result;
    }


    @Autowired
    private SendMessagesUtils sendMessagesUtils;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private Consolidator consolidator;

    @Autowired
    private ProjectMemoryStore projectMemoryStore;

    /**
     * 执行ReAct Agent工作流 (WebSocket版 - 协议v2)
     */
    @Async
    public void executeWs(WebSocketSession session, String text, String imageList, Long userId, String sessionId) {
        com.enumerate.disease_detection.Local.SessionHolder.setSession(session);
        List<Map<String, Object>> trace = new ArrayList<>();



        log.info("textttt  {}", text);

        // 1. 保存用户输入消息 (只存文字内容)
        saveMessage(sessionId, "0", text, null);

        // 2. 构建给 LLM 的完整 Prompt (包含图片链接供分析)
        String fullPrompt = text;
        if (imageList != null && !imageList.isEmpty() && !"[null]".equals(imageList) && !"[]".equals(imageList)) {
            fullPrompt = text + "\n[附带图片]: " + imageList;
        }

        // ====================== 核心：记忆管理（修正版） ======================
        String memoryId = "agent_session_" + sessionId;
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(99999) // 糊弄框架，等价于无限制
                .chatMemoryStore(persistentChatMemoryStore)
                .id(memoryId)
                .build();

        // ✅ 第一步：获取全量消息（必须先拿，否则变量未定义）
        List<ChatMessage> allMessages = chatMemory.messages();

        // ✅ 第二步：获取增量压缩游标（默认0）
        int lastConsolidated = LAST_CONSOLIDATED_MAP.getOrDefault(memoryId, 0);

        // ✅ 第三步：调用 Consolidator（1:1对齐Python，只压缩新消息）
        consolidator.maybeConsolidate(memoryId, userId, allMessages, lastConsolidated);

        // ✅ 第四步：压缩后，重新获取最新的消息列表（关键！）
        List<ChatMessage> latestMessages = chatMemory.messages();

        // ✅ 第五步：更新游标（永不重复压缩）
        // 计算已压缩的消息数量 = 原消息数 - 最新消息数
        int consolidatedCount = allMessages.size() - latestMessages.size();
        int newLastConsolidated = lastConsolidated + consolidatedCount;
        LAST_CONSOLIDATED_MAP.put(memoryId, Math.max(newLastConsolidated, 0));

        log.info("游标更新：{} → {}", lastConsolidated, newLastConsolidated);

        try {
            chatMemory.add(UserMessage.from(fullPrompt));

            List<ToolSpecification> allToolSpecs = new ArrayList<>(builtinToolSpecs);

            List<ChatMessage> messages = new ArrayList<>();
            messages.add(SystemMessage.from(buildSystemPrompt(userId)));
            messages.addAll(chatMemory.messages());

            // 发送初始思考状态（仅推送前端，不存trace）
            sendMessagesUtils.sendEvent(session, "thought", "正在分析您的问题...");

            boolean finalAnswerSent = false;
            String finalContent = null;

            for (int iteration = 1; iteration <= MAX_ITERATIONS; iteration++) {
                log.info("===== ReAct 迭代 {}/{} =====", iteration, MAX_ITERATIONS);

                ChatRequest request = ChatRequest.builder()
                        .messages(messages)
                        .toolSpecifications(allToolSpecs)
                        .build();

                ChatResponse response = model.chat(request);
                AiMessage aiMessage = response.aiMessage();
                messages.add(aiMessage);
                chatMemory.add(aiMessage); // 同步到记忆

                // A. 处理 AI 的文本回复
                if (aiMessage.text() != null && !aiMessage.text().isBlank()) {
                    if (aiMessage.hasToolExecutionRequests()) {
                        // 如果有文本且还要调用工具，这通常是 AI 的思考过程
                        sendMessagesUtils.sendEvent(session, "thought", aiMessage.text());
                        trace.add(createTraceNode("thought", aiMessage.text()));
                    } else {
                        // 最终回答
                        finalContent = aiMessage.text();
                        sendMessagesUtils.sendEvent(session, "answer", finalContent);
                        finalAnswerSent = true;
                        break; // 结束循环
                    }
                }

                // B. 处理工具调用请求
                if (aiMessage.hasToolExecutionRequests()) {
                    for (ToolExecutionRequest toolRequest : aiMessage.toolExecutionRequests()) {
                        String toolName = toolRequest.name();
                        log.info("调用工具: {} | 参数: {}", toolName, toolRequest.arguments());

                        // 发送 tool_call 事件
                        sendMessagesUtils.sendEvent(session, "tool_call",
                                toolRequest.arguments(), null, toolName);
                        trace.add(createTraceNode("tool_call", toolRequest.arguments(), toolName, null));

                        String result;
                        try {
                            if (builtinExecutors.containsKey(toolName)) {
                                result = builtinExecutors.get(toolName).execute(toolRequest, null);
                            } else {
                                result = "未找到工具: " + toolName;
                                log.warn("未找到工具: {}", toolName);
                            }
                        } catch (Exception e) {
                            log.error("工具执行失败: {}", toolName, e);
                            result = "工具执行失败: " + e.getMessage();
                        }

                        // 发送 tool_result 事件
                        sendMessagesUtils.sendEvent(session, "tool_result",
                                result, null, toolName);
                        trace.add(createTraceNode("tool_result", result, toolName, null));

                        ToolExecutionResultMessage resultMessage = ToolExecutionResultMessage.from(toolRequest, result);
                        messages.add(resultMessage);
                        chatMemory.add(resultMessage); // 同步到记忆
                    }
                } else if (aiMessage.text() == null || aiMessage.text().isBlank()) {
                    // 既无文本也无工具调用，异常情况
                    log.warn("AI 响应为空且无工具调用");
                    break;
                }
            }

            // 4. 兜底处理：如果循环结束仍未发送最终回答
            if (!finalAnswerSent) {
                finalContent = extractLastAiText(messages);
                if (finalContent == null) {
                    finalContent = "抱歉，我处理该请求时遇到了困难，请尝试换种说法。";
                }
                sendMessagesUtils.sendEvent(session, "answer", finalContent);
            }
            
            // 5. 保存 AI 回答消息及轨迹
            saveMessage(sessionId, "1", finalContent, trace);

        } catch (Exception e) {
            log.error("ReAct Agent执行失败", e);
            String errorMsg = "系统繁忙，请稍后再试: " + e.getMessage();
            sendMessagesUtils.sendEvent(session, "error", errorMsg);
            
            // 发生错误也保存一条记录
            trace.add(createTraceNode("error", errorMsg));
            saveMessage(sessionId, "1", "抱歉，我现在无法处理您的请求。", trace);
        } finally {
            com.enumerate.disease_detection.Local.SessionHolder.removeSession();
        }
    }

    private Map<String, Object> createTraceNode(String type, String content) {
        return createTraceNode(type, content, null, null);
    }

    private Map<String, Object> createTraceNode(String type, String content, String tool, Object payload) {
        Map<String, Object> node = new HashMap<>();
        node.put("type", type);
        node.put("content", content);
        node.put("timestamp", System.currentTimeMillis());
        if (tool != null) node.put("tool", tool);
        if (payload != null) node.put("payload", payload);
        return node;
    }

    private void saveMessage(String sessionId, String role, String content, List<Map<String, Object>> trace) {
        String agentDataJson = null;
        if (trace != null && !trace.isEmpty()) {
            try {
                agentDataJson = objectMapper.writeValueAsString(trace);
            } catch (Exception e) {
                log.error("Trace 序列化失败", e);
            }
        }

        chatMessageMapper.insert(com.enumerate.disease_detection.MVC.POJO.PO.ChatMessagePO.builder()
                .sessionId(sessionId)
                .messageRole(role)
                .messageContent(content)
                .agentData(agentDataJson)
                .messageTime(java.time.LocalDateTime.now())
                .deleted("0")
                .build());
    }


}


