# business.md - 核心业务规则

> **何时读**：改业务规则时按模块查。每个模块讲规则、字段、接口、易错点。

## 机构（mem_organization）

机构=殡仪馆/公墓，下挂多个逝者。`package_type`（**0基础1高级2VIP**，DDL 注释，与 P2 文档不一致）决定名下逝者可用功能范围（相册数/视频数/审核优先级，当前代码未实现限制）。`expire_time` 到期时间。机构管理员数据隔离**当前未实现**。

## 逝者纪念馆（mem_deceased）- 核心

- `family_user_id` 家属用户 ID，归属判定依据。`org_id` 所属机构（家属创建未传默认 0）。
- 三个开关：`is_public`（搜索仅返回 '0'）/ `allow_message` / `message_audit`（'0' 不需审核，提交留言时自动通过；家属创建纪念馆**默认 '1' 需审核**）。
- `qrcode_code`/`qrcode_url`：创建时自动生成（`generateQrcodeCode` 16位编码 + `QrCodeUtil.generateForCode` 上传 OSS）。家属可 `POST /api/family/memorial/qrcode/{id}` 重新生成（换新码，旧码失效）。
- 扫码内容 `<h5-base-url>/#/pages/memorial/detail?code=<code>`（`memorial.qrcode.h5-base-url` 生产必改为正式域名，**不能用 127.0.0.1**）。
- 访问流程：扫码 -> `GET /api/qrcode/{code}` -> 查 deceased（qrcode_code + del_flag=0）-> 校验 status=0 -> 记统计 -> 聚合返回。未找到/"暂不可访问"对应错误。

## 相册（mem_deceased_album）

所有人浏览；家属上传/删除/编辑。字段：`image_url`/`thumbnail_url`（当前多空串）/`description`/`sort_order`。上传 `POST /api/family/album/upload`（家属，返回 album）/ `POST /memorial/deceased/album/upload`（管理后台，返回 url），走 `OssService.upload(file, "memorial/<userId>/album")`。删除先查 album 拿 deceasedId 校验归属。PRD：单次9张、图片≤5MB。缩略图当前未生成。

## 视频（mem_deceased_video）

所有人播放；家属上传/删除/编辑。字段：`title`/`video_url`/`cover_url`（当前多空）/`description`/`sort_order`。上传 `POST /api/family/video/upload`（带 deceasedId/title/description），走 `OssService.upload(file, "memorial/<userId>/video")`。PRD：单视频≤50MB，**multipart 当前 20MB，需调大**。封面未自动生成。

## 留言（mem_message）

- 字段：`visitor_name`/`visitor_phone`/`relation`/`content`（text，PRD限500字）/`is_audited`（0待1通过2不通过）/`ip_address`。
- **提交**（`POST /api/message`）：访客提交，`is_audited='0'`，记 IP。**自动审核**：提交后查 `deceased.message_audit`，'0'（不需审核）则立即 auditMessage(id,'1') 自动通过，否则保持 '0' 待审。成功后 `incrementMessageCount`。前端提示"留言已提交，审核通过后将展示"。
- **展示**：`GET /api/messages/{deceasedId}` 只返回 `is_audited='1'`。
- **审核**（家属）：`GET /api/family/messages/pending/{id}`（待审列表）、`PUT /api/family/message/audit`（单条，body: messageId+isAudited）、`PUT /api/family/message/batchAudit?status=&messageIds=`（`@RequestParam`，前端用 params）、`GET /api/family/messages/pendingCount`（跨纪念馆待审总数，徽章用）。管理后台 `PUT /memorial/message/audit`、`/batchAudit`。审核需 verifyOwnership / @PreAuthorize。
- 家属可删除不当留言（逻辑删除）。

## 献花（mem_flower）

- 字段：`visitor_name`/`flower_type`（1菊花2百合3康乃馨4玫瑰）/`message`/`ip_address`。
- **提交**（`POST /api/flower`）：记 IP，`incrementFlowerCount`，立即展示（**献花无需审核**，区别于留言）。
- **⚠️ 频率限制（文档要求，代码未实现）**：PRD 同一用户对同一逝者每天可献花 3 次（IP+用户ID 双重）。当前 `FlowerServiceImpl.insertFlower` 无任何限制，可无限刷。**实现此需求需新增**：按 IP/userId + deceasedId + 当天计数判断，超限拒绝（Redis `flower:<deceasedId>:<ip>:<date>` 或 SQL count）。
- **展示**：`GET /api/flowers/{deceasedId}` 全量返回（时间倒序）。

## 统计（mem_statistics）

- 按 `visit_date`+`deceased_id` 聚合，`uk_deceased_date` 唯一约束（一天一逝者一行）。`visit_count`/`message_count`/`flower_count`。
- 计数：访问（`/api/qrcode/{code}` 时 `incrementVisitCount`）/ 留言（提交后）/ 献花（提交后）。
- ⚠️ `DeceasedMapper.incrementVisit` 是空操作（`select 1`），实际统计走 `StatisticsService` + `mem_statistics` 表，改统计别改错地方。
- 查询：家属 `GET /api/family/statistics/{id}`（总+dailyStats）、`GET /api/family/memorials/statistics`（摘要）；管理后台 `GET /memorial/statistics/list`、`/deceased/{id}`。
- **并发注意**：计数 select+update/insert 非原子，高并发不准。可改 `INSERT ... ON DUPLICATE KEY UPDATE visit_count = visit_count + 1`（利用 uk_deceased_date）。

## 家属管理（P1，已实现）

家属=关联了逝者（family_user_id）的注册用户。**身份判定**：前端 `getMyMemorials()` 非空 -> `isFamilyMember=true`，显示"我的纪念馆"入口。家属接口前缀 `/api/family/**`（见 api.md），所有涉及逝者资源的操作必须 `verifyOwnership`。

## 搜索（`GET /api/search?keyword=`）

公开搜索，**仅返回 `is_public='0'` 且 `status='0'`**，模糊匹配 name，按 create_time 倒序，**限制 20 条**。空关键词返回"请输入搜索关键词"。

## 字段对齐（前后端一致）

历史修复的映射（`docs/phase-b-api-store.md`），新增字段两端统一：

| 旧（错） | 新（对） |
|---|---|
| deceasedName | name |
| authorName | visitorName |
| auditStatus | isAudited |
| videoTitle | title |
| memorialTitle/Content | bio |
| orgType/orderType | packageType |

后端 domain 序列化为驼峰，前端 types 驼峰。新增字段四处同步：domain + resultMap + XML + 前端 types。

## 待办与已知缺陷

| 项 | 状态 | 说明 |
|---|---|---|
| 献花频率限制 | ❌ 未实现 | 文档要求每天3次，代码无限制 |
| 视频上传上限 | ⚠️ 需调大 | multipart 20MB，PRD 需 50MB |
| 视频封面/相册缩略图 | ❌ 未生成 | cover_url/thumbnail_url 存空串 |
| 机构数据隔离 | ❌ 未实现 | 管理后台未按 org_id 过滤 |
| 统计并发安全 | ⚠️ 非原子 | select+update 可能不准 |
| SMS 真实发送 | ❌ P0 直接返回 | 见 security.md |
| 接口限流 | ❌ 未实现 | /api/message /api/flower 易被刷 |
| 支付 | ❌ 未实现 | 见 payment.md |
