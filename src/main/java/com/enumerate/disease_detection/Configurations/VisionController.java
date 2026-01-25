package com.enumerate.disease_detection.Configurations;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.enumerate.disease_detection.ChatModel.MainModel;

import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.Properties.AiModelProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;


@RestController
@RequestMapping("/vision")
@Slf4j
public class VisionController {

    @Autowired
    private MainModel mainModel;


    @Autowired
    private AiModelProperties aiModelProperties;

    @GetMapping
    @CrossOrigin
    public com.enumerate.disease_detection.Common.Result<String> vision(@RequestParam(value = "url", required = false) String url,
                                                                @RequestParam(value = "cropType", required = false) String cropType) throws NoApiKeyException, UploadFileException {

        String text = "请基于图片内容，给出" + cropType + "的识别结果，并给出识别结果的解释。简要回复即可";

        MultiModalConversation conv = new MultiModalConversation();
        MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
                .content(Arrays.asList(
                        Collections.singletonMap("image", url),
                        Collections.singletonMap("text", text))).build();
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
                // 各地域的API Key不同。获取API Key：https://help.aliyun.com/zh/model-studio/get-api-key
                .apiKey(aiModelProperties.getTong().getApiKey())
                .model("qwen3-vl-plus")  // 此处以qwen3-vl-plus为例，可按需更换模型名称。模型列表：https://help.aliyun.com/zh/model-studio/models
                .messages(Collections.singletonList(userMessage))
                .build();
        MultiModalConversationResult result = conv.call(param);

        log.info("dashScope result: {}", result);
        log.info("dashScope result: {}", result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text"));


        return Result.success(result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text").toString());
    };
}
