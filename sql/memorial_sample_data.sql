-- =============================================
-- 纪念馆 - 示例数据 SQL
-- 包含：用户、机构、逝者、相册、视频、留言、献花、统计、订单
-- 执行前提：先执行 memorial_ddl.sql 建表
-- =============================================

-- ----------------------------
-- 1. 用户数据（手机号登录用户）
-- 密码为 BCrypt 加密的 "123456"
-- ----------------------------
insert into sys_user (user_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, login_ip, login_date, pwd_update_date, create_by, create_time, update_by, update_time, remark)
values (3, 103, 'user_17310503610', '吴丹', '00', '', '17310503610', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), sysdate(), 'admin', sysdate(), '', sysdate(), '纪念馆用户');

insert into sys_user_role (user_id, role_id) values (3, 2);

-- ----------------------------
-- 2. 机构数据
-- ----------------------------
insert into mem_organization (org_id, org_name, org_code, contact_name, contact_phone, address, status, package_type, expire_time, del_flag, remark, create_by, create_time, update_by, update_time)
values (1, '永安殡仪馆', 'YA-001', '张经理', '0731-88880001', '长沙市雨花区永安路88号', '0', 1, '2027-12-31', '0', '优质殡仪服务', 'admin', sysdate(), '', sysdate());

insert into mem_organization (org_id, org_name, org_code, contact_name, contact_phone, address, status, package_type, expire_time, del_flag, remark, create_by, create_time, update_by, update_time)
values (2, '青山陵园', 'QS-001', '李主任', '0731-88880002', '长沙市岳麓区青山路168号', '0', 2, '2028-06-30', '0', '生态园林陵园', 'admin', sysdate(), '', sysdate());

-- ----------------------------
-- 3. 逝者数据
-- ----------------------------
insert into mem_deceased (deceased_id, org_id, family_user_id, name, gender, birth_date, death_date, cemetery_area, cemetery_number, bio, cover_image, qrcode_code, qrcode_url, is_public, allow_message, message_audit, status, del_flag, remark, create_by, create_time, update_by, update_time)
values (1, 1, 3, '王建国', '0', '1945-03-15', '2023-11-20', 'A区', 'A-0088', '王建国同志，1945年生于湖南长沙，1965年参加工作，在长沙钢铁厂奉献了四十余年。他一生勤勉，待人宽厚，深受同事和邻里的敬爱。退休后热心社区公益，帮助邻里解决困难，是社区里德高望重的老人。', '/profile/upload/default-cover.jpg', 'WJG001', '', '0', '0', '0', '0', '0', null, 'admin', sysdate(), '', sysdate());

insert into mem_deceased (deceased_id, org_id, family_user_id, name, gender, birth_date, death_date, cemetery_area, cemetery_number, bio, cover_image, qrcode_code, qrcode_url, is_public, allow_message, message_audit, status, del_flag, remark, create_by, create_time, update_by, update_time)
values (2, 2, 3, '李秀英', '1', '1950-08-22', '2024-03-05', 'B区', 'B-0156', '李秀英女士，1950年生于湖南湘潭，是一位优秀的教育工作者。她在长沙市第一中学任教三十余年，桃李满天下。她用一生诠释了什么是敬业与奉献，她的学生们遍布全国各地，在各行各业发光发热。', '/profile/upload/default-cover.jpg', 'LXY002', '', '0', '0', '0', '0', '0', null, 'admin', sysdate(), '', sysdate());

insert into mem_deceased (deceased_id, org_id, family_user_id, name, gender, birth_date, death_date, cemetery_area, cemetery_number, bio, cover_image, qrcode_code, qrcode_url, is_public, allow_message, message_audit, status, del_flag, remark, create_by, create_time, update_by, update_time)
values (3, 1, 3, '张明远', '0', '1970-05-10', '2024-08-18', 'A区', 'A-0233', '张明远先生，1970年生于湖南株洲，是一位才华横溢的建筑设计师。他参与了多个城市地标建筑的设计工作，作品获得过国家建筑设计金奖。他热爱生活，喜欢旅行和摄影，留下了许多珍贵的影像记录。', '/profile/upload/default-cover.jpg', 'ZMY003', '', '0', '0', '1', '0', '0', null, 'admin', sysdate(), '', sysdate());

