package com.agileboot.api.customize.service;

import cn.hutool.core.util.IdUtil;
import com.agileboot.common.constant.Constants.Token;
import com.agileboot.common.enums.auth.ClientTypeEnum;
import com.agileboot.common.enums.auth.SubjectTypeEnum;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Client;
import com.agileboot.api.customize.config.MiniappAuthProperties;
import com.agileboot.infrastructure.auth.AuthSession;
import com.agileboot.infrastructure.auth.AuthSessionService;
import com.agileboot.infrastructure.user.miniapp.MiniappLoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 小程序 JWT V2 和 Redis 服务端会话。
 */
@Component
@RequiredArgsConstructor
public class MiniappTokenService {

    private final AuthSessionService authSessionService;

    private final MiniappAuthProperties properties;

    @Value("${token.header:Authorization}")
    private String header;

    @Value("${token.secret:agileboot-miniapp-secret}")
    private String fallbackSecret;

    public IssuedToken issue(MiniappLoginUser loginUser) {
        long now = System.currentTimeMillis();
        long ttlSeconds = TimeUnit.MINUTES.toSeconds(properties.getAccessTokenTtlMinutes());
        if (ttlSeconds <= 0) {
            // 非法配置不应产生立即失效的 Token，统一回退到服务端会话的默认有效期。
            ttlSeconds = AuthSessionService.DEFAULT_TTL_SECONDS;
        }
        String sessionId = IdUtil.fastSimpleUUID();

        // JWT 只承载会话索引和身份声明，完整登录态保存在 Redis，便于主动注销和服务端失效会话。
        AuthSession session = new AuthSession();
        session.setSessionId(sessionId);
        session.setClientType(ClientTypeEnum.WECHAT_MINIAPP);
        session.setSubjectType(SubjectTypeEnum.MINIAPP_USER);
        session.setSubjectId(loginUser.getIamUserId());
        session.setIssuedAt(now);
        session.setExpiresAt(now + TimeUnit.SECONDS.toMillis(ttlSeconds));
        session.setLastAccessAt(now);
        session.setIdentityVersion(1L);
        session.setPrincipal(loginUser);
        authSessionService.save(session, ttlSeconds);

        Date issuedAt = new Date(now);
        Date expiresAt = new Date(now + TimeUnit.SECONDS.toMillis(ttlSeconds));
        String token = Jwts.builder()
            .setClaims(new java.util.HashMap<String, Object>())
            .setId(sessionId)
            .setSubject(String.valueOf(loginUser.getIamUserId()))
            .claim("ver", 2)
            .claim("sid", sessionId)
            .claim("ct", ClientTypeEnum.WECHAT_MINIAPP.name())
            .claim("st", SubjectTypeEnum.MINIAPP_USER.name())
            .setIssuedAt(issuedAt)
            .setExpiration(expiresAt)
            .signWith(SignatureAlgorithm.HS512, getSecret())
            .compact();
        return new IssuedToken(token, sessionId, ttlSeconds);
    }

    public TokenSession authenticate(String token) {
        try {
            Claims claims = parseToken(token);
            String clientType = claims.get("ct", String.class);
            String subjectType = claims.get("st", String.class);
            String sessionId = claims.get("sid", String.class);
            String subjectId = claims.getSubject();
            // 先限制 Token 的签发端和主体类型，防止其他客户端使用相同密钥时发生 Token 串用。
            if (!ClientTypeEnum.WECHAT_MINIAPP.name().equals(clientType)
                || !SubjectTypeEnum.MINIAPP_USER.name().equals(subjectType)
                || sessionId == null || subjectId == null) {
                throw new ApiException(Client.AUTH_CLIENT_MISMATCH);
            }

            // JWT 验签通过并不代表会话仍有效；Redis 会话是注销、过期等服务端状态的最终依据。
            AuthSession session = authSessionService.get(ClientTypeEnum.WECHAT_MINIAPP, sessionId);
            if (session == null || session.getExpiresAt() <= System.currentTimeMillis()) {
                throw new ApiException(Client.AUTH_SESSION_EXPIRED);
            }
            // 对照两侧的会话及主体信息，避免合法 JWT 被错误关联到其他服务端会话。
            if (!sessionId.equals(session.getSessionId()) || !Long.valueOf(subjectId).equals(session.getSubjectId())
                || session.getClientType() != ClientTypeEnum.WECHAT_MINIAPP
                || session.getSubjectType() != SubjectTypeEnum.MINIAPP_USER) {
                throw new ApiException(Client.AUTH_CLIENT_MISMATCH);
            }
            return new TokenSession(claims, session);
        } catch (ApiException exception) {
            throw exception;
        } catch (ExpiredJwtException exception) {
            throw new ApiException(exception, Client.AUTH_SESSION_EXPIRED);
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException exception) {
            throw new ApiException(exception, Client.INVALID_TOKEN);
        }
    }

    public void logout(String token) {
        TokenSession tokenSession = authenticate(token);
        authSessionService.delete(ClientTypeEnum.WECHAT_MINIAPP, tokenSession.getSession().getSessionId());
    }

    public String getTokenFromRequest(HttpServletRequest request) {
        String value = request.getHeader(header);
        if (value != null && value.startsWith(Token.PREFIX)) {
            return value.substring(Token.PREFIX.length()).trim();
        }
        return value;
    }

    public Claims parseToken(String token) {
        return Jwts.parser().setSigningKey(getSecret()).parseClaimsJws(token).getBody();
    }

    private String getSecret() {
        // token.secret 作为兼容旧部署的兜底值，新配置优先使用小程序独立密钥。
        return properties.getTokenSecret() == null || properties.getTokenSecret().trim().isEmpty()
            ? fallbackSecret : properties.getTokenSecret();
    }

    @Data
    public static class IssuedToken {

        private final String accessToken;

        private final String sessionId;

        private final long expiresIn;
    }

    @Data
    @RequiredArgsConstructor
    public static class TokenSession {

        private final Claims claims;

        private final AuthSession session;
    }
}
