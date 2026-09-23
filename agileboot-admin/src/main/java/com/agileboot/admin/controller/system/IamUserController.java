package com.agileboot.admin.controller.system;

import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.domain.iam.auth.StaffBindingApplicationService;
import com.agileboot.domain.iam.auth.dto.IamUnboundUserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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

    @Operation(summary = "查询待绑定小程序账号")
    @PreAuthorize("@permission.has('system:user:miniappBind')")
    @GetMapping("/unbound")
    public ResponseDTO<List<IamUnboundUserDTO>> listUnboundMiniappUsers() {
        return ResponseDTO.ok(staffBindingApplicationService.listUnboundUsers());
    }
}