-- ----------------------------
-- 4. 相册数据
-- ----------------------------
insert into mem_deceased_album (album_id, deceased_id, image_url, thumbnail_url, description, sort_order, create_time)
values (1, 1, '/profile/upload/album/wjg-01.jpg', '/profile/upload/album/wjg-01-thumb.jpg', '青年时期在钢铁厂工作', 1, sysdate());
insert into mem_deceased_album (album_id, deceased_id, image_url, thumbnail_url, description, sort_order, create_time)
values (2, 1, '/profile/upload/album/wjg-02.jpg', '/profile/upload/album/wjg-02-thumb.jpg', '与家人合影', 2, sysdate());
insert into mem_deceased_album (album_id, deceased_id, image_url, thumbnail_url, description, sort_order, create_time)
values (3, 1, '/profile/upload/album/wjg-03.jpg', '/profile/upload/album/wjg-03-thumb.jpg', '退休后参加社区活动', 3, sysdate());
insert into mem_deceased_album (album_id, deceased_id, image_url, thumbnail_url, description, sort_order, create_time)
values (4, 2, '/profile/upload/album/lxy-01.jpg', '/profile/upload/album/lxy-01-thumb.jpg', '在学校授课', 1, sysdate());
insert into mem_deceased_album (album_id, deceased_id, image_url, thumbnail_url, description, sort_order, create_time)
values (5, 2, '/profile/upload/album/lxy-02.jpg', '/profile/upload/album/lxy-02-thumb.jpg', '与学生们的毕业合影', 2, sysdate());
insert into mem_deceased_album (album_id, deceased_id, image_url, thumbnail_url, description, sort_order, create_time)
values (6, 2, '/profile/upload/album/lxy-03.jpg', '/profile/upload/album/lxy-03-thumb.jpg', '退休旅行照片', 3, sysdate());
insert into mem_deceased_album (album_id, deceased_id, image_url, thumbnail_url, description, sort_order, create_time)
values (7, 2, '/profile/upload/album/lxy-04.jpg', '/profile/upload/album/lxy-04-thumb.jpg', '与家人的温馨时光', 4, sysdate());
insert into mem_deceased_album (album_id, deceased_id, image_url, thumbnail_url, description, sort_order, create_time)
values (8, 3, '/profile/upload/album/zmy-01.jpg', '/profile/upload/album/zmy-01-thumb.jpg', '设计作品展示', 1, sysdate());
insert into mem_deceased_album (album_id, deceased_id, image_url, thumbnail_url, description, sort_order, create_time)
values (9, 3, '/profile/upload/album/zmy-02.jpg', '/profile/upload/album/zmy-02-thumb.jpg', '旅行摄影', 2, sysdate());

-- ----------------------------
-- 5. 视频数据
-- ----------------------------
insert into mem_deceased_video (video_id, deceased_id, title, video_url, cover_url, description, sort_order, create_by, create_time, update_by, update_time)
values (1, 1, '王建国生平回顾', '/profile/upload/video/wjg-memorial.mp4', '/profile/upload/video/wjg-cover.jpg', '记录了王建国同志的一生', 1, 'admin', sysdate(), '', sysdate());

insert into mem_deceased_video (video_id, deceased_id, title, video_url, cover_url, description, sort_order, create_by, create_time, update_by, update_time)
values (2, 2, '李秀英老师纪念视频', '/profile/upload/video/lxy-memorial.mp4', '/profile/upload/video/lxy-cover.jpg', '学生们共同缅怀恩师', 1, 'admin', sysdate(), '', sysdate());

