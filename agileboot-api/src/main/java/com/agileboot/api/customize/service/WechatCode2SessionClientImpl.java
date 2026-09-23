package com.agileboot.api.customize.service;

import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Client;
import com.agileboot.api.customize.config.MiniappAuthProperties;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 微信官方 jscode2session HTTP 客户端。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WechatCode2SessionClientImpl implements WechatCode2SessionClient {

    private final RestTemplateBuilder restTemplateBuilder;

    private final MiniappAuthProperties properties;

    @Override
    public WechatSession exchange(String appId, String appSecret, String code) {
        if (isBlank(appId) || isBlank(appSecret)) {
            throw new ApiException(Client.MINIAPP_APP_NOT_CONFIGURED);
        }
        RestTemplate restTemplate = restTemplateBuilder
            .setConnectTimeout(java.time.Duration.ofMillis(properties.getWechat().getConnectTimeoutMs()))
            .setReadTimeout(java.time.Duration.ofMillis(properties.getWechat().getReadTimeoutMs()))
            .build();
        String url = UriComponentsBuilder.fromHttpUrl("https://api.weixin.qq.com/sns/jscode2session")
            .queryParam("appid", appId)
            .queryParam("secret", appSecret)
            .queryParam("js_code", code)
            .queryParam("grant_type", "authorization_code")
            .toUriString();
        long start = System.currentTimeMillis();
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map body = response.getBody();
            int errCode = body == null || body.get("errcode") == null
                ? 0 : Integer.parseInt(String.valueOf(body.get("errcode")));
            String openId = body == null ? null : stringValue(body.get("openid"));
            log.debug("wechat code2session completed, appId={}, errcode={}, durationMs={}",
                appId, errCode, System.currentTimeMillis() - start);
            if (errCode != 0 || isBlank(openId)) {
                throw new ApiException(Client.MINIAPP_CODE_INVALID);
            }
            return new WechatSession(openId, stringValue(body.get("unionid")), stringValue(body.get("session_key")));
        } catch (ApiException exception) {
            throw exception;
        } catch (HttpServerErrorException | ResourceAccessException exception) {
            log.warn("wechat code2session unavailable, appId={}, durationMs={}", appId,
                System.currentTimeMillis() - start);
            throw new ApiException(exception, Client.MINIAPP_PROVIDER_UNAVAILABLE);
        } catch (RuntimeException exception) {
            log.warn("wechat code2session failed, appId={}, durationMs={}", appId,
                System.currentTimeMillis() - start);
            throw new ApiException(exception, Client.MINIAPP_CODE_INVALID);
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
