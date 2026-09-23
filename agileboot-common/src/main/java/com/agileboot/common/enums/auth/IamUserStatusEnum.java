package com.agileboot.common.enums.auth;

/**
 * 统一账号状态，与用户表状态值保持一致。
 */
public enum IamUserStatusEnum {

    NORMAL(1),
    DISABLED(2),
    FROZEN(3);

    private final int value;

    IamUserStatusEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
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
