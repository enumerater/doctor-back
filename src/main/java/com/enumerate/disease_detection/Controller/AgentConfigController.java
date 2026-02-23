package com.enumerate.disease_detection.Controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.Local.UserContextHolder;
import com.enumerate.disease_detection.Mapper.AgentConfigMapper;
import com.enumerate.disease_detection.POJO.DTO.AgentConfigPODTO;
import com.enumerate.disease_detection.POJO.PO.AgentConfigPO;
import com.enumerate.disease_detection.POJO.VO.AgentConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/agentConfig")
@Slf4j
public class AgentConfigController {
    @Autowired
    private AgentConfigMapper agentConfigMapper;

    @GetMapping("/list")
    @CrossOrigin
    public Result<List<AgentConfigVO>> list() {

        log.info("=== agentConfig controller ===");
        List<AgentConfigPO> agentConfigPOList = agentConfigMapper.selectList(new QueryWrapper<AgentConfigPO>().eq("user_id", UserContextHolder.getUserId()));
        List<AgentConfigVO> agentConfigVOList = new ArrayList<>();
        for (AgentConfigPO agentConfigPO : agentConfigPOList) {
            AgentConfigVO agentConfigVO = AgentConfigVO.builder().build();
            BeanUtils.copyProperties(agentConfigPO, agentConfigVO);
            agentConfigVO.setId(agentConfigPO.getId().toString());
            agentConfigVOList.add(agentConfigVO);
        }
        return Result.success(agentConfigVOList);
    }

    @GetMapping("/{id}")
    @CrossOrigin
    public Result<AgentConfigPODTO> get(@PathVariable("id") Long id) {
        AgentConfigPODTO agentConfigPODTO = new AgentConfigPODTO();
        AgentConfigPO agentConfigPO = agentConfigMapper.selectById(id);
        BeanUtils.copyProperties(agentConfigPO, agentConfigPODTO);
        return Result.success(agentConfigPODTO);
    }

    @PostMapping
    @CrossOrigin
    public Result<AgentConfigPODTO> save(@RequestBody AgentConfigPO agentConfigPO) {
        log.info("保存配置{}", agentConfigPO);
        agentConfigPO.setUserId(UserContextHolder.getUserId());
        agentConfigMapper.insert(agentConfigPO);

        AgentConfigPODTO agentConfigPODTO = new AgentConfigPODTO();
        BeanUtils.copyProperties(agentConfigPO, agentConfigPODTO);

        return Result.success(agentConfigPODTO);
    }

    @PutMapping("/{id}")
    @CrossOrigin
    public Result<AgentConfigPODTO> update(@PathVariable("id") Long id, @RequestBody AgentConfigPO agentConfigPO) {
        log.info("更新配置{} {}", agentConfigPO,id);
        agentConfigPO.setId(id);
        agentConfigMapper.update(agentConfigPO, new QueryWrapper<AgentConfigPO>().eq("id", id));

        AgentConfigPODTO agentConfigPODTO = new AgentConfigPODTO();
        BeanUtils.copyProperties(agentConfigPO, agentConfigPODTO);
        return Result.success(agentConfigPODTO);
    }

    @DeleteMapping("/{id}")
    @CrossOrigin
    public Result<String> delete(@PathVariable("id") Long id) {
        log.info("删除id: {}", id);
        agentConfigMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @PutMapping("/{id}/setDefault")
    @CrossOrigin
    public Result<String> setDefault(@PathVariable("id") String id) {
        // 先把全部设置成false
        agentConfigMapper.update(AgentConfigPO.builder().build(), new UpdateWrapper<AgentConfigPO>().set("is_default", false));
        AgentConfigPO agentConfigPO = agentConfigMapper.selectById(id);
        agentConfigPO.setIsDefault(true);
        agentConfigMapper.updateById(agentConfigPO);
        return Result.success("设置成功");
    }

    @PostMapping("/{configId}/duplicate")
    @CrossOrigin
    public Result<AgentConfigPO> duplicateConfig(@PathVariable("configId") String configId) {

        // ========== 1. 基础校验：源配置是否存在 ==========
        AgentConfigPO sourceConfig = agentConfigMapper.selectById(configId);
        if (sourceConfig == null) {
            return Result.error(404, "源配置不存在");
        }


        // ========== 3. 复制配置字段（排除id、createdAt、updatedAt） ==========
        AgentConfigPO newConfig = AgentConfigPO.builder().build();
        // 复制所有字段，排除指定忽略字段
        BeanUtils.copyProperties(sourceConfig, newConfig,
                "id", "createdAt", "updatedAt");

        // ========== 4. 处理新配置的核心规则 ==========
        // 4.1 新配置名称：添加 "- 副本" + 时间戳（避免重复）
        String originalName = sourceConfig.getName();
        String timestamp = DateUtil.format(new Date(), "yyyyMMddHHmmss");
        String newName = StrUtil.format("{} - 副本 {}", originalName, timestamp);
        newConfig.setName(newName);

        // 4.2 新配置不设为默认
        newConfig.setIsDefault(false);

        // ========== 5. 保存新配置 ==========
        agentConfigMapper.insert(newConfig);

        // ========== 6. 返回新配置对象 ==========
        return Result.success(newConfig);
    }

    @GetMapping("/rename")
    @CrossOrigin
    public Result<String> rename(@RequestParam("configId") String configId, @RequestParam("name") String name) {
        log.info("重命名id: {} name: {}", configId, name);
        AgentConfigPO agentConfigPO = agentConfigMapper.selectById(configId);
        agentConfigPO.setName(name);
        agentConfigMapper.updateById(agentConfigPO);
        return Result.success("重命名成功");
    }


}