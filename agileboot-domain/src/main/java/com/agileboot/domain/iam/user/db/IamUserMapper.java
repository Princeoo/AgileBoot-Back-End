package com.agileboot.domain.iam.user.db;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
 * 统一账号 Mapper。
 */
public interface IamUserMapper extends BaseMapper<IamUserEntity> {

    @Select("SELECT * FROM iam_user WHERE deleted = 0 AND user_id NOT IN "
        + "(SELECT iam_user_id FROM sys_user WHERE iam_user_id IS NOT NULL AND deleted = 0) "
        + "ORDER BY create_time DESC")
    java.util.List<IamUserEntity> listUnbound();
}
