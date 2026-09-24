package com.agileboot.admin.controller.system;

import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.common.core.page.PageDTO;
import com.agileboot.common.enums.auth.ClientTypeEnum;
import com.agileboot.common.enums.auth.SubjectTypeEnum;
import com.agileboot.domain.iam.auth.StaffBindingApplicationService;
import com.agileboot.domain.iam.auth.dto.IamUnboundUserDTO;
import com.agileboot.domain.iam.user.IamUserApplicationService;
import com.agileboot.domain.iam.user.command.ChangeIamUserStatusCommand;
import com.agileboot.domain.iam.user.dto.IamUserDTO;
import com.agileboot.domain.iam.user.query.IamUserQuery;
import com.agileboot.infrastructure.auth.AuthSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Web 后台统一账号查询接口。
 */
@Tag(name = "统一账号API", description = "小程序统一账号后台管理")
@RestController
@RequestMapping("/system/iam-users")
@RequiredArgsConstructor
public class IamUserController {

    private final StaffBindingApplicationService staffBindingApplicationService;

    private final IamUserApplicationService iamUserApplicationService;

    private final AuthSessionService authSessionService;

    @Operation(summary = "小程序用户列表")
    @PreAuthorize("@permission.has('system:miniapp-user:list')")
    @GetMapping
    public ResponseDTO<PageDTO<IamUserDTO>> list(IamUserQuery query) {
        return ResponseDTO.ok(iamUserApplicationService.getUserList(query));
    }

    @Operation(summary = "小程序用户详情")
    @PreAuthorize("@permission.has('system:miniapp-user:query')")
    @GetMapping("/{userId}")
    public ResponseDTO<IamUserDTO> detail(@PathVariable Long userId) {
        return ResponseDTO.ok(iamUserApplicationService.getUserDetail(userId));
    }

    @Operation(summary = "修改小程序用户状态")
    @PreAuthorize("@permission.has('system:miniapp-user:edit')")
    @PutMapping("/{userId}/status")
    public ResponseDTO<Void> changeStatus(@PathVariable Long userId,
        @Validated @RequestBody ChangeIamUserStatusCommand command) {
        command.setUserId(userId);
        iamUserApplicationService.changeStatus(command);
        authSessionService.invalidateSubject(ClientTypeEnum.WECHAT_MINIAPP, SubjectTypeEnum.MINIAPP_USER, userId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "查询待绑定小程序账号")
    @PreAuthorize("@permission.has('system:user:miniappBind') OR @permission.has('system:miniapp-user:list')")
    @GetMapping("/unbound")
    public ResponseDTO<List<IamUnboundUserDTO>> listUnboundMiniappUsers() {
        return ResponseDTO.ok(staffBindingApplicationService.listUnboundUsers());
    }
}
