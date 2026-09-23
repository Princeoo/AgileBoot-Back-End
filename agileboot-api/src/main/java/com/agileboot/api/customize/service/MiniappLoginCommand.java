package com.agileboot.api.customize.service;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信小程序登录请求。
 */
@Data
public class MiniappLoginCommand {

    private String appKey;

    @NotBlank(message = "微信登录凭证不能为空")
    private String code;

    private String nickname;

    private String avatar;
}
