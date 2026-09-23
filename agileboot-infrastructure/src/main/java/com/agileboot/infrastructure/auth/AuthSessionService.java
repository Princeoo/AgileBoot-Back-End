package com.agileboot.infrastructure.auth;

import com.agileboot.common.enums.auth.ClientTypeEnum;
import com.agileboot.common.enums.auth.SubjectTypeEnum;
import com.agileboot.infrastructure.cache.RedisUtil;
import com.agileboot.infrastructure.user.miniapp.MiniappLoginUser;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 小程序服务端会话存取及按主体撤销。
 */
@Component
@RequiredArgsConstructor
public class AuthSessionService {

    private static final String SESSION_PREFIX = "auth:session:";

    private static final String SUBJECT_SESSIONS_PREFIX = "auth:subject-sessions:";

    public static final long DEFAULT_TTL_SECONDS = 120L * 60L;

    private final RedisUtil redisUtil;

    public void save(AuthSession session, long ttlSeconds) {
        String sessionKey = sessionKey(session.getClientType(), session.getSessionId());
        redisUtil.setCacheObject(sessionKey, session, (int) ttlSeconds, TimeUnit.SECONDS);
        String subjectKey = subjectKey(session.getClientType(), session.getSubjectType(), session.getSubjectId());
        redisUtil.addCacheSet(subjectKey, session.getSessionId());
        redisUtil.expire(subjectKey, ttlSeconds, TimeUnit.SECONDS);
    }

    public AuthSession get(ClientTypeEnum clientType, String sessionId) {
        if (sessionId == null || clientType == null) {
            return null;
        }
        return redisUtil.getCacheObject(sessionKey(clientType, sessionId));
    }

    public void delete(ClientTypeEnum clientType, String sessionId) {
        AuthSession session = get(clientType, sessionId);
        redisUtil.deleteObject(sessionKey(clientType, sessionId));
        if (session != null) {
            String subjectKey = subjectKey(session.getClientType(), session.getSubjectType(), session.getSubjectId());
            redisUtil.removeCacheSet(subjectKey, sessionId);
        }
    }

    public void invalidateSubject(ClientTypeEnum clientType, SubjectTypeEnum subjectType, Long subjectId) {
        if (clientType == null || subjectType == null || subjectId == null) {
            return;
        }
        String subjectKey = subjectKey(clientType, subjectType, subjectId);
        Set<String> sessionIds = redisUtil.getCacheSet(subjectKey);
        if (sessionIds == null) {
            sessionIds = Collections.emptySet();
        }
        for (String sessionId : sessionIds) {
            redisUtil.deleteObject(sessionKey(clientType, sessionId));
        }
        redisUtil.deleteObject(subjectKey);
    }

    public static String sessionKey(ClientTypeEnum clientType, String sessionId) {
        return SESSION_PREFIX + clientType.name() + ":" + sessionId;
    }

    public static String subjectKey(ClientTypeEnum clientType, SubjectTypeEnum subjectType, Long subjectId) {
        return SUBJECT_SESSIONS_PREFIX + clientType.name() + ":" + subjectType.name() + ":" + subjectId;
    }
}
