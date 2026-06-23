package com.hrrecruit.common.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Date;

/**
 * 阿里云OSS工具类
 * 封装文件上传和签名URL生成两个核心方法
 */
@Slf4j
public class AliOssUtil {

    private final String endpoint;
    private final String accessKeyId;
    private final String accessKeySecret;
    private final String bucketName;

    public AliOssUtil(String endpoint, String accessKeyId, String accessKeySecret, String bucketName) {
        this.endpoint = endpoint;
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.bucketName = bucketName;
    }

    /**
     * 上传字节数组到OSS
     *
     * @param bytes      文件字节数组
     * @param objectName OSS对象名称（含路径，如 "resume/xxx.pdf"）
     * @return 公开访问URL（格式：https://{bucket}.{endpoint}/{objectName}）
     */
    public String upload(byte[] bytes, String objectName) {
        OSS ossClient = null;
        try {
            DefaultCredentialProvider credentialsProvider = CredentialsProviderFactory.newDefaultCredentialProvider(accessKeyId, accessKeySecret);
            ossClient = new OSSClientBuilder().build(endpoint, credentialsProvider);
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
            String url = "https://" + bucketName + "." + endpoint + "/" + objectName;
            log.info("OSS上传成功: {}", url);
            return url;
        } catch (Exception e) {
            log.error("OSS上传失败: {}", objectName, e);
            throw new RuntimeException("文件上传到OSS失败", e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 为私有文件生成带签名的临时下载URL
     *
     * @param objectName    OSS对象名称（含路径）
     * @param expireSeconds 过期时间（秒）
     * @return 带签名的临时下载URL
     */
    public String getSignedUrl(String objectName, int expireSeconds) {
        OSS ossClient = null;
        try {
            DefaultCredentialProvider credentialsProvider = CredentialsProviderFactory.newDefaultCredentialProvider(accessKeyId, accessKeySecret);
            ossClient = new OSSClientBuilder().build(endpoint, credentialsProvider);
            Date expiration = new Date(System.currentTimeMillis() + expireSeconds * 1000L);
            URL url = ossClient.generatePresignedUrl(bucketName, objectName, expiration);
            log.info("OSS签名URL生成成功: {}, 有效期: {}秒", objectName, expireSeconds);
            return url.toString();
        } catch (Exception e) {
            log.error("OSS签名URL生成失败: {}", objectName, e);
            throw new RuntimeException("生成签名URL失败", e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
