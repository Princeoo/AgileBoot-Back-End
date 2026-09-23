package com.agileboot.api.controller.auth;

import com.agileboot.api.customize.service.MiniappAuthService;
import com.agileboot.api.customize.service.MiniappLoginCommand;
import com.agileboot.api.customize.service.MiniappLoginResponseDTO;
import com.agileboot.api.customize.service.MiniappTokenService;
import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.infrastructure.user.AuthenticationUtils;
import com.agileboot.infrastructure.user.miniapp.MiniappLoginUser;
import com.agileboot.infrastructure.annotations.ratelimit.RateLimit;
import com.agileboot.infrastructure.annotations.ratelimit.RateLimitKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微信小程序认证接口。
 */
@Tag(name = "小程序认证API", description = "微信小程序登录、身份上下文和退出")
@RestController
@RequestMapping("/miniapp/auth")
@RequiredArgsConstructor
@Validated
public class MiniappAuthController {

    private final MiniappAuthService authService;

    private final MiniappTokenService tokenService;

    @Operation(summary = "微信小程序登录")
    @RateLimit(key = RateLimitKey.MINIAPP_LOGIN_KEY, time = 60, maxCount = 10,
        cacheType = RateLimit.CacheType.REDIS, limitType = RateLimit.LimitType.IP)
    @PostMapping("/login")
    public ResponseDTO<MiniappLoginResponseDTO> login(@Valid @RequestBody MiniappLoginCommand command,
        HttpServletRequest request) {
        return ResponseDTO.ok(authService.login(command, request));
    }

    @Operation(summary = "获取当前小程序身份上下文")
    @GetMapping("/context")
    public ResponseDTO<?> context() {
        MiniappLoginUser loginUser = AuthenticationUtils.getMiniappLoginUser();
        return ResponseDTO.ok(authService.context(loginUser));
    }

    @Operation(summary = "退出小程序登录")
    @PostMapping("/logout")
    public ResponseDTO<Void> logout(HttpServletRequest request) {
        String token = tokenService.getTokenFromRequest(request);
        if (token != null) {
            tokenService.logout(token);
        }
        SecurityContextHolder.clearContext();
        return ResponseDTO.ok();
    }
}
