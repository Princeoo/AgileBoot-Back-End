package com.agileboot.domain.iam.auth.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * 小程序登录及身份上下文响应，不包含 OpenID、SessionKey 等敏感认证数据。
 */
@Data
public class MiniappAuthContextDTO {

    private MiniappUserDTO user;

    private String clientType;

    private List<String> identities = new ArrayList<>();

    private boolean workbenchEnabled;

    private String defaultEntry;

    private List<String> availableEntries = new ArrayList<>();

    private boolean switchEntryEnabled;

    private StaffDTO staff;

    private Map<String, Object> extensions = new LinkedHashMap<>();

    @Data
    public static class MiniappUserDTO {

        private Long userId;

        private String nickname;

        private String avatar;
    }

    @Data
    public static class StaffDTO {

        private Long sysUserId;

        private String displayName;
    }
}
