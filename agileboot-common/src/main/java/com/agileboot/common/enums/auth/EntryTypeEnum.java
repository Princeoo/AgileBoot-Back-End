package com.agileboot.common.enums.auth;

/**
 * 小程序入口。
 */
public enum EntryTypeEnum {

    PATIENT("患者端"),
    WORKBENCH("员工工作台");

    private final String desc;

    EntryTypeEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public String description() {
        return desc;
    }
}
