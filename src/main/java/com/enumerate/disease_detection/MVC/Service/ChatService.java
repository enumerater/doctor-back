package com.enumerate.disease_detection.MVC.Service;

import com.enumerate.disease_detection.MVC.POJO.PO.ChatMessagePO;
import com.enumerate.disease_detection.ModelInterfaces.Assistant;
import com.enumerate.disease_detection.ChatModel.MainModel;
import com.enumerate.disease_detection.ChatModel.PersistentChatMemoryStore;
import com.enumerate.disease_detection.MVC.Mapper.ChatMessageMapper;

import com.enumerate.disease_detection.Properties.AiModelProperties;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
@Slf4j
public class ChatService {

    private static final int STREAM_MAX_RETRIES = 2;

    @Autowired
    private MainModel mainModel;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Resource(name = "tongYiStreamingModel")
    private StreamingChatModel tongYiStreamingModel;

    @Resource(name = "deepThinkModel")
    private StreamingChatModel deepThinkModel;

    @Resource(name = "tongYiPlusStreamingModel")
    private StreamingChatModel tongYiPlusStreamingModel;

    @Resource(name = "tongYiFlashStreamingModel")
    private StreamingChatModel tongYiFlashStreamingModel;

    @Resource(name = "GlmStreamingModel")
    private StreamingChatModel GlmStreamingModel;

    @Resource(name = "deepseekStreamingModel")
    private StreamingChatModel deepseekStreamingModel;


    @Async("aiAsyncExecutor") // 指定使用我们自定义的线程池，精准控制
    public void stream(SseEmitter emitter, String prompt) {
        Assistant assistant = AiServices.create(Assistant.class, tongYiStreamingModel);
        executeStreamWithRetry(() -> assistant.streamChat(prompt), emitter, null);
    }

    @Async("aiAsyncExecutor")
    public void deepThink(SseEmitter emitter, String prompt) {
        log.info("开始执行深度思考");
        Assistant assistant = AiServices.create(Assistant.class, deepThinkModel);
        executeStreamWithRetry(() -> assistant.chatStream(prompt), emitter, null);
    }

    @Autowired
    private PersistentChatMemoryStore persistentChatMemoryStore;


    @Async
    public void memory(SseEmitter emitter, String prompt, String image, Long userId, Long sessionId,String modelName) {
        log.info("开始执行记忆对话");

        StreamingChatModel model = switch (modelName) {
            case "qwen-flash" -> tongYiStreamingModel;
            case "qwen3.5-flash" -> (OpenAiStreamingChatModel) tongYiFlashStreamingModel;
            case "qwen3.5-plus" -> (OpenAiStreamingChatModel) tongYiPlusStreamingModel;
            case "DeepSeek-V3.2" -> (OpenAiStreamingChatModel) deepseekStreamingModel;
            case "glm-5" -> (OpenAiStreamingChatModel) GlmStreamingModel;
            default -> (OpenAiStreamingChatModel) tongYiStreamingModel;
        };


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

        String memoryKey = userId + String.valueOf(sessionId);
        log.info("开始执行记忆对话{}", memoryKey);

        chatMessageMapper.insert(ChatMessagePO.builder()
                .id(null)
                .sessionId(memoryKey)
                .messageRole("0")
                .messageContent(prompt)
                .messageTime(LocalDateTime.now())
                .deleted("0")
                .build());

        executeStreamWithRetry(
                () -> assistant.chatMemory(memoryKey, prompt),
                emitter,
                chatResponse -> chatMessageMapper.insert(ChatMessagePO.builder()
                        .id(null)
                        .sessionId(memoryKey)
                        .messageRole("1")
                        .messageContent(chatResponse.aiMessage().text())
                        .messageTime(LocalDateTime.now())
                        .deleted("0")
                        .build())
        );
    }

    // ==================== 流式重试工具方法 ====================

    /**
     * 执行流式请求，遇到 Connection reset 自动重试
     *
     * @param streamSupplier 创建 TokenStream 的工厂（重试时会重新调用）
     * @param emitter        SSE 推送器
     * @param onComplete     流式完成后的额外回调（可为null），在 emitter.complete() 之前执行
     */
    private void executeStreamWithRetry(Supplier<TokenStream> streamSupplier,
                                        SseEmitter emitter,
                                        Consumer<ChatResponse> onComplete) {
        AtomicInteger msgId = new AtomicInteger(1);
        AtomicInteger retryCount = new AtomicInteger(0);
        doStartStream(streamSupplier, emitter, msgId, retryCount, onComplete);
    }

    private void doStartStream(Supplier<TokenStream> streamSupplier,
                               SseEmitter emitter,
                               AtomicInteger msgId,
                               AtomicInteger retryCount,
                               Consumer<ChatResponse> onComplete) {
        try {
            TokenStream tokenStream = streamSupplier.get();
            tokenStream.onPartialResponse(partialContent -> {
                        try {
                            SseEmitter.SseEventBuilder event = SseEmitter.event()
                                    .id(String.valueOf(msgId.getAndIncrement()))
                                    .data(partialContent)
                                    .name("data");
                            emitter.send(event);
                        } catch (IOException e) {
                            log.error("AI分片内容推送失败", e);
                            emitter.completeWithError(e);
                        }
                    })
                    .onCompleteResponse(chatResponse -> {
                        log.info("流式响应完成，完整结果：{}", chatResponse);
                        if (onComplete != null) {
                            onComplete.accept(chatResponse);
                        }
                        emitter.complete();
                    })
                    .onError(throwable -> {
                        if (isConnectionReset(throwable) && retryCount.getAndIncrement() < STREAM_MAX_RETRIES) {
                            log.warn("流式请求遇到Connection reset，第{}次重试", retryCount.get());
                            doStartStream(streamSupplier, emitter, msgId, retryCount, onComplete);
                        } else {
                            log.error("AI流式请求执行异常", throwable);
                            emitter.completeWithError(throwable);
                        }
                    })
                    .start();
        } catch (Exception e) {
            if (isConnectionReset(e) && retryCount.getAndIncrement() < STREAM_MAX_RETRIES) {
                log.warn("流式请求初始化遇到Connection reset，第{}次重试", retryCount.get());
                doStartStream(streamSupplier, emitter, msgId, retryCount, onComplete);
            } else {
                log.error("AI流式请求初始化失败", e);
                emitter.completeWithError(e);
            }
        }
    }

    /**
     * 判断异常链中是否包含 Connection reset
     */
    private boolean isConnectionReset(Throwable t) {
        while (t != null) {
            if (t instanceof SocketException && t.getMessage() != null
                    && t.getMessage().contains("Connection reset")) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }
}
