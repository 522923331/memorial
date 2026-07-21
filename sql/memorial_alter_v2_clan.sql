-- =============================================
-- 族谱模块表结构 + 字典 + 菜单（P4.2）
-- 新增表：mem_clan / mem_clan_member / mem_clan_relation
-- 新增字典：memorial_clan_relation_type（1生父 2生母 3配偶 4养父 5养母 6继父 7继母）
-- 新增菜单：族谱管理（2008）+ 按钮（2080-2083）
-- 说明：项目未发布生产，DDL 已同步 memorial_ddl.sql，字典菜单已同步 memorial_menu_dict.sql。
--       本脚本用于已有环境补建族谱模块。
-- =============================================

-- ----------------------------
-- 1、族谱信息表
-- ----------------------------
drop table if exists mem_clan;
create table mem_clan (
  clan_id           bigint(20)      not null auto_increment    comment '族谱ID',
  clan_name         varchar(100)    not null default ''        comment '族谱名',
  surname           varchar(20)     not null default ''        comment '姓氏',
  family_user_id    bigint(20)      not null default 0         comment '族长用户ID（归属判定）',
  org_id            bigint(20)      not null default 0         comment '所属机构ID',
  root_member_id    bigint(20)      default null               comment '始祖成员ID（族谱树根）',
  cover_image       varchar(200)    not null default ''        comment '封面图（OSS URL）',
  description       text            default null               comment '族谱简介',
  is_public         char(1)         not null default '0'       comment '是否公开（0公开 1不公开）',
  show_alive_avatar char(1)         not null default '0'       comment '在世成员肖像对访客可见（0显示 1不显示）',
  qrcode_code       varchar(50)     not null default ''        comment '二维码编码',
  qrcode_url        varchar(200)    not null default ''        comment '二维码图片URL',
  member_count      int(11)         not null default 0         comment '成员数（缓存）',
  generation_count  int(11)         not null default 0         comment '世代数（缓存）',
  status            char(1)         not null default '0'       comment '状态（0正常 1停用，停用访客不可见）',
  del_flag          char(1)         not null default '0'       comment '删除标志（0存在 2删除）',
  create_by         varchar(64)     not null default ''        comment '创建者',
  create_time       datetime        not null default current_timestamp comment '创建时间',
  update_by         varchar(64)     not null default ''        comment '更新者',
  update_time       datetime        not null default current_timestamp on update current_timestamp comment '更新时间',
  primary key (clan_id),
  key idx_family_user_id (family_user_id),
  key idx_qrcode_code (qrcode_code)
) engine=innodb auto_increment=1 comment = '族谱信息表';

-- ----------------------------
-- 2、族谱成员表
-- ----------------------------
drop table if exists mem_clan_member;
create table mem_clan_member (
  member_id         bigint(20)      not null auto_increment    comment '成员ID',
  clan_id           bigint(20)      not null default 0         comment '所属族谱ID',
  name              varchar(50)     not null default ''        comment '姓名',
  gender            char(1)         not null default '0'       comment '性别（0男 1女 2未知）',
  birth_date        date            default null               comment '出生日期（在世对访客脱敏）',
  death_date        date            default null               comment '逝世日期（空=在世）',
  is_alive          char(1)         not null default '0'       comment '是否在世（0在世 1已故）',
  birth_place       varchar(100)    not null default ''        comment '出生地',
  title             varchar(50)     not null default ''        comment '字号/辈分字',
  generation        int(11)         not null default 1         comment '世代（始祖=1，CacheService 维护）',
  avatar            varchar(200)    not null default ''        comment '肖像URL',
  bio               text            default null               comment '简介（在世对访客脱敏）',
  deceased_id       bigint(20)      default null               comment '关联逝者纪念馆ID（仅已故+已建馆）',
  sort_order        int(11)         not null default 0         comment '同代排序',
  del_flag          char(1)         not null default '0'       comment '删除标志（0存在 2删除）',
  create_by         varchar(64)     not null default ''        comment '创建者',
  create_time       datetime        not null default current_timestamp comment '创建时间',
  update_by         varchar(64)     not null default ''        comment '更新者',
  update_time       datetime        not null default current_timestamp on update current_timestamp comment '更新时间',
  primary key (member_id),
  key idx_clan_id (clan_id),
  key idx_deceased_id (deceased_id),
  key idx_generation (generation)
) engine=innodb auto_increment=1 comment = '族谱成员表';

-- ----------------------------
-- 3、族谱关系表（支持生父母/配偶/养继父母/多段婚姻）
-- ----------------------------
drop table if exists mem_clan_relation;
create table mem_clan_relation (
  relation_id       bigint(20)      not null auto_increment    comment '关系ID',
  clan_id           bigint(20)      not null default 0         comment '所属族谱ID',
  from_member_id    bigint(20)      not null default 0         comment '子辈/本人',
  to_member_id      bigint(20)      not null default 0         comment '父辈/配偶',
  relation_type     tinyint(4)      not null default 1         comment '关系类型（1生父 2生母 3配偶 4养父 5养母 6继父 7继母）',
  relation_order    int(11)         not null default 0         comment '配偶第几段婚姻（默认0，父母类为0）',
  extra             varchar(500)    not null default ''        comment '备注（婚姻起止、收养说明等）',
  create_by         varchar(64)     not null default ''        comment '创建者',
  create_time       datetime        not null default current_timestamp comment '创建时间',
  primary key (relation_id),
  key idx_clan_id (clan_id),
  key idx_from_member (from_member_id),
  key idx_to_member (to_member_id)
) engine=innodb auto_increment=1 comment = '族谱关系表';

-- ----------------------------
-- 4、字典类型 memorial_clan_relation_type
-- ----------------------------
insert into sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
values (107, '族谱关系类型', 'memorial_clan_relation_type', '0', 'admin', sysdate(), '', null, '族谱成员关系类型');

-- ----------------------------
-- 5、字典数据
-- ----------------------------
insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
values (124, 1, '生父', '1', 'memorial_clan_relation_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '', null, '生父');
insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
values (125, 2, '生母', '2', 'memorial_clan_relation_type', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '生母');
insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
values (126, 3, '配偶', '3', 'memorial_clan_relation_type', '', 'info',    'N', '0', 'admin', sysdate(), '', null, '配偶（双向）');
insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
values (127, 4, '养父', '4', 'memorial_clan_relation_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '养父');
insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
values (128, 5, '养母', '5', 'memorial_clan_relation_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '养母');
insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
values (129, 6, '继父', '6', 'memorial_clan_relation_type', '', 'danger',  'N', '0', 'admin', sysdate(), '', null, '继父');
insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
values (130, 7, '继母', '7', 'memorial_clan_relation_type', '', 'danger',  'N', '0', 'admin', sysdate(), '', null, '继母');

-- ----------------------------
-- 6、菜单：族谱管理（二级菜单 + 按钮）
-- ----------------------------
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2008', '族谱管理', '2000', '8', 'clan', 'memorial/clan/index', '', '', 1, 0, 'C', '0', '0', 'memorial:clan:list', 'tree-table', 'admin', sysdate(), '', null, '族谱管理菜单');

insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2080', '族谱查询', '2008', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:clan:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2081', '族谱新增', '2008', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:clan:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2082', '族谱修改', '2008', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:clan:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values ('2083', '族谱删除', '2008', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'memorial:clan:remove', '#', 'admin', sysdate(), '', null, '');
