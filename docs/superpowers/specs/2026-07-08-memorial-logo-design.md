# Memorial 项目 Logo 接入设计

**日期**: 2026-07-08
**作者**: wu.dang
**状态**: 待实现

## 背景

项目需要统一品牌 logo，用于替换/补充当前各端的视觉标识。用户提供了一组 logo 文件（同一图案的两种格式）：

- `/Users/k02/Downloads/memorial_logo.svg` (27KB, 矢量)
- `/Users/k02/Downloads/memoriallogo.png` (1.4MB, 2048×2048)

需要接入到两个端：
- **ruoyi-ui** (Vue 2 Web 后台前端) - 登录页 + 侧边栏
- **memorial-app** (uniapp 小程序/H5) - 登录页

## 现状分析

### ruoyi-ui

- 登录页 `src/views/login.vue`：当前**没有 logo 图片**，只有文字标题 `<h3 class="title">{{title}}</h3>`（标题来自 `VUE_APP_TITLE=若依管理系统`）
- 侧边栏 `src/layout/components/Sidebar/Logo.vue`：使用 `@/assets/logo/logo.png`，32×32px
- 现有 `src/assets/logo/logo.png` 有两处引用：
  - `src/layout/components/Sidebar/Logo.vue:17`（侧边栏）
  - `src/views/tool/build/index.vue:150`（表单构建器内部工具，显示"Form Generator"）
- Vue 2.6.12

### memorial-app

- 登录页 `src/pages/login/index.vue`：已有 `<image class="logo" src="/static/images/logo.png" mode="aspectFit" />`，160rpx × 160rpx
- `/static/images/logo.png` 仅登录页引用（grep 全项目确认）
- `/static/logo.png` 是孤立文件，无任何引用（预先存在，不在本次范围）
- uniapp + Vue 3

## 设计决策

| 决策项 | 选择 | 理由 |
|---|---|---|
| 格式分配 | SVG -> ruoyi-ui，PNG -> memorial-app | Web 端 SVG 矢量清晰、文件小；微信小程序对 SVG 渲染支持不稳定，PNG 兼容性最好 |
| 登录页 logo 位置 | 标题上方 | 保留原标题文字，logo 作为品牌图形补充 |
| 侧边栏 logo | 一并更新 | 保证登录后整个系统品牌一致 |
| 文件命名 | 保留用户原始文件名 (`memorial_logo.svg` / `memoriallogo.png`) | 品牌语义清晰，明确这是 memorial 项目 logo |
| 表单构建器 logo | 不动 | 内部开发工具，不属于用户可见品牌范围；且 `logo.png` 仍被它引用，不能删除 |

## 实施方案（方案 A：保留原始文件名，更新引用）

### ruoyi-ui

1. **新增文件**：`src/assets/logo/memorial_logo.svg`
   - 来源：`/Users/k02/Downloads/memorial_logo.svg`

2. **修改 `src/views/login.vue`**：
   - 在 `<h3 class="title">{{title}}</h3>` 上方加入：
     ```html
     <img src="@/assets/logo/memorial_logo.svg" class="login-logo" alt="logo">
     ```
   - 在 `<style>` 块中加入：
     ```scss
     .login-logo {
       width: 80px;
       height: 80px;
       display: block;
       margin: 0 auto 20px auto;
     }
     ```

3. **修改 `src/layout/components/Sidebar/Logo.vue`** 第 17 行：
   - 从：`import logoImg from '@/assets/logo/logo.png'`
   - 改为：`import logoImg from '@/assets/logo/memorial_logo.svg'`
   - 32×32 尺寸不变（已有 CSS）

4. **保留** `src/assets/logo/logo.png`（表单构建器 `src/views/tool/build/index.vue` 仍在使用）

### memorial-app

1. **新增文件**：`src/static/images/memoriallogo.png`
   - 来源：`/Users/k02/Downloads/memoriallogo.png`

2. **修改 `src/pages/login/index.vue`** 第 4 行：
   - 从：`<image class="logo" src="/static/images/logo.png" mode="aspectFit" />`
   - 改为：`<image class="logo" src="/static/images/memoriallogo.png" mode="aspectFit" />`
   - 160rpx × 160rpx 尺寸不变（已有 CSS）

3. **删除** `src/static/images/logo.png`（已确认全项目无其他引用，避免遗留混淆）

4. **不动** `/static/logo.png`（预先存在的孤立文件，不在本次范围）

## 验证方式

### ruoyi-ui
- `npm run dev` 启动开发服务器
- 访问登录页：确认 logo 显示在标题"若依管理系统"上方，80×80px 居中
- 登录后：确认侧边栏左上角 logo 已更新为新 logo（32×32px）
- 访问表单构建器 `/tool/build`：确认其 logo 仍正常显示（未受影响）

### memorial-app
- 用 HBuilder X 或微信开发者工具打开项目
- 进入登录页：确认 logo 显示在"云上纪念"上方，160rpx × 160rpx
- 确认 `memoriallogo.png` 已在 `src/static/images/` 下
- 确认旧 `logo.png` 已删除

## 不在本次范围

- 修改 `VUE_APP_TITLE`（当前为"若依管理系统"，与小程序"云上纪念"不一致）- 属于品牌文案统一，不是 logo 接入
- 清理 `/static/logo.png`（预先存在的孤立文件）
- 更新表单构建器 `src/views/tool/build/index.vue` 的 logo（内部工具）
- 小程序 tabBar 图标（`/static/icons/` 下的 home/mine 图标）- 不属于 logo

## 风险与回滚

- **风险低**：仅图片资源替换 + 少量引用路径修改，无逻辑变更
- **回滚**：`git revert` 即可恢复所有改动
