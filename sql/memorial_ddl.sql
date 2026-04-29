-- =============================================
-- 纪念馆管理 - 数据库表结构 DDL
-- 执行前提：先执行 ry_20250522.sql 初始化若依基础表
-- =============================================

-- ----------------------------
-- 1、机构信息表
-- ----------------------------
drop table if exists mem_organization;
create table mem_organization (
  org_id          bigint(20)      not null auto_increment    comment '机构ID',
  org_name        varchar(100)    not null default ''        comment '机构名称',
  org_code        varchar(50)     not null default ''        comment '机构编码',
  contact_name    varchar(30)     not null default ''        comment '联系人',
  contact_phone   varchar(20)     not null default ''        comment '联系电话',
  address         varchar(200)    not null default ''        comment '机构地址',
  status          char(1)         not null default '0'       comment '状态（0正常 1停用）',
  package_type    int(4)          not null default 0         comment '套餐类型（0基础 1高级 2VIP）',
  expire_time     varchar(20)     not null default ''        comment '到期时间',
  del_flag        char(1)         not null default '0'       comment '删除标志（0代表存在 2代表删除）',
  remark          varchar(500)    default null               comment '备注',
  create_by       varchar(64)     not null default ''        comment '创建者',
  create_time     datetime        not null default current_timestamp comment '创建时间',
  update_by       varchar(64)     not null default ''        comment '更新者',
  update_time     datetime        not null default current_timestamp on update current_timestamp comment '更新时间',
  primary key (org_id)
) engine=innodb auto_increment=1 comment = '机构信息表';

-- ----------------------------
-- 2、逝者信息表
-- ----------------------------
drop table if exists mem_deceased;
create table mem_deceased (
  deceased_id       bigint(20)      not null auto_increment    comment '逝者ID',
  org_id            bigint(20)      not null default 0         comment '所属机构ID',
  family_user_id    bigint(20)      not null default 0         comment '家属用户ID',
  name              varchar(50)     not null default ''        comment '逝者姓名',
  gender            char(1)         not null default '0'       comment '性别（0男 1女 2未知）',
  birth_date        date            not null                   comment '出生日期',
  death_date        date            not null                   comment '逝世日期',
  cemetery_area     varchar(50)     not null default ''        comment '陵园区域',
  cemetery_number   varchar(50)     not null default ''        comment '墓位编号',
  bio               text            default null               comment '生平简介',
  cover_image       varchar(200)    not null default ''        comment '封面图片',
  qrcode_code       varchar(50)     not null default ''        comment '二维码编码',
  qrcode_url        varchar(200)    not null default ''        comment '二维码图片URL',
  is_public         char(1)         not null default '0'       comment '是否公开（0公开 1不公开）',
  allow_message     char(1)         not null default '0'       comment '是否允许留言（0允许 1不允许）',
  message_audit     char(1)         not null default '0'       comment '留言审核方式（0不需审核 1需审核）',
  status            char(1)         not null default '0'       comment '状态（0正常 1停用）',
  del_flag          char(1)         not null default '0'       comment '删除标志（0代表存在 2代表删除）',
  remark            varchar(500)    default null               comment '备注',
  create_by         varchar(64)     not null default ''        comment '创建者',
  create_time       datetime        not null default current_timestamp comment '创建时间',
  update_by         varchar(64)     not null default ''        comment '更新者',
  update_time       datetime        not null default current_timestamp on update current_timestamp comment '更新时间',
  primary key (deceased_id),
  key idx_org_id (org_id),
  key idx_family_user_id (family_user_id),
  key idx_qrcode_code (qrcode_code)
) engine=innodb auto_increment=1 comment = '逝者信息表';

-- ----------------------------
-- 3、逝者相册表
-- ----------------------------
drop table if exists mem_deceased_album;
create table mem_deceased_album (
  album_id         bigint(20)      not null auto_increment    comment '相册ID',
  deceased_id      bigint(20)      not null default 0         comment '逝者ID',
  image_url        varchar(200)    not null default ''        comment '图片地址',
  thumbnail_url    varchar(200)    not null default ''        comment '缩略图地址',
  description      varchar(200)    not null default ''        comment '图片描述',
  sort_order       int(4)          not null default 0         comment '排序号',
  create_time      datetime        not null default current_timestamp comment '创建时间',
  primary key (album_id),
  key idx_deceased_id (deceased_id)
) engine=innodb auto_increment=1 comment = '逝者相册表';

-- ----------------------------
-- 4、逝者视频表
-- ----------------------------
drop table if exists mem_deceased_video;
create table mem_deceased_video (
  video_id         bigint(20)      not null auto_increment    comment '视频ID',
  deceased_id      bigint(20)      not null default 0         comment '逝者ID',
  title            varchar(100)    not null default ''        comment '视频标题',
  video_url        varchar(200)    not null default ''        comment '视频地址',
  cover_url        varchar(200)    not null default ''        comment '封面图片地址',
  description      varchar(200)    not null default ''        comment '视频描述',
  sort_order       int(4)          not null default 0         comment '排序号',
  create_by        varchar(64)     not null default ''        comment '创建者',
  create_time      datetime        not null default current_timestamp comment '创建时间',
  update_by        varchar(64)     not null default ''        comment '更新者',
  update_time      datetime        not null default current_timestamp on update current_timestamp comment '更新时间',
  primary key (video_id),
  key idx_deceased_id (deceased_id)
) engine=innodb auto_increment=1 comment = '逝者视频表';

