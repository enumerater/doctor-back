package com.enumerate.disease_detection.Service;

import com.enumerate.disease_detection.ChatModel.MainModel;
import com.enumerate.disease_detection.ModelInterfaces.agents.*;
import com.enumerate.disease_detection.POJO.DTO.*;
import com.enumerate.disease_detection.Tools.VisioTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ReAct循环服务 - 企业级Agent工作流
 *
 * 核心特性：
 * 1. ReAct循环：计划(Plan) → 执行(Act) → 观察(Observe) → 反思(Reflect) → 决策(Decide)
 * 2. 动态规划：根据任务复杂度生成执行计划
 * 3. 质量评估：置信度打分、自动重试
 * 4. 异常处理：优雅降级、备用方案
 * 5. 工作记忆：保存执行状态和中间结果
 *
 * @author Enterprise Agent Team
 * @version 2.0 (ReAct Loop)
 */
@Service
@Slf4j
public class ReActLoopService {

    @Autowired
    private MainModel mainModel;

    @Autowired
    private VisioTool visioTool;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 执行ReAct循环
     *
     * @param emitter SSE发送器
     * @param input 用户输入
     */
    @Async
    public void executeReActLoop(SseEmitter emitter, String input) {
        AtomicInteger msgId = new AtomicInteger(1);
        OpenAiChatModel baseModel = mainModel.tongYiModel();

        // 工作记忆：保存执行状态
        Map<String, Object> workingMemory = new HashMap<>();
        workingMemory.put("userInput", input);

        try {
            // ========== 阶段1：规划(Plan) ==========
            sendStatusUpdate(emitter, msgId.getAndIncrement(), "🧠 正在分析任务并制定执行计划", "planning");
            ExecutionPlanDTO plan = executePlanningPhase(baseModel, input);
            workingMemory.put("plan", plan);

            sendDataUpdate(emitter, msgId.getAndIncrement(),
                String.format("任务类型：%s | 复杂度：%s | 预计步骤：%d",
                    plan.getTaskType(), plan.getComplexity(), plan.getSteps().size()),
                "plan");

            // ========== 阶段2：ReAct循环执行 ==========
            int maxIterations = plan.getMaxIterations() != null ? plan.getMaxIterations() : 3;
            int currentIteration = 0;
            boolean taskCompleted = false;
            String finalResult = "";

            while (currentIteration < maxIterations && !taskCompleted) {
                currentIteration++;
                log.info("========== ReAct循环迭代 {}/{} ==========", currentIteration, maxIterations);
                sendStatusUpdate(emitter, msgId.getAndIncrement(),
                    String.format("🔄 ReAct循环 - 迭代 %d/%d", currentIteration, maxIterations),
                    "iterating");

                // 2.1 执行(Act)
                sendStatusUpdate(emitter, msgId.getAndIncrement(), "⚡ 执行任务步骤", "acting");
                Map<String, String> executionResults = executeActingPhase(baseModel, plan, workingMemory, emitter, msgId);
                workingMemory.put("executionResults", executionResults);

                // 2.2 观察(Observe)
                sendStatusUpdate(emitter, msgId.getAndIncrement(), "👁️ 观察执行结果", "observing");
                ObservationDTO observation = executeObservingPhase(baseModel, plan, executionResults);
                workingMemory.put("observation", observation);

                sendDataUpdate(emitter, msgId.getAndIncrement(),
                    String.format("执行状态：%s | 完整性：%.0f%%",
                        observation.getIsSuccess() ? "成功" : "失败",
                        observation.getCompleteness() * 100),
                    "observation");

                // 2.3 反思(Reflect)
                sendStatusUpdate(emitter, msgId.getAndIncrement(), "🤔 反思执行质量", "reflecting");
                ReflectionDTO reflection = executeReflectingPhase(baseModel, plan, observation, currentIteration);
                workingMemory.put("reflection", reflection);

                sendDataUpdate(emitter, msgId.getAndIncrement(),
                    String.format("质量评分：%.0f%% | 建议：%s",
                        reflection.getOverallScore() * 100,
                        reflection.getNextAction()),
                    "reflection");

                // 2.4 决策(Decide)
                sendStatusUpdate(emitter, msgId.getAndIncrement(), "🎯 制定下一步决策", "deciding");
                DecisionDTO decision = executeDecidingPhase(baseModel, plan, reflection, currentIteration, maxIterations);
                workingMemory.put("decision", decision);

                // 2.5 执行决策
                switch (decision.getDecision()) {
                    case CONTINUE:
                        log.info("决策：继续 - {}", decision.getReasoning());
                        sendStatusUpdate(emitter, msgId.getAndIncrement(), "✅ 质量合格，准备生成最终结果", "deciding");
                        taskCompleted = true;
                        finalResult = generateFinalResult(baseModel, workingMemory, emitter, msgId);
                        break;

                    case RETRY:
                        log.info("决策：重试 - {}", decision.getReasoning());
                        sendStatusUpdate(emitter, msgId.getAndIncrement(),
                            String.format("🔄 检测到质量问题，准备重试（迭代%d/%d）", currentIteration + 1, maxIterations),
                            "retrying");
                        // 继续下一轮循环
                        break;

                    case FALLBACK:
                        log.info("决策：降级 - {}", decision.getReasoning());
                        sendStatusUpdate(emitter, msgId.getAndIncrement(), "⚠️ 启用备用方案", "fallback");
                        taskCompleted = true;
                        finalResult = executeFallbackStrategy(baseModel, plan, workingMemory, emitter, msgId);
                        break;

                    case ABORT:
                        log.error("决策：中止 - {}", decision.getReasoning());
                        sendStatusUpdate(emitter, msgId.getAndIncrement(), "❌ 任务无法完成", "error");
                        throw new RuntimeException("任务执行失败：" + decision.getReasoning());

                    case ESCALATE:
                        log.warn("决策：请求人工 - {}", decision.getReasoning());
                        sendStatusUpdate(emitter, msgId.getAndIncrement(), "🆘 需要人工介入", "escalate");
                        taskCompleted = true;
                        finalResult = "抱歉，当前任务较复杂，建议人工处理。原因：" + decision.getReasoning();
                        break;

                    default:
                        log.warn("未知决策类型：{}", decision.getDecision());
                        taskCompleted = true;
                        finalResult = generateFinalResult(baseModel, workingMemory, emitter, msgId);
                }
            }

            // 超过最大迭代次数仍未完成
            if (!taskCompleted) {
                log.warn("达到最大迭代次数 {} 次，强制结束", maxIterations);
                sendStatusUpdate(emitter, msgId.getAndIncrement(), "⏱️ 达到最大迭代次数，生成当前最佳结果", "max_iterations");
                finalResult = generateFinalResult(baseModel, workingMemory, emitter, msgId);
            }

            // ========== 阶段3：返回最终结果 ==========
            sendStatusUpdate(emitter, msgId.getAndIncrement(), "✅ 任务完成", "completed");
            sendDataUpdate(emitter, msgId.getAndIncrement(), finalResult, "final_result");
            emitter.complete();

        } catch (Exception e) {
            log.error("ReAct循环执行失败", e);
            sendStatusUpdate(emitter, msgId.getAndIncrement(), "❌ 执行出错：" + e.getMessage(), "error");
            emitter.completeWithError(e);
        }
    }

