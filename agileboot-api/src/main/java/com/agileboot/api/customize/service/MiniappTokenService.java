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
            ttlSeconds = AuthSessionService.DEFAULT_TTL_SECONDS;
        }
        String sessionId = IdUtil.fastSimpleUUID();
        AuthSession session = new AuthSession();
        session.setSessionId(sessionId);
        session.setClientType(ClientTypeEnum.WECHAT_MINIAPP);
        session.setSubjectType(SubjectTypeEnum.IAM_USER);
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
            .claim("st", SubjectTypeEnum.IAM_USER.name())
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
            if (!ClientTypeEnum.WECHAT_MINIAPP.name().equals(clientType)
                || !SubjectTypeEnum.IAM_USER.name().equals(subjectType)
                || sessionId == null || subjectId == null) {
                throw new ApiException(Client.AUTH_CLIENT_MISMATCH);
            }
            AuthSession session = authSessionService.get(ClientTypeEnum.WECHAT_MINIAPP, sessionId);
            if (session == null || session.getExpiresAt() <= System.currentTimeMillis()) {
                throw new ApiException(Client.AUTH_SESSION_EXPIRED);
            }
            if (!sessionId.equals(session.getSessionId()) || !Long.valueOf(subjectId).equals(session.getSubjectId())
                || session.getClientType() != ClientTypeEnum.WECHAT_MINIAPP
                || session.getSubjectType() != SubjectTypeEnum.IAM_USER) {
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
