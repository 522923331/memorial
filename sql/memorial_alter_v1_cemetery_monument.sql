-- =============================================
-- 纪念馆 - 增量迁移：墓区地图+照片 / 立碑者
-- 适用：已在 memorial_ddl.sql 旧版基础上建库的环境
-- 执行方法：mysql -u root -p memorial < sql/memorial_alter_v1_cemetery_monument.sql
-- =============================================

-- 墓区经纬度（用于地图定位）
alter table mem_deceased add column cemetery_latitude decimal(10,7) default null comment '墓区纬度' after cemetery_number;
alter table mem_deceased add column cemetery_longitude decimal(10,7) default null comment '墓区经度' after cemetery_latitude;
alter table mem_deceased add column cemetery_photo varchar(200) not null default '' comment '墓区照片' after cemetery_longitude;

-- 立碑者（多行文本，选填）
alter table mem_deceased add column monument_eraser varchar(2000) not null default '' comment '立碑者（多行文本）' after bio;
