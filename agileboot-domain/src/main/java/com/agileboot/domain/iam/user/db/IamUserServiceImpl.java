package com.agileboot.domain.iam.user.db;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.agileboot.domain.iam.user.query.IamUserQuery;
import org.springframework.stereotype.Service;

/**
 * 统一账号服务实现。
 */
@Service
public class IamUserServiceImpl extends ServiceImpl<IamUserMapper, IamUserEntity>
    implements IamUserService {

    @Override
    public List<IamUserEntity> listUnbound() {
        return baseMapper.listUnbound();
    }

    @Override
    public Page<IamUserDO> getUserList(IamUserQuery query) {
        return baseMapper.getUserList(query.toPage(), query.toQueryWrapper());
    }
}
