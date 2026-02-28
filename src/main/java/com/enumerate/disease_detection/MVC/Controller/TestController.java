package com.enumerate.disease_detection.MVC.Controller;

import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.enumerate.disease_detection.Common.Result;

import com.enumerate.disease_detection.MVC.Service.TestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {

    @Autowired
    private TestService testService;

    @GetMapping
    public Result<String> test() {
        log.info("=== test controller ===");
        String res = testService.test();

        return Result.success(res);
    }

    @GetMapping("/dashScope")
    public Result<String> dashScope() throws NoApiKeyException, InputRequiredException {
        log.info("=== dashScope controller ===");
        String res = testService.dashScope();
        return Result.success(res);
    }
}
