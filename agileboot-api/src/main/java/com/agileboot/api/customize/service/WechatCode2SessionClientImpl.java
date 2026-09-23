package com.agileboot.api.customize.service;

import com.agileboot.api.customize.config.MiniappAuthProperties;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Client;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final ObjectMapper objectMapper;

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
            // 微信偶尔以 text/plain 返回 JSON，先按字符串接收，避免消息转换器拒绝响应类型。
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            Map<String, Object> body = parseResponse(response.getBody());
            // 微信在 HTTP 200 响应中通过 errcode 表示业务失败，不能只依赖 HTTP 状态判断成功。
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
            // 保留上方已经归类的业务异常，避免被后续 RuntimeException 分支重新包装。
            throw exception;
        } catch (HttpServerErrorException | ResourceAccessException exception) {
            // 微信服务端错误、连接失败和读取超时属于上游不可用，客户端可据此决定是否重试。
            log.warn("wechat code2session unavailable, appId={}, durationMs={}", appId,
                System.currentTimeMillis() - start);
            throw new ApiException(exception, Client.MINIAPP_PROVIDER_UNAVAILABLE);
        } catch (JsonProcessingException exception) {
            log.warn("wechat code2session returned invalid json, appId={}, durationMs={}", appId,
                System.currentTimeMillis() - start);
            throw new ApiException(exception, Client.MINIAPP_CODE_INVALID);
        } catch (RuntimeException exception) {
            // 响应格式异常等其余失败按无效登录凭证处理，且日志中不记录 code、secret 或 session_key。
            log.warn("wechat code2session failed, appId={}, durationMs={}", appId,
                System.currentTimeMillis() - start);
            throw new ApiException(exception, Client.MINIAPP_CODE_INVALID);
        }
    }

    private Map<String, Object> parseResponse(String responseBody) throws JsonProcessingException {
        if (isBlank(responseBody)) {
            return null;
        }
        return objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {
        });
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
