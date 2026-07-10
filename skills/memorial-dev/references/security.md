# security.md - 鉴权与登录

> **何时读**：改登录/鉴权/权限，或任何 `/api/**` 安全相关时。本项目无独立签名验签业务，本文讲实际安全机制。

## 核心设计：`/api/**` permitAll + 手动校验

### SecurityConfig（`ruoyi-framework/.../SecurityConfig.java`，勿改）
```java
requests.requestMatchers("/api/**").permitAll()   // 纪念馆公开/家属 API 全放行
       .anyRequest().authenticated();              // 其余（/memorial/** 等）需登录
```

`/api/**` permitAll **不代表**拿不到登录用户。

### JwtAuthenticationTokenFilter 仍处理 `/api/**` 的 token
```java
LoginUser loginUser = tokenService.getLoginUser(request);   // 解析 Authorization header
if (loginUser != null && SecurityUtils.getAuthentication() == null) {
    tokenService.verifyToken(loginUser);                    // 续期
    SecurityContextHolder.getContext().setAuthentication(...);  // 塞入上下文
}
chain.doFilter(request, response);
```
该 filter 对所有请求生效（含 `/api/**`）。所以 `/api/**` 带 `Authorization: Bearer <token>` 就能 `SecurityUtils.getLoginUser()` 拿到用户，不带则 null。

### 三类接口鉴权
| 接口 | 策略 | 实现 |
|---|---|---|
| `/api/qrcode`、`/api/search`、`/api/message`、`/api/flower` 等 | 完全公开 | 直接处理，不校验 |
| `/api/userInfo`、`/api/bindPhone`、`/api/family/**` | 需登录，手动校验 | `SecurityUtils.getLoginUser()` 判 null 返回 401 |
| `/memorial/**` | 需登录+权限 | `@PreAuthorize` 自动校验 |

家属接口用 `requireUserId` + `verifyOwnership`（见 api.md），不走 RuoYi 权限注解，因为是"是否为该逝者家属"的数据级权限。

> ⚠️ **别**为了给 `/api/family` 加鉴权而把 `/api/**` 改成 `authenticated()`，否则公开接口全 401。手动校验是刻意设计。

## 登录方式

### 手机号 + SMS 验证码（`/api/phoneLogin`）
1. `POST /api/sms/send {phone}` -> 生成 6 位码存 Redis `sms:phone:<phone>`（5 分钟），**P0 直接返回验证码**（技术债，见下）。
2. `POST /api/phoneLogin {phone, code}` -> Redis 校验 -> 删码 -> 查 sys_user（按 phonenumber）-> 不存在则**自动注册**（用户名 `user_<phone>`，默认密码 `123456` 加密）-> 检查状态 -> 发 token。
3. 前端存 token，后续带 `Authorization: Bearer <token>`。

### 微信小程序登录（`/api/wxLogin`）
前端 `uni.login()` 拿 code -> `POST /api/wxLogin {code}` -> 后端换 openid -> 找/建用户 -> 发 token。需在 `manifest.json` 配 appid，后端配 appid/secret（当前可能未配）。

### 管理后台登录（`/login`）
若依原生：用户名+密码+验证码，返回 token。

## token 机制

- header `Authorization`，密钥 `token.secret`，有效期 `token.expireTime`（分钟，默认 30）。
- `TokenService`：`createToken` / `getLoginUser` / `verifyToken`（自动续期）。token 存 Redis。
- 退出 `/logout` 清 Redis。
- ⚠️ `token.secret` 当前是弱默认值 `abcdefghijklmnopqrstuvwxyz`，**上线前必改强随机 ≥32 位**。

## SMS 验证码（技术债，必改）

当前 `sendSmsCode` 直接把验证码返回前端：
```java
data.put("code", code);
return AjaxResult.success("验证码已发送", data);  // ← 直接返回！
```
任何人可登录任意手机号。**上线前必须**：接入真实 SMS 服务不返回 code；`phoneLogin` 自动注册加风控/频控；发送频率限制（同手机号 60s 一次）。

## 权限体系

### 管理后台（RuoYi 菜单权限）
`@PreAuthorize("@ss.hasPermi('memorial:deceased:list')")` 检查权限标识，标识在 `sys_menu`（`memorial_menu_dict.sql` 注册）按角色分配。`@ss` 是 `PermissionService`，方法 `hasPermi` / `hasRole` / `hasAnyPermi`。

### 家属数据级权限（业务自定义）
不走 RuoYi，靠 `verifyOwnership(deceasedId, userId)`。家属身份前端判定：`getMyMemorials()` 非空 -> `isFamilyMember=true`。**每个家属写接口必须调 verifyOwnership**。

### 机构管理员权限
理论上只能管本机构，但当前代码**未见机构级数据隔离**（管理后台未按 org_id 过滤）。待确认/实现。

## 其他安全配置

- **XSS**：`xss.enabled=true`，匹配 `/system/*,/monitor/*,/tool/*`。`/api/**` 不在范围，富文本 bio 需自行处理 XSS。
- **CORS**：`CorsFilter` 在 JWT filter 前。开发靠 Vite/dev proxy，生产靠 nginx 同源反代。
- **密码**：`BCryptPasswordEncoder`。
- **Druid 监控台**：`/druid/`，账密 `application-druid.yml`（默认 `ruoyi/123456`，**生产必改**）。

## 安全待办（上线前）

- [ ] `token.secret` 改强随机
- [ ] SMS 接真实短信服务，不返回 code
- [ ] SMS 发送频率限制
- [ ] Druid 监控台账密改强（或关公网）
- [ ] DB 密码环境变量注入
- [ ] OSS AK/SK 环境变量注入（已改，确认未回退）
- [ ] 小程序 appid/secret 配置
- [ ] 机构管理员数据隔离确认/实现
- [ ] 富文本 bio XSS 过滤
- [ ] 接口限流（`/api/message`、`/api/flower` 易被刷）
