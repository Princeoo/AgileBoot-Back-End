package com.agileboot.domain.iam.wechat.db;

import com.agileboot.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 微信小程序身份标识。
 */
@Getter
@Setter
@TableName("iam_wechat_identity")
public class IamWechatIdentityEntity extends BaseEntity<IamWechatIdentityEntity> {

    @TableId(value = "identity_id", type = IdType.AUTO)
    private Long identityId;

    @TableField("user_id")
    private Long userId;

    @TableField("app_id")
    private String appId;

    @TableField("open_id")
    private String openId;

    @TableField("union_id")
    private String unionId;

    @Override
    public java.io.Serializable pkVal() {
        return identityId;
    }
}
