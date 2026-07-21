-- =============================================
-- 统计表改造：通用 subject 模型（P4.1）
-- mem_statistics: deceased_id -> subject_type + subject_id
-- uk_deceased_date -> uk_subject_date(subject_type, subject_id, visit_date)
-- 说明：项目未发布生产，DDL 已同步更新 memorial_ddl.sql（新环境直接用 ddl）。
--       本脚本用于已有开发/测试环境从旧结构迁移到新结构。
-- subject_type: 0逝者 1族谱（未来可扩）；subject_id: deceased_id 或 clan_id
-- =============================================

-- 1. 新增 subject 字段
alter table mem_statistics add column subject_type tinyint(4) not null default 0 comment '统计主体类型（0逝者 1族谱）' after stat_id;
alter table mem_statistics add column subject_id bigint(20) not null default 0 comment '统计主体ID（deceased_id 或 clan_id）' after subject_type;

-- 2. 迁移历史数据：旧 deceased_id 数据转为 subject_type=0
update mem_statistics set subject_type = 0, subject_id = deceased_id where deceased_id is not null and subject_id = 0;

-- 3. 删除旧唯一约束，建新唯一约束
alter table mem_statistics drop index uk_deceased_date;
alter table mem_statistics add unique key uk_subject_date (subject_type, subject_id, visit_date);

-- 4. 删除旧字段
alter table mem_statistics drop column deceased_id;