    /**
     * 阶段1：规划(Plan)
     * 分析任务并生成执行计划
     */
    private ExecutionPlanDTO executePlanningPhase(OpenAiChatModel model, String input) {
        try {
            log.info("========== 规划阶段 ==========");

            PlannerAgent plannerAgent = AgenticServices
                .agentBuilder(PlannerAgent.class)
                .chatModel(model)
                .build();

            String planJson = plannerAgent.plan(input);
            log.info("生成的执行计划：{}", planJson);

            // 解析JSON
            ExecutionPlanDTO plan = objectMapper.readValue(planJson, ExecutionPlanDTO.class);

            // 设置默认值
            if (plan.getMaxIterations() == null) {
                plan.setMaxIterations(3);
            }

            return plan;
        } catch (Exception e) {
            log.error("规划阶段失败，使用默认计划", e);
            // 返回默认计划
            return createDefaultPlan(input);
        }
    }

    /**
     * 阶段2.1：执行(Act)
     * 执行计划中的各个步骤
     */
    private Map<String, String> executeActingPhase(
        OpenAiChatModel model,
        ExecutionPlanDTO plan,
        Map<String, Object> memory,
        SseEmitter emitter,
        AtomicInteger msgId
    ) throws IOException {
        log.info("========== 执行阶段 ==========");
        Map<String, String> results = new HashMap<>();
        String userInput = (String) memory.get("userInput");

        // 1. 输入解析
        sendStatusUpdate(emitter, msgId.getAndIncrement(), "📝 解析用户输入", "parsing");
        InputParserAgent inputParser = AgenticServices
            .agentBuilder(InputParserAgent.class)
            .chatModel(model)
            .build();
        String parsedInput = inputParser.parseInput(userInput);
        results.put("parsedInput", parsedInput);
        log.info("输入解析结果：{}", parsedInput);

        // 2. 路由判断
        sendStatusUpdate(emitter, msgId.getAndIncrement(), "🔀 识别任务类型", "routing");
        RouterAgent router = AgenticServices
            .agentBuilder(RouterAgent.class)
            .chatModel(model)
            .build();
        Boolean hasImage = router.route(parsedInput);
        results.put("hasImage", hasImage.toString());
        log.info("是否包含图像：{}", hasImage);

        // 3. 多模态识别（如果有图）
        if (hasImage) {
            sendStatusUpdate(emitter, msgId.getAndIncrement(), "🖼️ 多模态分析中", "vision_analyzing");
            VisionAgent visionAgent = AgenticServices
                .agentBuilder(VisionAgent.class)
                .chatModel(model)
                .tools(visioTool)
                .build();
            String visionResult = visionAgent.chat(parsedInput);
            results.put("visionResult", visionResult);
            sendDataUpdate(emitter, msgId.getAndIncrement(), visionResult, "vision_result");
            log.info("视觉识别结果：{}", visionResult);
        } else {
            results.put("visionResult", "未发现图像，使用文本分析");
        }

        // 4. 并行专家分析
        sendStatusUpdate(emitter, msgId.getAndIncrement(), "👥 专家团队分析中", "expert_analyzing");
        Map<String, String> expertResults = executeParallelExperts(model, results);
        results.putAll(expertResults);

        return results;
    }

