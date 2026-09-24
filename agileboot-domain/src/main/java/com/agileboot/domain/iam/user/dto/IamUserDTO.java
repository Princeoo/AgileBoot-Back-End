package com.agileboot.domain.iam.user.dto;

import com.agileboot.domain.iam.user.db.IamUserDO;
import com.agileboot.domain.iam.user.db.IamUserEntity;
import java.util.Date;
import java.util.List;
import lombok.Data;

/** 小程序用户后台展示模型，不包含 OpenID、UnionID 等敏感标识。 */
@Data
public class IamUserDTO {

    private Long iamUserId;
    private String nickname;
    private String avatar;
    private String phoneNumber;
    private Integer status;
    private Date createTime;
    private Date lastLoginTime;
    private Long boundSysUserId;
    private String boundSysUsername;
    private String boundSysUserNickname;
    private Boolean boundWorkbenchEnabled;
    private Integer wechatIdentityCount;
    private List<WechatIdentitySummaryDTO> wechatIdentities;

    public IamUserDTO(IamUserEntity entity) {
        iamUserId = entity.getUserId();
        nickname = entity.getNickname();
        avatar = entity.getAvatar();
        phoneNumber = entity.getPhoneNumber();
        status = entity.getStatus();
        createTime = entity.getCreateTime();
        lastLoginTime = entity.getLastLoginTime();
        if (entity instanceof IamUserDO) {
            IamUserDO result = (IamUserDO) entity;
            boundSysUserId = result.getBoundSysUserId();
            boundSysUsername = result.getBoundSysUsername();
            boundSysUserNickname = result.getBoundSysUserNickname();
            boundWorkbenchEnabled = result.getBoundWorkbenchEnabled();
            wechatIdentityCount = result.getWechatIdentityCount();
        }
    }
}
