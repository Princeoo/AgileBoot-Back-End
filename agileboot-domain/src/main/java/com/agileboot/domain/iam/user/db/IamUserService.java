package com.agileboot.domain.iam.user.db;

import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 统一账号服务。
 */
public interface IamUserService extends IService<IamUserEntity> {

    List<IamUserEntity> listUnbound();
}
