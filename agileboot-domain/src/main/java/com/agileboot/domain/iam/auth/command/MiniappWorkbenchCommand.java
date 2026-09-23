package com.agileboot.domain.iam.auth.command;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * Web 后台工作台开关请求。
 */
@Data
public class MiniappWorkbenchCommand {

    @NotNull(message = "工作台开关不能为空")
    private Boolean enabled;
}
