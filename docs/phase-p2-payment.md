# P2 阶段：订单与支付

## 阶段目标
实现服务套餐选择、微信支付、订单管理功能。

## 前置依赖
- P1 阶段完成
- 微信商户号申请、微信支付开通
- 后端已有订单 CRUD 接口

---

## 一、套餐体系

| 套餐 | package_type | 功能 | 定价 |
|------|-------------|------|------|
| 基础纪念 | 1 | 纪念页 + 留言 + 献花 + 5张相册 | 免费 |
| 高级纪念 | 2 | 基础功能 + 50张相册 + 3个视频 + 自定义样式 | ¥99/年 |
| VIP纪念 | 3 | 高级功能 + 无限相册 + 无限视频 + 优先审核 + 专属客服 | ¥299/年 |

## 二、新增页面

| 页面 | 路径 | 说明 |
|------|------|------|
| 套餐选择 | pages/order/package | 展示三种套餐，对比功能差异 |
| 订单确认 | pages/order/confirm | 确认订单信息、金额 |
| 订单支付 | pages/order/pay | 微信支付调用 |
| 支付结果 | pages/order/result | 支付成功/失败展示 |
| 我的订单 | pages/mine/orders | 订单列表 |

## 三、新增组件

| 组件 | 路径 | 说明 |
|------|------|------|
| PackageCard | src/components/PackageCard.vue | 套餐卡片（名称、价格、功能列表、选择按钮） |
| OrderItem | src/components/OrderItem.vue | 订单列表项 |

## 四、新增 API

### `src/api/order.ts`
```typescript
// 获取套餐列表
export function getPackageList()                    // GET /api/packages
// 创建订单
export function createOrder(data)                   // POST /memorial/order
// 获取订单详情
export function getOrder(orderId)                   // GET /memorial/order/{orderId}
// 获取我的订单列表
export function getMyOrders(query)                  // GET /api/my/orders
// 微信支付统一下单
export function createWxPay(orderId)                // POST /api/pay/wxpay
// 查询支付状态
export function queryPayStatus(orderId)             // GET /api/pay/status/{orderId}
```

## 五、订单流程

```
1. 用户进入"套餐选择"页 → 选择套餐
2. 进入"订单确认"页 → 确认逝者、套餐、金额
3. 点击"立即支付" → 后端调用微信统一下单 API → 返回支付参数
4. 前端调用 uni.requestPayment → 拉起微信支付
5. 支付完成后 → 跳转"支付结果"页
6. 前端轮询查询支付状态（最多3次，每次2秒）
7. 支付成功 → 套餐权益即时生效
8. 支付失败 → 提示重新支付
```

## 六、页面设计

### 6.1 套餐选择 `pages/order/package`
- 三列套餐卡片，当前套餐高亮
- 每个卡片：名称、价格、功能清单（✓/✗）
- "选择"按钮
- 基础纪念标记"免费"
- 选中后跳转订单确认

### 6.2 订单确认 `pages/order/confirm`
- 逝者信息摘要
- 套餐名称和价格
- 有效期
- "立即支付"按钮

### 6.3 支付结果 `pages/order/result`
- 成功：✓ 图标 + "支付成功" + 套餐信息 + "查看纪念页"按钮
- 失败：✗ 图标 + "支付失败" + "重新支付"按钮

### 6.4 我的订单 `pages/mine/orders`
- 订单列表，按时间倒序
- 每项：订单号、套餐名、金额、状态（待支付/已支付/已取消/已退款）、时间
- 待支付订单可"继续支付"或"取消订单"

## 七、后端新增

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/packages` | GET | 获取套餐列表（含当前用户的套餐状态） |
| `/api/my/orders` | GET | 获取当前用户订单列表 |
| `/api/pay/wxpay` | POST | 微信统一下单，返回支付参数 |
| `/api/pay/status/{orderId}` | GET | 查询支付状态 |
| `/api/pay/notify` | POST | 微信支付回调通知（无需鉴权） |

### 后端支付核心逻辑
```
1. 创建订单 → 写入 mem_order 表，status=待支付
2. 统一下单 → 调用微信支付 API → 获取 prepay_id → 返回签名参数给前端
3. 支付回调 → 微信服务器通知 → 验证签名 → 更新订单状态为已支付 → 更新逝者套餐权益
4. 前端轮询 → 查询订单状态 → 返回最新支付结果
```

### 配置项 `application.yml`
```yaml
wx:
  pay:
    appid: wxXXXXXXXXXXXX
    mchid: "1234567890"
    apikey: xxxxxxxxxxxxxxxxxxxxxxx
    certPath: /path/to/apiclient_cert.p12
    notifyUrl: https://your-domain.com/api/pay/notify
```

## 八、预计工期
1 周
