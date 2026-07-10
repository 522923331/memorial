# api.md - Controller 与接口返回规范

> **何时读**：新增或改接口时。两类 Controller 的鉴权和返回值写法是核心。

## 两类 Controller

新增接口先判断属于哪类，鉴权方式完全不同：

| 类型 | 路径 | 继承 BaseController | 鉴权 | 返回值 |
|---|---|---|---|---|
| 管理后台 | `/memorial/**` | 是 | `@PreAuthorize("@ss.hasPermi(...)")` | `TableDataInfo`（列表）/ `AjaxResult` |
| 小程序公开 | `/api/**`（非 family） | 否 | permitAll，无需登录 | `AjaxResult` |
| 小程序家属 | `/api/family/**` | 否 | 手动校验 token + 归属 | `AjaxResult` |

原理：`/api/**` 在 SecurityConfig 是 `permitAll`，但 `JwtAuthenticationTokenFilter` 仍解析 token 塞入上下文。所以 `/api/**` 带 token 能拿到用户，不带则 null。家属接口靠这个做"可选鉴权"。详见 `security.md`。

## 返回值

### AjaxResult（单对象/操作结果）
`{code, msg, data}`：
```java
AjaxResult.success()             // {code:200, msg:"操作成功"}
AjaxResult.success(data)         // 带数据
AjaxResult.error("msg")          // {code:500, msg:"msg"}
AjaxResult.error(401, "请先登录") // 自定义 code
AjaxResult.warn("msg")           // {code:601} 警告
```

多对象用 `ajax.put`：
```java
AjaxResult ajax = AjaxResult.success();
ajax.put("deceased", deceased);
ajax.put("albums", albums);
return ajax;
```
首屏聚合 `/api/qrcode/{code}` 就是这种（deceased + albums + videos + messages + flowers + 统计一次返回）。

前端约定：`code===200` 成功，`code===401` 登录过期，其他 toast `msg`。

### TableDataInfo（分页列表）
`{code, msg, rows, total}`：
```java
@GetMapping("/list")
public TableDataInfo list(Deceased deceased) {
    startPage();                              // 读 pageNum/pageSize
    List<Deceased> list = deceasedService.selectDeceasedList(deceased);
    return getDataTable(list);
}
```
只有管理后台列表用。小程序公开接口目前不分页，全量返回 + AjaxResult。

### toAjax（写操作）
`BaseController.toAjax(int rows)`：rows>0 success 否则 error。小程序 Controller 自己写同逻辑。

## 参数接收

| 场景 | 注解 |
|---|---|
| 路径变量 | `@PathVariable` |
| JSON body | `@RequestBody` |
| 表单/查询 | `@RequestParam` |
| 实体接收查询 | 无注解（自动绑定，如 `list(Deceased query)`） |

`@RequestParam` 的接口（如 batchAudit）前端必须用 `params` 传，不是 `data`。文件上传 `@RequestParam("file") MultipartFile file`。

## 管理后台接口（`/memorial/**`）

标准 CRUD（以 DeceasedController 为模板）：

| 操作 | 方法 | 路径 | 权限 | 返回 |
|---|---|---|---|---|
| 列表 | GET | `/list` | `:list` | TableDataInfo |
| 详情 | GET | `/{id}` | `:query` | AjaxResult |
| 新增 | POST | `` | `:add` | toAjax |
| 修改 | PUT | `` | `:edit` | toAjax |
| 删除 | DELETE | `/{ids}` | `:remove` | toAjax |
| 导出 | POST | `/export` | `:export` | AjaxResult(Excel) |

删除传 `Long[] ids`，逻辑删除。导出用 `ExcelUtil<Xxx>.exportExcel(list, "sheet名")`。权限标识需在 `memorial_menu_dict.sql` 注册。

## 小程序公开接口（`/api/**`，无需登录）

`PublicApiController` + `WxMiniLoginController`，不继承 BaseController，无 @PreAuthorize：

