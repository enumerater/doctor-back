package com.enumerate.disease_detection.Controller;

import com.enumerate.disease_detection.ChatModel.MainModel;

import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.POJO.VO.VisionVO;
import com.enumerate.disease_detection.Properties.AiModelProperties;
import com.enumerate.disease_detection.VisionAssisant;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.service.AiServices;
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
    public Result<VisionVO> vision(@RequestParam(value = "url", required = false) String url,
                                   @RequestParam(value = "cropType", required = false) String cropType) {

        VisionAssisant openAiChatModel = AiServices.create(VisionAssisant.class,mainModel.visionModel());

        String prompt = "理解图片，输出农业病害诊断结果，用户提供物种是" + cropType + "用户可能提供错误类型" +
            "严格匹配 Java 实体 VisionVO 结构：\n" +
            "- diseaseName（字符串）：病害名称\n" +
            "- confidence（0-100整数）：识别置信度\n" +
            "- severity（仅轻微/中度/重度）：病害等级\n" +
            "- symptoms（字符串列表）：病害症状\n" +
            "- prevention（嵌套结构）：\n" +
            "  - agricultural（字符串列表）：农业防治\n" +
            "  - chemical（字符串列表）：化学防治\n" +
            "  - biological（字符串列表）：生物防治\n" +
            "- notes（字符串列表）：注意事项\n" +
            "\n" +
            "仅输出上述字段的内容，按字段名分条列出，列表项用数字序号开头，无多余文字。";

        // 2. 构建包含图片+文本的UserMessage
        List<Content> contents = new ArrayList<>();
        // 添加图片内容（URL形式）
        contents.add(ImageContent.from(url));
        // 添加文本问题
        contents.add(TextContent.from(prompt));
        // 构建UserMessage（无name，仅包含多模态内容）
        UserMessage userMessage = new UserMessage(contents);

        VisionVO res = openAiChatModel.visionChat(userMessage);



        log.info("================{}",res);



        return Result.success(res);
    }



}
