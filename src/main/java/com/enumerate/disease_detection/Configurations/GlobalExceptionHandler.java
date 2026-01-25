package com.enumerate.disease_detection.Configurations;

import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.Constant.ResultCodeConstant;
import dev.langchain4j.exception.AuthenticationException;
import dev.langchain4j.exception.HttpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 全局异常处理器
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理AI模型额度耗尽异常
    @ExceptionHandler({AuthenticationException.class, HttpException.class})
    public Result<String> handleAiModelException(Exception e) {
        String errorMsg = "AI模型服务暂时不可用：";
        // 识别额度耗尽的错误信息
        if (e.getMessage().contains("AllocationQuota.FreeTierOnly") ||
                e.getMessage().contains("free tier of the model has been exhausted")) {
            errorMsg += "模型免费额度已用尽，请联系管理员充值或切换模型";
        } else {
            errorMsg += e.getMessage();
        }
        // 返回友好提示，避免500错误
        return Result.error(ResultCodeConstant.FAIL, errorMsg);
    }
}