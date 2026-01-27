package com.enumerate.disease_detection.Tools;

import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MutiTool {

    @Tool("乘法函数")
    public String Muti(String a, String b) {
        log.info("乘法函数xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        return String.valueOf(Integer.parseInt(a) * Integer.parseInt(b));
    }
}
