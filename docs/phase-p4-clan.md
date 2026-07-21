# 族谱模块 - 产品设计方案（P4）

> 更新日期：2026-07-17
> 状态：方案待评审
> 依赖基线：`docs/memorial-user-prd.md`、`docs/iteration-plan.md`、`sql/memorial_ddl.sql`、`skills/memorial-dev/`

---

## 〇、架构原则（前提）

本项目**尚未发布生产，代码风格未定**，代码与表结构均可改。本模块按以下原则设计：

1. **为微服务拆分留口**：后续可能拆为 customer 服务（用户端系统）与 admin 服务（运营系统）。业务模块须**自包含**（domain/mapper/service/controller/cache 聚合），模块间依赖走**接口**而非实现类，便于未来跨服务替换为 RPC。
2. **共享能力下沉**：跨模块复用的能力（统计、OSS、二维码）归入 `shared/`，作为可独立抽取单元。
3. **派生数据分层维护**：generation 等源数据变化时需重算的字段，由模块自有的 Cache Service 统一维护入口，不散落在业务 Service。
4. **不过度抽象**：各业务模块按需定义自己的 `XxxCacheService`，不强制统一接口；不搞全项目通用 CacheManager。

> 注：现有 `deceased` 等模块的包结构平迁工作量大，**本次不动**，仅族谱模块与统计能力采用新组织。现有模块平迁列为后续演进方向。

---

## 一、模块定位与价值

现有项目核心是「逝者纪念馆」--一个逝者一个纪念馆（`mem_deceased`），家属可建多个。族谱模块把独立的逝者（及亲属）组织成**家族血脉网络**，与纪念馆是串联而非替代关系：

- **族谱进纪念馆**：族谱中已故且建馆的成员，点击跳纪念页缅怀。
- **纪念馆进族谱**：逝者纪念馆页加「查看族谱」入口，看逝者在家族中的位置。
- **成体系缅怀**：一个家族多个逝者纪念馆经族谱串联，缅怀有脉络。

---

## 二、用户角色与权限

| 角色 | 说明 | 能力 |
|------|------|------|
| 族长 | 创建族谱的家属用户（`family_user_id`） | 增删改族谱/成员/关系、设公开、设肖像开关、生成二维码；查看完整信息含在世成员 |
| 访客 | 亲友或扫码者 | 浏览公开族谱；在世成员脱敏（生卒/bio 隐藏，仅名讳；肖像按族长开关） |
| 管理后台 | ruoyi-ui 管理员 | 族谱/成员列表、编辑成员、下架、删除 |

> 沿用现有家属身份判定：`getMyClans()` 非空即族长身份，显示「我的族谱」入口。

---

## 三、功能模块

### 3.1 用户端（memorial-app，小程序+H5）

| 功能 | 说明 |
|------|------|
| 我的族谱 | 族长创建的族谱列表，点击进入管理 |
| 创建族谱 | 族谱名/姓氏/简介/封面/始祖成员/公开开关/在世肖像开关 |
| 编辑族谱 | 修改基本信息、重新生成二维码 |
| 成员管理 | 新增/编辑/删除成员；设置生父母、配偶、养继父母；多段婚姻 |
| 族谱浏览 | 世系层级列表（宝塔式）展示；在世成员对访客脱敏 |
| 二维码 | 生成族谱专属二维码（复用 `QrCodeUtil`），扫码直达 |
| 搜索 | 按族谱名/姓氏搜索公开族谱 |
| 纪念馆跳转 | 成员关联 `deceased_id` 时，「查看纪念馆」跳转纪念页 |
| 纪念馆入口 | 逝者纪念馆页加「查看族谱」入口（若该逝者关联了族谱成员） |

### 3.2 管理后台（ruoyi-ui）

| 功能 | 说明 |
|------|------|
| 族谱列表 | 分页列表（族谱名/族长/成员数/公开/状态） |
| 族谱详情 | 查看族谱基本信息 + 成员世系 |
| 成员编辑 | 管理员可代为新增/编辑/删除成员 |
| 下架/恢复 | 改 `status`（0正常 1停用），停用后访客不可见 |
| 删除 | 逻辑删除（`del_flag='2'`） |

---

## 四、架构分层设计

### 4.1 包组织（为拆分留口）

