package com.agileboot.domain.iam.wechat.db;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 微信身份服务实现。
 */
@Service
public class IamWechatIdentityServiceImpl extends ServiceImpl<IamWechatIdentityMapper,
    IamWechatIdentityEntity> implements IamWechatIdentityService {

    @Override
    public IamWechatIdentityEntity findByAppIdAndOpenId(String appId, String openId) {
        return baseMapper.findByAppIdAndOpenId(appId, openId);
    }
}
