package com.enumerate.disease_detection.Service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.enumerate.disease_detection.ModelInterfaces.Assistant;
import com.enumerate.disease_detection.ChatModel.MainModel;
import com.enumerate.disease_detection.ChatModel.PersistentChatMemoryStore;
import com.enumerate.disease_detection.Mapper.ChatMessageMapper;
import com.enumerate.disease_detection.Mapper.SessionMapper;
import com.enumerate.disease_detection.POJO.PO.ChatMessagePO;
import com.enumerate.disease_detection.POJO.PO.ChatSessionPO;
import com.enumerate.disease_detection.POJO.PO.VectorStorePO;
import com.enumerate.disease_detection.Utils.MysqlEmbeddingStore;
import com.enumerate.disease_detection.Tools.TitleTool;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class ChatService {

    @Autowired
    private MainModel mainModel;

    @Autowired
    private MysqlEmbeddingStore mysqlEmbeddingStore;

    @Autowired
    private ChatMessageMapper chatMessageMapper;


    @Async("aiAsyncExecutor") // 指定使用我们自定义的线程池，精准控制
    public void hello2(SseEmitter emitter, String prompt) {
        Assistant assistant = AiServices.create(Assistant.class,mainModel.streamingModel());

        // 1. 定义线程安全的ID生成器（可选：递增序号 / UUID，二选一即可）
        // 方式1：递增序号（推荐，前端可按序号排序）
        AtomicInteger msgId = new AtomicInteger(1);
        // 方式2：UUID（全局唯一，适合分布式场景）
        // String generateId() { return UUID.randomUUID().toString(); }

        try {
            EmbeddingModel embeddingModel = mainModel.embeddingModel();

            Embedding queryEmbedding = embeddingModel.embed(prompt).content();

            // 5. 相似度搜索（从MySQL中找最相似的向量）
            VectorStorePO mostSimilar = mysqlEmbeddingStore.searchMostSimilar(queryEmbedding);

            String knowledgeContext = mostSimilar.getTextContent() != null ? mostSimilar.getTextContent() : "无知识";

            // 调用你的AI流式接口
            TokenStream tokenStream = assistant.ragChat(knowledgeContext,prompt);

            // 处理分片响应：即时推送
            tokenStream.onPartialResponse(partialContent -> {
                        try {
                            log.info("流式分片内容：{}", partialContent);
                            // SseEmitter天生无缓冲，直接send就是即时推送，无需额外配置
//                            emitter.send(StreamChatMsg.builder().text(partialContent).build());

                            // ========== 关键修改：构建带ID的SSE事件 ==========
                            SseEmitter.SseEventBuilder event = SseEmitter.event()
                                    .id(String.valueOf(msgId.getAndIncrement())) // 设置SSE的ID字段
                                    // .id(generateId()) // 若用UUID则替换这行
                                    .data(partialContent) // 业务数据
                                    .name("data"); // 可选：设置事件名称（前端可按名称监听）；不设则默认是message

                            // 发送带ID的事件（替代原来直接send对象的方式）
                            emitter.send(event);
                        } catch (IOException e) {
                            log.error("AI分片内容推送失败", e);
                            emitter.completeWithError(e);
                        }
                    })
                    // 处理流式完成：正常关闭连接
                    .onCompleteResponse(chatResponse -> {
                        log.info("\n流式响应完成，完整结果：{}", chatResponse);
                        emitter.complete();
                    })
                    // 处理AI请求异常：异常关闭连接
                    .onError(throwable -> {
                        log.error("AI流式请求执行异常", throwable);
                        emitter.completeWithError(throwable);
                    })
                    // 启动流式请求（同步阻塞，仅阻塞当前异步线程，无任何影响）
                    .start();

        } catch (Exception e) {
            log.error("AI流式请求初始化失败", e);
            emitter.completeWithError(e);
        }
    }

    @Async("aiAsyncExecutor") // 指定使用我们自定义的线程池，精准控制
    public void stream(SseEmitter emitter, String prompt) {
        Assistant assistant = AiServices.create(Assistant.class,mainModel.streamingModel());
        AtomicInteger msgId = new AtomicInteger(1);
        try {
            TokenStream tokenStream = assistant.streamChat(prompt);
            tokenStream.onPartialResponse(partialContent -> {
                try {
                    log.info("流式分片内容：{}", partialContent);
                    SseEmitter.SseEventBuilder event = SseEmitter.event()
                            .id(String.valueOf(msgId.getAndIncrement()))
                            .data(partialContent) // 业务数据
                            .name("data");
                    emitter.send(event);
                } catch (IOException e) {
                    log.error("AI分片内容推送失败", e);
                    emitter.completeWithError(e);
                }
            })
            // 处理流式完成：正常关闭连接
            .onCompleteResponse(chatResponse -> {
                log.info("\n流式响应完成，完整结果：{}", chatResponse);
                emitter.complete();
            })
            // 处理AI请求异常：异常关闭连接
            .onError(throwable -> {
                log.error("AI流式请求执行异常", throwable);
                emitter.completeWithError(throwable);
            })
            // 启动流式请求（同步阻塞，仅阻塞当前异步线程，无任何影响）
            .start();
        } catch (Exception e) {
            log.error("AI流式请求初始化失败", e);
            emitter.completeWithError(e);
        }
    }

    @Async("aiAsyncExecutor")
    public void deepThink(SseEmitter emitter, String prompt) {
        log.info("开始执行深度思考");
        Assistant assistant = AiServices.create(Assistant.class,mainModel.deepThinkModel());
        AtomicInteger msgId = new AtomicInteger(1);
        try {
            TokenStream tokenStream = assistant.chatStream(prompt);
            tokenStream.onPartialResponse(partialContent -> {
                        try {
                            log.info("流式分片内容：{}", partialContent);
                            SseEmitter.SseEventBuilder event = SseEmitter.event()
                                    .id(String.valueOf(msgId.getAndIncrement()))
                                    .data(partialContent) // 业务数据
                                    .name("data");
                            emitter.send(event);
                        } catch (IOException e) {
                            log.error("AI分片内容推送失败", e);
                            emitter.completeWithError(e);
                        }
                    })
                    // 处理流式完成：正常关闭连接
                    .onCompleteResponse(chatResponse -> {
                        log.info("\n流式响应完成，完整结果：{}", chatResponse);
                        emitter.complete();
                    })
                    // 处理AI请求异常：异常关闭连接
                    .onError(throwable -> {
                        log.error("AI流式请求执行异常", throwable);
                        emitter.completeWithError(throwable);
                    })
                    // 启动流式请求（同步阻塞，仅阻塞当前异步线程，无任何影响）
                    .start();
        } catch (Exception e) {
            log.error("AI流式请求初始化失败", e);
            emitter.completeWithError(e);
        }
    }

    @Autowired
    private PersistentChatMemoryStore persistentChatMemoryStore;
    @Autowired
    private SessionMapper sessionMapper;
    @Autowired
    private TitleTool titleTool;

    @Async("aiAsyncExecutor")
    public void memory(SseEmitter emitter, String prompt, Long userId, Long sessionId) {
        log.info("开始执行记忆对话");

        String sessionTitle = titleTool.summarizeConversationTopic(prompt);
        // 1. 先定义要修改的字段和值（推荐用UpdateWrapper的set，避免实体空值问题）
        UpdateWrapper<ChatSessionPO> updateWrapper = new UpdateWrapper<>();
        // 条件：session_id = 拼接后的值（先拼接成变量，方便调试）
        String targetSessionId = userId + String.valueOf(sessionId);
        updateWrapper.eq("session_id", targetSessionId);
        // 设置要修改的字段（直接在Wrapper中set，更稳妥）
        updateWrapper.set("session_title", sessionTitle);

        // 2. 执行更新（第一个参数传null，所有修改字段都在Wrapper中定义）
        sessionMapper.update(null, updateWrapper);


        OpenAiStreamingChatModel model = mainModel.streamingModel();

        Assistant assistant = AiServices.builder(Assistant.class)
                .streamingChatModel(model)
                // 关键修改：provider的入参memoryId就是调用chatMemory时传入的customMemoryId
                .chatMemoryProvider(memoryId -> {
                    // 每次调用都为当前memoryId构建专属的ChatMemory实例
                    return MessageWindowChatMemory.builder()
                            .maxMessages(10)
                            .chatMemoryStore(persistentChatMemoryStore)
                            .id(memoryId) // 把传入的memoryId绑定到ChatMemory实例上！！！
                            .build();
                })
                .build();

        AtomicInteger msgId = new AtomicInteger(1);
        try {
            log.info("开始执行记忆对话{}", userId + String.valueOf(sessionId));
            TokenStream tokenStream = assistant.chatMemory(userId + String.valueOf(sessionId) ,prompt);

            chatMessageMapper.insert(ChatMessagePO.builder()
                    .id(null)
                    .sessionId(userId + String.valueOf(sessionId))
                    .messageRole("0")
                    .messageContent(prompt)
                    .messageTime(LocalDateTime.now())
                    .deleted("0")
                    .build());


            tokenStream.onPartialResponse(partialContent -> {
                        try {
                            log.info("流式分片内容：{}", partialContent);
                            SseEmitter.SseEventBuilder event = SseEmitter.event()
                                    .id(String.valueOf(msgId.getAndIncrement()))
                                    .data(partialContent) // 业务数据
                                    .name("data");
                            emitter.send(event);
                        } catch (IOException e) {
                            log.error("AI分片内容推送失败", e);
                            emitter.completeWithError(e);
                        }
                    })
                    // 处理流式完成：正常关闭连接
                    .onCompleteResponse(chatResponse -> {
                        log.info("\n流式响应完成，完整结果：{}", chatResponse);
                        chatMessageMapper.insert(ChatMessagePO.builder()
                                .id(null)
                                .sessionId(userId + String.valueOf(sessionId))
                                .messageRole("1")
                                .messageContent(chatResponse.aiMessage().text())
                                .messageTime(LocalDateTime.now())
                                .deleted("0")
                                .build()
                        );

                        emitter.complete();
                    })
                    // 处理AI请求异常：异常关闭连接
                    .onError(throwable -> {
                        log.error("AI流式请求执行异常", throwable);
                        emitter.completeWithError(throwable);
                    })
                    // 启动流式请求（同步阻塞，仅阻塞当前异步线程，无任何影响）
                    .start();
        } catch (Exception e) {
            log.error("AI流式请求初始化失败", e);
            emitter.completeWithError(e);
        }


    }
}
