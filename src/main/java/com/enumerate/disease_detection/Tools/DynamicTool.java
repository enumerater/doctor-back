package com.enumerate.disease_detection.Tools;

import com.enumerate.disease_detection.POJO.DTO.ToolDefinitionDTO;
import com.enumerate.disease_detection.Utils.ToolApiClient;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 动态Tool工具
 * 根据Tool定义动态调用Python FastAPI接口
 */
@Slf4j
public class DynamicTool {

    private final ToolDefinitionDTO toolDefinition;

    public DynamicTool(ToolDefinitionDTO toolDefinition) {
        this.toolDefinition = toolDefinition;
    }

    /**
     * 执行Tool
     *
     * @param params 参数Map（key为参数名，value为参数值）
     * @return 执行结果
     */
    public String execute(Map<String, Object> params) {
        try {
            log.info("执行Tool: {} | 参数: {}", toolDefinition.getName(), params);

            ToolDefinitionDTO.ApiEndpointConfig apiConfig = toolDefinition.getApiConfig();
            if (apiConfig == null) {
                throw new IllegalStateException(
                    String.format("Tool [%s] 缺少API配置，请在数据库skills表的params字段中配置endpoint、method等信息",
                        toolDefinition.getName()));
            }
            if (apiConfig.getEndpoint() == null || apiConfig.getMethod() == null) {
                throw new IllegalStateException(
                    String.format("Tool [%s] 的API配置不完整，endpoint或method为空", toolDefinition.getName()));
            }

            int timeout = apiConfig.getTimeout() != null ? apiConfig.getTimeout() : 30;

            String response;
            String method = apiConfig.getMethod().toUpperCase();

            switch (method) {
                case "GET":
                    response = ToolApiClient.callGetApi(apiConfig.getEndpoint(), params, timeout);
                    break;
                case "POST":
                    response = ToolApiClient.callPostApi(apiConfig.getEndpoint(), params, timeout);
                    break;
                case "STREAM":
                    response = ToolApiClient.callStreamApi(apiConfig.getEndpoint(), params, timeout);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的HTTP方法: " + method);
            }

            // 提取结果
            String result = ToolApiClient.extractResult(response);
            log.info("Tool执行成功: {} | 结果: {}", toolDefinition.getName(), result);

            return result;

        } catch (Exception e) {
            log.error("Tool执行失败: {}", toolDefinition.getName(), e);
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
        desc.append(toolDefinition.getDescription());

        if (toolDefinition.getApiConfig() != null && toolDefinition.getApiConfig().getParams() != null) {
            desc.append("\n参数：");
            for (ToolDefinitionDTO.ParamDefinition param : toolDefinition.getApiConfig().getParams()) {
                desc.append("\n- ").append(param.getName())
                        .append(" (").append(param.getType()).append(")")
                        .append(param.getRequired() ? " [必填]" : " [可选]")
                        .append(": ").append(param.getDescription());
            }
        }

        if (toolDefinition.getTriggers() != null && !toolDefinition.getTriggers().isEmpty()) {
            desc.append("\n适用场景：").append(String.join("、", toolDefinition.getTriggers()));
        }

        return desc.toString();
    }

    public ToolDefinitionDTO getToolDefinition() {
        return toolDefinition;
    }
}
