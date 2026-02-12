package com.enumerate.disease_detection.Controller;

import cn.hutool.json.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.Mapper.AgentConfigMapper;
import com.enumerate.disease_detection.Mapper.SkillsMapper;
import com.enumerate.disease_detection.POJO.DTO.SkillsDTO;
import com.enumerate.disease_detection.POJO.PO.AgentConfigPO;
import com.enumerate.disease_detection.POJO.PO.SkillsPO;
import com.enumerate.disease_detection.POJO.VO.SkillsVO;
import dev.langchain4j.internal.Json;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/skills")
@Slf4j
public class SkillsController {

    @Autowired
    private SkillsMapper skillMapper;
    @Autowired
    private AgentConfigMapper agentConfigMapper;

    @GetMapping
    @CrossOrigin
    public Result<SkillsVO> getSkills(){
        List<SkillsPO> skillsPOList = skillMapper.selectList(null);
        List<String> skills = skillsPOList.stream().map(SkillsPO::getName).toList();
        List<String> enabledSkillIds = skillsPOList.stream().filter(SkillsPO::getEnabled).map(SkillsPO::getName).toList();
        SkillsVO skillsVO = SkillsVO.builder()
                .skills(skills)
                .enabledSkillIds(enabledSkillIds)
                .build();
        return Result.success(skillsVO);
    }


    @PutMapping("/{id}/status")
    @CrossOrigin
    public Result<String> updateSkillStatus(@PathVariable("id") String id, @RequestBody SkillsDTO skillsDTO) {
        log.info("更新技能状态：{}", skillsDTO.getEnabled());

        skillMapper.update(SkillsPO.builder().enabled(Boolean.valueOf(skillsDTO.getEnabled())).build(),new QueryWrapper<SkillsPO>().eq("name", id));

        return Result.success("success");
    }

}



