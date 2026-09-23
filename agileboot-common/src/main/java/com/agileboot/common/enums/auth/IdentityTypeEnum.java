package com.agileboot.common.enums.auth;

/**
 * 小程序登录身份。
 */
public enum IdentityTypeEnum {

    MINIAPP_USER("小程序用户"),
    STAFF("员工");

    private final String desc;

    IdentityTypeEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public String description() {
        return desc;
    }
}
