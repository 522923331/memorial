package com.ruoyi.memorial.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云 OSS 客户端 Bean 配置
 */
@Configuration
@EnableConfigurationProperties(OssProperties.class)
public class OssConfig {

    private final OssProperties props;
    private OSS ossClient;

    public OssConfig(OssProperties props) {
        this.props = props;
    }

    @Bean(destroyMethod = "")
    public OSS ossClient() {
        this.ossClient = new OSSClientBuilder().build(props.getEndpoint(), props.getAccessKeyId(), props.getAccessKeySecret());
        return this.ossClient;
    }

    @PreDestroy
    public void shutdown() {
        if (this.ossClient != null) {
            this.ossClient.shutdown();
        }
    }
}
