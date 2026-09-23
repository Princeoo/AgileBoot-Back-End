package com.agileboot.domain.iam.user.db;

import com.agileboot.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * 小程序统一登录账号。
 */
@Getter
@Setter
@TableName("iam_user")
public class IamUserEntity extends BaseEntity<IamUserEntity> {

    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    @TableField("nickname")
    private String nickname;

    @TableField("avatar")
    private String avatar;

    @TableField("phone_number")
    private String phoneNumber;

    @TableField("status")
    private Integer status;

    @TableField("last_login_ip")
    private String lastLoginIp;

    @TableField("last_login_time")
    private Date lastLoginTime;

    @Override
    public java.io.Serializable pkVal() {
        return userId;
    }
}
