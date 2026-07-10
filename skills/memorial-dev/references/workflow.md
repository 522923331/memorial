# workflow.md - 实现流程、验证与陷阱

> **何时读**：每次接需求时参考流程；改完代码验证时；查陷阱速查时。SKILL.md 有精简版，本文是完整版。

## 目录
- [实现流程](#实现流程)
- [常见需求实现路径](#常见需求实现路径)
- [验证方法](#验证方法)
- [自检清单](#自检清单)
- [陷阱速查](#陷阱速查)

## 实现流程

1. **定位端与类型**：改后端 / memorial-app / ruoyi-ui？新增还是修改？读对应 reference（见 SKILL.md 指针表）和同类现有代码作模板。

2. **确认设计要点**（动手前）：
   - 接口属于哪类鉴权？管理后台（`/memorial/**`+`@PreAuthorize`）/ 公开（`/api/**` 无需登录）/ 家属（`/api/family/**`+`requireUserId`+`verifyOwnership`）。
   - 返回值？列表 `TableDataInfo` / 其他 `AjaxResult` / 多对象 `ajax.put`。
   - 字段是否前后端四处同步？（domain + resultMap + XML + 前端 types）
   - 多表写加 `@Transactional`。
   - 是否碰红线？（见 SKILL.md）

3. **实现**：抄同类现有代码风格，别引入新依赖/新风格。遵循 `coding.md`。新表/字段写 `sql/memorial_alter_vN_xxx.sql` 增量 + 更新主 DDL。新菜单/权限改 `memorial_menu_dict.sql`。

4. **验证**：见下文，**必须跑**。

5. **自检 + 提交**：对照自检清单。提交前确认没把真实凭据写进 yml。

## 常见需求实现路径

### 新增管理后台模块（如"祭品"）
1. `sql/`：建 `mem_offering` 表 + alter 脚本 + 菜单字典 SQL。
2. `domain/Offering.java`：实体（不继承 BaseEntity，带审计字段）。
3. `mapper/OfferingMapper.java` + `resources/mapper/memorial/OfferingMapper.xml`：resultMap + CRUD（带 del_flag）。
4. `service/IOfferingService.java` + `impl/OfferingServiceImpl.java`：透传 Mapper。
5. `controller/OfferingController.java`：`@RequestMapping("/memorial/offering")` extends BaseController，`@PreAuthorize('memorial:offering:*')` + `@Log`，标准 CRUD。
6. `ruoyi-ui/src/api/memorial/offering.js` + `views/memorial/offering/index.vue`。
7. 执行菜单字典 SQL，分配权限。

### 新增小程序家属接口
1. `FamilyApiController` 加方法，路径 `/api/family/<resource>`。
2. 首行 `Long userId = requireUserId(); if (userId == null) return AjaxResult.error(401, "请先登录");`。
3. 涉及逝者资源 `verifyOwnership(deceasedId, userId)`。
4. 调 Service，返回 `AjaxResult.success(...)`。
5. `memorial-app/src/api/family.ts` 加 API 函数（`needToken=true`）。
6. `types/family.ts` 加类型。页面调用，store 按需更新。

### 新增公开接口
1. `PublicApiController` 加方法，路径 `/api/<resource>`。
2. 参数手动校验，返回 `AjaxResult`。
3. 前端 `api/memorial.ts` 加函数（`needToken=false`）。

### 改业务规则（如献花频率限制）
1. 定位 `FlowerServiceImpl.insertFlower`。
2. insert 前加频率校验：按 IP/userId + deceasedId + 当天 count，超 3 次抛 `ServiceException("今日献花次数已达上限")`。
3. 实现后更新 `business.md` 待办表。

### 改数据库字段
1. 写 `sql/memorial_alter_vN_xxx.sql`（alter table）。
2. 更新 `memorial_ddl.sql` 主文件。
3. 更新 domain + resultMap + XML 的 select/insert/update 列。
4. 前端 types 加字段。固定取值加字典 SQL + 前端 useDict。

## 验证方法

### 后端
```bash
cd /Users/k02/myProjects/memorial
mvn -pl ruoyi-admin -am compile -DskipTests    # 编译，必跑
mvn -pl ruoyi-admin -am clean package -DskipTests   # 打包，产物 ruoyi-admin/target/ruoyi-admin.jar
```
启动：`./dev.sh`（macOS 开发模式）或 `java -jar ruoyi-admin/target/ruoyi-admin.jar`。后端 18080，Swagger `http://localhost:18080/swagger-ui.html`。

冒烟测试：
```bash
curl -s http://localhost:18080/api/qrcode/<编码> | python3 -m json.tool
curl -s 'http://localhost:18080/api/search?keyword=张' | python3 -m json.tool
curl -s -X POST http://localhost:18080/api/sms/send -H 'Content-Type: application/json' -d '{"phone":"13800138000"}' | python3 -m json.tool
```
管理后台接口需先 `POST /login` 拿 token，带 `Authorization: Bearer <token>` 调试。

### 用户端前端
```bash
cd memorial-app
npm run type-check        # 类型检查，必跑
npm run build:h5          # H5 构建，必跑
npm run dev:h5            # H5 dev 5173
npm run dev:mp-weixin     # 小程序（微信开发者工具打开 dist/build/mp-weixin）
```
`type-check` 和 `build:h5` 都必须通过。H5 dev 走 Vite proxy 到 18080，无需 nginx。验证页面视觉与改动前一致（组件重构时）。

### 管理后台前端
```bash
cd ruoyi-ui
npm run build:prod        # 生产构建，必跑
npm run dev               # dev 8008，proxy 到 18080
```
验证菜单可见、CRUD 正常、字段名对齐。

### 一键联调
```bash
./dev.sh            # macOS：后端(18080) + H5(5173) + 管理后台(8008)
./dev.sh -d         # 后台运行，日志 /tmp/memorial-dev.log
./dev.sh stop       # 停止
./dev.sh status     # 状态
./dev.sh -c         # 强制 clean 重编译后端
./dev.sh --prod     # 生产模式（build + 部署 nginx + 启 jar）
```
首次自动 `npm install`。前提：MySQL（`application-druid.yml` 的 8.140.249.192:3306/memorial）和 Redis（8.140.249.192:6379 db=11）可达，连不上改本地配置。

### 数据库
- DDL 在本地 MySQL 执行确认表结构。
- 查示例数据：`SELECT * FROM mem_deceased WHERE del_flag='0';`（应有 3 条）。
- 字典：`SELECT * FROM sys_dict_data WHERE dict_type='memorial_package_type';`。

### 联调（端到端）
家属登录 -> 创建纪念馆 -> 上传相册/视频 -> 审核留言 -> 生成二维码 -> 扫码访问 -> 留言/献花 -> 统计更新。验收标准见 `docs/iteration-plan.md` 各阶段清单。

## 自检清单

### 通用
- [ ] 没碰红线（`/api/**` permitAll、`ruoyi.profile`、`del_flag` 逻辑删除、框架基类）
- [ ] 没引入新依赖（除非必要且确认）
- [ ] 没在日志/返回值/文档泄露真实凭据
- [ ] 没硬编码 127.0.0.1 作为生产配置

### 后端
- [ ] `mvn compile` 通过
- [ ] Controller 鉴权模式正确
- [ ] 管理后台写操作加 `@Log`
- [ ] Service 多表写加 `@Transactional`
- [ ] Mapper XML 带 `del_flag='0'`，用 `#{}`
- [ ] 异常用 `ServiceException` / `AjaxResult.error`，未吞异常
- [ ] 新增字段四处同步（domain + resultMap + XML + 前端 types）

### 前端
- [ ] `type-check` 通过
- [ ] `build:h5` / `build:prod` 通过
- [ ] 字段名与后端一致（驼峰）
- [ ] API 函数 needToken 设置正确
- [ ] 新页面在 `pages.json` 注册
- [ ] 组件复用现有共享组件，用 rpx

### 数据库
- [ ] 增量 SQL 已写，主 DDL 已同步
- [ ] 字段有 comment，状态码取值写明
- [ ] 外键关联字段有索引
- [ ] 菜单/字典 SQL 已写

## 陷阱速查

| 陷阱 | 后果 | 对策 |
|---|---|---|
| 把 `/api/**` 改成 authenticated | 小程序公开接口全 401 | 保持 permitAll，家属接口手动校验 |
| 用 `/common/upload` 上传 | 上传失败 | 用 `OssService.upload` |
| OSS url-prefix 用 internal 域名 | 前端看不到图 | 指向 nginx 公网地址 |
| OSS url-prefix 用 127.0.0.1 | 用户浏览器访问不到 | 用公网 IP/域名 |
| delete 改物理删除 | 数据丢失 | 逻辑删除 `set del_flag='2'` |
| 套餐取值用 1/2/3 | 与 DDL 0/1/2 冲突 | 以 DDL 为准 |
| 家属接口不校验归属 | 越权操作他人纪念馆 | 每个写操作 `verifyOwnership` |
| 前端字段名没对齐 | 接口数据不显示 | 对照 domain 驼峰，四处同步 |
| 视频上传没调大 multipart | 大视频上传失败 | 改 max-file-size/max-request-size |
| `@PreAuthorize` 保护 /api/family | 不生效（permitAll） | 用手动 `requireUserId` |
| 留言自动审核判断写错 | 不该通过的通过/反之 | `message_audit='0'` 才自动通过 |
| 统计 incrementVisit 是空操作 | 改错地方 | 统计走 StatisticsService + mem_statistics 表 |
| nginx proxy_pass 尾斜杠错 | 路由错乱 | `/api/` 不带尾斜杠（保留前缀），`/prod-api/` `/oss/` 带尾斜杠（去前缀） |

## 不确定时

- 文档与代码冲突 -> **以代码/DDL 为准**，在 business.md/payment.md 标注待核实。
- 不确定鉴权方式 -> 抄同类现有接口。
- 不确定字段含义 -> 查 `sql/memorial_ddl.sql` 注释 + Mapper resultMap。
- 不确定前端调用 -> 查 `memorial-app/src/api/*.ts` 现有函数。
- 涉及支付/安全/凭据等高风险改动 -> 先确认再动手，别自行猜测。
