# P0 实施计划：云上纪念用户端 (memorial-app)

## Context
基于已完成的 PRD 文档，P0 阶段实现核心功能：微信登录、纪念页浏览、留言寄语、献花致敬、扫码访问。技术方案为 uni-app (Vue 3 + TS)，一套代码编译为微信小程序 + H5。

## 项目信息
- 项目路径：`/Users/k02/myProjects/memorial/memorial-app`
- 技术栈：uni-app + Vue 3 + TypeScript + Pinia + uni-ui
- 后端 API 基地址：`http://localhost:18080`
- 公开 API 前缀：`/api/`（无需认证）

---

## 一、文件清单

### 项目配置文件（6个）
| 文件 | 说明 |
|------|------|
| `package.json` | 依赖：pinia, @dcloudio/uni-ui 等 |
| `tsconfig.json` | TypeScript 配置 |
| `vite.config.ts` | Vite 配置 |
| `.env.development` | VITE_API_BASE_URL=http://localhost:18080 |
| `.env.production` | 生产环境 API 地址 |
| `src/manifest.json` | 小程序 appid、H5 代理等 |

### 核心配置文件（4个）
| 文件 | 说明 |
|------|------|
| `src/main.ts` | 入口，注册 Pinia |
| `src/App.vue` | 根组件，处理扫码启动和 401 拦截 |
| `src/pages.json` | 页面路由 + tabBar（首页、我的） |
| `src/uni.scss` | 全局样式变量 |

### 类型定义（3个）
| 文件 | 说明 |
|------|------|
| `src/types/api.ts` | AjaxResult、PageResult |
| `src/types/memorial.ts` | Deceased、Album、Video、Message、Flower、MemorialPageData、RecentVisit |
| `src/types/user.ts` | UserInfo、WxLoginResult |

### 工具函数（3个）
| 文件 | 说明 |
|------|------|
| `src/utils/auth.ts` | Token 存取（uni.storage） |
| `src/utils/storage.ts` | 类型安全的 storage 封装 |
| `src/utils/qrcode.ts` | 扫码结果解析 |

### API 层（3个）
| 文件 | 说明 |
|------|------|
| `src/api/request.ts` | uni.request 封装：Token 注入、401 拦截、upload 支持 |
| `src/api/memorial.ts` | getMemorialByCode, searchDeceased, getMessages, getFlowers, submitMessage, submitFlower |
| `src/api/auth.ts` | wxMiniLogin, phoneLogin, getUserInfo, bindPhone, logout |

### Pinia Store（2个）
| 文件 | 说明 |
|------|------|
| `src/stores/user.ts` | 用户状态：token、userInfo、登录/登出 |
| `src/stores/memorial.ts` | 纪念页状态：当前逝者数据、留言、献花、最近访问 |

### 组件（6个）
| 文件 | 说明 |
|------|------|
| `src/components/MemorialHeader.vue` | 逝者信息头部卡片 |
| `src/components/AlbumGrid.vue` | 相册九宫格 + 点击预览 |
| `src/components/VideoCard.vue` | 视频封面卡片 |
| `src/components/MessageItem.vue` | 单条留言展示 |
| `src/components/FlowerItem.vue` | 单条献花记录 |
| `src/components/NavBar.vue` | 自定义导航栏 |

### 页面（9个）
| 页面路径 | 说明 |
|---------|------|
| `pages/index/index` | 首页：搜索 + 最近访问 + 扫码按钮 |
| `pages/login/index` | 登录：微信一键登录 / 手机号登录 |
| `pages/memorial/detail` | 纪念页核心：逝者信息 + 相册 + 视频 + 留言 + 献花 |
| `pages/memorial/message` | 留言寄语：查看 + 发表 |
| `pages/memorial/flower` | 献花致敬：选花 + 提交 |
| `pages/memorial/album` | 相册浏览：全屏图片预览 |
| `pages/memorial/video` | 视频播放 |
| `pages/mine/index` | 我的：头像、昵称、菜单 |
| `pages/mine/profile` | 个人信息 |

### 静态资源
- `static/images/`: logo.png, default-avatar.png, empty.png
- `static/icons/`: tabBar 图标（home/mine）、flower-1~4.png

