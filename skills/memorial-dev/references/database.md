# database.md - 数据库、Mapper、SQL 与存储

> **何时读**：改表结构、写 Mapper XML、做文件上传/OSS 时。

## 数据库

MySQL 8，utf8mb4 / utf8mb4_general_ci，库名 `memorial`，数据源 `application-druid.yml`。Druid 连接池，慢 SQL 阈值 1000ms，监控台 `/druid/`。

初始化顺序：`ry_20250522.sql` -> `quartz.sql` -> `memorial_ddl.sql` -> `memorial_menu_dict.sql` -> `memorial_sample_data.sql`。

## 业务表（8 张，DDL 在 `sql/memorial_ddl.sql`）

| 表 | 主键 | 说明 |
|---|---|---|
| `mem_organization` | org_id | 机构（package_type / expire_time） |
| `mem_deceased` | deceased_id | 逝者（核心） |
| `mem_deceased_album` | album_id | 相册 |
| `mem_deceased_video` | video_id | 视频 |
| `mem_message` | message_id | 留言 |
| `mem_flower` | flower_id | 献花 |
| `mem_statistics` | stat_id | 统计（按日聚合，uk_deceased_date） |
| `mem_order` | order_id | 订单 |

## 关键字段

**mem_deceased**：`family_user_id`（家属关联）、三个开关 `is_public`（0公开1不公开，搜索仅返回0）/ `allow_message`（0允许1不允许）/ `message_audit`（0不需审核1需审核，提交留言时0则自动通过）、`qrcode_code`/`qrcode_url`、`cemetery_latitude/longitude`（decimal(10,7)）、`bio`（text 富文本）、`status`（0正常1停用）、`del_flag`（0存在2删除）、`gender`（0男1女2未知）。

**mem_message**：`is_audited`（0待审核1通过2不通过，公开只返回1）、`content`（text，PRD限500字）、`ip_address`。

**mem_flower**：`flower_type`（1菊花2百合3康乃馨4玫瑰）、`ip_address`。

**mem_statistics**：按 `visit_date`+`deceased_id` 聚合，`uk_deceased_date` 唯一约束，`visit_count`/`message_count`/`flower_count`。

**mem_order**：`package_type`（**0基础1高级2VIP**，DDL 注释，与 P2 文档 1/2/3 不一致，以 DDL 为准）、`status`（**0待支付1已支付2已退款**，3 态，与 P2 文档 4 态不一致）、`amount`（decimal(10,2)）、`order_no`。

所有表带审计字段（相册表仅 create_time）。

## Mapper 规范

- 接口 `com.ruoyi.memorial.mapper.XxxMapper`，XML 在 `resources/mapper/memorial/XxxMapper.xml`。
- `application.yml` 已配 `mapperLocations: classpath*:mapper/**/*Mapper.xml`、`typeAliasesPackage: com.ruoyi.**.domain`（XML 可直接用类名 `parameterType="Deceased"`）。

### XML 写法（参照 DeceasedMapper.xml）
1. **用 resultMap 显式映射驼峰↔下划线**，别依赖自动映射。
2. **查询写全字段**（别 `select *`），关联机构名 `left join mem_organization o` 取 `o.org_name`。
3. **永远带 `del_flag = '0'`**。
4. **条件用 `<if>`**，字符串判空 `name != null and name != ''`，模糊 `like concat('%', #{name}, '%')`。
5. **参数一律 `#{}`**，禁 `${}`（SQL 注入），排序字段经 `SqlUtil.escapeOrderBySql` 转义。
6. **插入 `useGeneratedKeys="true" keyProperty="deceasedId"`** 回填主键。
7. **更新用 `<set>` + `<if>`** 动态字段。
8. **批量 `<foreach>`**：
   ```xml
   update mem_deceased set del_flag = '2' where deceased_id in
   <foreach item="id" collection="array" open="(" separator="," close=")">#{id}</foreach>
   ```
9. **逻辑删除**：`delete` 标签写 `update ... set del_flag='2'`。

### Service
接口 `IXxxService` + `XxxServiceImpl`，`@Service`，`@Autowired` 注入 Mapper。简单 CRUD 纯透传不加事务，多表写加 `@Transactional(rollbackFor = Exception.class)`。校验失败抛 `ServiceException`。

## 分页

管理后台列表：`startPage()` -> 查 list -> `getDataTable(list)`（TableDataInfo）。前端传 `pageNum`/`pageSize`。小程序公开接口不分页。

## OSS 存储

### 现状
所有文件走阿里云 OSS，bucket=memorials（北京）。若依本地文件功能**已失效**（`ruoyi.profile` 占位串），`/common/upload`、`/common/download`、`/profile/**`、头像上传均不可用。

核心类：
- `OssProperties`（`@ConfigurationProperties(prefix="memorial.oss")`）：endpoint / bucket / accessKeyId / accessKeySecret / urlPrefix。
- `OssConfig`：创建 `OSS` Client Bean。
- `OssService`：`upload(MultipartFile, directory)` 返回**完整 URL**；`uploadBytes(byte[], directory, fileName)` 上传字节数组（二维码用）。objectKey 规则 `<directory>/<32位UUID>.<ext>`。
- `QrCodeUtil.generateForCode(code, userId)`：zxing 生成 300x300 PNG，存 `memorial/<userId>/qrcode/<code>.png`，返回 `QrCodeResult(url, content)`。

### URL 约定
`OssService.buildUrl` = `urlPrefix` 去尾斜杠 + `/` + objectKey。**返回 URL 直接存库**（cover_image/image_url/video_url/qrcode_url），前端 `<image :src>` 直接用，**不拼前缀**。

### endpoint vs url-prefix（易踩坑）
| 配置 | 用途 | 开发 | 生产 |
|---|---|---|---|
| `memorial.oss.endpoint` | 后端**上传** | 公网 `oss-cn-beijing.aliyuncs.com` | **内网** `oss-cn-beijing-internal.aliyuncs.com`（省流量费） |
| `memorial.oss.url-prefix` | 前端**查看** | 直连 OSS 公网域名 | `http://<公网IP/域名>/oss`（走 nginx 反代内网 OSS） |

- endpoint 生产保持内网，别改公网。
- url-prefix 指向 nginx 公网地址，**不能用 internal 域名**（公网不可达），**不能用 127.0.0.1**（前端在用户浏览器会指向用户本机）。
- nginx `/oss/` 反代需 `proxy_set_header Host <OSS内网域名>` + `proxy_ssl_server_name on`。
- ECS 与 OSS 必须同地域（北京）。
- 旧数据迁移：历史内网域名 URL 用 SQL `REPLACE` 成 `https://域名/oss`（字段：coverImage/cemeteryPhoto/imageUrl/videoUrl/coverUrl/qrcodeUrl/avatar）。

### 上传上限
`max-file-size=10MB`、`max-request-size=20MB`。PRD 要求图片 ≤5MB、视频 ≤50MB--**视频上传前需调大**（如 100MB/200MB）。

## SQL 变更

- 新表/加字段写 `sql/memorial_alter_vN_xxx.sql` 增量 + 同步更新 `memorial_ddl.sql`。
- 字段加 `comment`，状态码取值在注释写明。
- 外键关联字段（deceased_id/org_id/family_user_id）必须有索引。
- 套餐类型用字典 `memorial_package_type`（`memorial_menu_dict.sql`）。
- 别对生产库直接 DDL，先写脚本评审。
