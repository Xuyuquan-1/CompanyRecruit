package com.hrrecruit.common.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectResult;
import com.hrrecruit.common.config.AliOssProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

/**
 * 阿里云 OSS 文件上传工具
 * 通过注入 AliOssProperties 获取配置
 */
@Slf4j
@Component
public class OssUtil {

    private final String endpoint;
    private final String accessKeyId;
    private final String accessKeySecret;
    private final String bucketName;
    private final String baseUrl;

    public OssUtil(AliOssProperties aliOssProperties) {
        this.endpoint = aliOssProperties.getEndpoint();
        this.accessKeyId = aliOssProperties.getAccessKeyId();
        this.accessKeySecret = aliOssProperties.getAccessKeySecret();
        this.bucketName = aliOssProperties.getBucketName();
        this.baseUrl = aliOssProperties.getBaseUrl();
    }

    /**
     * 上传文件到OSS
     *
     * @param originalFilename 原始文件名
     * @param inputStream      文件流
     * @param fileSize         文件大小（字节）
     * @return 上传后的URL
     */
    public String upload(String originalFilename, InputStream inputStream, long fileSize) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String objectName = "resume/" + UUID.randomUUID().toString() + extension;

        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            PutObjectResult result = ossClient.putObject(bucketName, objectName, inputStream);
            log.info("OSS上传成功: {} -> {}", originalFilename, objectName);
        } catch (Exception e) {
            log.error("OSS上传失败: {}", originalFilename, e);
            throw new RuntimeException("文件上传到OSS失败", e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        return baseUrl + "/" + objectName;
    }

    /**
     * 上传字节数组到OSS
     */
    public String upload(String originalFilename, byte[] bytes) {
        return upload(originalFilename, new ByteArrayInputStream(bytes), bytes.length);
    }

    /**
     * 删除OSS上的文件
     *
     * @param objectUrl OSS文件完整URL
     */
    public void delete(String objectUrl) {
        if (objectUrl == null || !objectUrl.contains(".aliyuncs.com/")) {
            return;
        }
        String objectName = objectUrl.substring(objectUrl.indexOf(".aliyuncs.com/") + 13);
        // 去除可能的前导斜杠，确保路径格式正确
        if (objectName.startsWith("/")) {
            objectName = objectName.substring(1);
        }

        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            ossClient.deleteObject(bucketName, objectName);
            log.info("OSS删除成功: {}", objectName);
        } catch (Exception e) {
            log.error("OSS删除失败: {}", objectName, e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 从OSS下载文件，返回字节数组
     *
     * @param objectUrl OSS文件完整URL
     */
    public byte[] download(String objectUrl) {
        if (objectUrl == null || !objectUrl.contains(".aliyuncs.com/")) {
            throw new RuntimeException("无效的OSS文件地址");
        }
        String objectName = objectUrl.substring(objectUrl.indexOf(".aliyuncs.com/") + 13);
        // 去除可能的前导斜杠，确保路径格式正确
        if (objectName.startsWith("/")) {
            objectName = objectName.substring(1);
        }

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try (InputStream inputStream = ossClient.getObject(bucketName, objectName).getObjectContent()) {
            return inputStream.readAllBytes();
        } catch (Exception e) {
            log.error("OSS下载失败: {}", objectName, e);
            throw new RuntimeException("文件下载失败", e);
        } finally {
            ossClient.shutdown();
        }
    }
}