---
name: memorial-dev
description: 在「云上纪念」纪念馆平台仓库（/Users/k02/myProjects/memorial）做任何开发任务时使用。技术栈 RuoYi-Vue 3.9 + SpringBoot 3 + MySQL + Redis + 阿里云OSS，三端：后端 ruoyi-admin、用户端 memorial-app（uni-app Vue3/TS，小程序+H5）、管理后台 ruoyi-ui（Vue2）。当用户在本仓库提任何后端 Java/接口、前端页面、数据库、部署联调、支付或业务规则相关的编码、调试、重构、新增功能需求时，务必先读本 skill 再动手——本项目有若干刻意设计（/api/** 全 permitAll 靠手动校验 token、OSS 已替代若依本地文件、逻辑删除、docs 与 DDL 取值不一致），不读就改极易踩坑。即使用户没明说"用 skill"，只要任务涉及这个仓库的代码就应触发。
---

# 云上纪念 开发指南

本 skill 是在这个仓库工作的入口。读完本文件你就能安全地动手；需要细节时再按"按需深入"一节去读 `references/` 下对应文档。

## 这是什么项目

面向逝者家属与亲友的线上纪念馆平台。家属为逝者建纪念页（生平/相册/视频），生成二维码贴在墓碑上；亲友扫码进入留言、献花。基于若依 RuoYi-Vue 二次开发，三端：

- **ruoyi-admin**（后端，SpringBoot 3）：业务代码集中在 `com.ruoyi.memorial` 包。
- **memorial-app**（用户端，uni-app + Vue3 + TS + Pinia）：微信小程序为主端，H5 为辅端，一套代码多端编译。
- **ruoyi-ui**（管理后台，Vue2 + Element UI）：后台/机构管理员用。

## 技术栈速查

| 层 | 技术 | 版本/备注 |
|---|---|---|
| JDK | Java | 17（无 `-XX:+UseParallelOldGC`，已移除） |
| 后端 | Spring Boot / RuoYi | 3.5.8 / 3.9.0 |
| 安全 | Spring Security + JWT | token header `Authorization` |
| ORM | MyBatis + PageHelper | resultMap 显式映射 |
| DB | MySQL 8 | utf8mb4，库名 `memorial` |
| 缓存 | Redis | database=11 |
| 存储 | 阿里云 OSS | bucket=memorials（北京） |
| 用户端 | uni-app + Vue3 + TS + Pinia | Vite 5 |
| 管理后台 | Vue2 + Element UI | RuoYi 原生 |
| 部署 | Nginx（唯一公网入口） | 后端 18080 内网反代 |

端口：后端 18080 / H5 dev 5173 / 管理后台 dev 8008 / MySQL 3306 / Redis 6379。

## 代码在哪

后端业务代码全在 `ruoyi-admin/src/main/java/com/ruoyi/memorial/`：
```
controller/   10 个 Controller，两类（管理后台 /memorial/** + 小程序 /api/**）
service/      IXxxService + impl/XxxServiceImpl（+ OssService 无接口）
mapper/       MyBatis 接口（XML 在 resources/mapper/memorial/）
domain/       实体（不继承 BaseEntity）
config/       OssConfig / OssProperties
utils/        QrCodeUtil（zxing 生成二维码 + 上传 OSS）
```
框架代码在 `ruoyi-common` / `ruoyi-framework` / `ruoyi-system`——**尽量不动**，扩展优先在 `memorial` 包内新增。

前端：用户端 `memorial-app/src/`（api/types/stores/components/pages），管理后台 `ruoyi-ui/src/api/memorial/` + `views/memorial/`。

## 红线：先读这几条再改代码

这些是项目刻意设计或高风险点，踩了代价很大。理解**为什么**比记规则重要。

1. **`/api/**` 在 SecurityConfig 是 permitAll，别改成 authenticated。**
   小程序公开接口（扫码首屏 `/api/qrcode/{code}`、登录 `/api/phoneLogin`、提交留言/献花）必须匿名可访问。需要登录的 `/api/family/**` 和 `/api/userInfo` 靠 Controller 内**手动校验 token**（`SecurityUtils.getLoginUser()` 判 null 返回 401）。原理：`JwtAuthenticationTokenFilter` 仍会对 `/api/**` 解析 token 塞入上下文，所以带了 token 才能拿到用户。别为了"加鉴权"把 `/api/**` 改成需要登录，否则公开接口全 401。详见 `references/security.md`。

2. **文件上传走 `OssService`，别用若依 `/common/upload`。**
   `ruoyi.profile` 已被改成占位字符串，若依原生本地文件功能（`/common/upload`、`/common/download`、`/profile/**`、头像上传）**全部失效**。业务上传调 `OssService.upload(file, "memorial/<userId>/<type>")`，返回完整 URL 直接存库。管理后台通用 `ImageUpload` 组件默认 `action=/common/upload` 会失败。详见 `references/database.md`。

3. **OSS 的 `endpoint` 和 `url-prefix` 是两回事，别混用。**
   `endpoint` 是后端上传用（生产必须 `-internal` 内网域名，省流量费）；`url-prefix` 是前端查看用（必须指向 nginx 公网域名/IP，不能用 internal 域名——公网不可达，不能用 127.0.0.1——前端在用户浏览器会指向用户本机）。详见 `references/database.md`。

4. **删除是逻辑删除，别改物理删除。**
   所有业务表用 `del_flag`（'0'存在 '2'删除），Mapper 的 `delete` 实际是 `update set del_flag='2'`，查询必须带 `del_flag='0'`。

5. **docs 与 DDL 取值冲突时，以 DDL 为准。**
   套餐类型 DDL 是 `0基础/1高级/2VIP`，但 `docs/phase-p2-payment.md` 写 `1/2/3`；订单状态 DDL 是 3 态（0待支付/1已支付/2已退款），P2 文档写 4 态（含"已取消"）。改支付/套餐代码前先核实实际取值。详见 `references/payment.md`。

6. **memorial 的 domain 不继承 `BaseEntity`。**
   与 ruoyi 系统表实体不同，`createBy/createTime/updateBy/updateTime` 在 memorial 实体里手写。新增实体保持一致，别套 BaseEntity。

7. **家属接口必须校验归属，否则可越权。**
   `/api/family/**` 每个涉及逝者资源的写操作都要 `verifyOwnership(deceasedId, userId)`（`deceased.family_user_id == 当前 userId`）。不校验就能操作别人家的纪念馆。详见 `references/api.md`。

8. **别在日志、返回值、文档里泄露真实凭据。**
   `application*.yml` / `application-druid.yml` 中可能含真实 DB 密码、Redis、OSS AK/SK。生产应环境变量注入。写文档/对话用占位符。

## 接到需求怎么做

按这个顺序，别跳步：

1. **定位端与类型**：改后端 / memorial-app / ruoyi-ui？新增还是修改？读对应 `references/`（见下方指针表）和同类现有代码作模板。

2. **确认设计要点**（动手前想清楚）：
   - 接口属于哪类鉴权？管理后台（`/memorial/**` + `@PreAuthorize`）/ 公开（`/api/**` 无需登录）/ 家属（`/api/family/**` + `requireUserId` + `verifyOwnership`）。
   - 返回值？列表 `TableDataInfo` / 其他 `AjaxResult` / 多对象 `ajax.put("key", obj)`。
   - 字段是否要前后端四处同步？（domain + Mapper resultMap + XML + 前端 types）
   - 多表写要加 `@Transactional(rollbackFor = Exception.class)`。
   - 是否碰到上面的红线？

3. **实现**：抄同类现有代码风格，别引入新依赖/新风格（前端不引第三方库，后端字段注入与现有一致）。遵循 `references/coding.md`。

4. **验证**：见下一节，**必须跑**。

5. **自检 + 提交**：对照 `references/workflow.md` 的自检清单。提交前确认没把真实凭据写进 yml。

常见需求（新增管理后台模块 / 新增家属接口 / 新增公开接口 / 改业务规则 / 改数据库字段）的逐步实现路径见 `references/workflow.md`。

## 改完怎么验证

最低要求——编译/类型检查/构建必须通过：

```bash
# 后端编译（必跑）
cd /Users/k02/myProjects/memorial && mvn -pl ruoyi-admin -am compile -DskipTests

# 用户端类型检查 + H5 构建（必跑）
cd memorial-app && npm run type-check && npm run build:h5

# 管理后台构建（改了 ruoyi-ui 时）
cd ruoyi-ui && npm run build:prod
```

启动联调（macOS 开发模式一键起 3 端）：
```bash
./dev.sh            # 后端 18080 + H5 5173 + 管理后台 8008
./dev.sh -d         # 后台运行，日志 /tmp/memorial-dev.log
./dev.sh stop       # 停止
```
前提：MySQL（`application-druid.yml` 配置）和 Redis 可达。后端起来后 Swagger 在 `http://localhost:18080/swagger-ui.html`。

冒烟测试核心接口：
```bash
curl -s http://localhost:18080/api/qrcode/<编码> | python3 -m json.tool
curl -s 'http://localhost:18080/api/search?keyword=张' | python3 -m json.tool
```
管理后台接口需先 `POST /login` 拿 token 再带 `Authorization: Bearer <token>` 调试。

完整的验证命令、联调流程、自检清单、陷阱速查见 `references/workflow.md`。

## 按需深入：何时读哪个 reference

SKILL.md 只给红线和工作流骨架。做具体任务时读对应的 reference：

| 要做的事 | 读这个 |
|---|---|
| 了解完整目录树、配置文件、各阶段实现状态 | `references/project.md` |
| 写后端代码（Controller/Service/domain/Mapper 规范、异常、日志、事务） | `references/coding.md` |
| 写/改接口（两类 Controller 模式、返回值、家属鉴权、接口清单） | `references/api.md` |
| 改登录/鉴权/权限，或任何 `/api/**` 安全相关 | `references/security.md` |
| 改数据库表、写 Mapper XML、文件上传/OSS | `references/database.md` |
| 改业务规则（机构/逝者/相册/视频/留言/献花/统计/家属/二维码/搜索） | `references/business.md` |
| 做支付（微信支付，当前未实现）、套餐、订单 | `references/payment.md` |
| 改前端（memorial-app 用户端 或 ruoyi-ui 管理后台） | `references/frontend.md` |
| 查完整验证流程、常见需求实现路径、自检清单、陷阱速查 | `references/workflow.md` |

大文件（>300 行）的 reference 顶部有目录。读时只读与当前任务相关的章节即可。

## 常见陷阱速查

| 现象 | 原因 | 对策 |
|---|---|---|
| 小程序公开接口全 401 | 把 `/api/**` 改成 authenticated | 保持 permitAll，家属接口手动校验 |
| 上传失败 | 用了 `/common/upload` | 用 `OssService.upload` |
| 前端看不到图 | url-prefix 用了 internal 域名或 127.0.0.1 | url-prefix 指向 nginx 公网地址 |
| 越权操作他人纪念馆 | 家属接口没校验归属 | 每个写操作 `verifyOwnership` |
| 接口数据前端不显示 | 字段名没对齐 | 对照 domain 驼峰，四处同步 |
| `@PreAuthorize` 在 `/api/family` 不生效 | `/api/**` permitAll | 用手动 `requireUserId` |
| 大视频上传失败 | multipart 上限 20MB | 调大 max-file-size/max-request-size |
| 套餐值用 1/2/3 出错 | 与 DDL 0/1/2 冲突 | 以 DDL 为准 |
| 删除后数据没了 | 改成了物理删除 | 逻辑删除 `set del_flag='2'` |

更多陷阱和详解见 `references/workflow.md` 第 6 节。

## 当前未完成（接需求时留意）

P2 支付完全未实现（订单仅 CRUD 骨架，无微信支付代码）；献花频率限制文档要求但代码未实现；视频上传上限未调大；机构数据隔离未实现；SMS 验证码 P0 直接返回（安全风险）。详见 `references/business.md` 待办表和 `references/payment.md`。