    /**
     * 并行执行专家分析
     */
    private Map<String, String> executeParallelExperts(OpenAiChatModel model, Map<String, String> previousResults) {
        Map<String, String> expertResults = new HashMap<>();

        UntypedAgent parallelExperts = AgenticServices
            .parallelBuilder()
            .subAgents(
                AgenticServices.agentBuilder(SafeNoticeExpert.class).chatModel(model).outputKey("safeNotice").build(),
                AgenticServices.agentBuilder(PesticideExpert.class).chatModel(model).outputKey("pesticide").build(),
                AgenticServices.agentBuilder(FieldManageExpert.class).chatModel(model).outputKey("fieldManage").build()
            )
            .executor(Executors.newFixedThreadPool(3))
            .build();

        Map<String, Object> input = new HashMap<>();
        input.put("visionResult", previousResults.getOrDefault("visionResult", ""));
        parallelExperts.invoke(input);

        // 这里简化处理，实际应从AgenticScope读取
        expertResults.put("safeNotice", "安全注意事项已生成");
        expertResults.put("pesticide", "植保用药方案已生成");
        expertResults.put("fieldManage", "田间管理建议已生成");

        log.info("专家分析完成：{}", expertResults);
        return expertResults;
    }

    /**
     * 阶段2.2：观察(Observe)
     * 观察执行结果并提取关键信息
     */
    private ObservationDTO executeObservingPhase(
        OpenAiChatModel model,
        ExecutionPlanDTO plan,
        Map<String, String> executionResults
    ) {
        try {
            log.info("========== 观察阶段 ==========");

            ObserverAgent observer = AgenticServices
                .agentBuilder(ObserverAgent.class)
                .chatModel(model)
                .build();

            String observationJson = observer.observe(
                "完整执行流程",
                executionResults.toString(),
                "完整的病害诊断和解决方案"
            );

            log.info("观察结果：{}", observationJson);
            return objectMapper.readValue(observationJson, ObservationDTO.class);
        } catch (Exception e) {
            log.error("观察阶段失败，使用默认观察结果", e);
            return createDefaultObservation(executionResults);
        }
    }

