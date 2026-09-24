-- 小程序用户后台管理菜单及权限（已有 MySQL 环境执行一次）。
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, deleted)
VALUES
    (69, '小程序用户', 1, 'MiniappUser', 1, '/system/miniapp-user/index', 0,
        'system:miniapp-user:list', '{"title":"小程序用户","icon":"ep:cellphone","showParent":true}',
        1, '小程序用户管理菜单', 0, NOW(), 0),
    (70, '小程序用户查询', 0, ' ', 69, '', 1, 'system:miniapp-user:query',
        '{"title":"小程序用户查询"}', 1, '', 0, NOW(), 0),
    (71, '小程序用户状态修改', 0, ' ', 69, '', 1, 'system:miniapp-user:edit',
        '{"title":"小程序用户状态修改"}', 1, '', 0, NOW(), 0);

-- 与初始化数据保持一致，为普通管理员角色授予新菜单；其他角色按需在后台分配。
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
    (2, 69),
    (2, 70),
    (2, 71);
