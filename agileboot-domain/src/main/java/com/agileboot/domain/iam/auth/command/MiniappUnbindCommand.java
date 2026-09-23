package com.agileboot.domain.iam.auth.command;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Web 后台解绑员工请求。
 */
@Data
public class MiniappUnbindCommand {

    @NotBlank(message = "解绑原因不能为空")
    private String reason;
}
