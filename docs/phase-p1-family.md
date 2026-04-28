# P1 阶段：家属功能

## 阶段目标
实现家属用户的核心管理功能：创建/编辑纪念页、相册管理、视频管理、二维码生成。

## 前置依赖
- P0 阶段完成
- 后端已有逝者 CRUD、相册上传/删除、视频 CRUD 接口（需家属权限鉴权）

---

## 一、新增页面

| 页面 | 路径 | 说明 |
|------|------|------|
| 创建纪念页 | pages/memorial/create | 填写逝者信息表单 |
| 编辑纪念页 | pages/memorial/edit | 修改逝者信息表单 |
| 相册管理 | pages/memorial/album-manage | 上传/删除照片（家属专属） |
| 视频管理 | pages/memorial/video-manage | 上传/删除视频（家属专属） |
| 留言管理 | pages/memorial/message-manage | 查看和删除留言（家属维度） |
| 二维码页面 | pages/memorial/qrcode | 生成和保存小程序码 |

## 二、新增组件

| 组件 | 路径 | 说明 |
|------|------|------|
| DeceasedForm | src/components/DeceasedForm.vue | 逝者信息表单（创建/编辑共用） |
| ImageUploader | src/components/ImageUploader.vue | 图片上传组件（封装 uni.chooseImage + upload） |
| VideoUploader | src/components/VideoUploader.vue | 视频上传组件 |

## 三、新增 API

### 3.1 家属纪念页管理 `src/api/family.ts`
```typescript
// 创建纪念页（家属）
export function createDeceased(data)          // POST /memorial/deceased
// 编辑纪念页（家属）
export function updateDeceased(data)          // PUT /memorial/deceased
// 获取我的纪念页列表
export function getMyDeceasedList()           // GET /memorial/deceased/listByFamily/{userId}
// 上传相册照片
export function uploadAlbumPhoto(deceasedId, filePath)  // POST /memorial/deceased/album/upload
// 删除相册照片
export function deleteAlbumPhoto(albumIds)    // DELETE /memorial/deceased/album/{albumIds}
// 上传视频
export function uploadVideo(data)             // POST /memorial/video
// 删除视频
export function deleteVideo(videoIds)         // DELETE /memorial/video/{videoIds}
// 家属删除留言
export function deleteMessage(messageIds)     // DELETE /memorial/message/{messageIds}
```

### 3.2 二维码 `src/api/qrcode.ts`
```typescript
// 生成小程序码图片
export function generateQrcode(deceasedId)    // GET /api/qrcode/generate/{deceasedId}
```

## 四、页面设计

### 4.1 创建纪念页 `pages/memorial/create`
- 表单字段：姓名*、性别*、出生日期*、死亡日期*、机构ID、陵园区域、陵园编号、生平简介（富文本）、是否公开、是否允许留言、留言审核方式
- 提交后跳转到纪念页详情
- 需要登录，未登录跳转登录页

### 4.2 编辑纪念页 `pages/memorial/edit`
- 与创建共用 DeceasedForm 组件
- 通过 deceasedId 加载已有数据填充表单

### 4.3 相册管理 `pages/memorial/album-manage`
- 九宫格展示 + 右上角"上传"按钮
- 点击"上传"→ 调用 uni.chooseImage（最多9张）→ 逐张上传
- 长按进入删除模式，多选后批量删除
- 显示上传进度

### 4.4 视频管理 `pages/memorial/video-manage`
- 视频列表 + "上传视频"按钮
- 点击"上传"→ 调用 uni.chooseVideo → 上传
- 视频卡片带删除按钮
- 单个视频限制 ≤ 50MB

### 4.5 留言管理 `pages/memorial/message-manage`
- 留言列表（含待审核留言）
- 每条留言有删除按钮
- 待审核留言有"审核通过"/"审核不通过"操作

### 4.6 二维码页面 `pages/memorial/qrcode`
- 展示小程序码图片
- "保存到相册"按钮（调用 uni.saveImageToPhotosAlbum）
- "分享"按钮

## 五、我的页面增强

### pages/mine/index 增强
- 新增菜单项："我的纪念页" → 跳转到我的纪念页列表
- 我的纪念页列表：展示当前用户创建的所有纪念页，每个卡片有"编辑""管理相册""管理视频""二维码"入口
- 右上角"+"按钮 → 创建纪念页

## 六、后端新增/修改

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/qrcode/generate/{deceasedId}` | GET | 生成小程序码图片（返回图片 URL） |
| `/api/my/deceased` | GET | 获取当前用户的纪念页列表 |

注意：后端已有的 `/memorial/deceased` POST/PUT、`/memorial/deceased/album/upload`、`/memorial/video` POST/DELETE 等接口需要家属用户鉴权。前端调用时需携带 Bearer token。

## 七、预计工期
1.5 周
