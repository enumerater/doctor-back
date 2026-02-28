package com.enumerate.disease_detection.MVC.Controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.Constant.ResultCodeConstant;
import com.enumerate.disease_detection.MVC.Mapper.PicMapper;
import com.enumerate.disease_detection.MVC.POJO.DTO.PicDTO;
import com.enumerate.disease_detection.Utils.OSSUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/oss")
public class OSSController {

    private final OSSUtil ossUtil;

    public OSSController(OSSUtil ossUtil) {
        this.ossUtil = ossUtil;
    }

    @Autowired
    private PicMapper picMapper;

    @PostMapping("/upload")
    @CrossOrigin
    public Result<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 1. 计算文件内容的MD5哈希值（核心修改点）
            String fileMd5 = calculateFileMd5(file);

            // 2. 根据MD5查询是否已存在该文件
            PicDTO pic = picMapper.selectOne(new QueryWrapper<PicDTO>().eq("pic_code", fileMd5));

            if (pic == null) {
                // 3. 新文件：上传到OSS并保存记录
                String fileUrl = ossUtil.uploadFile(file);
                PicDTO picDTO = PicDTO.builder()
                        .picUrl(fileUrl)
                        .picCode(fileMd5) // 存储MD5而非hashCode
                        .build();
                picMapper.insert(picDTO);

                Map<String, String> result = new HashMap<>();
                result.put("url", fileUrl);
                result.put("message", "文件上传成功（新文件）");
                return Result.success(result);
            } else {
                // 4. 已存在：直接返回原有URL
                Map<String, String> result = new HashMap<>();
                result.put("url", pic.getPicUrl());
                result.put("message", "文件上传成功（复用已有文件）");
                return Result.success(result);
            }

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return Result.error(ResultCodeConstant.FAIL);
        }
    }

    /**
     * 计算MultipartFile文件内容的MD5哈希值
     * @param file 上传的文件
     * @return 文件内容的MD5字符串（32位小写）
     */
    private String calculateFileMd5(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] bytes = file.getBytes();
        md5.update(bytes);
        byte[] digest = md5.digest();

        // 将字节数组转换为32位小写十六进制字符串
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
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