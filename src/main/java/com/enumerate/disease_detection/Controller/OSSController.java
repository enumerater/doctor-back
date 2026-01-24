package com.enumerate.disease_detection.Controller;

import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.Constant.ResultCodeConstant;
import com.enumerate.disease_detection.Utils.OSSUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/oss")
public class OSSController {

    private final OSSUtil ossUtil;

    public OSSController(OSSUtil ossUtil) {
        this.ossUtil = ossUtil;
    }

    @PostMapping("/upload")
    public Result<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = ossUtil.uploadFile(file);
            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("message", "文件上传成功");
            return Result.success(result);
        } catch (IOException e) {
            return Result.error(ResultCodeConstant.FAIL);
        }
    }

    @DeleteMapping("/delete")
    public Result<Map<String, String>> deleteFile(String fileName) {
        try {
            ossUtil.deleteFile(fileName);
            Map<String, String> result = new HashMap<>();
            result.put("message", "文件删除成功");
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(ResultCodeConstant.FAIL);
        }
    }
}