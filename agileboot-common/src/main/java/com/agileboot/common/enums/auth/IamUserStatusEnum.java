package com.agileboot.common.enums.auth;

import com.agileboot.common.enums.BasicEnum;

/**
 * 统一账号状态，与用户表状态值保持一致。
 */
public enum IamUserStatusEnum implements BasicEnum<Integer> {

    NORMAL(1, "正常"),
    DISABLED(2, "禁用"),
    FROZEN(3, "冻结");

    private final int value;

    private final String desc;

    IamUserStatusEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String description() {
        return desc;
    }

    public static IamUserStatusEnum fromValue(Integer value) {
        if (value != null) {
            for (IamUserStatusEnum status : values()) {
                if (status.value == value) {
                    return status;
                }
            }
        }
        return null;
    }
}
