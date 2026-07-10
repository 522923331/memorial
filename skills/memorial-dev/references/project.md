# project.md — 项目完整档案

> **何时读**：第一次接手项目，或需要完整目录树、配置文件清单、端口/部署细节、各阶段实现状态时。日常改代码看 SKILL.md 速览即可。

## 目录

- [顶层结构](#顶层结构)
- [后端业务代码](#后端业务代码)
- [Controller 清单](#controller-清单)
- [配置文件](#配置文件)
- [端口与部署](#端口与部署)
- [各阶段实现状态](#各阶段实现状态)
- [文档索引](#文档索引)

## 顶层结构

```
memorial/
├── ruoyi-admin/        # 后端启动模块 + 全部纪念馆业务代码（com.ruoyi.memorial）
├── ruoyi-common/       # 通用工具/基类/异常（若依框架，勿乱改）
├── ruoyi-framework/    # Security/JWT/AOP/Web 配置（若依框架，勿乱改）
├── ruoyi-system/       # 系统管理：用户/角色/菜单/字典（若依原生）
├── ruoyi-quartz/       # 定时任务
├── ruoyi-generator/    # 代码生成器
├── ruoyi-ui/           # 管理后台前端（Vue2 + Element UI）
├── memorial-app/       # 用户端前端（uni-app + Vue3 + TS + Pinia）
├── sql/                # DDL / 菜单字典 / 示例数据 / 若依基础表 / quartz
├── docs/               # 设计文档与部署文档
├── bin/                # 启动脚本 ry.sh / ry.bat
├── dev.sh              # 一键编译+启动 后端+H5+管理后台
└── pom.xml             # Maven 父 POM
```

## 后端业务代码

业务代码全在 `ruoyi-admin/src/main/java/com/ruoyi/memorial/`：

```
com.ruoyi.memorial
├── controller/   10 个 Controller（两类，见 api.md）
├── service/      IXxxService 接口 + impl/XxxServiceImpl
│   └── OssService   OSS 上传（无接口，直接注入）
├── mapper/       MyBatis 接口
├── domain/       实体（不继承 BaseEntity）
├── config/       OssConfig / OssProperties
└── utils/        QrCodeUtil（zxing 生成二维码 + 上传 OSS）
```

Mapper XML：`ruoyi-admin/src/main/resources/mapper/memorial/*Mapper.xml`。

框架代码（`ruoyi-common`/`ruoyi-framework`/`ruoyi-system`）尽量不动，扩展在 `memorial` 包内新增。

## Controller 清单

| Controller | 路径前缀 | 端 | 鉴权 |
|---|---|---|---|
| PublicApiController | `/api` | 小程序/H5 | permitAll |
| WxMiniLoginController | `/api` | 小程序/H5 | permitAll / 部分手动校验 |
| FamilyApiController | `/api/family` | 小程序/H5 | 手动校验 token + 归属 |
| DeceasedController | `/memorial/deceased` | 管理后台 | @PreAuthorize |
| OrganizationController | `/memorial/organization` | 管理后台 | @PreAuthorize |
| DeceasedVideoController | `/memorial/video` | 管理后台 | @PreAuthorize |
| MessageController | `/memorial/message` | 管理后台 | @PreAuthorize |
| FlowerController | `/memorial/flower` | 管理后台 | @PreAuthorize |
| StatisticsController | `/memorial/statistics` | 管理后台 | @PreAuthorize |
| OrderController | `/memorial/order` | 管理后台 | @PreAuthorize（仅 CRUD 骨架） |

两类鉴权的原理和写法见 `api.md` / `security.md`。

## 配置文件

| 文件 | 作用 |
|---|---|
| `ruoyi-admin/src/main/resources/application.yml` | 主配置：端口、Redis、token、memorial.oss/qrcode、multipart 上限 |
| `application-dev.yml` | 开发覆盖：OSS 公网 endpoint、url-prefix 直连 OSS |
| `application-prod.yml` | 生产覆盖：OSS 内网 endpoint、url-prefix 走 nginx /oss |
| `application-druid.yml` | 数据源（MySQL） |
| `memorial-app/.env.development` / `.env.production` | 用户端 API 基址（仅小程序读，H5 走同源 proxy） |
| `ruoyi-ui/.env.production` | 管理后台 API 基址 `/prod-api` |
| `memorial-app/src/manifest.json` | 小程序 AppID（发布前必填） |

`spring.profiles.active` 默认 `dev,druid`，生产用 `prod,druid`（`dev.sh --prod` 自动切换）。

> ⚠️ 配置文件可能含真实 DB 密码 / Redis / OSS AK·SK。改配置用占位符或环境变量，生产用 `OSS_ACCESS_KEY_ID` / `OSS_ACCESS_KEY_SECRET` / `SERVER_PUBLIC_IP` 注入。

## 端口与部署

| 端口 | 用途 | 公网 |
|---|---|---|
| 18080 | 后端 SpringBoot | 否（nginx 内网反代） |
| 5173 | H5 dev（开发） | - |
| 8008 | 管理后台 dev（开发） | - |
| 8080 | 管理后台（生产，nginx） | 是 |
| 80/443 | Nginx（H5 + admin + API + OSS 反代） | 是 |
| 3306 / 6379 | MySQL / Redis | 否 |

生产单机部署，Nginx 是唯一公网入口：`/api/` 反代后端 18080（不带尾斜杠，保留 /api 前缀）；`/prod-api/` 反代后端（带尾斜杠，去前缀）；`/oss/` 反代 OSS 内网 endpoint（带尾斜杠，需 `proxy_set_header Host <OSS内网域名>` + `proxy_ssl_server_name on`）。详见 `docs/deployment-guide.md`。

## 各阶段实现状态

截至 `docs/iteration-plan.md`（2026-04-29）：

| 阶段 | 状态 |
|---|---|
| 迭代0 基础就绪（9 Controller + 8 表 + 管理后台7页 + 登录） | ✅ 基本完成 |
| 迭代1 Phase C 共享组件（6 个） | ✅ 完成 |
| 迭代2 P1 家属基础（FamilyApiController 15 端点 + 6 页面） | ✅ 完成 |
| 迭代3 P1 家属增强（批量审核/隐私/统计/二维码） | ✅ 完成 |
| 迭代4 P2 支付 | ❌ **未完成** |
| 迭代5 P3 优化 | ✅ 大部分完成 |

## 文档索引

- `docs/deployment-guide.md` — 部署（最详尽）
- `docs/memorial-user-prd.md` — 产品需求
- `docs/iteration-plan.md` — 迭代计划与验收清单
- `docs/phase-p1-family.md` — 家属功能
- `docs/phase-p2-payment.md` — 支付（取值与 DDL 不一致，注意）
- `docs/phase-c-components.md` — 前端组件
- `docs/phase-b-api-store.md` — 字段名修复 + 待开发 API
- `sql/memorial_ddl.sql` — 8 张业务表 DDL（表结构以此为准）
