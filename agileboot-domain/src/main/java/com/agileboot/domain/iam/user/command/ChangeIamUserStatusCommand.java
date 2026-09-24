package com.agileboot.domain.iam.user.command;

import javax.validation.constraints.NotNull;
import lombok.Data;

/** 修改小程序账号状态。 */
@Data
public class ChangeIamUserStatusCommand {

    private Long userId;

    @NotNull
    private Integer status;
}
