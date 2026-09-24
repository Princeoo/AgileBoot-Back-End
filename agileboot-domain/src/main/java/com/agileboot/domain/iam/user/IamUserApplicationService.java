package com.agileboot.domain.iam.user;

import com.agileboot.common.core.page.PageDTO;
import com.agileboot.common.enums.auth.IamUserStatusEnum;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode;
import com.agileboot.domain.iam.user.command.ChangeIamUserStatusCommand;
import com.agileboot.domain.iam.user.db.IamUserDO;
import com.agileboot.domain.iam.user.db.IamUserEntity;
import com.agileboot.domain.iam.user.db.IamUserService;
import com.agileboot.domain.iam.user.dto.IamUserDTO;
import com.agileboot.domain.iam.user.dto.WechatIdentitySummaryDTO;
import com.agileboot.domain.iam.user.query.IamUserQuery;
import com.agileboot.domain.iam.wechat.db.IamWechatIdentityEntity;
import com.agileboot.domain.iam.wechat.db.IamWechatIdentityService;
import com.agileboot.domain.system.user.db.SysUserEntity;
import com.agileboot.domain.system.user.db.SysUserMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 小程序用户后台管理应用服务。 */
@Service
@RequiredArgsConstructor
public class IamUserApplicationService {

    private final IamUserService iamUserService;

    private final SysUserMapper sysUserMapper;

    private final IamWechatIdentityService wechatIdentityService;

    public PageDTO<IamUserDTO> getUserList(IamUserQuery query) {
        Page<IamUserDO> page = iamUserService.getUserList(query);
        List<IamUserDTO> rows = page.getRecords().stream().map(IamUserDTO::new).collect(Collectors.toList());
        return new PageDTO<>(rows, page.getTotal());
    }

    public IamUserDTO getUserDetail(Long userId) {
        IamUserEntity user = iamUserService.getById(userId);
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, userId, "小程序用户");
        }
        IamUserDO detail = new IamUserDO();
        BeanUtils.copyProperties(user, detail);
        SysUserEntity sysUser = sysUserMapper.getByIamUserId(userId);
        if (sysUser != null) {
            detail.setBoundSysUserId(sysUser.getUserId());
            detail.setBoundSysUsername(sysUser.getUsername());
            detail.setBoundSysUserNickname(sysUser.getNickname());
            detail.setBoundWorkbenchEnabled(sysUser.getMiniappWorkbenchEnabled());
        }
        List<IamWechatIdentityEntity> identities = wechatIdentityService.listByUserId(userId);
        IamUserDTO result = new IamUserDTO(detail);
        result.setWechatIdentityCount(identities.size());
        result.setWechatIdentities(identities.stream()
            .map(WechatIdentitySummaryDTO::new)
            .collect(Collectors.toList()));
        return result;
    }

    @Transactional
    public void changeStatus(ChangeIamUserStatusCommand command) {
        if (IamUserStatusEnum.fromValue(command.getStatus()) == null) {
            throw new ApiException(ErrorCode.Business.IAM_USER_STATUS_INVALID, command.getStatus());
        }
        IamUserEntity user = iamUserService.getById(command.getUserId());
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, command.getUserId(), "小程序用户");
        }
        user.setStatus(command.getStatus());
        if (!iamUserService.updateById(user)) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, command.getUserId(), "小程序用户");
        }
    }
}
