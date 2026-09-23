package com.agileboot.domain.iam.user;

import com.agileboot.common.enums.auth.IamUserStatusEnum;
import com.agileboot.domain.iam.user.db.IamUserEntity;
import com.agileboot.domain.iam.user.db.IamUserService;
import com.agileboot.domain.iam.wechat.db.IamWechatIdentityEntity;
import com.agileboot.domain.iam.wechat.db.IamWechatIdentityService;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 首次微信登录时创建统一账号和微信身份，两个写入必须处于同一事务。
 */
@Service
@RequiredArgsConstructor
public class IamAccountProvisioningService {

    private final IamUserService iamUserService;

    private final IamWechatIdentityService identityService;

    @Transactional
    public IamWechatIdentityEntity createWechatIdentity(String appId, String openId, String unionId,
        String nickname, String avatar) {
        Date now = new Date();
        IamUserEntity iamUser = new IamUserEntity();
        iamUser.setNickname(defaultValue(nickname, "微信用户"));
        iamUser.setAvatar(defaultValue(avatar, ""));
        iamUser.setPhoneNumber("");
        iamUser.setStatus(IamUserStatusEnum.NORMAL.getValue());
        iamUser.setCreateTime(now);
        iamUser.setDeleted(false);
        iamUserService.save(iamUser);

        IamWechatIdentityEntity identity = new IamWechatIdentityEntity();
        identity.setUserId(iamUser.getUserId());
        identity.setAppId(appId);
        identity.setOpenId(openId);
        identity.setUnionId(defaultValue(unionId, ""));
        identity.setCreateTime(now);
        identity.setDeleted(false);
        identityService.save(identity);
        return identity;
    }

    private String defaultValue(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }
}
