package com.enumerate.disease_detection.Utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Component
public class OSSUtil {

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    // 新增注入endpoint配置（解决getEndpoint()方法不存在的问题）
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    private final OSS ossClient;

    // 构造注入OSS客户端
    public OSSUtil(OSS ossClient) {
        this.ossClient = ossClient;
    }

    /**
     * 上传文件到OSS
     * @param file 上传的文件
     * @return 文件访问URL
     * @throws IOException 输入流异常
     */
    public String uploadFile(MultipartFile file) throws IOException {
        // 空文件校验
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 生成唯一文件名（避免覆盖）
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("文件名称不合法，缺少扩展名");
        }
        String fileName = UUID.randomUUID().toString() +
                originalFilename.substring(originalFilename.lastIndexOf("."));

        // 获取文件输入流
        try (InputStream inputStream = file.getInputStream()) {
            // 设置文件元信息
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());

            // 上传文件（捕获OSS权限相关异常）
            try {
                ossClient.putObject(bucketName, fileName, inputStream, metadata);
            } catch (OSSException e) {
                // 针对性处理权限拒绝异常，方便排查
                if ("AccessDenied".equals(e.getErrorCode())) {
                    throw new RuntimeException("OSS上传失败：权限被拒绝，请检查AccessKey权限或Bucket ACL配置", e);
                } else {
                    throw new RuntimeException("OSS上传失败：" + e.getErrorMessage(), e);
                }
            }

            // 修复URL拼接方式（使用注入的endpoint，而非从客户端获取）
            // 处理endpoint格式：如果包含https://，先截取主机部分
            String host = endpoint.replace("https://", "").replace("http://", "");
            return "https://" + bucketName + "." + host + "/" + fileName;
        }
    }

    /**
     * 下载OSS文件
     * @param objectName OSS中的文件名
     * @return 文件输入流
     */
    public InputStream downloadFile(String objectName) {
        try {
            OSSObject ossObject = ossClient.getObject(bucketName, objectName);
            return ossObject.getObjectContent();
        } catch (OSSException e) {
            if ("AccessDenied".equals(e.getErrorCode())) {
                throw new RuntimeException("OSS下载失败：权限被拒绝，请检查AccessKey权限或Bucket ACL配置", e);
            } else {
                throw new RuntimeException("OSS下载失败：" + e.getErrorMessage(), e);
            }
        }
    }

    /**
     * 删除OSS文件
     * @param objectName OSS中的文件名
     */
    public void deleteFile(String objectName) {
        try {
            ossClient.deleteObject(bucketName, objectName);
            log.info("文件删除成功：" + objectName);
        } catch (OSSException e) {
            if ("AccessDenied".equals(e.getErrorCode())) {
                throw new RuntimeException("OSS删除失败：权限被拒绝，请检查AccessKey权限或Bucket ACL配置", e);
            } else {
                throw new RuntimeException("OSS删除失败：" + e.getErrorMessage(), e);
            }
        }
    }

    /**
     * 判断文件是否存在
     * @param objectName OSS中的文件名
     * @return 是否存在
     */
    public boolean doesFileExist(String objectName) {
        try {
            return ossClient.doesObjectExist(bucketName, objectName);
        } catch (OSSException e) {
            if ("AccessDenied".equals(e.getErrorCode())) {
                throw new RuntimeException("OSS查询文件失败：权限被拒绝，请检查AccessKey权限或Bucket ACL配置", e);
            } else {
                throw new RuntimeException("OSS查询文件失败：" + e.getErrorMessage(), e);
            }
        }
    }
}