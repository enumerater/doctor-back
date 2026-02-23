package com.enumerate.disease_detection.Service;

import com.enumerate.disease_detection.Mapper.ToolsMapper;
import com.enumerate.disease_detection.POJO.DTO.ToolDefinitionDTO;
import com.enumerate.disease_detection.POJO.PO.ToolsPO;
import com.enumerate.disease_detection.Tools.DynamicTool;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tool动态加载服务
 * 加载所有启用的Tools
 */
@Service
@Slf4j
public class ToolLoaderService {

    @Autowired
    private ToolsMapper toolsMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 加载所有启用的Tools
     *
     * @return DynamicTool列表
     */
    public List<DynamicTool> loadAllTools() {
        try {
            log.info("开始加载所有启用的Tools");

            // 从数据库加载所有记录
            List<ToolsPO> toolsPOList = toolsMapper.selectList(null);
            if (toolsPOList == null || toolsPOList.isEmpty()) {
                log.warn("未查询到任何Tool记录");
                return Collections.emptyList();
            }

            // 转换为DynamicTool，保留enabled过滤
            List<DynamicTool> tools = toolsPOList.stream()
                    .filter(tool -> tool.getEnabled() != null && tool.getEnabled())
                    .map(this::convertToTool)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // 按Tool名称去重，避免数据库中存在重复记录
            Set<String> seenNames = new HashSet<>();
            tools.removeIf(tool -> !seenNames.add(tool.getToolDefinition().getName()));

            log.info("成功加载 {} 个Tools: {}", tools.size(),
                    tools.stream().map(tool -> tool.getToolDefinition().getName()).collect(Collectors.toList()));

            return tools;

        } catch (Exception e) {
            log.error("加载Tools失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 将ToolsPO转换为DynamicTool
     */
    private DynamicTool convertToTool(ToolsPO toolsPO) {
        try {
            ToolDefinitionDTO definition = ToolDefinitionDTO.builder()
                    .id(toolsPO.getId())
                    .name(toolsPO.getName())
                    .description(toolsPO.getDescription())
                    .category(toolsPO.getCategory())
                    .enabled(toolsPO.getEnabled())
                    .build();

            // 解析triggers（JSON数组 -> List<String>）
            if (toolsPO.getTriggers() != null && !toolsPO.getTriggers().isEmpty()) {
                List<String> triggers = objectMapper.readValue(
                        toolsPO.getTriggers(),
                        new TypeReference<List<String>>() {
                        }
                );
                definition.setTriggers(triggers);
            }

            // 解析params（JSON对象 -> ApiEndpointConfig）
            if (toolsPO.getParams() != null && !toolsPO.getParams().isEmpty()) {
                ToolDefinitionDTO.ApiEndpointConfig apiConfig = objectMapper.readValue(
                        toolsPO.getParams(),
                        ToolDefinitionDTO.ApiEndpointConfig.class
                );
                definition.setApiConfig(apiConfig);
            } else {
                log.warn("Tool [{}] 的params字段为空，该Tool将无法正常执行API调用", toolsPO.getName());
            }

            return new DynamicTool(definition);

        } catch (Exception e) {
            log.error("转换Tool失败: {}", toolsPO.getName(), e);
            return null;
        }
    }

    /**
     * 为LLM生成Tools的提示文本
     *
     * @param tools Tool工具列表
     * @return 提示文本
     */
    public String generateToolsPrompt(List<DynamicTool> tools) {
        if (tools == null || tools.isEmpty()) {
            return "当前没有可用的Tools。";
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("你当前可以使用以下Tools来帮助用户：\n\n");

        for (int i = 0; i < tools.size(); i++) {
            DynamicTool tool = tools.get(i);
            ToolDefinitionDTO def = tool.getToolDefinition();

            prompt.append(String.format("%d. **%s** [%s]\n", i + 1, def.getName(), def.getCategory()));
            prompt.append(String.format("   %s\n", tool.getToolDescription()));
            prompt.append("\n");
        }

        prompt.append("当用户的需求匹配某个Tool时，请主动建议或直接调用该Tool。\n");

        return prompt.toString();
    }
}