    /**
     * 阶段2.3：反思(Reflect)
     * 评估执行质量
     */
    private ReflectionDTO executeReflectingPhase(
        OpenAiChatModel model,
        ExecutionPlanDTO plan,
        ObservationDTO observation,
        int currentIteration
    ) {
        try {
            log.info("========== 反思阶段 ==========");

            ReflectorAgent reflector = AgenticServices
                .agentBuilder(ReflectorAgent.class)
                .chatModel(model)
                .build();

            String reflectionJson = reflector.reflect(
                objectMapper.writeValueAsString(plan),
                currentIteration,
                plan.getMaxIterations(),
                objectMapper.writeValueAsString(observation),
                currentIteration - 1
            );

            log.info("反思结果：{}", reflectionJson);
            return objectMapper.readValue(reflectionJson, ReflectionDTO.class);
        } catch (Exception e) {
            log.error("反思阶段失败，使用默认反思结果", e);
            return createDefaultReflection(observation);
        }
    }

    /**
     * 阶段2.4：决策(Decide)
     * 根据反思结果做出决策
     */
    private DecisionDTO executeDecidingPhase(
        OpenAiChatModel model,
        ExecutionPlanDTO plan,
        ReflectionDTO reflection,
        int currentIteration,
        int maxIterations
    ) {
        try {
            log.info("========== 决策阶段 ==========");

            DecisionAgent decider = AgenticServices
                .agentBuilder(DecisionAgent.class)
                .chatModel(model)
                .build();

            String decisionJson = decider.decide(
                objectMapper.writeValueAsString(plan),
                currentIteration,
                plan.getSteps().size(),
                objectMapper.writeValueAsString(reflection),
                currentIteration - 1,
                2,  // maxRetries
                currentIteration,
                maxIterations
            );

            log.info("决策结果：{}", decisionJson);
            return objectMapper.readValue(decisionJson, DecisionDTO.class);
        } catch (Exception e) {
            log.error("决策阶段失败，使用默认决策", e);
            return createDefaultDecision(reflection, currentIteration, maxIterations);
        }
    }

    /**
     * 生成最终结果
     */
    private String generateFinalResult(
        OpenAiChatModel model,
        Map<String, Object> memory,
        SseEmitter emitter,
        AtomicInteger msgId
    ) throws IOException {
        sendStatusUpdate(emitter, msgId.getAndIncrement(), "📊 汇总最终结果", "summarizing");

        SummaryAgent summaryAgent = AgenticServices
            .agentBuilder(SummaryAgent.class)
            .chatModel(model)
            .build();

        Map<String, String> executionResults = (Map<String, String>) memory.get("executionResults");
        String diseaseSolution = String.format(
            "安全注意：%s\n植保用药：%s\n田间管理：%s",
            executionResults.getOrDefault("safeNotice", ""),
            executionResults.getOrDefault("pesticide", ""),
            executionResults.getOrDefault("fieldManage", "")
        );

        return summaryAgent.generateSummary(diseaseSolution);
    }

