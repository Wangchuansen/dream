package com.wcs.core.aliyun.config;

import com.aliyun.teaopenapi.models.Config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author: wcs
 * @create: 2023-01-09 9:05
 * @description:
 */
@Data
@Configuration
@RefreshScope
@ConfigurationProperties(prefix = "sms")
public class SmsConfig {

    /**appid*/
    private String accessKeyId;

    /**密钥*/
    private String accessKeySecret;

    /**请求地址*/
    private String endpoint;

    /**短信签名*/
    private String sign;

    /**可以使用的模板编号*/
    private Map<String,String> template;

    public com.aliyun.dysmsapi20170525.Client getSmsClient() throws Exception {
        Config config = new Config()
                // 您的 AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 您的 AccessKey Secret
                .setAccessKeySecret(accessKeySecret);
        // 访问的域名
        config.endpoint = endpoint;
        return new com.aliyun.dysmsapi20170525.Client(config);
    }
}
