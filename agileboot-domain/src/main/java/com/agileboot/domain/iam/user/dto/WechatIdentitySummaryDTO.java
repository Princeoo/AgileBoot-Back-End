package com.agileboot.domain.iam.user.dto;

import com.agileboot.domain.iam.wechat.db.IamWechatIdentityEntity;
import java.util.Date;
import lombok.Data;

/** 后台展示的微信身份概要，不暴露完整微信标识。 */
@Data
public class WechatIdentitySummaryDTO {

    private Long identityId;
    private String appId;
    private String maskedOpenId;
    private Date createTime;

    public WechatIdentitySummaryDTO(IamWechatIdentityEntity entity) {
        identityId = entity.getIdentityId();
        appId = entity.getAppId();
        maskedOpenId = mask(entity.getOpenId());
        createTime = entity.getCreateTime();
    }

    private String mask(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (value.length() <= 2) {
            return "****";
        }
        if (value.length() <= 8) {
            return value.substring(0, 1) + "****" + value.substring(value.length() - 1);
        }
        return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
    }
}
