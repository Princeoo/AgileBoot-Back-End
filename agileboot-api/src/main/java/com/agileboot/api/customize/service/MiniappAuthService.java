package com.agileboot.api.customize.service;

import cn.hutool.extra.servlet.ServletUtil;
import com.agileboot.api.customize.config.MiniappAuthProperties;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Client;
import com.agileboot.domain.iam.auth.MiniappAuthApplicationService;
import com.agileboot.domain.iam.auth.dto.MiniappAuthContextDTO;
import com.agileboot.infrastructure.user.miniapp.MiniappLoginUser;
import java.util.LinkedHashSet;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 小程序登录应用层，负责配置解析、微信网关和 Token 编排。
 */
@Service
@RequiredArgsConstructor
public class MiniappAuthService {

    private final MiniappAuthProperties properties;

    private final WechatCode2SessionClient wechatClient;

    private final MiniappAuthApplicationService authApplicationService;

    private final MiniappTokenService tokenService;

    public MiniappLoginResponseDTO login(MiniappLoginCommand command, HttpServletRequest request) {
        if (command == null || isBlank(command.getCode())) {
            throw new ApiException(Client.MINIAPP_CODE_INVALID);
        }
        String appKey = resolveAppKey(command.getAppKey());
        MiniappAuthProperties.AppConfig appConfig = properties.getApps().get(appKey);
        if (appConfig == null || isBlank(appConfig.getAppId()) || isBlank(appConfig.getAppSecret())) {
            throw new ApiException(Client.MINIAPP_APP_NOT_CONFIGURED);
        }
        WechatCode2SessionClient.WechatSession wechatSession = wechatClient.exchange(
            appConfig.getAppId(), appConfig.getAppSecret(), command.getCode());
        MiniappAuthContextDTO context = authApplicationService.login(appConfig.getAppId(),
            wechatSession.getOpenId(), wechatSession.getUnionId(), command.getNickname(), command.getAvatar(),
            ServletUtil.getClientIP(request));
        MiniappLoginUser loginUser = toLoginUser(context);
        MiniappTokenService.IssuedToken issuedToken = tokenService.issue(loginUser);
        return MiniappLoginResponseDTO.of(issuedToken, context);
    }

    public MiniappAuthContextDTO context(MiniappLoginUser loginUser) {
        return authApplicationService.getContext(loginUser.getIamUserId());
    }

    public MiniappLoginUser toLoginUser(MiniappAuthContextDTO context) {
        Long sysUserId = context.getStaff() == null ? null : context.getStaff().getSysUserId();
        return new MiniappLoginUser(context.getUser().getUserId(), 1, sysUserId,
            context.isWorkbenchEnabled(), new LinkedHashSet<>(context.getIdentities()));
    }

    private String resolveAppKey(String requestedAppKey) {
        if (!isBlank(requestedAppKey)) {
            return requestedAppKey;
        }
        Map<String, MiniappAuthProperties.AppConfig> apps = properties.getApps();
        if (apps != null && apps.size() == 1) {
            return apps.keySet().iterator().next();
        }
        throw new ApiException(Client.MINIAPP_APP_NOT_CONFIGURED);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