```
com.ruoyi.memorial
├── clan/                              # 族谱模块（自包含，可独立抽取）
│   ├── domain/      Clan, ClanMember, ClanRelation
│   ├── mapper/      ClanMapper, ClanMemberMapper, ClanRelationMapper
│   ├── service/     IClanService, IClanMemberService, IClanRelationService + impl
│   ├── cache/       IClanCacheService + impl（派生数据维护层）
│   └── controller/  ClanController(后台), ClanPublicController, ClanFamilyController
├── deceased/  organization/  message/  flower/  album/  video/  order/   # 现有模块（本次不平迁）
└── shared/                            # 跨模块共享能力
    ├── statistics/  IStatisticsService + impl + Statistics(domain/mapper/xml) ← 从根目录迁入
    ├── oss/         OssService, OssConfig, OssProperties
    └── qrcode/      QrCodeUtil
```

- Mapper XML 跟随包路径放 `resources/mapper/memorial/clan/`、`resources/mapper/memorial/shared/statistics/`（`application.yml` 用 `classpath*:mapper/**/*Mapper.xml` 通配，无需改 yml）。
- `typeAliasesPackage: com.ruoyi.**.domain` 覆盖新包，XML 仍可用类名。
- 模块间依赖走接口：`ClanCacheService` 依赖 `IStatisticsService` 接口，不依赖实现。

### 4.2 缓存服务 `IClanCacheService`（族谱派生数据维护层）

```
IClanService / IClanMemberService / IClanRelationService   ← 业务规则 CRUD（归属/字段校验/写库）
        ↓ 写操作完成后委托
IClanCacheService                                            ← 派生数据维护层
        ├── refreshGeneration(clanId)     从 root BFS 沿生父母链重算所有成员 generation
        ├── refreshCounts(clanId)         重算 member_count / generation_count
        ├── buildTree(clanId, viewer)     构建世系树（含在世脱敏）
        └── incrementVisit(clanId)        族谱访问 +1（委托 IStatisticsService，subjectType=1）
```

| 写操作 | 触发的缓存刷新 |
|---|---|
| 新增/编辑/删除成员 | `refreshGeneration` + `refreshCounts` |
| 新增/删除关系（设父母/配偶） | `refreshGeneration` |
| 族长改始祖 `root_member_id` | `refreshGeneration` |
| 访客扫码/浏览 | `incrementVisit` |
| 读取世系树 | `buildTree`（直接走 CacheService） |

- 第一版**不接 Redis**：generation 是 DB 冗余物化字段，读零成本；Redis 留给 token/SMS。
- 未来读频繁时这层内可加 Redis，不影响上层 Service。
- 各业务模块按需自有 `XxxCacheService`，不强制统一接口。

### 4.3 统计共享化（改造现有 `mem_statistics`）

把 `mem_statistics` 改造为**通用 subject 统计表**，族谱与逝者共用，统计能力抽为 `shared/statistics`：

```sql
mem_statistics (
  stat_id, subject_type tinyint, subject_id bigint,
  visit_date, visit_count, message_count, flower_count,
  unique key uk_subject_date (subject_type, subject_id, visit_date),
  审计字段
)
-- subject_type: 0逝者 1族谱（未来可扩）
-- subject_id  : deceased_id 或 clan_id
```

改动面（6 文件 + 表 + 前端 types）：

| 文件 | 改动 |
|---|---|
| `Statistics.java`(迁 shared) | `deceasedId` -> `subjectType` + `subjectId` |
| `StatisticsMapper.java` + `.xml`(迁 shared) | SQL 全改 subject 字段，uk 改 |
| `IStatisticsService` + impl(迁 shared) | increment/total 方法加 `subjectType` 参数 |
| `PublicApiController` | 3 increment + 3 total 补 `subjectType=0` |
| `StatisticsController` | list/total 补 `subjectType` |
| `FamilyApiController` | 统计相关补 `subjectType=0` |
| `memorial_ddl.sql` | `mem_statistics` 表结构改 |
| 前端 `types/statistics` | 字段同步 |

- 沿用现有 `INSERT ... ON DUPLICATE KEY UPDATE` 原子计数。
- 族谱访问计数：`incrementVisit` -> `IStatisticsService.incrementVisitCount(1, clanId)`。

---

## 五、数据模型设计

新增 3 表 + 改造 1 表，沿用规范：`del_flag` 逻辑删除、审计字段、`domain` 不继承 `BaseEntity`、外键字段加索引、取值在注释写明。

### 5.1 族谱表 `mem_clan`

