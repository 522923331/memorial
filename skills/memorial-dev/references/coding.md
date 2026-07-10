# coding.md — 后端 Java 编码规范

> **何时读**：写或改后端 Java 代码时（Controller/Service/domain/Mapper）。先读 SKILL.md 的红线。

## 包结构

业务代码一律在 `com.ruoyi.memorial` 下，按 `controller` / `service`(+`impl`) / `mapper` / `domain` / `config` / `utils` 分包。别把业务类放进 `com.ruoyi.web` 或 `com.ruoyi.system`。

## Controller

### 两类，写法不同

**管理后台 Controller**（`/memorial/**`）— 继承 `BaseController`，用 RuoYi 权限注解：

```java
@RestController
@RequestMapping("/memorial/deceased")
public class DeceasedController extends BaseController {
    @Autowired
    private IDeceasedService deceasedService;

    @PreAuthorize("@ss.hasPermi('memorial:deceased:list')")
    @GetMapping("/list")
    public TableDataInfo list(Deceased deceased) {
        startPage();
        List<Deceased> list = deceasedService.selectDeceasedList(deceased);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('memorial:deceased:add')")
    @Log(title = "逝者管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Deceased deceased) {
        deceased.setCreateBy(String.valueOf(getUserId()));
        deceased.setCreateTime(DateUtils.getNowDate());
        return toAjax(deceasedService.insertDeceased(deceased));
    }
}
```

**小程序家属 Controller**（`/api/family/**`）— 不继承 BaseController，手动校验 token：

```java
@GetMapping("/memorials")
public AjaxResult listMyMemorials() {
    Long userId = requireUserId();
    if (userId == null) return AjaxResult.error(401, "请先登录");
    return AjaxResult.success(deceasedService.selectDeceasedByFamilyId(userId));
}
```

### 注解约定
- 管理后台写操作必须有 `@PreAuthorize("@ss.hasPermi('memorial:<模块>:<动作>')")`，动作：list/query/add/edit/remove/export/audit。
- 写操作加 `@Log(title = "xxx管理", businessType = BusinessType.INSERT/UPDATE/DELETE/EXPORT)` 记操作日志。
- HTTP 方法：查 GET / 增 POST / 改 PUT / 删 DELETE。

### Controller 该做和不该做
该做：参数校验、组装返回值、调 Service、记录创建人/时间（`getUserId()` / `DateUtils.getNowDate()`）。不该做：复杂业务逻辑（放 Service）、直接调 Mapper、拼 SQL。

管理后台用 BaseController 的 `startPage()` / `getDataTable()` / `toAjax()` / `getUserId()`。小程序用 `SecurityUtils.getLoginUser()` 拿用户（封进 `requireUserId()`），别用 `@PreAuthorize`（`/api/**` permitAll，注解不生效）。

## Service

- 接口 `IXxxService` + `XxxServiceImpl`（`@Service`），`@Autowired` 字段注入 Mapper。
- 命名：`selectXxxList` / `selectXxxById` / `insertXxx` / `updateXxx` / `deleteXxxByIds`。
- 简单单表 CRUD 不加事务。**多表写**（支付下单+改权益、批量跨表）必须 `@Transactional(rollbackFor = Exception.class)`。
- 业务校验失败抛 `new ServiceException("msg")`（被 `GlobalExceptionHandler` 转 `AjaxResult.error`），或 Controller 直接 `return AjaxResult.error(msg)`。别 try-catch 吞异常返回 null。

## domain

- **不继承 `BaseEntity`**。审计字段（createBy/createTime/updateBy/updateTime）手写。
- 字段驼峰对应表下划线，映射靠 Mapper resultMap（见 database.md）。
- 金额用 `BigDecimal`，别用 double。
- Excel 导出字段加 `@Excel(name = "xxx")`。

## 异常

`GlobalExceptionHandler`（勿改）已统一处理，业务代码不必 try-catch：

| 异常 | 处理 |
|---|---|
| `ServiceException` | `AjaxResult.error(code, msg)` |
| `AccessDeniedException` | 403 |
| `BindException` / `MethodArgumentNotValidException` | 第一个校验错误 |
| `RuntimeException` / `Exception` | 记日志，`AjaxResult.error(e.getMessage())` |

报错用 `throw new ServiceException("msg")`。别 `catch(Exception){ return error("失败"); }` 吞堆栈。注意 `e.getMessage()` 会返回前端，别含 SQL/敏感细节。

## 日志

- 操作审计：管理后台写操作用 `@Log` 注解，自动记 sys_oper_log，别手动写。
- 运行日志：`private static final Logger log = LoggerFactory.getLogger(Xxx.class);`。异常 `log.error("OSS 上传失败 key={}", key, e)`（带堆栈）。参数用 `{}` 占位，别拼接。
- 日志级别 `com.ruoyi: debug`（开发），生产 `info`。
- 别用 `System.out.println` / `printStackTrace`。**别打印完整 token/密码/AK-SK/手机号全量**。

## 工具类

| 工具（com.ruoyi.common.utils） | 用途 |
|---|---|
| `SecurityUtils` | `getLoginUser()` / `getUserId()` / `encryptPassword()` |
| `StringUtils` | `isEmpty` / `isNotEmpty` |
| `DateUtils` | `getNowDate()` / `dateTimeNow("yyyy-MM-dd HH:mm:ss")` |
| `IpUtils` | `getIpAddr(request)`（留言/献花记 IP） |
| `ExcelUtil` | Excel 导出 |
| `RedisCache` | 缓存（验证码、token） |

状态码常量在 `com.ruoyi.common.constant.HttpStatus`：SUCCESS=200 / ERROR=500 / WARN=601 / UNAUTHORIZED=401。套餐/状态等取值目前硬编码，**新增建议**建 `com.ruoyi.memorial.constant.MemorialConstants` 集中定义，避免魔法数字。

## 命名

- 类：`XxxController` / `IXxxService` / `XxxServiceImpl` / `XxxMapper` / `Xxx`（domain）。
- 路径：管理后台 `/memorial/<模块>`，小程序 `/api` 或 `/api/family/<资源>`。
- 权限标识 `memorial:<模块>:<动作>`，需在 `memorial_menu_dict.sql` 注册。

## 注释

类头说明职责。复杂业务方法加 Javadoc 讲规则（如留言自动审核）。TODO 带原因和计划：`// TODO 接入真实 SMS（P0 直接返回，上线前必改）`。别写无意义注释。
