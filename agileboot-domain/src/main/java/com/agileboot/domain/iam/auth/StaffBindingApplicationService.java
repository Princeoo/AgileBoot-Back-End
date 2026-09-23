package com.agileboot.domain.iam.auth;

import com.agileboot.common.enums.auth.IamUserStatusEnum;
import com.agileboot.common.enums.common.UserStatusEnum;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode;
import com.agileboot.domain.iam.auth.command.MiniappWorkbenchCommand;
import com.agileboot.domain.iam.auth.dto.IamUnboundUserDTO;
import com.agileboot.domain.iam.user.db.IamUserEntity;
import com.agileboot.domain.iam.user.db.IamUserService;
import com.agileboot.domain.system.user.db.SysUserEntity;
import com.agileboot.domain.system.user.db.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Web 后台手动管理员工与小程序统一账号的绑定关系。
 */
@Service
@RequiredArgsConstructor
public class StaffBindingApplicationService {

    private final IamUserService iamUserService;

    private final SysUserMapper sysUserMapper;

    public List<IamUnboundUserDTO> listUnboundUsers() {
        return iamUserService.listUnbound().stream().map(IamUnboundUserDTO::new).collect(Collectors.toList());
    }

    @Transactional
    public Long bind(Long sysUserId, Long iamUserId) {
        SysUserEntity sysUser = sysUserMapper.selectById(sysUserId);
        IamUserEntity iamUser = iamUserService.getById(iamUserId);
        if (sysUser == null || iamUser == null || Boolean.TRUE.equals(sysUser.getDeleted())
            || Boolean.TRUE.equals(iamUser.getDeleted())) {
            throw new ApiException(ErrorCode.Business.STAFF_BIND_INVALID);
        }
        if (!isNormal(sysUser.getStatus()) || !isNormalIam(iamUser.getStatus())) {
            throw new ApiException(ErrorCode.Business.STAFF_BIND_INVALID);
        }
        if (!Boolean.TRUE.equals(sysUser.getMiniappWorkbenchEnabled())) {
            throw new ApiException(ErrorCode.Business.STAFF_WORKBENCH_DISABLED);
        }
        if (iamUserId.equals(sysUser.getIamUserId())) {
            return iamUserId;
        }
        SysUserEntity existingSysUser = sysUserMapper.getByIamUserId(iamUserId);
        if (sysUser.getIamUserId() != null || existingSysUser != null) {
            throw new ApiException(ErrorCode.Business.STAFF_ALREADY_BOUND);
        }
        try {
            if (sysUserMapper.bindIamUser(sysUserId, iamUserId, true) != 1) {
                throw new ApiException(ErrorCode.Business.STAFF_ALREADY_BOUND);
            }
        } catch (DuplicateKeyException exception) {
            throw new ApiException(ErrorCode.Business.STAFF_ALREADY_BOUND);
        }
        return iamUserId;
    }

    @Transactional
    public Long unbind(Long sysUserId) {
        SysUserEntity sysUser = sysUserMapper.selectById(sysUserId);
        if (sysUser == null || Boolean.TRUE.equals(sysUser.getDeleted()) || sysUser.getIamUserId() == null) {
            throw new ApiException(ErrorCode.Business.STAFF_BIND_INVALID);
        }
        Long iamUserId = sysUser.getIamUserId();
        if (sysUserMapper.unbindIamUser(sysUserId) != 1) {
            throw new ApiException(ErrorCode.Business.STAFF_BIND_INVALID);
        }
        return iamUserId;
    }

    @Transactional
    public Long updateWorkbench(Long sysUserId, MiniappWorkbenchCommand command) {
        SysUserEntity sysUser = sysUserMapper.selectById(sysUserId);
        if (sysUser == null || Boolean.TRUE.equals(sysUser.getDeleted())) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, sysUserId, "用户");
        }
        if (Boolean.TRUE.equals(command.getEnabled()) && !isNormal(sysUser.getStatus())) {
            throw new ApiException(ErrorCode.Business.STAFF_BIND_INVALID);
        }
        if (sysUserMapper.updateMiniappWorkbench(sysUserId, Boolean.TRUE.equals(command.getEnabled())) != 1) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, sysUserId, "用户");
        }
        return sysUser.getIamUserId();
    }

    private boolean isNormal(Integer status) {
        return status != null && status == UserStatusEnum.NORMAL.getValue();
    }

    private boolean isNormalIam(Integer status) {
        return status != null && status == IamUserStatusEnum.NORMAL.getValue();
    }

}
