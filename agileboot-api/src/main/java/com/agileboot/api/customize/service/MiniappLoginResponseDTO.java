package com.agileboot.api.customize.service;

import com.agileboot.domain.iam.auth.dto.MiniappAuthContextDTO;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * 小程序登录响应，在身份上下文之外附带访问令牌。
 */
@Data
public class MiniappLoginResponseDTO {

    private String accessToken;

    private String tokenType = "Bearer";

    private long expiresIn;

    private String sessionId;

    private String clientType;

    private MiniappAuthContextDTO.MiniappUserDTO user;

    private List<String> identities;

    private boolean workbenchEnabled;

    private String defaultEntry;

    private List<String> availableEntries;

    private boolean switchEntryEnabled;

    private MiniappAuthContextDTO.StaffDTO staff;

    private Map<String, Object> extensions;

    public static MiniappLoginResponseDTO of(MiniappTokenService.IssuedToken token,
        MiniappAuthContextDTO context) {
        MiniappLoginResponseDTO result = new MiniappLoginResponseDTO();
        result.accessToken = token.getAccessToken();
        result.expiresIn = token.getExpiresIn();
        result.sessionId = token.getSessionId();
        result.clientType = context.getClientType();
        result.user = context.getUser();
        result.identities = context.getIdentities();
        result.workbenchEnabled = context.isWorkbenchEnabled();
        result.defaultEntry = context.getDefaultEntry();
        result.availableEntries = context.getAvailableEntries();
        result.switchEntryEnabled = context.isSwitchEntryEnabled();
        result.staff = context.getStaff();
        result.extensions = context.getExtensions();
        return result;
    }
}
