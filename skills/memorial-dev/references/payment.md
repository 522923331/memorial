# payment.md - 支付业务规则

> **何时读**：做支付相关需求时。**当前支付完全未实现**（迭代4 P2，验收全未勾）。

## 代码现状

- `OrderController`（`/memorial/order`）：标准管理后台 CRUD（list/get/add/edit/remove/export），`@PreAuthorize('memorial:order:*')`。
- `OrderServiceImpl`：**纯透传 Mapper**，无任何支付逻辑（无统一下单、无回调、无状态机、无退款）。
- **没有** `/api/pay/**` 接口（P2 文档规划的 `POST /api/pay/wxpay`、`GET /api/pay/status/{orderId}`、`POST /api/pay/notify` 当前不存在）。

## 实现支付要新增

- `PayController`（`/api/pay/**`，小程序端，手动校验 token）。
- `OrderService` 加 `createWxPayOrder` / `handleNotify` / `queryPayStatus` / `refund`。
- 微信支付 SDK（`wechatpay-java`）或自行 HTTP 调用。
- 配置 `wx.pay.*`（appid/mchid/apikey/certPath/notifyUrl）。
- 订单状态机 + 套餐权益生效（需事务）。

## 套餐体系

| 套餐 | DDL package_type | P2 文档值 | 功能 | 定价 |
|---|---|---|---|---|
| 基础 | 0 | 1 | 纪念页+留言+献花+5张相册 | 免费 |
| 高级 | 1 | 2 | +50张相册+3视频+自定义样式 | ¥99/年 |
| VIP | 2 | 3 | +无限相册/视频+优先审核+专属客服 | ¥299/年 |

> ⚠️ **取值不一致（必须核实）**：DDL 是 0/1/2，P2 文档是 1/2/3。以 DDL 为准（与现有代码一致），更新 P2 文档。统一代码、字典 `memorial_package_type`、前端。

套餐决定逝者可用功能范围，当前代码**未实现套餐限制**（相册/视频上传未按 package_type 限数量），实现支付时需补。

## 微信支付流程（规划）

```
1. 选套餐 -> 订单确认页
2. POST /api/pay/wxpay -> 后端统一下单 -> 拿 prepay_id -> 返回签名参数
3. 前端 uni.requestPayment 拉起支付
4. 支付完成 -> 结果页
5. 前端轮询 GET /api/pay/status/{orderId}（最多3次，每次2s）
6. 成功 -> 套餐权益即时生效
7. 失败 -> 提示重新支付
```

后端核心：
1. **创建订单**：写 `mem_order`（status=待支付，生成 order_no），调微信统一下单，返回支付参数。
2. **微信回调**（`POST /api/pay/notify`，**无鉴权**，走 `/api/**` permitAll）：验签 -> 解析订单号 -> 更新 status=已支付 + pay_time -> 更新套餐权益（package_type + expire_time） -> **返回微信成功应答**（否则重复通知）。回调需**幂等**（同订单多次只处理一次）+ **事务**。
3. **前端轮询**：`GET /api/pay/status/{orderId}` 返回最新 status。

## 订单状态机

DDL `mem_order.status` 注释 **3 态**：0待支付 / 1已支付 / 2已退款。P2 文档提到 4 态（含"已取消"）。

> ⚠️ **必须核实**是否需要"已取消"。若需要，确认状态码（建议 3），统一 DDL/代码/前端。当前 DDL 无"已取消"。

流转：待支付(0) --支付--> 已支付(1) --退款--> 已退款(2)；待支付(0) --取消--> 已取消(3)（若实现）。

## 订单管理

- **我的订单**（小程序，待新增）：`GET /api/family/orders`，按时间倒序，待支付可"继续支付/取消"。
- **后台订单管理**（骨架已有）：list/get/add/edit/remove/export。待补退款流程、Excel 导出。机构管理员查看本机构订单需加 org_id 隔离（当前未实现）。

## 配置（规划）

```yaml
wx:
  pay:
    appid: wxXXXXXXXXXXXX
    mchid: "1234567890"
    apikey: xxxxxxxxxxxxxxxxxxxxxxx
    certPath: /path/to/apiclient_cert.p12
    notifyUrl: https://your-domain.com/api/pay/notify
```
AK/SK/证书路径环境变量注入，`notifyUrl` 必须 HTTPS 公网。

## 前置条件

- [ ] 微信商户号开通（审核周期长，提前办）
- [ ] 后端配置证书和密钥
- [ ] **正式域名 + HTTPS**（微信支付强制）
- [ ] 小程序 appid 配置
- [ ] 套餐取值已统一
- [ ] 订单状态已确认
- [ ] notifyUrl 公网可达

## 实现注意

1. **回调无鉴权**：`/api/pay/notify` 走 `/api/**` permitAll，不加 token 校验。
2. **验签**：微信回调必须验签防伪造（本项目唯一真正涉及签名验签的地方，用微信 SDK 或官方文档实现）。
3. **幂等**：回调可能重复，用 status 或回调记录表去重。
4. **事务**：更新订单 + 套餐权益同一事务。
5. **权益生效**：支付成功更新 `mem_organization.package_type` + `expire_time`（需确认权益挂机构还是逝者）。
6. **金额**：`amount` decimal(10,2) 单位元，微信单位分，注意换算。
7. **订单号**：`order_no` 唯一，作为微信 out_trade_no。

## 相关文件
- `docs/phase-p2-payment.md`（取值与 DDL 不一致）
- `OrderController.java` / `OrderServiceImpl.java`（CRUD 骨架）
- `MemOrder.java` / `mem_order` DDL（取值以此为准）
