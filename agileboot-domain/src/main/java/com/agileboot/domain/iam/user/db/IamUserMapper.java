package com.agileboot.domain.iam.user.db;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.agileboot.domain.iam.user.query.IamUserQuery;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 统一账号 Mapper。
 */
public interface IamUserMapper extends BaseMapper<IamUserEntity> {

    @Select("SELECT * FROM iam_user WHERE deleted = 0 AND user_id NOT IN "
        + "(SELECT iam_user_id FROM sys_user WHERE iam_user_id IS NOT NULL AND deleted = 0) "
        + "ORDER BY create_time DESC")
    java.util.List<IamUserEntity> listUnbound();

    @Select("SELECT iu.*, su.user_id AS bound_sys_user_id, su.username AS bound_sys_username, "
        + "su.nickname AS bound_sys_user_nickname, "
        + "su.miniapp_workbench_enabled AS bound_workbench_enabled, "
        + "(SELECT COUNT(1) FROM iam_wechat_identity iwi WHERE iwi.user_id = iu.user_id "
        + "AND iwi.deleted = 0) AS wechat_identity_count "
        + "FROM iam_user iu LEFT JOIN sys_user su ON su.iam_user_id = iu.user_id AND su.deleted = 0 "
        + "${ew.customSqlSegment}")
    Page<IamUserDO> getUserList(Page<IamUserDO> page,
        @Param(Constants.WRAPPER) Wrapper<IamUserDO> queryWrapper);
}
