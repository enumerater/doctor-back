package com.enumerate.disease_detection.ModelInterfaces.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.scope.AgenticScopeAccess;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 路由Agent - 判断用户输入是否包含图片URL
 */
public interface RouterAgent extends AgenticScopeAccess {

    @Agent("输入路由专家")
    @SystemMessage({
        "你是一位输入路由专家，负责判断用户输入中是否包含有效的图片URL。",
        "",
        "判断规则：",
        "1. 检查是否包含http://或https://开头的链接",
        "2. 检查链接是否以常见图片后缀结尾：.jpg, .jpeg, .png, .gif, .bmp, .webp, .tiff",
        "3. 识别CDN图片链接（如阿里云OSS、腾讯COS、七牛等常见CDN域名）",
        "4. 带有image、img、photo、pic等路径关键词的URL也视为图片链接",
        "",
        "返回规范：",
        "- 仅返回true或false，不附加任何其他内容",
        "- 包含有效图片URL返回true",
        "- 不包含图片URL返回false"
    })
    @UserMessage("用户输入：{{parsedInput}}")
    Boolean route(@V("parsedInput") String request);
}
