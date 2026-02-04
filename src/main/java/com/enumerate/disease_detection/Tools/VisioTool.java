package com.enumerate.disease_detection.Tools;

import com.enumerate.disease_detection.ChatModel.MainModel;
import com.enumerate.disease_detection.ModelInterfaces.VisionAssisant;
import com.enumerate.disease_detection.POJO.VO.VisionVO;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class VisioTool {

    @Autowired
    private MainModel mainModel;

    @Tool("视觉模型工具")
    public String visionTool(@P("imageUrl")  String imageUrl,@P("作物类型") String cropType) {
        log.info("工具调用: 视觉模型工具，参数: imageUrl={}, cropType={}", imageUrl, cropType);
        
        VisionAssisant openAiChatModel = AiServices.create(VisionAssisant.class,mainModel.visionModel());

        String prompt = "简洁回答！！！理解图片，输出农业病害诊断结果，用户提供物种是" + cropType;

        // 2. 构建包含图片+文本的UserMessage
        List<Content> contents = new ArrayList<>();
        // 添加图片内容（URL形式）
        contents.add(ImageContent.from(imageUrl));
        // 添加文本问题
        contents.add(TextContent.from(prompt));
        // 构建UserMessage（无name，仅包含多模态内容）
        UserMessage userMessage = new UserMessage(contents);

        String result = openAiChatModel.chat(userMessage);
        log.info("工具结果: 视觉模型工具，结果: {}", result);
        return result;
    }
    
    // 使用Slf4j日志记录
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VisioTool.class);
}