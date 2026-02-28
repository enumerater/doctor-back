package com.enumerate.disease_detection.MVC.Service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.enumerate.disease_detection.ChatModel.MainModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@Slf4j
public class TestService {

    @Autowired
    private MainModel mainModel;

    public String test() {
        log.info("=== test service start ===");
        return "test service end";
    }

    public String dashScope() throws NoApiKeyException, InputRequiredException {
        log.info("=== dashScope service start ===");
        Generation gen = new Generation();

        Message userMsg = Message.builder().role(Role.USER.getValue()).content("你是谁？").build();

        GenerationParam param = GenerationParam.builder()
                // 若没有配置环境变量，请用阿里云百炼API Key将下行替换为：.apiKey("sk-xxx")
                .apiKey("sk-9c6b5f80e77b4beaaf299d06308d7f9d")
                .model("qwen-plus")
                .enableThinking(true)
                .messages(Arrays.asList(userMsg))
                .resultFormat("message") // ✅ 必须设置为message（强制约束
                .build();

        GenerationResult result = gen.call(param);
        log.info("dashScope result: {}", result);


        return "dashScope service end";
    }
}
