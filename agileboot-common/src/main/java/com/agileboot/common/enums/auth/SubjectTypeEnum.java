package com.agileboot.common.enums.auth;

/**
 * 登录主体类型（当前 Token 对应哪种账号主体）
 */
public enum SubjectTypeEnum {

    SYS_USER("系统用户"),
    MINIAPP_USER("小程序用户");

    private final String desc;

    SubjectTypeEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public String description() {
        return desc;
    }
}
