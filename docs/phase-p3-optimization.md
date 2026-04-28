# P3 阶段：优化完善

## 阶段目标
完善个人中心、H5 适配、分享优化、性能优化，提升整体用户体验。

## 前置依赖
- P2 阶段完成

---

## 一、个人中心完善

### 1.1 新增页面

| 页面 | 路径 | 说明 |
|------|------|------|
| 我的留言 | pages/mine/messages | 我发表的所有留言及审核状态 |
| 我的献花 | pages/mine/flowers | 我的献花记录 |
| 关于我们 | pages/about/index | 平台介绍、联系方式、协议 |

### 1.2 新增 API

```typescript
// src/api/user.ts
export function getMyMessages(query)    // GET /api/my/messages
export function getMyFlowers(query)     // GET /api/my/flowers
export function updateProfile(data)     // PUT /api/user/profile  修改昵称/头像
```

### 1.3 页面设计

**我的留言** `pages/mine/messages`
- 留言列表：逝者名称 + 留言内容 + 审核状态（待审核/已通过/未通过）+ 时间
- 审核状态用不同颜色标签

**我的献花** `pages/mine/flowers`
- 献花记录：逝者名称 + 鲜花类型 + 时间

**关于我们** `pages/about/index`
- 应用 Logo + 版本号
- 平台简介
- 联系方式（电话、邮箱）
- 用户协议链接
- 隐私政策链接
- 技术支持信息

### 1.4 个人信息编辑增强

`pages/mine/profile` 增强：
- 支持修改昵称
- 支持修改头像（调用 uni.chooseImage + upload）
- 支持修改性别

---

## 二、H5 适配

### 2.1 微信 H5 授权登录
```
流程:
1. H5 检测到在微信浏览器中打开
2. 跳转微信 OAuth2.0 授权页面
3. 用户授权后回调到应用页面，携带 code
4. 后端通过 code 换取 openid 和用户信息
5. 自动注册/登录，返回 token
```

后端新增：
- `GET /api/wxOAuth/authorize` — 生成微信授权跳转 URL
- `GET /api/wxOAuth/callback` — OAuth 回调处理

配置：
```yaml
wx:
  h5:
    appid: wxXXXXXXXXXXXX
    secret: xxxxxxxxxxxxxxxxxxxxxxx
    redirectUri: https://your-domain.com/api/wxOAuth/callback
```

### 2.2 H5 分享功能
- 使用微信 JS-SDK 配置分享卡片
- 自定义分享标题、描述、缩略图
- 后端新增 `GET /api/wx/jssdk/config?url=xxx` 返回 JS-SDK 签名

### 2.3 H5 样式适配
- 安全区域适配（iPhone X 底部横条）
- 字体和间距在不同屏幕尺寸下的适配
- H5 端滚动行为优化（避免橡皮筋效果导致的体验问题）

---

## 三、分享优化

### 3.1 小程序分享
- 纪念页详情页支持 `onShareAppMessage` 和 `onShareTimeline`
- 分享卡片：逝者姓名 + 生卒日期 + 默认封面图
- 分享路径携带 qrcodeCode 参数，接收者直接打开纪念页

### 3.2 分享海报
- 纪念页生成分享海报图片（Canvas 绘制）
- 海报内容：逝者头像 + 姓名 + 二维码 + 纪念语
- 用户可保存海报到相册，发朋友圈

新增组件：
- `src/components/SharePoster.vue` — 分享海报生成组件

### 3.3 H5 分享
- 微信内分享：JS-SDK 自定义分享卡片
- 非微信浏览器：复制链接 + 提示"复制成功"

---

## 四、性能优化

### 4.1 图片优化
- 相册缩略图使用后端返回的 `thumbnailUrl`，大图使用 `imageUrl`
- 图片懒加载（使用 uni-app 的 lazy-load 属性）
- 图片 CDN 加速（后端配置）

### 4.2 列表性能
- 留言/献花列表使用虚拟列表（长列表场景）
- 分页加载，下拉刷新 + 上拉加载更多

### 4.3 缓存策略
- 纪念页数据本地缓存（有效期5分钟），避免重复请求
- 用户信息本地缓存，减少 getUserInfo 调用
- 静态资源缓存（H5 端 Service Worker）

### 4.4 首屏优化
- 纪念页骨架屏（Skeleton Screen）
- 图片预加载关键资源
- 路由预加载

### 4.5 包体积优化
- 小程序分包加载
- 静态资源压缩（图片使用 WebP 格式）
- 移除未使用的组件和依赖

---

## 五、体验优化

### 5.1 交互优化
- 献花动画增强（花瓣飘落效果）
- 留言提交成功后的反馈动画
- 页面切换过渡动画
- 下拉刷新统一风格

### 5.2 错误处理
- 网络异常统一提示 + 重试按钮
- 接口超时自动重试（最多2次）
- 表单输入校验增强（手机号格式、日期合理性）
- 图片/视频上传失败重试

### 5.3 无障碍
- 关键按钮添加 aria-label
- 图片添加 alt 文本
- 对比度符合 WCAG 标准

---

## 六、后端新增接口汇总

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/my/messages` | GET | 我的留言列表 |
| `/api/my/flowers` | GET | 我的献花记录 |
| `/api/user/profile` | PUT | 修改个人信息 |
| `/api/wxOAuth/authorize` | GET | 微信 H5 授权跳转 |
| `/api/wxOAuth/callback` | GET | 微信 H5 授权回调 |
| `/api/wx/jssdk/config` | GET | 微信 JS-SDK 签名配置 |

---

## 七、预计工期
1 周
