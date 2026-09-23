package com.agileboot.api.customize.config;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 微信小程序认证配置。AppSecret 只允许通过环境变量或密钥管理系统注入。
 */
@Data
@ConfigurationProperties(prefix = "miniapp.auth")
public class MiniappAuthProperties {

    private String tokenSecret;

    private long accessTokenTtlMinutes = 120L;

    private long bindCodeTtlMinutes = 10L;

    private Map<String, AppConfig> apps = new LinkedHashMap<>();

    private WechatConfig wechat = new WechatConfig();

    @Data
    public static class AppConfig {

        private String appId;

        private String appSecret;
    }

    @Data
    public static class WechatConfig {

        private int connectTimeoutMs = 3000;

        private int readTimeoutMs = 5000;
    }
}
