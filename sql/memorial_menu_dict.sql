-- =============================================
-- 纪念馆管理 - 菜单和字典初始化 SQL
-- 执行前提：先执行 ry_20250522.sql 初始化若依基础表
-- =============================================

-- ----------------------------
-- 1. 菜单数据 (sys_menu)
-- ----------------------------

-- 一级目录
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2000', '纪念馆管理', '0', '5', 'memorial', null, '', '', 1, 0, 'M', '0', '0', '', 'education', 'admin', sysdate(), '', null, '纪念馆管理目录');

-- 二级菜单
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2001', '逝者管理', '2000', '1', 'deceased',     'memorial/deceased/index',     '', '', 1, 0, 'C', '0', '0', 'memorial:deceased:list',     'peoples',    'admin', sysdate(), '', null, '逝者管理菜单');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2002', '订单管理', '2000', '2', 'order',        'memorial/order/index',        '', '', 1, 0, 'C', '0', '0', 'memorial:order:list',        'shopping',   'admin', sysdate(), '', null, '订单管理菜单');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2003', '留言管理', '2000', '3', 'message',      'memorial/message/index',      '', '', 1, 0, 'C', '0', '0', 'memorial:message:list',      'message',    'admin', sysdate(), '', null, '留言管理菜单');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2004', '献花记录', '2000', '4', 'flower',       'memorial/flower/index',       '', '', 1, 0, 'C', '0', '0', 'memorial:flower:list',       'star',       'admin', sysdate(), '', null, '献花记录菜单');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2005', '机构管理', '2000', '5', 'organization', 'memorial/organization/index', '', '', 1, 0, 'C', '0', '0', 'memorial:org:list',          'tree',       'admin', sysdate(), '', null, '机构管理菜单');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2006', '统计管理', '2000', '6', 'statistics',   'memorial/statistics/index',   '', '', 1, 0, 'C', '0', '0', 'memorial:statistics:list',   'chart',      'admin', sysdate(), '', null, '统计管理菜单');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2007', '视频管理', '2000', '7', 'video',        'memorial/video/index',        '', '', 1, 0, 'C', '0', '0', 'memorial:video:list',        'video',      'admin', sysdate(), '', null, '视频管理菜单');

-- 逝者管理按钮
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2010', '逝者查询', '2001', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:deceased:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2011', '逝者新增', '2001', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:deceased:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2012', '逝者修改', '2001', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:deceased:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2013', '逝者删除', '2001', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:deceased:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2014', '逝者导出', '2001', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:deceased:export', '#', 'admin', sysdate(), '', null, '');

-- 订单管理按钮
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2020', '订单查询', '2002', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:order:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2021', '订单新增', '2002', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:order:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2022', '订单修改', '2002', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:order:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2023', '订单删除', '2002', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:order:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2024', '订单导出', '2002', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:order:export', '#', 'admin', sysdate(), '', null, '');

-- 留言管理按钮
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2030', '留言查询', '2003', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:message:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2031', '留言审核', '2003', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:message:audit',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2032', '留言删除', '2003', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:message:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2033', '留言导出', '2003', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:message:export', '#', 'admin', sysdate(), '', null, '');

-- 献花记录按钮
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2040', '献花查询', '2004', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:flower:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2041', '献花删除', '2004', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:flower:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2042', '献花导出', '2004', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:flower:export', '#', 'admin', sysdate(), '', null, '');

-- 机构管理按钮
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2050', '机构查询', '2005', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:org:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2051', '机构新增', '2005', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:org:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2052', '机构修改', '2005', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:org:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2053', '机构删除', '2005', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:org:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2054', '机构导出', '2005', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:org:export', '#', 'admin', sysdate(), '', null, '');

-- 统计管理按钮
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2060', '统计查询', '2006', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:statistics:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2061', '统计删除', '2006', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:statistics:remove', '#', 'admin', sysdate(), '', null, '');

-- 视频管理按钮
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2070', '视频查询', '2007', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:video:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2071', '视频新增', '2007', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:video:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2072', '视频修改', '2007', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:video:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2073', '视频删除', '2007', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:video:remove', '#', 'admin', sysdate(), '', null, '');

