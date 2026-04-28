# Phase B：完善 API + Store

## 待修复问题：前端字段名与后端不一致

后端返回的 JSON 字段名与前端 TypeScript 类型定义有差异，需要统一。以下是需要修改的映射关系：

| 前端旧字段 | 后端实际字段 | 涉及文件 |
|-----------|------------|---------|
| `deceasedName` | `name` | types/memorial.ts, stores/memorial.ts, pages/index/index.vue, pages/memorial/detail.vue |
| `authorName` | `visitorName` | types/memorial.ts, api/memorial.ts, stores/memorial.ts, pages/memorial/message.vue, pages/memorial/flower.vue, pages/memorial/detail.vue |
| `auditStatus` | `isAudited` | types/memorial.ts |
| `videoTitle` | `title` | types/memorial.ts, pages/memorial/detail.vue, pages/memorial/video.vue |
| `coverUrl`(video) | `coverUrl` | 已正确 |
| `thumbnailUrl`(album) | `thumbnailUrl` | 已正确 |

## 已修复的文件
- `src/types/memorial.ts` — 已更新为与后端一致的字段名
- `src/api/memorial.ts` — submitMessage/submitFlower 已改为 visitorName
- `src/stores/memorial.ts` — sendMessage/sendFlower 参数已改为 visitorName，addRecentVisit 已改为 name
- `src/pages/index/index.vue` — item.deceasedName → item.name, item.name → item.name
- `src/pages/memorial/detail.vue` — deceased.deceasedName → deceased.name, msg.authorName → msg.visitorName, fl.authorName → fl.visitorName, video.videoTitle → video.title

## 仍需修复的文件
- `src/pages/memorial/message.vue` — msg.authorName → msg.visitorName（已修复），authorName ref 变量名可保留（是本地输入变量，提交时通过 API 映射）
- `src/pages/memorial/flower.vue` — fl.authorName → fl.visitorName（已修复），同上

## 新增后端 API 待开发

### 1. 手机号登录 `POST /api/phoneLogin`
```
入参: { phone: "17310503610", code: "验证码" }
逻辑:
  1. 从 Redis 中获取 key=sms:phone:17310503610 的验证码
  2. 比对验证码是否正确
  3. 通过 phonenumber 查找 sys_user，未找到则自动注册
  4. 生成 JWT token 返回
返回: { code: 200, data: { token: "xxx" } }
```

### 2. 发送验证码 `POST /api/sms/send`
```
入参: { phone: "17310503610" }
逻辑:
  1. 生成6位随机验证码
  2. 存入 Redis: key=sms:phone:17310503610, value=验证码, TTL=300秒
  3. P0阶段：不实际发送短信，验证码直接返回给前端（或通过日志输出）
  4. 生产环境：接入阿里云/腾讯云短信服务
返回: { code: 200, msg: "验证码已发送", data: { code: "123456" } }  // P0阶段返回验证码方便测试
```

### 3. 搜索逝者 `GET /api/search?keyword=xxx`
```
逻辑:
  1. 查询 mem_deceased 表：name LIKE '%keyword%' AND is_public='0' AND status='0' AND del_flag='0'
  2. 左连接 mem_organization 获取 orgName
  3. 限制返回 20 条
返回: { code: 200, data: [{ deceasedId, name, gender, birthDate, deathDate, coverImage, qrcodeCode, orgName }] }
```

### 4. 获取用户信息 `GET /api/userInfo`
```
逻辑:
  1. 从 Authorization header 中提取 token
  2. 通过 TokenService 解析获取 LoginUser
  3. 返回用户基本信息
返回: { code: 200, data: { userId, userName, nickName, phonenumber, avatar, sex } }
```

### 后端待创建/修改的文件
| 文件 | 操作 |
|------|------|
| `WxMiniLoginController.java` | 新建 - 手机号登录 + 发送验证码 + 获取用户信息 |
| `PublicApiController.java` | 修改 - 新增 GET /api/search 搜索逝者 |
| `DeceasedMapper.xml` | 修改 - 新增 searchPublic 查询 |
| `IDeceasedService.java` | 修改 - 新增 searchPublic 方法声明 |
| `DeceasedServiceImpl.java` | 修改 - 实现 searchPublic |
| `application.yml` | 修改 - 可选，微信 appid/secret 配置 |

## 示例数据 SQL
已生成：`sql/memorial_sample_data.sql`

包含：
- 用户（user_id=3, phone=17310503610, nickname=吴丹）
- 2个机构
- 3个逝者（王建国 qrcodeCode=WJG001, 李秀英 LXY002, 张明远 ZMY003）
- 9张相册
- 2个视频
- 8条留言（7条已审核，1条待审核）
- 13条献花
- 9条统计
- 3个订单

测试流程：
1. 首页搜索"王建国" → 进入纪念页 → 查看相册/视频/留言/献花
2. 直接访问 `http://localhost:5173/#/pages/memorial/detail?code=WJG001`
3. H5 登录：手机号 17310503610，验证码从后端日志/接口返回获取
