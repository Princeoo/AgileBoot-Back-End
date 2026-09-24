package com.agileboot.domain.iam.user.db;

import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.agileboot.domain.iam.user.query.IamUserQuery;

/**
 * 统一账号服务。
 */
public interface IamUserService extends IService<IamUserEntity> {

    List<IamUserEntity> listUnbound();

    Page<IamUserDO> getUserList(IamUserQuery query);
}
