# frontend.md - 前端开发规范

> **何时读**：改前端时。先确认是 memorial-app（用户端）还是 ruoyi-ui（管理后台），两套技术栈不同。

## 一、用户端 memorial-app

### 技术栈
uni-app 3 + Vue 3.4 + TypeScript 4.9 + Pinia 2.3 + uni-ui + Vite 5.2。一套代码多端编译（微信小程序主端、H5 辅端）。小程序基础库 ≥2.20.0。

构建脚本：`dev:h5` / `dev:mp-weixin` / `build:h5` / `build:mp-weixin` / `type-check`（vue-tsc --noEmit）。

### 目录（`memorial-app/src/`）
```
api/          request.ts(封装) + auth.ts/memorial.ts/family.ts/order.ts
types/        api.ts(AjaxResult/PageResult) + memorial.ts + user.ts + family.ts
stores/       user.ts / memorial.ts / family.ts（Pinia setup 风格）
utils/        auth.ts(Token 存取) + storage.ts + qrcode.ts
components/   7 个共享组件（NavBar/MemorialHeader/AlbumGrid/VideoCard/MessageItem/FlowerItem/ImageUploader）
pages/        index/ login/ mine/ memorial/ family/
```

### 请求封装（`api/request.ts`，核心）
- **BASE_URL**：H5（`#ifdef H5`）空串走 Vite proxy 同源；小程序（`#ifndef H5`）读 `VITE_API_BASE_URL`（dev `http://localhost:18080`，prod 正式域名）。
- **导出** `get/post/put/del/upload`，签名 `get<T>(url, data?, needToken=true): Promise<AjaxResult<T>>`。
- **Token 注入**：`needToken=true` 时加 `header['Authorization'] = 'Bearer ' + getToken()`。登录/验证码接口传 `needToken=false`。
- **响应**：`code===200` resolve；`code===401` 清 token + toast + `uni.reLaunch` 登录页（避免登录页重复 push）；其他 toast `msg`。
- 超时 15s。`loading=true` 显示 loading。

### 类型（`types/api.ts`）
```ts
interface AjaxResult<T = any> { code: number; msg: string; data: T }
interface PageResult<T = any> { rows: T[]; total: number }
```
业务类型在 `types/memorial.ts`：`Deceased`/`DeceasedAlbum`/`DeceasedVideo`/`Message`/`Flower`/`MemorialPageData`（首屏聚合）/`RecentVisit`。字段名严格驼峰对应后端 JSON。

### 状态管理（Pinia setup，`stores/user.ts`）
`useUserStore`：`token`/`userInfo`/`isLoggedIn`/`isFamilyMember`。
- **家属身份判定**：`checkFamilyStatus()` 调 `getMyMemorials()`，数组非空则 `isFamilyMember=true`，"我的纪念馆"入口显隐依据。
- 登录流程（`wxLogin`/`loginByPhone`）：拿 token -> `fetchUserInfo()` -> `checkFamilyStatus()`。
- Token 存 `uni.storage`（key 见 `utils/auth.ts` / `storage.ts` 的 `STORAGE_KEYS`）。

### 组件规范（`components/`）
- `<script setup lang="ts">`。
- Props `defineProps<{...}>()` + TS 接口 + `withDefaults`。
- Events `defineEmits<{ (e: 'xxx', payload: T): void }>()`。
- `<style scoped>`，尺寸用 **rpx**（系统 px 如状态栏高度除外）。
- 图标用 **uni-icons**，**不引新第三方依赖**。
- 6 个共享组件优先复用，新增页面优先用它们。

### 路由（`pages.json`）
按模块：`pages/index/`、`pages/login/`、`pages/mine/`、`pages/memorial/`（访客）、`pages/family/`（家属）。新增页面必须在 `pages.json` 注册。扫码启动：`App.vue` 的 `onLaunch/onShow` 接收 `options.query.code` 跳 `pages/memorial/detail?code=xxx`。

### 多端条件编译
H5/小程序差异用 `#ifdef H5` / `#ifdef MP-WEIXIN`（BASE_URL、分享、扫码）。H5 不支持扫码（手动输入/搜索）。分享：小程序 `onShareAppMessage`/`onShareTimeline`，H5 提示复制链接。安全区域底部 `safe-area-inset-bottom`，导航栏用 NavBar 组件算 `statusBarHeight`。

### 环境变量
`.env.development`：`VITE_API_BASE_URL=http://localhost:18080`；`.env.production`：正式域名。**仅小程序读**，H5 走同源 proxy BASE_URL 恒空。

## 二、管理后台 ruoyi-ui

### 技术栈
Vue 2 + Element UI + Vue CLI（若依原生）。API 基址 `.env.production` `VUE_APP_BASE_API='/prod-api'`，`.env.development` `/dev-api`（proxy 到 18080）。

### 目录
```
src/api/memorial/      7 个 API 文件：deceased/organization/video/message/flower/statistics/order .js
src/views/memorial/    7 个页面：*/index.vue
```

### API 调用（`api/memorial/deceased.js` 范式）
```js
import request from '@/utils/request'
export function listDeceased(query) {
  return request({ url: '/memorial/deceased/list', method: 'get', params: query })
}
export function addDeceased(data) {
  return request({ url: '/memorial/deceased', method: 'post', data })
}
```
路径对应后端 `/memorial/**`。列表用 `params`，新增/修改用 `data`。`@RequestParam` 接口（如 batchAudit）用 `params` 不是 `data`。

### 页面规范
每模块一个 `views/memorial/<module>/index.vue`，标准若依 CRUD（搜索栏+工具栏+表格+分页+弹窗）。字典用 `memorial_package_type`（`useDict`）。**上传别用**若依通用 `ImageUpload`（`action=/common/upload` 已失效），相册上传已改用 `/memorial/deceased/album/upload`。按钮权限 `v-hasPermi="['memorial:deceased:add']"` 对应后端 `@PreAuthorize`。

### 字段对齐
页面字段名必须与后端 domain 一致（历史不一致已修复，见 business.md）。新增字段同步前端。

## 三、构建与验证

| 目标 | 命令 |
|---|---|
| 用户端 H5 开发 | `cd memorial-app && npm run dev:h5`（5173） |
| 用户端小程序开发 | `cd memorial-app && npm run dev:mp-weixin` |
| 用户端类型检查 | `cd memorial-app && npm run type-check` |
| 用户端 H5 构建 | `cd memorial-app && npm run build:h5` |
| 管理后台开发 | `cd ruoyi-ui && npm run dev`（8008） |
| 管理后台构建 | `cd ruoyi-ui && npm run build:prod` |
| 一键起 3 端 | `./dev.sh`（macOS 开发模式） |

前端改完**必须**跑类型检查 + 构建（`type-check` / `build:h5` / `build:prod`）确认无报错。