-- ----------------------------
-- 6. 留言数据（is_audited: 0=待审核 1=审核通过 2=审核不通过）
-- ----------------------------
insert into mem_message (message_id, deceased_id, visitor_name, visitor_phone, relation, content, is_audited, ip_address, create_by, create_time, update_by, update_time)
values (1, 1, '王小明', '', '儿子', '爸爸，您永远活在我们心中。您的坚强和乐观一直激励着我前行，我会好好照顾妈妈。', '1', '127.0.0.1', '', sysdate(), '', sysdate());
insert into mem_message (message_id, deceased_id, visitor_name, visitor_phone, relation, content, is_audited, ip_address, create_by, create_time, update_by, update_time)
values (2, 1, '赵丽华', '', '邻居', '王叔是个好人，退休后还经常帮我们修水管、修电器，街坊邻居都很想念他。', '1', '127.0.0.1', '', sysdate(), '', sysdate());
insert into mem_message (message_id, deceased_id, visitor_name, visitor_phone, relation, content, is_audited, ip_address, create_by, create_time, update_by, update_time)
values (3, 1, '刘建国', '', '同事', '老王是我们厂里的技术骨干，他教会了我很多做人做事的道理。永远怀念您！', '1', '127.0.0.1', '', sysdate(), '', sysdate());
insert into mem_message (message_id, deceased_id, visitor_name, visitor_phone, relation, content, is_audited, ip_address, create_by, create_time, update_by, update_time)
values (4, 2, '陈思思', '', '学生', '李老师，是您让我爱上了文学，是您教会了我做人的道理。师恩难忘，永远怀念！', '1', '127.0.0.1', '', sysdate(), '', sysdate());
insert into mem_message (message_id, deceased_id, visitor_name, visitor_phone, relation, content, is_audited, ip_address, create_by, create_time, update_by, update_time)
values (5, 2, '周文博', '', '学生', '三十年前的学生，如今已是大学教授。李老师的教诲我终生受用，愿天堂没有病痛。', '1', '127.0.0.1', '', sysdate(), '', sysdate());
insert into mem_message (message_id, deceased_id, visitor_name, visitor_phone, relation, content, is_audited, ip_address, create_by, create_time, update_by, update_time)
values (6, 2, '李小华', '', '女儿', '妈妈，您是最伟大的母亲和老师。您用一生诠释了什么是爱与奉献，我永远爱您。', '1', '127.0.0.1', '', sysdate(), '', sysdate());
insert into mem_message (message_id, deceased_id, visitor_name, visitor_phone, relation, content, is_audited, ip_address, create_by, create_time, update_by, update_time)
values (7, 3, '张小雪', '', '妻子', '明远，你设计的每一栋建筑都还矗立在城市里，你的作品将永远陪伴我们。', '1', '127.0.0.1', '', sysdate(), '', sysdate());
insert into mem_message (message_id, deceased_id, visitor_name, visitor_phone, relation, content, is_audited, ip_address, create_by, create_time, update_by, update_time)
values (8, 1, '老同事', '', '朋友', '怀念一起奋斗的日子。', '0', '127.0.0.1', '', sysdate(), '', sysdate());

-- ----------------------------
-- 7. 献花数据（flower_type: 1=菊花 2=百合 3=康乃馨 4=玫瑰）
-- ----------------------------
insert into mem_flower (flower_id, deceased_id, visitor_name, flower_type, message, ip_address, create_by, create_time, update_by, update_time)
values (1, 1, '王小明', 3, '爸爸，送您最喜欢的康乃馨', '127.0.0.1', '', sysdate(), '', sysdate());
insert into mem_flower (flower_id, deceased_id, visitor_name, flower_type, message, ip_address, create_by, create_time, update_by, update_time)
values (2, 1, '赵丽华', 1, '王叔一路走好', '127.0.0.1', '', sysdate(), '', sysdate());
insert into mem_flower (flower_id, deceased_id, visitor_name, flower_type, message, ip_address, create_by, create_time, update_by, update_time)
values (3, 1, '刘建国', 2, '', '127.0.0.1', '', sysdate(), '', sysdate());
insert into mem_flower (flower_id, deceased_id, visitor_name, flower_type, message, ip_address, create_by, create_time, update_by, update_time)
values (4, 1, '匿名', 1, '', '127.0.0.1', '', sysdate(), '', sysdate());
insert into mem_flower (flower_id, deceased_id, visitor_name, flower_type, message, ip_address, create_by, create_time, update_by, update_time)
values (5, 1, '匿名', 4, '', '127.0.0.1', '', sysdate(), '', sysdate());
insert into mem_flower (flower_id, deceased_id, visitor_name, flower_type, message, ip_address, create_by, create_time, update_by, update_time)
values (6, 2, '陈思思', 3, '恩师一路走好', '127.0.0.1', '', sysdate(), '', sysdate());
insert into mem_flower (flower_id, deceased_id, visitor_name, flower_type, message, ip_address, create_by, create_time, update_by, update_time)
values (7, 2, '周文博', 1, '', '127.0.0.1', '', sysdate(), '', sysdate());
insert into mem_flower (flower_id, deceased_id, visitor_name, flower_type, message, ip_address, create_by, create_time, update_by, update_time)
values (8, 2, '李小华', 4, '妈妈，送您玫瑰花', '127.0.0.1', '', sysdate(), '', sysdate());
insert into mem_flower (flower_id, deceased_id, visitor_name, flower_type, message, ip_address, create_by, create_time, update_by, update_time)
values (9, 2, '匿名', 2, '', '127.0.0.1', '', sysdate(), '', sysdate());
insert into mem_flower (flower_id, deceased_id, visitor_name, flower_type, message, ip_address, create_by, create_time, update_by, update_time)
values (10, 2, '匿名', 1, '', '127.0.0.1', '', sysdate(), '', sysdate());
insert into mem_flower (flower_id, deceased_id, visitor_name, flower_type, message, ip_address, create_by, create_time, update_by, update_time)
values (11, 2, '匿名', 3, '', '127.0.0.1', '', sysdate(), '', sysdate());
insert into mem_flower (flower_id, deceased_id, visitor_name, flower_type, message, ip_address, create_by, create_time, update_by, update_time)
values (12, 3, '张小雪', 4, '永远爱你', '127.0.0.1', '', sysdate(), '', sysdate());
insert into mem_flower (flower_id, deceased_id, visitor_name, flower_type, message, ip_address, create_by, create_time, update_by, update_time)
values (13, 3, '匿名', 1, '', '127.0.0.1', '', sysdate(), '', sysdate());