-- ----------------------------
-- 5、留言寄语表
-- ----------------------------
drop table if exists mem_message;
create table mem_message (
  message_id       bigint(20)      not null auto_increment    comment '留言ID',
  deceased_id      bigint(20)      not null default 0         comment '逝者ID',
  visitor_name     varchar(50)     not null default ''        comment '留言人姓名',
  visitor_phone    varchar(20)     not null default ''        comment '留言人手机号',
  relation         varchar(30)     not null default ''        comment '与逝者关系',
  content          text            not null                   comment '留言内容',
  is_audited       char(1)         not null default '0'       comment '审核状态（0待审核 1审核通过 2审核不通过）',
  ip_address       varchar(128)    not null default ''        comment 'IP地址',
  create_by        varchar(64)     not null default ''        comment '创建者',
  create_time      datetime        not null default current_timestamp comment '创建时间',
  update_by        varchar(64)     not null default ''        comment '更新者',
  update_time      datetime        not null default current_timestamp on update current_timestamp comment '更新时间',
  primary key (message_id),
  key idx_deceased_id (deceased_id)
) engine=innodb auto_increment=1 comment = '留言寄语表';

-- ----------------------------
-- 6、献花记录表
-- ----------------------------
drop table if exists mem_flower;
create table mem_flower (
  flower_id        bigint(20)      not null auto_increment    comment '献花ID',
  deceased_id      bigint(20)      not null default 0         comment '逝者ID',
  visitor_name     varchar(50)     not null default ''        comment '献花人姓名',
  flower_type      int(4)          not null default 1         comment '鲜花类型（1菊花 2百合 3康乃馨 4玫瑰）',
  message          varchar(200)    not null default ''        comment '献花留言',
  ip_address       varchar(128)    not null default ''        comment 'IP地址',
  create_by        varchar(64)     not null default ''        comment '创建者',
  create_time      datetime        not null default current_timestamp comment '创建时间',
  update_by        varchar(64)     not null default ''        comment '更新者',
  update_time      datetime        not null default current_timestamp on update current_timestamp comment '更新时间',
  primary key (flower_id),
  key idx_deceased_id (deceased_id)
) engine=innodb auto_increment=1 comment = '献花记录表';

-- ----------------------------
-- 7、访问统计表
-- ----------------------------
drop table if exists mem_statistics;
create table mem_statistics (
  stat_id          bigint(20)      not null auto_increment    comment '统计ID',
  deceased_id      bigint(20)      not null default 0         comment '逝者ID',
  visit_date       date            not null                   comment '访问日期',
  visit_count      int(11)         not null default 0         comment '访问次数',
  message_count    int(11)         not null default 0         comment '留言数',
  flower_count     int(11)         not null default 0         comment '献花数',
  create_by        varchar(64)     not null default ''        comment '创建者',
  create_time      datetime        not null default current_timestamp comment '创建时间',
  update_by        varchar(64)     not null default ''        comment '更新者',
  update_time      datetime        not null default current_timestamp on update current_timestamp comment '更新时间',
  primary key (stat_id),
  unique key uk_deceased_date (deceased_id, visit_date)
) engine=innodb auto_increment=1 comment = '访问统计表';

-- ----------------------------
-- 8、订单表
-- ----------------------------
drop table if exists mem_order;
create table mem_order (
  order_id         bigint(20)      not null auto_increment    comment '订单ID',
  order_no         varchar(50)     not null default ''        comment '订单编号',
  org_id           bigint(20)      not null default 0         comment '所属机构ID',
  deceased_id      bigint(20)      not null default 0         comment '逝者ID',
  customer_name    varchar(50)     not null default ''        comment '客户姓名',
  customer_phone   varchar(20)     not null default ''        comment '客户电话',
  package_type     int(4)          not null default 0         comment '套餐类型（0基础 1高级 2VIP）',
  amount           decimal(10,2)   not null default 0.00      comment '订单金额',
  status           int(4)          not null default 0         comment '订单状态（0待支付 1已支付 2已退款）',
  pay_time         varchar(20)     not null default ''        comment '支付时间',
  remark           varchar(500)    default null               comment '备注',
  create_by        varchar(64)     not null default ''        comment '创建者',
  create_time      datetime        not null default current_timestamp comment '创建时间',
  update_by        varchar(64)     not null default ''        comment '更新者',
  update_time      datetime        not null default current_timestamp on update current_timestamp comment '更新时间',
  primary key (order_id),
  key idx_org_id (org_id),
  key idx_deceased_id (deceased_id),
  key idx_order_no (order_no)
) engine=innodb auto_increment=1 comment = '订单表';
