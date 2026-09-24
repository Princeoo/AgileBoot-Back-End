package com.agileboot.domain.iam.wechat.db;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 微信身份 Mapper。
 */
public interface IamWechatIdentityMapper extends BaseMapper<IamWechatIdentityEntity> {

    @Select("SELECT * FROM iam_wechat_identity WHERE app_id = #{appId} "
        + "AND open_id = #{openId} AND deleted = 0 LIMIT 1")
    IamWechatIdentityEntity findByAppIdAndOpenId(@Param("appId") String appId,
        @Param("openId") String openId);

    @Select("SELECT * FROM iam_wechat_identity WHERE user_id = #{userId} "
        + "AND deleted = 0 ORDER BY create_time DESC")
    List<IamWechatIdentityEntity> listByUserId(@Param("userId") Long userId);
}
