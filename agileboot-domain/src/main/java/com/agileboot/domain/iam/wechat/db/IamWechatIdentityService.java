package com.agileboot.domain.iam.wechat.db;

import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 微信身份服务。
 */
public interface IamWechatIdentityService extends IService<IamWechatIdentityEntity> {

    IamWechatIdentityEntity findByAppIdAndOpenId(String appId, String openId);

    List<IamWechatIdentityEntity> listByUserId(Long userId);
}
