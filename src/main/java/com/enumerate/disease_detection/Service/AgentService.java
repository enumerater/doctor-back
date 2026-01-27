package com.enumerate.disease_detection.Service;

import com.enumerate.disease_detection.ChatModel.MainModel;
import com.enumerate.disease_detection.ChatModel.PersistentChatMemoryStore;
import com.enumerate.disease_detection.Mapper.ChatMessageMapper;
import com.enumerate.disease_detection.POJO.PO.ChatMessagePO;
import com.enumerate.disease_detection.Tools.AddTool;
import com.enumerate.disease_detection.Tools.MutiTool;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.enumerate.disease_detection.ModelInterfaces.Agent;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class AgentService {


    @Autowired
    private MainModel mainModel;

    @Autowired
    private PersistentChatMemoryStore persistentChatMemoryStore;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private AddTool addTool;

    @Autowired
    private MutiTool mutiTool;

    @Async("aiAsyncExecutor")
    public void agent(SseEmitter emitter, String prompt, Long userId, Long sessionId) {
        log.info("开始执行think-execute");

        OpenAiStreamingChatModel model = mainModel.streamingModel();

        Agent agent = AiServices.builder(Agent.class)
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
                .tools(addTool, mutiTool)
                .build();

        AtomicInteger msgId = new AtomicInteger(1);
        try {
            log.info("开始执行记忆对话{}", userId + String.valueOf(sessionId));
            TokenStream tokenStream = agent.brain(userId + String.valueOf(sessionId) ,prompt);

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