| 接口 | 方法 | 说明 |
|---|---|---|
| `/api/qrcode/{code}` | GET | 扫码首屏聚合 + 记访问 |
| `/api/search?keyword=` | GET | 搜索（仅 is_public=0/status=0，限 20 条） |
| `/api/message` | POST | 提交留言（自动审核判断） |
| `/api/flower` | POST | 提交献花 |
| `/api/messages/{deceasedId}` | GET | 已审核留言 |
| `/api/flowers/{deceasedId}` | GET | 献花列表 |
| `/api/sms/send` | POST | 发验证码（P0 直接返回，技术债） |
| `/api/phoneLogin` | POST | 手机号+验证码登录 |
| `/api/wxLogin` | POST | 微信小程序登录 |
| `/api/userInfo` | GET | 当前用户（手动校验 token） |
| `/api/bindPhone` | POST | 绑定手机号（手动校验 token） |

参数手动校验，返回 `AjaxResult.error("参数不完整")`。记 IP 用 `IpUtils.getIpAddr(request)`。

## 小程序家属接口（`/api/family/**`，手动鉴权）

**每个接口必须**：
1. `Long userId = requireUserId();` - null 则 `return AjaxResult.error(401, "请先登录");`
2. 涉及逝者资源的操作，`verifyOwnership(deceasedId, userId)` - 校验 `deceased.familyUserId == userId`，否则"无权操作"。
3. 返回 `AjaxResult.success(...)` 或 `toAjax(...)`。

```java
private Long requireUserId() {
    try {
        LoginUser u = SecurityUtils.getLoginUser();
        if (u != null) return u.getUserId();
    } catch (Exception ignored) {}
    return null;
}
private AjaxResult verifyOwnership(Long deceasedId, Long userId) {
    Deceased d = deceasedService.selectDeceasedById(deceasedId);
    if (d == null) return AjaxResult.error("未找到逝者信息");
    if (!userId.equals(d.getFamilyUserId())) return AjaxResult.error("无权操作");
    return null;  // null = 校验通过
}
```

家属接口清单（FamilyApiController）：

| 资源 | 接口 |
|---|---|
| 纪念馆 | GET `/memorials`、GET `/memorial/{id}`、POST `/memorial`、PUT `/memorial`、POST `/memorial/qrcode/{id}` |
| 上传 | POST `/upload`（封面） |
| 相册 | POST `/album/upload`、DELETE `/album/{albumId}`、PUT `/album` |
| 视频 | POST `/video/upload`、DELETE `/video/{videoId}`、PUT `/video` |
| 留言审核 | GET `/messages/pending/{id}`、PUT `/message/audit`、PUT `/message/batchAudit`、GET `/messages/pendingCount` |
| 统计 | GET `/statistics/{id}`、GET `/memorials/statistics` |
| 个人信息 | PUT `/profile` |

**安全要点**：创建纪念馆 `setFamilyUserId(userId)` 强制绑定；更新时也 `setFamilyUserId(userId)` 防篡改归属；删相册/视频/审核留言前先查实体拿 deceasedId 再 verifyOwnership；`updateProfile` 把 userName/password/status/delFlag 置 null 防篡改。

## 上传接口

| 接口 | 端 | 返回 |
|---|---|---|
| `POST /api/family/upload` | 家属 | success(url) |
| `POST /api/family/album/upload` | 家属 | success(album) |
| `POST /api/family/video/upload` | 家属 | success(video) |
| `POST /memorial/deceased/album/upload` | 管理后台 | success(imageUrl) |

调 `OssService.upload(file, "memorial/<userId>/<type>")`，返回完整 URL 存库。上限 10MB/单文件（视频需调大，见 database.md）。**别用 `/common/upload`**（已失效）。

## 新增接口自检

- [ ] 路径前缀对吗（`/memorial/<模块>` / `/api` / `/api/family/<资源>`）
- [ ] 鉴权对吗（@PreAuthorize / requireUserId+verifyOwnership / 无）
- [ ] 返回值对吗（TableDataInfo / AjaxResult）
- [ ] 管理后台写操作加 `@Log` 了吗
- [ ] 权限标识注册了吗
- [ ] 多对象返回用 `ajax.put` 了吗，前端类型同步了吗
- [ ] 家属涉及逝者资源的接口，verifyOwnership 调了吗
