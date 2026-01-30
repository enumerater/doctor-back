package com.enumerate.disease_detection.Service;

import com.enumerate.disease_detection.ChatModel.MainModel;
import com.enumerate.disease_detection.ModelInterfaces.agents.*;
import com.enumerate.disease_detection.Tools.*;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class AgentService {


    @Autowired
    private MainModel mainModel;

    @Autowired
    private VisioTool visioTool;

    @Async
    public void invokes(SseEmitter emitter, String input) throws IOException {
        OpenAiChatModel baseModel = mainModel.tongYiModel();
        AtomicInteger msgId = new AtomicInteger(1);

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
                .subAgents(agenticScope -> agenticScope.readState("category") == Boolean.TRUE, visionAgent)
                .outputKey("analysisResult")
                .beforeCall(agenticScope -> {
                    log.info("分支流 执行中{}", agenticScope);
                    sendStatusUpdate(emitter, msgId.getAndIncrement(), "多模态分析", "processing");
                })
                .output(agenticScope -> {
                    log.info("visionAgent 执行完毕{}", agenticScope);
                    Boolean res1 = agenticScope.readState("category", false);
                    if (res1){
                        log.info("visionAgent 执行完毕{}", agenticScope);
                        sendDataUpdate(emitter, msgId.getAndIncrement(), agenticScope.readState("analysisResult", "暂未生成有效识别结果"), "img_find");
                    }
                    else{
                        log.info("visionAgent 未执行{}", agenticScope);
                        sendDataUpdate(emitter, msgId.getAndIncrement(), "暂未发现多模态相关内容", "img_find");
                    }

                    return agenticScope.readState("analysisResult", "暂未生成有效识别结果");
                })
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
                .beforeCall(agenticScope -> {
                    log.info("并行流 执行中{}", agenticScope);
                    sendStatusUpdate(emitter, msgId.getAndIncrement(), "正在生成方案", "processing");
                })
                .output(agenticScope -> {
                    log.info("plannerAgent 执行完毕{}", agenticScope);
                    
                    String res1 = agenticScope.readState("safeNotice", "暂未生成有效注意事项");
                    String res2 = agenticScope.readState("pesticide", "暂未生成有效用药方案");
                    String res3 = agenticScope.readState("fieldManage", "暂未生成有效管理方案");
                    
                    sendStatusUpdate(emitter, msgId.getAndIncrement(), "方案生成完成", "processing");
                    
                    // 流式发送方案结果
                    sendDataUpdate(emitter, msgId.getAndIncrement(), res1, "safe_notice");
                    sendDataUpdate(emitter, msgId.getAndIncrement(), res2, "pesticide");
                    sendDataUpdate(emitter, msgId.getAndIncrement(), res3, "field_manage");
                    
                    return String.format("安全注意：%s\n植保用药：%s\n田间管理：%s", res1, res2, res3);
                })
                .outputKey("diseaseSolution")
                .build();

        UntypedAgent novelCreator = AgenticServices
        .sequenceBuilder()
        .subAgents(
                inputParserAgent,        // 步骤1：意图路由（PIC/TEXT）
                routerAgent,       // 步骤2：分支识别（文本/视觉）→ 输出response
                expertsAgent,         // 步骤3：拆解识别结果 → 输出splitResult（作物=XXX|病害=XXX）
                plannerAgent,      // 步骤4：并行生成方案 → 输出diseaseSolution
                summaryAgent        // 步骤5：最终整合 → 输出finalResponse
        )
        .beforeCall(agenticScope -> {
            log.info("主流 执行中{}", agenticScope);
            sendStatusUpdate(emitter, msgId.getAndIncrement(), "agent思考中", "processing");
        })
        .outputKey("finalResponse")
        .output(agenticScope -> {
            // 保留日志需求：读取finalResponse并打印
            String finalResult = agenticScope.readState("finalResponse", "暂未生成有效内容");
            log.info("novelCreator 执行完毕，最终结果：{}", finalResult);
            
            sendStatusUpdate(emitter, msgId.getAndIncrement(), "处理完成", "completed");
            // 发送最终结果
            sendDataUpdate(emitter, msgId.getAndIncrement(), finalResult, "final_result");
            
            emitter.complete();
            return finalResult;
        })
        .build();
        
        // 执行agent调用链
        novelCreator.invoke(Map.of("request", input));
    }
    
    /**
     * 发送状态更新
     */
    private void sendStatusUpdate(SseEmitter emitter, int id, String message, String status) {
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
            log.error("发送状态更新失败", e);
            emitter.completeWithError(e);
        }
    }
    
    /**
     * 发送数据更新
     */
    private void sendDataUpdate(SseEmitter emitter, int id, String data, String type) {
        try {
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .id(String.valueOf(id))
                    .name("data")
                    .data(Map.of(
                            "type", type,
                            "content", data,
                            "timestamp", System.currentTimeMillis()
                    ));
            emitter.send(event);
        } catch (IOException e) {
            log.error("发送数据更新失败", e);
            emitter.completeWithError(e);
        }
    }

}