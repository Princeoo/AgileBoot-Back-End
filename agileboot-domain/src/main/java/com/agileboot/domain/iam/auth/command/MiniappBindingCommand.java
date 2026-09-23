package com.agileboot.domain.iam.auth.command;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * Web 后台手动绑定小程序账号请求。
 */
@Data
public class MiniappBindingCommand {

    @NotNull(message = "统一账号ID不能为空")
    private Long iamUserId;
}
