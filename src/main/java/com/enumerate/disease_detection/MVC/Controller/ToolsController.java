package com.enumerate.disease_detection.MVC.Controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.MVC.Mapper.ToolsMapper;
import com.enumerate.disease_detection.MVC.POJO.DTO.ToolsDTO;
import com.enumerate.disease_detection.MVC.POJO.PO.ToolsPO;
import com.enumerate.disease_detection.MVC.POJO.VO.ToolsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tools")
@Slf4j
public class ToolsController {

    @Autowired
    private ToolsMapper toolsMapper;

    @GetMapping
    @CrossOrigin
    public Result<ToolsVO> getTools(){
        List<ToolsPO> toolsPOList = toolsMapper.selectList(null);
        List<String> tools = toolsPOList.stream().map(ToolsPO::getName).toList();
        List<String> enabledToolIds = toolsPOList.stream().filter(ToolsPO::getEnabled).map(ToolsPO::getName).toList();
        ToolsVO toolsVO = ToolsVO.builder()
                .tools(tools)
                .enabledToolIds(enabledToolIds)
                .build();
        return Result.success(toolsVO);
    }


    @PutMapping("/{id}/status")
    @CrossOrigin
    public Result<String> updateToolStatus(@PathVariable("id") String id, @RequestBody ToolsDTO toolsDTO) {
        log.info("更新Tool状态：{}", toolsDTO.getEnabled());

        toolsMapper.update(ToolsPO.builder().enabled(Boolean.valueOf(toolsDTO.getEnabled())).build(),new QueryWrapper<ToolsPO>().eq("name", id));

        return Result.success("success");
    }

}