-- ----------------------------
-- 8. 访问统计数据
-- ----------------------------
insert into mem_statistics (stat_id, deceased_id, visit_date, visit_count, message_count, flower_count, create_by, create_time, update_by, update_time)
values (1, 1, '2024-12-01', 15, 2, 3, '', sysdate(), '', sysdate());
insert into mem_statistics (stat_id, deceased_id, visit_date, visit_count, message_count, flower_count, create_by, create_time, update_by, update_time)
values (2, 1, '2024-12-02', 8, 1, 2, '', sysdate(), '', sysdate());
insert into mem_statistics (stat_id, deceased_id, visit_date, visit_count, message_count, flower_count, create_by, create_time, update_by, update_time)
values (3, 1, '2025-01-15', 12, 0, 0, '', sysdate(), '', sysdate());
insert into mem_statistics (stat_id, deceased_id, visit_date, visit_count, message_count, flower_count, create_by, create_time, update_by, update_time)
values (4, 1, '2025-04-28', 5, 1, 1, '', sysdate(), '', sysdate());
insert into mem_statistics (stat_id, deceased_id, visit_date, visit_count, message_count, flower_count, create_by, create_time, update_by, update_time)
values (5, 2, '2024-12-01', 20, 3, 5, '', sysdate(), '', sysdate());
insert into mem_statistics (stat_id, deceased_id, visit_date, visit_count, message_count, flower_count, create_by, create_time, update_by, update_time)
values (6, 2, '2025-01-10', 10, 2, 3, '', sysdate(), '', sysdate());
insert into mem_statistics (stat_id, deceased_id, visit_date, visit_count, message_count, flower_count, create_by, create_time, update_by, update_time)
values (7, 2, '2025-04-28', 8, 1, 2, '', sysdate(), '', sysdate());
insert into mem_statistics (stat_id, deceased_id, visit_date, visit_count, message_count, flower_count, create_by, create_time, update_by, update_time)
values (8, 3, '2024-12-01', 6, 1, 1, '', sysdate(), '', sysdate());
insert into mem_statistics (stat_id, deceased_id, visit_date, visit_count, message_count, flower_count, create_by, create_time, update_by, update_time)
values (9, 3, '2025-04-28', 3, 0, 1, '', sysdate(), '', sysdate());

-- ----------------------------
-- 9. 订单数据
-- ----------------------------
insert into mem_order (order_id, order_no, org_id, deceased_id, customer_name, customer_phone, package_type, amount, status, pay_time, remark, create_by, create_time, update_by, update_time)
values (1, 'ORD20241120001', 1, 1, '王小明', '13800001111', 2, 99.00, 1, '2024-11-20 10:00:00', '高级纪念套餐', 'admin', sysdate(), '', sysdate());

insert into mem_order (order_id, order_no, org_id, deceased_id, customer_name, customer_phone, package_type, amount, status, pay_time, remark, create_by, create_time, update_by, update_time)
values (2, 'ORD20250305001', 2, 2, '李小华', '13800002222', 2, 99.00, 1, '2024-03-05 14:00:00', '高级纪念套餐', 'admin', sysdate(), '', sysdate());

insert into mem_order (order_id, order_no, org_id, deceased_id, customer_name, customer_phone, package_type, amount, status, pay_time, remark, create_by, create_time, update_by, update_time)
values (3, 'ORD20250818001', 1, 3, '张小雪', '13800003333', 3, 299.00, 1, '2024-08-18 09:00:00', 'VIP纪念套餐', 'admin', sysdate(), '', sysdate());
