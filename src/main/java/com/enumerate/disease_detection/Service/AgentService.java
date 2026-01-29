package com.enumerate.disease_detection.Service;

import com.enumerate.disease_detection.ChatModel.MainModel;
import com.enumerate.disease_detection.ChatModel.PersistentChatMemoryStore;
import com.enumerate.disease_detection.ModelInterfaces.agents.*;
import com.enumerate.disease_detection.Tools.*;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class AgentService {


    @Autowired
    private MainModel mainModel;

    @Autowired
    private PersistentChatMemoryStore persistentChatMemoryStore;

//    @Autowired
//    private ChatMessageMapper chatMessageMapper;
//
//    @Autowired
//    private LongMemoryTool longMemoryTool;
//
//    @Autowired
//    private VisioTool visioTool;
//
//    @Autowired
//    private RagTool ragTool;
//
//    @Async("aiAsyncExecutor")
//    public void agent(SseEmitter emitter, String prompt, Long userId, Long sessionId) {
//        log.info("========================开始执行think-execute============================");
//
//        OpenAiStreamingChatModel model = mainModel.streamingModel();
//
//        Agent agent = AiServices.builder(Agent.class)
//                .streamingChatModel(model)
//                // 关键修改：provider的入参memoryId就是调用chatMemory时传入的customMemoryId
//                .chatMemoryProvider(memoryId -> {
//                    // 每次调用都为当前memoryId构建专属的ChatMemory实例
//                    return MessageWindowChatMemory.builder()
//                            .maxMessages(20)
//                            .chatMemoryStore(persistentChatMemoryStore)
//                            .id(memoryId) // 把传入的memoryId绑定到ChatMemory实例上！！！
//                            .build();
//                })
//                .tools(longMemoryTool, visioTool,ragTool)
//                .build();
//
//        AtomicInteger msgId = new AtomicInteger(1);
//        try {
//            log.info("开始执行记忆对话{}", userId + String.valueOf(sessionId));
//            TokenStream tokenStream = agent.brain(userId + String.valueOf(sessionId) ,prompt);
//
//            chatMessageMapper.insert(ChatMessagePO.builder()
//                    .id(null)
//                    .sessionId(userId + String.valueOf(sessionId))
//                    .messageRole("0")
//                    .messageContent(prompt)
//                    .messageTime(LocalDateTime.now())
//                    .deleted("0")
//                    .build());
//
//
//            tokenStream.onPartialResponse(partialContent -> {
//                        try {
//                            log.info("流式分片内容：{}", partialContent);
//                            SseEmitter.SseEventBuilder event = SseEmitter.event()
//                                    .id(String.valueOf(msgId.getAndIncrement()))
//                                    .data(partialContent) // 业务数据
//                                    .name("data");
//                            emitter.send(event);
//                        } catch (IOException e) {
//                            log.error("AI分片内容推送失败", e);
//                            emitter.completeWithError(e);
//                        }
//                    })
//                    // 处理流式完成：正常关闭连接
//                    .onCompleteResponse(chatResponse -> {
//                        log.info("\n流式响应完成，完整结果：{}", chatResponse);
//                        chatMessageMapper.insert(ChatMessagePO.builder()
//                                .id(null)
//                                .sessionId(userId + String.valueOf(sessionId))
//                                .messageRole("1")
//                                .messageContent(chatResponse.aiMessage().text())
//                                .messageTime(LocalDateTime.now())
//                                .deleted("0")
//                                .build()
//                        );
//
//                        emitter.complete();
//                    })
//                    // 处理AI请求异常：异常关闭连接
//                    .onError(throwable -> {
//                        log.error("AI流式请求执行异常", throwable);
//                        emitter.completeWithError(throwable);
//                    })
//                    // 启动流式请求（同步阻塞，仅阻塞当前异步线程，无任何影响）
//                    .start();
//        } catch (Exception e) {
//            log.error("AI流式请求初始化失败", e);
//            emitter.completeWithError(e);
//        }
//
//
//    }


//
//    CategoryRouter routerAgent = AgenticServices
//            .agentBuilder(CategoryRouter.class)
//            .chatModel(baseModel)
//            .outputKey("category")
//            .build();
//
//    TextAgent textAgent = AgenticServices
//            .agentBuilder(TextAgent.class)
//            .chatModel(baseModel)
//            .outputKey("response")
//            .build();
//    VisionAgent visionAgent = AgenticServices
//            .agentBuilder(VisionAgent.class)
//            .chatModel(baseModel)
//            .outputKey("response")
//            .build();
//
//    UntypedAgent expertsAgent = AgenticServices.conditionalBuilder()
//            // 条件1：状态中category=PIC → 调用视觉Agent
//            .subAgents(scope -> RequestCategory.PIC.equals(scope.readState("category")), visionAgent)
//            // 条件2：状态中category=TEXT → 调用文本Agent
//            .subAgents(scope -> RequestCategory.TEXT.equals(scope.readState("category")), textAgent)
//            .build();
//
//    MainAgent mainAgent = AgenticServices
//            .sequenceBuilder(MainAgent.class)
//            .subAgents(routerAgent, expertsAgent)
//            .outputKey("response")
//            .build();
//
//
//    // 1. 构建三大平行子Agent，配置outputKey（存储子Agent执行结果到状态）
//    PesticideExpert pesticideExpert = AgenticServices
//            .agentBuilder(PesticideExpert.class)
//            .chatModel(baseModel)
//            .outputKey("pesticide") // 植保结果存入状态key="pesticide"
//            .build();
//
//    FieldManageExpert fieldManageExpert = AgenticServices
//            .agentBuilder(FieldManageExpert.class)
//            .chatModel(baseModel)
//            .outputKey("fieldManage") // 管理结果存入状态key="fieldManage"
//            .build();
//
//    SafeNoticeExpert safeNoticeExpert = AgenticServices
//            .agentBuilder(SafeNoticeExpert.class)
//            .chatModel(baseModel)
//            .outputKey("safeNotice") // 注意事项存入状态key="safeNotice"
//            .build();
//
//    // 2. 构建并行顶层Agent（核心！完全对齐官方示例）
//    DiseaseSchemeParallelAgent parallelAgent = AgenticServices
//            // 绑定顶层Agent接口，指定并行执行
//            .parallelBuilder(DiseaseSchemeParallelAgent.class)
//            // 加入三大并行子Agent（执行顺序无关，完全并行）
//            .subAgents(pesticideExpert, fieldManageExpert, safeNoticeExpert)
//            // 可选：自定义线程池（官方示例配置，高并发场景推荐）
//            // 核心数设为3，与子Agent数量一致，避免线程竞争
//            .executor(Executors.newFixedThreadPool(3))
//            // 配置并行执行的输出键（可省略，仅用于状态标记）
//            .outputKey("diseaseSolution")
//            // 核心：自定义结果合并逻辑（对应示例电影+餐食组合）
//            // 从AgenticScope读取各子Agent的outputKey结果，封装为DiseaseSolution
//            .output(agenticScope -> {
//                // 从状态中读取三大子Agent的执行结果，指定默认值为空字符串（避免空指针）
//                String pesticide = agenticScope.readState("pesticide", "");
//                String fieldManage = agenticScope.readState("fieldManage", "");
//                String safeNotice = agenticScope.readState("safeNotice", "");
//
//                // 合并结果为结构化DiseaseSolution对象（核心逻辑）
//                return new DiseaseSolution(pesticide, fieldManage, safeNotice);
//            })
//            // 构建最终的并行Agent
//            .build();


//    public String invokes(Map<String, Object> input) {
//        StreamingChatModel baseModelStream = mainModel.streamingModel();
//        ChatModel baseModel = mainModel.tongYiModel();
//        // ===================== 第一步：构建基础Agent（路由+文本+视觉） =====================
//        CategoryRouter routerAgent = AgenticServices
//                .agentBuilder(CategoryRouter.class)
//                .chatModel(baseModel)
//                .outputKey("category") // 路由结果存入状态：category
//                .build();
//
//        TextAgent textAgent = AgenticServices
//                .agentBuilder(TextAgent.class)
//                .chatModel(baseModel)
//                .outputKey("response") // 文本识别结果存入：response
//                .build();
//
//        VisionAgent visionAgent = AgenticServices
//                .agentBuilder(VisionAgent.class)
//                .chatModel(mainModel.visionModel()) // 关键：使用视觉模型，而非baseModel
//                .outputKey("response") // 视觉识别结果存入：response（与文本Agent统一，方便后续处理）
//                .build();
//
//        // 条件分支Agent（PIC→视觉Agent，TEXT→文本Agent）
//        UntypedAgent expertsAgent = AgenticServices.conditionalBuilder()
//                .subAgents(scope -> RequestCategory.PIC.equals(scope.readState("category")), visionAgent)
//                .subAgents(scope -> RequestCategory.TEXT.equals(scope.readState("category")), textAgent)
//                .build();
//
//        // ===================== 第二步：构建方案拆解Agent（衔接识别与并行） =====================
//        SchemeSplitAgent splitAgent = AgenticServices
//                .agentBuilder(SchemeSplitAgent.class)
//                .chatModel(baseModel)
//                .outputKey("splitResult") // 拆解结果存入：splitResult
//                .build();
//
//        // ===================== 第三步：构建三大并行子Agent+并行顶层Agent =====================
//// 3.1 三大并行子Agent
//        PesticideExpert pesticideExpert = AgenticServices
//                .agentBuilder(PesticideExpert.class)
//                .chatModel(baseModel)
//                .outputKey("pesticide")
//                .build();
//
//        FieldManageExpert fieldManageExpert = AgenticServices
//                .agentBuilder(FieldManageExpert.class)
//                .chatModel(baseModel)
//                .outputKey("fieldManage")
//                .build();
//
//        SafeNoticeExpert safeNoticeExpert = AgenticServices
//                .agentBuilder(SafeNoticeExpert.class)
//                .chatModel(baseModel)
//                .outputKey("safeNotice")
//                .build();
//
//        // 3.2 并行顶层Agent（官方范式，并行执行+结果合并）
//        DiseaseSchemeParallelAgent parallelAgent = AgenticServices
//                .parallelBuilder(DiseaseSchemeParallelAgent.class)
//                .subAgents(pesticideExpert, fieldManageExpert, safeNoticeExpert)
//                .executor(Executors.newFixedThreadPool(3)) // 3核心线程池匹配子Agent数量
//                .outputKey("diseaseSolution")
//                .output(scope -> {
//                    // 读取并行子Agent结果，设置默认值避免空指针
//                    String pesticide = scope.readState("pesticide", "暂未生成有效用药方案");
//                    String fieldManage = scope.readState("fieldManage", "暂未生成有效管理方案");
//                    String safeNotice = scope.readState("safeNotice", "暂未生成有效注意事项");
//                    // 非空校验
//                    pesticide = pesticide.isBlank() ? "暂未生成有效用药方案" : pesticide;
//                    fieldManage = fieldManage.isBlank() ? "暂未生成有效管理方案" : fieldManage;
//                    safeNotice = safeNotice.isBlank() ? "暂未生成有效注意事项" : safeNotice;
//                    return new DiseaseSolution(pesticide, fieldManage, safeNotice);
//                })
//                .build();
//
//        // ===================== 第四步：构建汇总Agent（最终整合输出） =====================
//        SummaryAgent summaryAgent = AgenticServices
//                .agentBuilder(SummaryAgent.class)
//                .chatModel(baseModel)
//                .outputKey("finalResponse") // 最终结果存入：finalResponse
//                .build();
//
//        // ===================== 第五步：全流程整合（核心！串行+并行组合） =====================
//        // 最终主Agent：按顺序串行执行，前一个Agent的输出作为后一个的输入
//
//        UntypedAgent mainAgent = AgenticServices
//                .sequenceBuilder()
//                .subAgents(
//                        routerAgent,        // 步骤1：意图路由（PIC/TEXT）
//                        expertsAgent,       // 步骤2：分支识别（文本/视觉）→ 输出response
//                        splitAgent,         // 步骤3：拆解识别结果 → 输出splitResult（作物=XXX|病害=XXX）
//                        parallelAgent,      // 步骤4：并行生成方案 → 输出diseaseSolution
//                        summaryAgent        // 步骤5：最终整合 → 输出finalResponse
//                )
//                .outputKey("finalResponse")
//                .build();
//
//        return (String) mainAgent.invoke(input);
//    }

    @Autowired
    private VisioTool visioTool;


    public String invokes(String input) {
        OpenAiChatModel baseModel = mainModel.tongYiModel();

        //1 解析agent
        InputParserAgent inputParserAgent = AgenticServices
                .agentBuilder(InputParserAgent.class)
                .chatModel(baseModel)
                .outputKey("analysisResult")
                .build();

        // 分支
        RouterAgent routerAgent = AgenticServices
                .agentBuilder(RouterAgent.class)
                .chatModel(baseModel)
                .outputKey("category")
                .build();

        //2 识别agent
        VisionAgent visionAgent = AgenticServices
                .agentBuilder(VisionAgent.class)
                .chatModel(baseModel)
                .outputKey("analysisResult")
                .tools(visioTool)
                .build();

        UntypedAgent expertsAgent = AgenticServices.conditionalBuilder()
                .subAgents( agenticScope -> agenticScope.readState("category") == Boolean.TRUE, visionAgent)
                .build();


        //4 safe notice agent
        SafeNoticeExpert safeNoticeExpert = AgenticServices
                .agentBuilder(SafeNoticeExpert.class)
                .chatModel(baseModel)
                .outputKey("safeNotice")
                .build();

        //5 pesticide expert
        PesticideExpert pesticideExpert = AgenticServices
                .agentBuilder(PesticideExpert.class)
                .chatModel(baseModel)
                .outputKey("pesticide")
                .build();

        //6 field manage expert
        FieldManageExpert fieldManageExpert = AgenticServices
                .agentBuilder(FieldManageExpert.class)
                .chatModel(baseModel)
                .outputKey("fieldManage")
                .build();

        //7 总结agent
        SummaryAgent summaryAgent = AgenticServices
                .agentBuilder(SummaryAgent.class)
                .chatModel(baseModel)
                .outputKey("finalResponse")
                .build();

        PlannerAgent plannerAgent = AgenticServices
                .parallelBuilder(PlannerAgent.class)
                .subAgents(
                        safeNoticeExpert,
                        pesticideExpert,
                        fieldManageExpert
                )
                .executor(Executors.newFixedThreadPool(3))
                .output(agenticScope -> {
                    log.info("plannerAgent 执行完毕{}", agenticScope);

                    String res1 = agenticScope.readState("safeNotice", "暂未生成有效注意事项");
                    String res2 = agenticScope.readState("pesticide", "暂未生成有效用药方案");
                    String res3 = agenticScope.readState("fieldManage", "暂未生成有效管理方案");

                    return String.format("安全注意：%s\n植保用药：%s\n田间管理：%s", res1, res2, res3);
                })
                .outputKey("diseaseSolution")
                .build();

        UntypedAgent novelCreator = AgenticServices
        .sequenceBuilder()
        .subAgents(inputParserAgent, routerAgent,expertsAgent,plannerAgent,summaryAgent)
        .outputKey("finalResponse")
        .output(agenticScope -> {
            // 保留日志需求：读取finalResponse并打印
            String finalResult = agenticScope.readState("finalResponse", "暂未生成有效内容");
            log.info("novelCreator 执行完毕，最终结果：{}", finalResult);
            return finalResult; // ✅ 正确：返回字符串，而非agenticScope
        })
        .build();

        return (String) novelCreator.invoke(Map.of("request", input));

    }

}