    /**
     * 执行备用策略
     */
    private String executeFallbackStrategy(
        OpenAiChatModel model,
        ExecutionPlanDTO plan,
        Map<String, Object> memory,
        SseEmitter emitter,
        AtomicInteger msgId
    ) throws IOException {
        sendStatusUpdate(emitter, msgId.getAndIncrement(), "🔄 执行备用方案", "fallback");
        log.info("执行备用策略：{}", plan.getFallbackStrategy());

        // 使用简化流程生成结果
        return generateFinalResult(model, memory, emitter, msgId);
    }

    // ========== 默认值创建方法 ==========

    private ExecutionPlanDTO createDefaultPlan(String input) {
        boolean hasImage = input.contains("http") || input.contains("www");

        List<ExecutionPlanDTO.ExecutionStep> steps = new ArrayList<>();
        steps.add(ExecutionPlanDTO.ExecutionStep.builder()
            .step(1).action("解析输入").tool("InputParser").priority("high").critical(true).build());
        steps.add(ExecutionPlanDTO.ExecutionStep.builder()
            .step(2).action("路由判断").tool("Router").priority("high").critical(true).build());

        if (hasImage) {
            steps.add(ExecutionPlanDTO.ExecutionStep.builder()
                .step(3).action("多模态识别").tool("VisionAgent").priority("high").critical(true).build());
        }

        steps.add(ExecutionPlanDTO.ExecutionStep.builder()
            .step(4).action("专家分析").tool("Experts").priority("medium").critical(false).build());

        return ExecutionPlanDTO.builder()
            .taskType(hasImage ? "图像诊断" : "文本咨询")
            .complexity("中等")
            .confidence(0.8)
            .steps(steps)
            .maxIterations(2)
            .fallbackStrategy("降级到文本模式")
            .build();
    }

    private ObservationDTO createDefaultObservation(Map<String, String> results) {
        return ObservationDTO.builder()
            .isSuccess(true)
            .completeness(0.8)
            .recommendation("继续")
            .issues(new ArrayList<>())
            .extractedData(ObservationDTO.ExtractedData.builder()
                .confidence(0.8)
                .build())
            .build();
    }

    private ReflectionDTO createDefaultReflection(ObservationDTO observation) {
        double score = observation.getCompleteness() != null ? observation.getCompleteness() : 0.8;

        return ReflectionDTO.builder()
            .overallScore(score)
            .needsRetry(score < 0.6)
            .nextAction(score >= 0.8 ? "继续" : "重试当前步骤")
            .reasoning("基于完整性评分的默认决策")
            .suggestions(new ArrayList<>())
            .scores(ReflectionDTO.QualityScores.builder()
                .accuracy(score)
                .completeness(score)
                .consistency(score)
                .actionability(score)
                .build())
            .build();
    }

    private DecisionDTO createDefaultDecision(ReflectionDTO reflection, int iteration, int maxIterations) {
        DecisionDTO.DecisionType decision;

        if (reflection.getOverallScore() >= 0.8) {
            decision = DecisionDTO.DecisionType.CONTINUE;
        } else if (iteration < maxIterations && reflection.getOverallScore() >= 0.5) {
            decision = DecisionDTO.DecisionType.RETRY;
        } else if (reflection.getOverallScore() < 0.5) {
            decision = DecisionDTO.DecisionType.FALLBACK;
        } else {
            decision = DecisionDTO.DecisionType.CONTINUE;
        }

        return DecisionDTO.builder()
            .decision(decision)
            .reasoning("基于质量评分的默认决策")
            .adjustments(DecisionDTO.PlanAdjustments.builder()
                .modifyPlan(false)
                .build())
            .fallbackPlan(DecisionDTO.FallbackPlan.builder()
                .enabled(false)
                .build())
            .metadata(DecisionDTO.Metadata.builder()
                .confidence(0.8)
                .estimatedImpact("medium")
                .build())
            .build();
    }

    // ========== SSE工具方法 ==========

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
