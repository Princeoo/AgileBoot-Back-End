package com.agileboot.domain.iam.user.db;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;

/** 小程序用户列表查询结果，附带绑定的后台用户概要。 */
@Getter
@Setter
public class IamUserDO extends IamUserEntity {

    @TableField("bound_sys_user_id")
    private Long boundSysUserId;

    @TableField("bound_sys_username")
    private String boundSysUsername;

    @TableField("bound_sys_user_nickname")
    private String boundSysUserNickname;

    @TableField("bound_workbench_enabled")
    private Boolean boundWorkbenchEnabled;

    @TableField("wechat_identity_count")
    private Integer wechatIdentityCount;
}
