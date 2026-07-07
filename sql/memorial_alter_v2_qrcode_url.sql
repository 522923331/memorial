-- =============================================
-- 纪念馆 - 增量迁移：修正历史二维码 URL
-- 问题：旧代码将 qrcode_url 存为 /memorial/qrcode/xxx.png，缺少 /profile 前缀，
--       导致前端请求 404。Spring 资源处理器仅从上传目录映射 /profile/**。
-- 适用：已使用旧版本生成过二维码的环境
-- 执行方法：mysql -u root -p memorial < sql/memorial_alter_v2_qrcode_url.sql
-- =============================================

-- 1) 修正已落库的二维码 URL：/memorial/qrcode/  ->  /profile/memorial/qrcode/
UPDATE mem_deceased
   SET qrcode_url = CONCAT('/profile', qrcode_url)
 WHERE qrcode_url LIKE '/memorial/qrcode/%'
   AND qrcode_url NOT LIKE '/profile/memorial/qrcode/%';
