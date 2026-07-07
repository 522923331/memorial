package com.ruoyi.memorial.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 阿里云 OSS 配置项，对应 application.yml 中 memorial.oss.*
 */
@ConfigurationProperties(prefix = "memorial.oss")
public class OssProperties {

    private String endpoint;
    private String bucket;
    private String accessKeyId;
    private String accessKeySecret;
    /** 对象 URL 前缀，如 https://memorials.oss-cn-beijing.aliyuncs.com */
    private String urlPrefix;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getUrlPrefix() {
        return urlPrefix;
    }

    public void setUrlPrefix(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }
}
