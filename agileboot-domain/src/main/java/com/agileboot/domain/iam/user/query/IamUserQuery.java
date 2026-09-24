package com.agileboot.domain.iam.user.query;

import cn.hutool.core.util.StrUtil;
import com.agileboot.common.core.page.AbstractPageQuery;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.agileboot.domain.iam.user.db.IamUserDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 小程序用户分页查询条件。 */
@EqualsAndHashCode(callSuper = true)
@Data
public class IamUserQuery extends AbstractPageQuery<IamUserDO> {

    private String nickname;

    private String phoneNumber;

    private Integer status;

    /** 是否已绑定后台系统用户。 */
    private Boolean bound;

    @Override
    public QueryWrapper<IamUserDO> addQueryCondition() {
        QueryWrapper<IamUserDO> wrapper = new QueryWrapper<>();
        wrapper.like(StrUtil.isNotEmpty(nickname), "iu.nickname", nickname)
            .like(StrUtil.isNotEmpty(phoneNumber), "iu.phone_number", phoneNumber)
            .eq(status != null, "iu.status", status)
            .apply(Boolean.TRUE.equals(bound), "su.user_id IS NOT NULL")
            .isNull(Boolean.FALSE.equals(bound), "su.user_id")
            .eq("iu.deleted", 0);
        wrapper.orderByDesc(StrUtil.isEmpty(orderColumn), "iu.create_time");
        timeRangeColumn = "iu.create_time";
        return wrapper;
    }

    @Override
    public void addSortCondition(QueryWrapper<IamUserDO> wrapper) {
        if (wrapper == null || StrUtil.isEmpty(orderColumn)) {
            return;
        }
        String column = null;
        if ("createTime".equals(orderColumn)) {
            column = "iu.create_time";
        } else if ("lastLoginTime".equals(orderColumn)) {
            column = "iu.last_login_time";
        }
        Boolean ascending = convertSortDirection();
        if (column != null && ascending != null) {
            wrapper.orderBy(true, ascending, column);
        }
    }
}
