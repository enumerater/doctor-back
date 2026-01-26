package com.enumerate.disease_detection.Controller;


import com.enumerate.disease_detection.ChatModel.MainModel;

import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.Properties.AiModelProperties;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/vision")
@Slf4j
public class VisionController {

    @Autowired
    private MainModel mainModel;


    @Autowired
    private AiModelProperties aiModelProperties;

//    @GetMapping
//    @CrossOrigin
//    public com.enumerate.disease_detection.Common.Result<String> vision(@RequestParam(value = "url", required = false) String url,
//                                                                @RequestParam(value = "cropType", required = false) String cropType) throws NoApiKeyException, UploadFileException {
//
//        String text = "请基于图片内容，给出" + cropType + "的识别结果，并给出识别结果的解释。简要回复即可";
//
//        MultiModalConversation conv = new MultiModalConversation();
//        MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
//                .content(Arrays.asList(
//                        Collections.singletonMap("image", url),
//                        Collections.singletonMap("text", text))).build();
//        MultiModalConversationParam param = MultiModalConversationParam.builder()
//                // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
//                // 各地域的API Key不同。获取API Key：https://help.aliyun.com/zh/model-studio/get-api-key
//                .apiKey(aiModelProperties.getTong().getApiKey())
//                .model("qwen3-vl-plus")  // 此处以qwen3-vl-plus为例，可按需更换模型名称。模型列表：https://help.aliyun.com/zh/model-studio/models
//                .messages(Collections.singletonList(userMessage))
//                .build();
//        MultiModalConversationResult result = conv.call(param);
//
//        log.info("dashScope result: {}", result);
//        log.info("dashScope result: {}", result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text"));
//
//
//        return Result.success(result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text").toString());
//    };

    @GetMapping
    @CrossOrigin
    public Result<String> vision(@RequestParam(value = "url", required = false) String url,
                                                                @RequestParam(value = "cropType", required = false) String cropType) {
        OpenAiChatModel openAiChatModel = mainModel.qwen3VlStreamingModel();

        // 2. 构建包含图片+文本的UserMessage
        List<Content> contents = new ArrayList<>();
        // 添加图片内容（URL形式）
        contents.add(ImageContent.from("https://img.alicdn.com/imgextra/i1/O1CN01gDEY8M1W114Hi3XcN_!!6000000002727-0-tps-1024-406.jpg"));
        // 添加文本问题
        contents.add(TextContent.from("这道题怎么解答？"));
        // 构建UserMessage（无name，仅包含多模态内容）
        UserMessage userMessage = new UserMessage(contents);

        ChatResponse chat = openAiChatModel.chat(userMessage);

        log.info("============{}",chat);



        return Result.success("res");
    }



}
