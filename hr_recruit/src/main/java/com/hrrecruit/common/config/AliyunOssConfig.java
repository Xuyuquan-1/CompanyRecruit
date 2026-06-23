package com.hrrecruit.common.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云OSS配置类
 * 通过注入 AliOssProperties 获取配置，创建 OSS Client Bean
 */
@Configuration
@ConditionalOnProperty(name = "file.storage.type", havingValue = "oss")
public class AliyunOssConfig {

    @Bean
    public OSS ossClient(AliOssProperties aliOssProperties) {
        return new OSSClientBuilder().build(
                aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret()
        );
    }
}
