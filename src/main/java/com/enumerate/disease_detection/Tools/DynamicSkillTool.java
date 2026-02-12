package com.enumerate.disease_detection.Tools;

import com.enumerate.disease_detection.POJO.DTO.SkillDefinitionDTO;
import com.enumerate.disease_detection.Utils.SkillApiClient;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 动态Skill工具
 * 根据Skill定义动态调用Python FastAPI接口
 */
@Slf4j
public class DynamicSkillTool {

    private final SkillDefinitionDTO skillDefinition;

    public DynamicSkillTool(SkillDefinitionDTO skillDefinition) {
        this.skillDefinition = skillDefinition;
    }

    /**
     * 执行Skill
     *
     * @param params 参数Map（key为参数名，value为参数值）
     * @return 执行结果
     */
    public String execute(Map<String, Object> params) {
        try {
            log.info("执行Skill: {} | 参数: {}", skillDefinition.getName(), params);

            SkillDefinitionDTO.ApiEndpointConfig apiConfig = skillDefinition.getApiConfig();
            int timeout = apiConfig.getTimeout() != null ? apiConfig.getTimeout() : 30;

            String response;
            String method = apiConfig.getMethod().toUpperCase();

            switch (method) {
                case "GET":
                    response = SkillApiClient.callGetApi(apiConfig.getEndpoint(), params, timeout);
                    break;
                case "POST":
                    response = SkillApiClient.callPostApi(apiConfig.getEndpoint(), params, timeout);
                    break;
                case "STREAM":
                    response = SkillApiClient.callStreamApi(apiConfig.getEndpoint(), params, timeout);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的HTTP方法: " + method);
            }

            // 提取结果
            String result = SkillApiClient.extractResult(response);
            log.info("Skill执行成功: {} | 结果: {}", skillDefinition.getName(), result);

            return result;

        } catch (Exception e) {
            log.error("Skill执行失败: {}", skillDefinition.getName(), e);
            return String.format("执行失败：%s", e.getMessage());
        }
    }

    /**
     * 获取工具描述（供LLM理解）
     *
     * @return 工具描述
     */
    public String getToolDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append(skillDefinition.getDescription());

        if (skillDefinition.getApiConfig() != null && skillDefinition.getApiConfig().getParams() != null) {
            desc.append("\n参数：");
            for (SkillDefinitionDTO.ParamDefinition param : skillDefinition.getApiConfig().getParams()) {
                desc.append("\n- ").append(param.getName())
                        .append(" (").append(param.getType()).append(")")
                        .append(param.getRequired() ? " [必填]" : " [可选]")
                        .append(": ").append(param.getDescription());
            }
        }

        if (skillDefinition.getTriggers() != null && !skillDefinition.getTriggers().isEmpty()) {
            desc.append("\n适用场景：").append(String.join("、", skillDefinition.getTriggers()));
        }

        return desc.toString();
    }

    public SkillDefinitionDTO getSkillDefinition() {
        return skillDefinition;
    }
}