| 字段 | 类型 | 说明 |
|------|------|------|
| clan_id | bigint PK | 族谱ID |
| clan_name | varchar(100) | 族谱名 |
| surname | varchar(20) | 姓氏（分类/搜索） |
| family_user_id | bigint | 族长用户ID（归属判定） |
| org_id | bigint | 所属机构（家属创建默认0） |
| root_member_id | bigint | 始祖成员ID（族谱树根） |
| cover_image | varchar(200) | 族谱封面图（OSS URL） |
| description | text | 族谱简介 |
| is_public | char(1) | 是否公开（0公开 1不公开） |
| show_alive_avatar | char(1) | 在世成员肖像对访客可见（0显示 1不显示），族长设 |
| qrcode_code | varchar(50) | 二维码编码 |
| qrcode_url | varchar(200) | 二维码图片URL（OSS） |
| member_count | int | 成员数（缓存） |
| generation_count | int | 世代数（缓存） |
| status | char(1) | 状态（0正常 1停用，停用访客不可见） |
| del_flag | char(1) | 删除标志（0存在 2删除） |
| 审计字段 | - | create_by/create_time/update_by/update_time |

索引：`idx_family_user_id`、`idx_qrcode_code`。

### 5.2 族谱成员表 `mem_clan_member`

| 字段 | 类型 | 说明 |
|------|------|------|
| member_id | bigint PK | 成员ID |
| clan_id | bigint | 所属族谱 |
| name | varchar(50) | 姓名 |
| gender | char(1) | 性别（0男 1女 2未知） |
| birth_date | date | 出生日期（可空，在世对访客脱敏） |
| death_date | date | 逝世日期（空=在世） |
| is_alive | char(1) | 是否在世（0在世 1已故，冗余便于查询脱敏） |
| birth_place | varchar(100) | 出生地 |
| title | varchar(50) | 字号/辈分字 |
| generation | int | 世代（始祖=1，CacheService 维护） |
| avatar | varchar(200) | 肖像（OSS URL） |
| bio | text | 简介（在世对访客脱敏） |
| deceased_id | bigint | 弱关联逝者纪念馆（可空，仅已故+已建馆时填） |
| sort_order | int | 同代排序 |
| del_flag | char(1) | 删除标志 |
| 审计字段 | - | - |

索引：`idx_clan_id`、`idx_deceased_id`、`idx_generation`。

> `deceased_id` 仅在「已故 + 已建纪念馆」时填；在世成员必为空。删除逝者纪念馆不级联删成员，仅置空 `deceased_id`。

### 5.3 族谱关系表 `mem_clan_relation`

支持复杂关系（生父母/配偶/养继父母/多段婚姻）。

| 字段 | 类型 | 说明 |
|------|------|------|
| relation_id | bigint PK | 关系ID |
| clan_id | bigint | 所属族谱 |
| from_member_id | bigint | 子辈/本人 |
| to_member_id | bigint | 父辈/配偶 |
| relation_type | tinyint | 关系类型（见下） |
| relation_order | int | 配偶第几段婚姻（默认0，父母类为0） |
| extra | varchar(500) | 备注（婚姻起止、收养说明等） |
| 审计字段 | - | create_by/create_time |

`relation_type`：1生父 2生母 3配偶(双向) 4养父 5养母 6继父 7继母（方向：from=子辈 to=父辈/配偶）。

索引：`idx_clan_id`、`idx_from_member`、`idx_to_member`。

> 一个成员可有多个父类关系（生父+继父），展示取生父母为主、养继为辅。世代沿生父母链 BFS。

### 5.4 `mem_statistics` 改造

见 4.3。

### 5.5 字典与菜单

- 新增字典 `memorial_clan_relation_type`（1-7）。
- 管理后台菜单注册：族谱管理 + 族谱成员（list/query/edit/remove 权限标识）。
- 写 `sql/memorial_alter_v1_clan.sql` 增量，同步更新 `memorial_ddl.sql` 与 `memorial_menu_dict.sql`。

---

## 六、接口设计

