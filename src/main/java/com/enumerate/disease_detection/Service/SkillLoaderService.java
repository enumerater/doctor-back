package com.enumerate.disease_detection.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enumerate.disease_detection.Mapper.AgentConfigMapper;
import com.enumerate.disease_detection.Mapper.SkillsMapper;
import com.enumerate.disease_detection.POJO.DTO.SkillDefinitionDTO;
import com.enumerate.disease_detection.POJO.PO.AgentConfigPO;
import com.enumerate.disease_detection.POJO.PO.SkillsPO;
import com.enumerate.disease_detection.Tools.DynamicSkillTool;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Skill动态加载服务
 * 根据用户的Agent配置，动态加载对应的Skills
 */
@Service
@Slf4j
public class SkillLoaderService {

    @Autowired
    private SkillsMapper skillsMapper;

    @Autowired
    private AgentConfigMapper agentConfigMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 根据用户ID和AgentConfigID加载Skills
     *
     * @param userId        用户ID
     * @param agentConfigId Agent配置ID（如果为null，则加载默认配置）
     * @return DynamicSkillTool列表
     */
    public List<DynamicSkillTool> loadSkillsForUser(Long userId, Long agentConfigId) {
        try {
            log.info("开始加载用户Skills - userId: {}, agentConfigId: {}", userId, agentConfigId);

            // 1. 获取Agent配置
            AgentConfigPO agentConfig = getAgentConfig(userId, agentConfigId);
            if (agentConfig == null) {
                log.warn("未找到Agent配置，返回空Skill列表");
                return Collections.emptyList();
            }

            // 2. 解析enabledSkillIds
            String enabledSkillIds = agentConfig.getEnabledSkillIds();
            if (enabledSkillIds == null || enabledSkillIds.trim().isEmpty()) {
                log.info("Agent配置中未启用任何Skill");
                return Collections.emptyList();
            }

            List<Long> skillIdList = parseSkillIds(enabledSkillIds);
            if (skillIdList.isEmpty()) {
                log.warn("解析enabledSkillIds失败或为空");
                return Collections.emptyList();
            }

            log.info("需要加载的Skill IDs: {}", skillIdList);

            // 3. 从数据库查询Skills
            List<SkillsPO> skillsPOList = skillsMapper.selectBatchIds(skillIdList);
            if (skillsPOList == null || skillsPOList.isEmpty()) {
                log.warn("未查询到任何Skill记录");
                return Collections.emptyList();
            }

            // 4. 转换为DynamicSkillTool
            List<DynamicSkillTool> skillTools = skillsPOList.stream()
                    .filter(skill -> skill.getEnabled() != null && skill.getEnabled())
                    .map(this::convertToSkillTool)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            log.info("成功加载 {} 个Skills: {}", skillTools.size(),
                    skillTools.stream().map(tool -> tool.getSkillDefinition().getName()).collect(Collectors.toList()));

            return skillTools;

        } catch (Exception e) {
            log.error("加载Skills失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取Agent配置
     */
    private AgentConfigPO getAgentConfig(Long userId, Long agentConfigId) {
        if (agentConfigId != null) {
            return agentConfigMapper.selectById(agentConfigId);
        }

        // 如果未指定配置ID，则查找用户的默认配置
        LambdaQueryWrapper<AgentConfigPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentConfigPO::getUserId, userId)
                .eq(AgentConfigPO::getIsDefault, true)
                .last("LIMIT 1");

        return agentConfigMapper.selectOne(wrapper);
    }

    /**
     * 解析enabledSkillIds字符串为Long列表
     * 支持格式：
     * - 逗号分隔："1,2,3,4"
     * - JSON数组："[1,2,3,4]"
     */
    private List<Long> parseSkillIds(String enabledSkillIds) {
        try {
            String trimmed = enabledSkillIds.trim();

            // 尝试解析为JSON数组
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                return objectMapper.readValue(trimmed, new TypeReference<List<Long>>() {
                });
            }

            // 解析为逗号分隔的字符串
            return Arrays.stream(trimmed.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("解析enabledSkillIds失败: {}", enabledSkillIds, e);
            return Collections.emptyList();
        }
    }

    /**
     * 将SkillsPO转换为DynamicSkillTool
     */
    private DynamicSkillTool convertToSkillTool(SkillsPO skillsPO) {
        try {
            SkillDefinitionDTO definition = SkillDefinitionDTO.builder()
                    .id(skillsPO.getId())
                    .name(skillsPO.getName())
                    .description(skillsPO.getDescription())
                    .category(skillsPO.getCategory())
                    .enabled(skillsPO.getEnabled())
                    .build();

            // 解析triggers（JSON数组 -> List<String>）
            if (skillsPO.getTriggers() != null && !skillsPO.getTriggers().isEmpty()) {
                List<String> triggers = objectMapper.readValue(
                        skillsPO.getTriggers(),
                        new TypeReference<List<String>>() {
                        }
                );
                definition.setTriggers(triggers);
            }

            // 解析params（JSON对象 -> ApiEndpointConfig）
            if (skillsPO.getParams() != null && !skillsPO.getParams().isEmpty()) {
                SkillDefinitionDTO.ApiEndpointConfig apiConfig = objectMapper.readValue(
                        skillsPO.getParams(),
                        SkillDefinitionDTO.ApiEndpointConfig.class
                );
                definition.setApiConfig(apiConfig);
            }

            return new DynamicSkillTool(definition);

        } catch (Exception e) {
            log.error("转换Skill失败: {}", skillsPO.getName(), e);
            return null;
        }
    }

    /**
     * 为LLM生成Skills的提示文本
     *
     * @param skillTools Skill工具列表
     * @return 提示文本
     */
    public String generateSkillsPrompt(List<DynamicSkillTool> skillTools) {
        if (skillTools == null || skillTools.isEmpty()) {
            return "当前没有可用的Skills。";
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("你当前可以使用以下Skills来帮助用户：\n\n");

        for (int i = 0; i < skillTools.size(); i++) {
            DynamicSkillTool tool = skillTools.get(i);
            SkillDefinitionDTO def = tool.getSkillDefinition();

            prompt.append(String.format("%d. **%s** [%s]\n", i + 1, def.getName(), def.getCategory()));
            prompt.append(String.format("   %s\n", tool.getToolDescription()));
            prompt.append("\n");
        }

        prompt.append("当用户的需求匹配某个Skill时，请主动建议或直接调用该Skill。\n");
        prompt.append("调用格式：使用{SKILL:技能名称:参数JSON}来调用Skill。\n");

        return prompt.toString();
    }
}
