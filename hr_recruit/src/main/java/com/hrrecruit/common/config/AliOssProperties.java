package com.hrrecruit.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云OSS配置属性类
 * 绑定 application.yml 中 aliyun.oss 前缀的配置项
 */
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
@Data
public class AliOssProperties {

    /** 地域节点 */
    private String endpoint;

    /** AccessKey ID */
    private String accessKeyId;

    /** AccessKey Secret */
    private String accessKeySecret;

    /** Bucket名称 */
    private String bucketName;

    /** OSS外网访问域名 */
    private String baseUrl;
}
