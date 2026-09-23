package com.agileboot.infrastructure.user.miniapp;

import com.agileboot.infrastructure.user.base.BaseLoginUser;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 微信小程序登录主体。不携带 Web 菜单和项目权限。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class MiniappLoginUser extends BaseLoginUser {

    private static final long serialVersionUID = 1L;

    private Long iamUserId;

    private Integer accountStatus;

    private Long sysUserId;

    private boolean workbenchEnabled;

    private Set<String> identities = new LinkedHashSet<>();

    public MiniappLoginUser(Long iamUserId, Integer accountStatus, Long sysUserId,
        boolean workbenchEnabled, Set<String> identities) {
        super(iamUserId, String.valueOf(iamUserId), "");
        this.iamUserId = iamUserId;
        this.accountStatus = accountStatus;
        this.sysUserId = sysUserId;
        this.workbenchEnabled = workbenchEnabled;
        if (identities != null) {
            this.identities = new LinkedHashSet<>(identities);
        }
    }
}