-- ----------------------------
-- 2. 字典类型 (sys_dict_type)
-- ----------------------------

insert into sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
values (100, '订单类型',   'memorial_order_type',   '0', 'admin', sysdate(), '', null, '纪念订单类型列表');
insert into sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
values (101, '支付状态',   'memorial_pay_status',   '0', 'admin', sysdate(), '', null, '纪念订单支付状态');
insert into sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
values (102, '订单状态',   'memorial_order_status', '0', 'admin', sysdate(), '', null, '纪念订单状态列表');
insert into sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
values (103, '审核状态',   'memorial_audit_status', '0', 'admin', sysdate(), '', null, '留言审核状态列表');
insert into sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
values (104, '鲜花类型',   'memorial_flower_type',  '0', 'admin', sysdate(), '', null, '献花鲜花类型列表');
insert into sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
values (105, '机构类型',   'memorial_org_type',     '0', 'admin', sysdate(), '', null, '纪念机构类型列表');

-- ----------------------------
-- 3. 字典数据 (sys_dict_data)
-- ----------------------------

-- 订单类型 memorial_order_type
insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
values (100, 1, '基础纪念', '0', 'memorial_order_type', '', 'default', 'Y', '0', 'admin', sysdate(), '', null, '基础纪念套餐');
insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
values (101, 2, '高级纪念', '1', 'memorial_order_type', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '高级纪念套餐');
insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
values (102, 3, 'VIP纪念',  '2', 'memorial_order_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, 'VIP纪念套餐');

-- 支付状态 memorial_pay_status
insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
values (103, 1, '未支付', '0', 'memorial_pay_status', '', 'info',    'Y', '0', 'admin', sysdate(), '', null, '未支付');
insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
values (104, 2, '已支付', '1', 'memorial_pay_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '已支付');
insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
values (105, 3, '已退款', '2', 'memorial_pay_status', '', 'danger',  'N', '0', 'admin', sysdate(), '', null, '已退款');

-- 订单状态 memorial_order_status
insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
values (106, 1, '待处理', '0', 'memorial_order_status', '', 'info',    'Y', '0', 'admin', sysdate(), '', null, '待处理');
insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
values (107, 2, '进行中', '1', 'memorial_order_status', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '进行中');
insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
values (108, 3, '已完成', '2', 'memorial_order_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '已完成');
insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
values (109, 4, '已取消', '3', 'memorial_order_status', '', 'danger',  'N', '0', 'admin', sysdate(), '', null, '已取消');

-- 审核状态 memorial_audit_status
insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
values (110, 1, '待审核',     '0', 'memorial_audit_status', '', 'info',    'Y', '0', 'admin', sysdate(), '', null, '待审核');
insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
values (111, 2, '审核通过',   '1', 'memorial_audit_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '审核通过');
insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
values (112, 3, '审核不通过', '2', 'memorial_audit_status', '', 'danger',  'N', '0', 'admin', sysdate(), '', null, '审核不通过');

-- 鲜花类型 memorial_flower_type
insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
values (113, 1, '菊花',     '1', 'memorial_flower_type', '', 'default', 'Y', '0', 'admin', sysdate(), '', null, '菊花');
insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
values (114, 2, '百合',     '2', 'memorial_flower_type', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '百合');
insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
values (115, 3, '康乃馨',   '3', 'memorial_flower_type', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '康乃馨');
insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
values (116, 4, '玫瑰',     '4', 'memorial_flower_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '玫瑰');

-- 机构类型 memorial_org_type
insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
values (117, 1, '殡仪馆', '1', 'memorial_org_type', '', 'default', 'Y', '0', 'admin', sysdate(), '', null, '殡仪馆');
insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
values (118, 2, '公墓',   '2', 'memorial_org_type', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '公墓');
insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
values (119, 3, '骨灰堂', '3', 'memorial_org_type', '', 'info',    'N', '0', 'admin', sysdate(), '', null, '骨灰堂');
insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
values (120, 4, '纪念馆', '4', 'memorial_org_type', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '纪念馆');
