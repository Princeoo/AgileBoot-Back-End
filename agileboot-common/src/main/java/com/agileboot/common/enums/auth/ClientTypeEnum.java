package com.agileboot.common.enums.auth;

/**
 * 登录客户端类型（从哪个客户端登录）
 */
public enum ClientTypeEnum {

    WEB_ADMIN("Web 管理端"),
    WECHAT_MINIAPP("微信小程序");

    private final String desc;

    ClientTypeEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public String description() {
        return desc;
    }
}
