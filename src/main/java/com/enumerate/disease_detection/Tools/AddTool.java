package com.enumerate.disease_detection.Tools;

import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AddTool {


    @Tool("加法函数")
    public String add(int a, int b) {
        log.info("加法函数+++++++++++++++++++++++++++++++");
        return String.valueOf(a + b);
    }
}