### 6.1 用户端公开接口 `/api/clan/**`（permitAll）

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/clan/qrcode/{code}` | GET | 扫码进入族谱，聚合族谱信息+世系树（在世脱敏），记访问 |
| `/api/clan/{clanId}` | GET | 浏览公开族谱（在世脱敏） |
| `/api/clan/search?keyword=` | GET | 搜索公开族谱（仅 is_public=0/status=0，限 20 条） |

### 6.2 用户端家属接口 `/api/family/clan/**`（手动鉴权 + 校验归属）

每接口必须 `requireUserId()` + `verifyClanOwnership(clanId, userId)`。

| 资源 | 接口 |
|------|------|
| 族谱 | GET `/clans`、GET `/clan/{id}`、POST `/clan`、PUT `/clan`、DELETE `/clan/{id}`、POST `/clan/qrcode/{id}` |
| 成员 | GET `/clan/{clanId}/members`、POST `/clan/member`、PUT `/clan/member`、DELETE `/clan/member/{memberId}` |
| 关系 | POST `/clan/relation`、DELETE `/clan/relation/{relationId}`、PUT `/clan/member/{memberId}/parents`（批量设父母） |
| 世系树 | GET `/clan/{clanId}/tree`（完整，含在世） |

安全要点：创建/更新族谱 `setFamilyUserId(userId)` 强制绑定；成员/关系写操作前查实体拿 `clan_id` 再校验归属；关联 `deceased_id` 时校验该逝者 `family_user_id == userId`。

### 6.3 管理后台接口 `/memorial/clan/**`（@PreAuthorize）

| 操作 | 方法 | 路径 | 权限 |
|------|------|------|------|
| 族谱列表 | GET | `/list` | `memorial:clan:list` |
| 族谱详情 | GET | `/{clanId}` | `memorial:clan:query` |
| 成员列表 | GET | `/members/{clanId}` | `memorial:clan:query` |
| 新增成员 | POST | `/member` | `memorial:clan:add` |
| 编辑成员 | PUT | `/member` | `memorial:clan:edit` |
| 删除成员 | DELETE | `/member/{memberIds}` | `memorial:clan:remove` |
| 下架/恢复 | PUT | `` | `memorial:clan:edit` |
| 删除族谱 | DELETE | `/{clanIds}` | `memorial:clan:remove` |

> 后台成员编辑与用户端共用 `IClanMemberService`，写操作后同样委托 `IClanCacheService` 刷新派生数据。

---

## 七、可视化方案：世系层级列表（宝塔式）

第一版用递归组件渲染，不引第三方库（符合项目规范）。

### 7.1 数据结构

`GET /clan/{clanId}/tree`（`IClanCacheService.buildTree`）返回嵌套树：

```json
{
  "clan": { "clanName": "张氏族谱", "generationCount": 5 },
  "root": {
    "memberId": 1, "name": "张某某", "gender": "0", "generation": 1,
    "isAlive": "1", "deceasedId": 12, "hasMemorial": true,
    "spouses": [ {"memberId": 2, ...} ],
    "children": [ { ...递归... } ]
  }
}
```

### 7.2 构建逻辑（CacheService）

1. 以 `root_member_id` 为根。
2. 反查子女：`to_member_id = X` 且 `relation_type ∈ {1,2,4,5,6,7}` -> `from_member_id` 为子女（去重，一个子女只挂一次主链）。
3. 配偶：`relation_type = 3` 涉及 X -> 并列 `spouses`，按 `relation_order` 排序。
4. 递归构建 `children`，按 `generation` + `sort_order` 排序。
5. 世代：root=1 BFS 沿生父母链 +1，写入/改关系后 `refreshGeneration` 重算缓存。

### 7.3 前端组件

- `ClanTreeNode.vue`：递归渲染节点（头像/姓名/生卒/字号）+ 配偶横排 + 子代缩进。
- 已故且 `hasMemorial=true` 显示「查看纪念馆」按钮。
- 在世成员仅显示名讳（脱敏数据后端返回）。

---

## 八、隐私脱敏规则

| 数据 | 族长/管理后台 | 访客 |
|------|--------------|------|
| 在世成员生卒 | 完整 | 隐藏（返回 null） |
| 在世成员简介/字号 | 完整 | 隐藏（仅显示姓名） |
| 在世成员肖像 | 完整 | 按 `show_alive_avatar`（0显示 1不显示） |
| 已故成员 | 完整 | 完整 |
| 私密族谱（is_public=1） | 可见 | 不可见 |
| 停用族谱（status=1） | 可见 | 不可见 |

脱敏在**后端响应层**做（公开接口/访客身份），不在前端脱敏。

---

## 九、与现有模块的结合点

| 结合点 | 实现 |
|--------|------|
| 纪念馆 -> 族谱 | `mem_deceased` 详情查 `mem_clan_member.deceased_id = ?` 取 clan，有则显示「查看族谱」入口 |
| 族谱 -> 纪念馆 | 成员 `deceased_id` 非空时，「查看纪念馆」跳 `/pages/memorial/detail?code=<该逝者qrcode_code>` |
| 二维码 | 复用 `QrCodeUtil.generateForCode(code, userId)`，存 `memorial/<userId>/qrcode/clan-<code>.png`；扫码内容 `<h5-base-url>/#/pages/clan/detail?code=<code>` |
| 上传 | 族谱封面/成员肖像走 `OssService.upload(file, "memorial/<userId>/clan")`，URL 直接存库（不用 /common/upload） |
| 统计 | 族谱访问经 `IStatisticsService(subjectType=1)`，与逝者统计共用 `mem_statistics` |

---

## 十、关键业务规则与红线

1. **鉴权**：`/api/clan/**` permitAll；`/api/family/clan/**` 手动 `requireUserId` + `verifyClanOwnership`；`/memorial/clan/**` 用 `@PreAuthorize`。不改 `/api/**` 为 authenticated。
2. **逻辑删除**：三表均 `del_flag`，查询带 `del_flag='0'`。
3. **domain 不继承 BaseEntity**：`Clan`/`ClanMember`/`ClanRelation` 手写审计字段。
4. **字段四处同步**：domain + resultMap + XML + 前端 types。
5. **上传走 OssService**：封面/肖像/二维码均走 OSS。
6. **模块自包含 + 接口依赖**：族谱代码在 `clan/` 子包；依赖统计走 `IStatisticsService` 接口。
7. **派生数据经 CacheService**：generation/counts/tree/incrementVisit 统一经 `IClanCacheService`，业务 Service 不直接改这些字段。
8. **越权防护**：家属写操作校验归属；关联 `deceased_id` 校验该逝者归属。
9. **凭据**：DDL/文档用占位符，不泄露真实 OSS AK/SK。

> 撤销"现有代码尽量不动"约束：本次可改 `mem_statistics` 表结构与现有调用点（风格未定，面向拆分重设计）。

---

## 十一、实施计划与验收

### 11.1 迭代规划

| 迭代 | 内容 | 预计 |
|------|------|------|
| P4.1 | 包结构调整 + 统计改造：建 `clan/`、`shared/` 子包；`mem_statistics` 改通用 subject 表；迁统计到 shared；改 6 处调用点 + 前端 types | 1-2 天 |
| P4.2 | 后端族谱：3 表 DDL + 字典菜单 SQL + domain/mapper/service/controller（公开/家属/后台三类）+ `IClanCacheService`（generation/counts/tree/incrementVisit）+ 脱敏 | 3-4 天 |
| P4.3 | 用户端：我的族谱/创建/编辑/成员管理/关系设置/浏览页/二维码/纪念馆跳转 + `ClanTreeNode` 组件 | 4-5 天 |
| P4.4 | 管理后台：ruoyi-ui 族谱列表/详情/成员编辑/下架页 + API 文件 | 2 天 |
| P4.5 | 联调：脱敏验证、世系树渲染、扫码/搜索/双向跳转/统计计数链路、性能抽测 | 1-2 天 |

### 11.2 验收清单

- [ ] `clan/`、`shared/statistics/` 子包建立，包路径与类引用一致
- [ ] `mem_statistics` 改 subject 通用表，uk 改 `uk_subject_date`，现有逝者统计回归通过（subjectType=0）
- [ ] 族谱访问计数写入 `mem_statistics`(subjectType=1) 正常
- [ ] 3 表 DDL + 字典菜单 SQL 执行成功
- [ ] 后端 `mvn -pl ruoyi-admin -am compile -DskipTests` 通过
- [ ] 公开/家属/后台三类接口联调通过（curl 冒烟）
- [ ] 在世成员对访客脱敏生效（生卒/bio 不返回，肖像按开关）
- [ ] `IClanCacheService`：成员/关系写操作后 generation/counts 正确刷新
- [ ] 世系树构建正确，复杂关系（继父母/多段婚姻）正确挂载
- [ ] 族长完整走通：建谱 -> 加成员 -> 设关系 -> 生成二维码 -> 扫码浏览
- [ ] 纪念馆 ↔ 族谱双向跳转正常
- [ ] 用户端 `npm run type-check && npm run build:h5` 通过
- [ ] 管理后台 `npm run build:prod` 通过
- [ ] 越权访问他人族谱被拒
- [ ] 无真实凭据写入 yml/文档

---

## 十二、待确认/风险

| 项 | 说明 |
|----|------|
| 现有模块包平迁 | 本次仅族谱+统计采用新包组织，deceased 等模块平迁留待后续 |
| 世系树规模上限 | 超过 500 成员的分代加载留待后续 |
| 统计改造回归 | 改 `mem_statistics` 影响逝者统计，须回归家属/后台统计接口 |