### 后端新增文件
| 文件 | 说明 |
|------|------|
| `WxMiniLoginController.java` | /api/wxLogin, /api/phoneLogin, /api/bindPhone, /api/userInfo |

### 后端修改文件
| 文件 | 修改内容 |
|------|---------|
| `PublicApiController.java` | 新增 GET /api/search 搜索逝者 |
| `DeceasedMapper.xml` | 新增 searchPublic 查询 |
| `IDeceasedService.java` | 新增 searchPublic 方法 |
| `DeceasedServiceImpl.java` | 实现 searchPublic |
| `application.yml` | 新增 wx.appid, wx.secret 配置 |

---

## 二、后端新增 API 详述

### 2.1 微信小程序登录 `POST /api/wxLogin`
- 入参：`{ code: string }`（wx.login 获取的 code）
- 逻辑：调用微信 jscode2session → 获取 openid → 查找/创建用户（userName='wx_'+openid）→ 生成 JWT token
- 返回：`{ code: 200, data: { token: "xxx" } }`

### 2.2 手机号登录 `POST /api/phoneLogin`
- 入参：`{ phone: string, code: string }`
- 逻辑：验证短信验证码 → 查找/创建用户 → 生成 token
- P0 阶段：开发环境跳过短信验证，生产接入阿里云/腾讯云短信

### 2.3 获取用户信息 `GET /api/userInfo`
- 请求头：`Authorization: Bearer token`
- 逻辑：从 token 获取 LoginUser → 返回 SysUser 基本信息
- 无 token 时返回 401

### 2.4 绑定手机号 `POST /api/bindPhone`
- 入参：`{ phone: string, code: string }`
- 需登录，更新用户手机号

### 2.5 搜索逝者 `GET /api/search?keyword=xxx`
- 公开接口，无需认证
- 查询条件：name LIKE '%keyword%' AND is_public='0' AND status='0'
- 返回：`[{ deceasedId, name, gender, birthDate, deathDate, coverImage, qrcodeCode, orgName }]`

---

## 三、实施顺序

### Phase A：项目骨架（第1天）
1. 创建 uni-app 项目、安装依赖
2. 环境配置文件（.env）
3. 类型定义（types/）
4. 工具函数（utils/）
5. 请求封装（api/request.ts）
6. pages.json、manifest.json、App.vue、main.ts、uni.scss

### Phase B：API + Store（第1-2天）
7. API 文件（api/memorial.ts, api/auth.ts）
8. Pinia Store（stores/user.ts, stores/memorial.ts）

### Phase C：组件（第2天）
9. 6 个共享组件

### Phase D：核心页面（第2-3天）
10. 纪念页详情（detail.vue）— 最关键
11. 首页（index/index.vue）
12. 相册、视频页面

### Phase E：交互页面（第3-4天）
13. 留言页
14. 献花页
15. 登录页

### Phase F：用户页面（第4天）
16. 我的页面
17. 个人信息页面

### Phase G：后端新增（第3-5天，可与前端并行）
18. WxMiniLoginController
19. PublicApiController 新增搜索接口
20. DeceasedMapper 新增查询
21. application.yml 微信配置

---

## 四、关键设计决策

1. **认证策略**：`/api/**` 路径在 SecurityConfig 中已 permitAll，JWT 过滤器仍会处理 token。`/api/userInfo` 和 `/api/bindPhone` 在 controller 中手动校验 token，无需修改 SecurityConfig。

2. **纪念页数据加载**：首屏使用 `GET /api/qrcode/{code}` 一次加载全部数据，提交留言/献花后通过独立 API 刷新对应列表。

3. **最近访问**：存储在本地 `uni.storage`，最多 20 条，无需后端 API。

4. **扫码启动**：小程序从二维码启动时，`App.vue` 的 `onLaunch/onShow` 接收 `options.query.code`，自动跳转纪念页。

5. **H5 扫码**：P0 阶段 H5 不支持扫码，改为手动输入编号/搜索访问。

---

## 五、验证方式

1. `npm run dev:mp-weixin` — 微信开发者工具中预览小程序
2. `npm run dev:h5` — 浏览器中预览 H5
3. 核心流程测试：
   - 首页搜索逝者 → 进入纪念页 → 查看相册/视频 → 留言 → 献花
   - 扫码进入纪念页
   - 微信登录 → 查看个人信息 → 退出
