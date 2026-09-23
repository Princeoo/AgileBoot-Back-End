package com.agileboot.domain.iam.auth;

import com.agileboot.common.enums.auth.ClientTypeEnum;
import com.agileboot.common.enums.auth.EntryTypeEnum;
import com.agileboot.common.enums.auth.IdentityTypeEnum;
import com.agileboot.common.enums.auth.IamUserStatusEnum;
import com.agileboot.common.enums.common.UserStatusEnum;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode;
import com.agileboot.domain.iam.auth.dto.MiniappAuthContextDTO;
import com.agileboot.domain.iam.user.IamAccountProvisioningService;
import com.agileboot.domain.iam.user.db.IamUserEntity;
import com.agileboot.domain.iam.user.db.IamUserService;
import com.agileboot.domain.iam.wechat.db.IamWechatIdentityEntity;
import com.agileboot.domain.iam.wechat.db.IamWechatIdentityService;
import com.agileboot.domain.system.user.db.SysUserEntity;
import com.agileboot.domain.system.user.db.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.Arrays;
import java.util.Date;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * 小程序统一账号登录与身份上下文服务。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MiniappAuthApplicationService {

    private final IamUserService iamUserService;

    private final IamWechatIdentityService identityService;

    private final IamAccountProvisioningService provisioningService;

    private final SysUserMapper sysUserMapper;

    /**
     * 使用微信服务端返回的 appId/openId 登录。openId 不接受前端传值。
     */
    public MiniappAuthContextDTO login(String appId, String openId, String unionId, String nickname,
        String avatar, String ipAddress) {
        IamWechatIdentityEntity identity = identityService.findByAppIdAndOpenId(appId, openId);
        if (identity == null) {
            try {
                identity = provisioningService.createWechatIdentity(appId, openId, unionId, nickname, avatar);
            } catch (DuplicateKeyException duplicateKeyException) {
                // 首次并发登录由数据库唯一键仲裁，失败事务回滚后读取已提交的身份。
                identity = identityService.findByAppIdAndOpenId(appId, openId);
                if (identity == null) {
                    throw duplicateKeyException;
                }
            }
        }

        IamUserEntity iamUser = iamUserService.getById(identity.getUserId());
        if (iamUser == null || Boolean.TRUE.equals(iamUser.getDeleted())) {
            throw new ApiException(ErrorCode.Business.IAM_ACCOUNT_DISABLED);
        }
        assertLoginAllowed(iamUser);

        boolean changed = false;
        if (notBlank(nickname) && !nickname.equals(iamUser.getNickname())) {
            iamUser.setNickname(nickname);
            changed = true;
        }
        if (notBlank(avatar) && !avatar.equals(iamUser.getAvatar())) {
            iamUser.setAvatar(avatar);
            changed = true;
        }
        iamUser.setLastLoginIp(defaultValue(ipAddress, ""));
        iamUser.setLastLoginTime(new Date());
        changed = true;
        if (changed) {
            iamUserService.updateById(iamUser);
        }

        return buildContext(iamUser);
    }

    /**
     * 从数据库实时计算当前入口，避免绑定或停用后继续使用旧身份摘要。
     */
    public MiniappAuthContextDTO getContext(Long iamUserId) {
        IamUserEntity iamUser = iamUserService.getById(iamUserId);
        if (iamUser == null || Boolean.TRUE.equals(iamUser.getDeleted())) {
            throw new ApiException(ErrorCode.Business.IAM_ACCOUNT_DISABLED);
        }
        assertLoginAllowed(iamUser);
        return buildContext(iamUser);
    }

    public void assertLoginAllowed(Long iamUserId) {
        IamUserEntity iamUser = iamUserService.getById(iamUserId);
        if (iamUser == null || Boolean.TRUE.equals(iamUser.getDeleted())) {
            throw new ApiException(ErrorCode.Business.IAM_ACCOUNT_DISABLED);
        }
        assertLoginAllowed(iamUser);
    }

    private void assertLoginAllowed(IamUserEntity iamUser) {
        IamUserStatusEnum status = IamUserStatusEnum.fromValue(iamUser.getStatus());
        if (status == IamUserStatusEnum.FROZEN) {
            throw new ApiException(ErrorCode.Business.IAM_ACCOUNT_FROZEN);
        }
        if (status != IamUserStatusEnum.NORMAL) {
            throw new ApiException(ErrorCode.Business.IAM_ACCOUNT_DISABLED);
        }
    }

    private MiniappAuthContextDTO buildContext(IamUserEntity iamUser) {
        SysUserEntity sysUser = sysUserMapper.getByIamUserId(iamUser.getUserId());
        boolean workbenchEnabled = isWorkbenchEnabled(iamUser, sysUser);

        MiniappAuthContextDTO context = new MiniappAuthContextDTO();
        MiniappAuthContextDTO.MiniappUserDTO user = new MiniappAuthContextDTO.MiniappUserDTO();
        user.setUserId(iamUser.getUserId());
        user.setNickname(defaultValue(iamUser.getNickname(), "微信用户"));
        user.setAvatar(defaultValue(iamUser.getAvatar(), ""));
        context.setUser(user);
        context.setClientType(ClientTypeEnum.WECHAT_MINIAPP.name());
        context.setIdentities(workbenchEnabled
            ? Arrays.asList(IdentityTypeEnum.MINIAPP_USER.name(), IdentityTypeEnum.STAFF.name())
            : Collections.singletonList(IdentityTypeEnum.MINIAPP_USER.name()));
        context.setWorkbenchEnabled(workbenchEnabled);
        // 患者端是统一小程序的默认入口。员工身份仅额外展示工作台快捷入口，
        // 不在登录时强制跳转，避免员工无法使用患者端能力。
        context.setDefaultEntry(EntryTypeEnum.PATIENT.name());
        context.setAvailableEntries(workbenchEnabled
            ? Arrays.asList(EntryTypeEnum.PATIENT.name(), EntryTypeEnum.WORKBENCH.name())
            : Collections.singletonList(EntryTypeEnum.PATIENT.name()));
        context.setSwitchEntryEnabled(false);
        if (workbenchEnabled) {
            MiniappAuthContextDTO.StaffDTO staff = new MiniappAuthContextDTO.StaffDTO();
            staff.setSysUserId(sysUser.getUserId());
            staff.setDisplayName(defaultValue(sysUser.getNickname(), sysUser.getUsername()));
            context.setStaff(staff);
        }
        return context;
    }

    private boolean isWorkbenchEnabled(IamUserEntity iamUser, SysUserEntity sysUser) {
        return iamUser.getStatus() != null
            && iamUser.getStatus() == IamUserStatusEnum.NORMAL.getValue()
            && sysUser != null
            && !Boolean.TRUE.equals(sysUser.getDeleted())
            && sysUser.getStatus() != null
            && sysUser.getStatus() == UserStatusEnum.NORMAL.getValue()
            && Boolean.TRUE.equals(sysUser.getMiniappWorkbenchEnabled());
    }

    private String defaultValue(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
