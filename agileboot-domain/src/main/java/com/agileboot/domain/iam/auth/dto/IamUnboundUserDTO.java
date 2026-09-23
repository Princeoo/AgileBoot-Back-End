package com.agileboot.domain.iam.auth.dto;

import com.agileboot.domain.iam.user.db.IamUserEntity;
import java.util.Date;
import lombok.Data;

/**
 * 后台待绑定账号列表项，只返回用于人工核对的非敏感字段。
 */
@Data
public class IamUnboundUserDTO {

    private Long iamUserId;

    private String nickname;

    private String avatar;

    private String phoneNumber;

    private Integer status;

    private Date lastLoginTime;

    public IamUnboundUserDTO(IamUserEntity entity) {
        this.iamUserId = entity.getUserId();
        this.nickname = entity.getNickname();
        this.avatar = entity.getAvatar();
        this.phoneNumber = entity.getPhoneNumber();
        this.status = entity.getStatus();
        this.lastLoginTime = entity.getLastLoginTime();
    }
}
