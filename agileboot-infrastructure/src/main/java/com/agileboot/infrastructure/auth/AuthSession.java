package com.agileboot.infrastructure.auth;

import com.agileboot.common.enums.auth.ClientTypeEnum;
import com.agileboot.common.enums.auth.SubjectTypeEnum;
import com.agileboot.infrastructure.user.miniapp.MiniappLoginUser;
import java.io.Serializable;
import lombok.Data;

/**
 * 服务端认证会话。JWT 只保存 sessionId，完整上下文放在 Redis。
 */
@Data
public class AuthSession implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sessionId;

    private ClientTypeEnum clientType;

    private SubjectTypeEnum subjectType;

    private Long subjectId;

    private long issuedAt;

    private long expiresAt;

    private long lastAccessAt;

    private long identityVersion;

    private MiniappLoginUser principal;
}
