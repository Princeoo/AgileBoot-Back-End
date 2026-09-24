package com.agileboot.domain.iam.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agileboot.common.core.page.PageDTO;
import com.agileboot.common.enums.auth.IamUserStatusEnum;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode;
import com.agileboot.domain.iam.user.command.ChangeIamUserStatusCommand;
import com.agileboot.domain.iam.user.db.IamUserDO;
import com.agileboot.domain.iam.user.db.IamUserEntity;
import com.agileboot.domain.iam.user.db.IamUserService;
import com.agileboot.domain.iam.user.dto.IamUserDTO;
import com.agileboot.domain.iam.user.query.IamUserQuery;
import com.agileboot.domain.iam.wechat.db.IamWechatIdentityService;
import com.agileboot.domain.system.user.db.SysUserEntity;
import com.agileboot.domain.system.user.db.SysUserMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IamUserApplicationServiceTest {

    private IamUserService iamUserService;
    private SysUserMapper sysUserMapper;
    private IamWechatIdentityService wechatIdentityService;
    private IamUserApplicationService applicationService;

    @BeforeEach
    void setUp() {
        iamUserService = mock(IamUserService.class);
        sysUserMapper = mock(SysUserMapper.class);
        wechatIdentityService = mock(IamWechatIdentityService.class);
        applicationService = new IamUserApplicationService(iamUserService, sysUserMapper, wechatIdentityService);
    }

    @Test
    void shouldReturnPagedUsersWithBindingSummary() {
        IamUserDO user = new IamUserDO();
        user.setUserId(11L);
        user.setNickname("mini user");
        user.setBoundSysUserId(21L);
        user.setBoundSysUsername("employee");
        Page<IamUserDO> page = new Page<>(1, 10, 1);
        page.setRecords(Collections.singletonList(user));
        when(iamUserService.getUserList(any(IamUserQuery.class))).thenReturn(page);

        PageDTO<IamUserDTO> result = applicationService.getUserList(new IamUserQuery());

        assertEquals(1L, result.getTotal());
        assertEquals(11L, result.getRows().get(0).getIamUserId());
        assertEquals(21L, result.getRows().get(0).getBoundSysUserId());
        assertEquals("employee", result.getRows().get(0).getBoundSysUsername());
    }

    @Test
    void shouldReturnDetailWithBindingSummary() {
        IamUserEntity user = new IamUserEntity();
        user.setUserId(11L);
        user.setNickname("mini user");
        SysUserEntity sysUser = new SysUserEntity();
        sysUser.setUserId(21L);
        sysUser.setUsername("employee");
        when(iamUserService.getById(11L)).thenReturn(user);
        when(sysUserMapper.getByIamUserId(11L)).thenReturn(sysUser);
        when(wechatIdentityService.listByUserId(11L)).thenReturn(Collections.emptyList());

        IamUserDTO result = applicationService.getUserDetail(11L);

        assertEquals(21L, result.getBoundSysUserId());
        assertEquals("employee", result.getBoundSysUsername());
    }

    @Test
    void shouldRejectInvalidStatus() {
        ChangeIamUserStatusCommand command = new ChangeIamUserStatusCommand();
        command.setUserId(11L);
        command.setStatus(99);

        ApiException exception = assertThrows(ApiException.class, () -> applicationService.changeStatus(command));

        assertEquals(ErrorCode.Business.IAM_USER_STATUS_INVALID, exception.getErrorCode());
    }

    @Test
    void shouldUpdateValidStatus() {
        IamUserEntity user = new IamUserEntity();
        user.setUserId(11L);
        user.setStatus(IamUserStatusEnum.NORMAL.getValue());
        when(iamUserService.getById(11L)).thenReturn(user);
        when(iamUserService.updateById(user)).thenReturn(true);
        ChangeIamUserStatusCommand command = new ChangeIamUserStatusCommand();
        command.setUserId(11L);
        command.setStatus(IamUserStatusEnum.DISABLED.getValue());

        applicationService.changeStatus(command);

        assertEquals(IamUserStatusEnum.DISABLED.getValue(), user.getStatus());
        verify(iamUserService).updateById(user);
    }
}
