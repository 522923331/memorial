# Phase C：组件开发

## 待开发的 6 个共享组件

### 1. MemorialHeader.vue — 逝者信息头部卡片
```
路径: src/components/MemorialHeader.vue
用途: 纪念页顶部展示逝者头像、姓名、生卒日期、机构名
Props:
  - deceased: Deceased 对象
  - statusBarHeight: number（自定义导航栏用）
特性:
  - 全宽封面背景图 + 渐变遮罩
  - 居中圆形头像 + 白色边框
  - 姓名白色大字、日期、机构名
  - 左上角返回按钮
复用页面: detail.vue
```

### 2. AlbumGrid.vue — 相册九宫格
```
路径: src/components/AlbumGrid.vue
用途: 相册缩略图展示和预览
Props:
  - albums: DeceasedAlbum[]
  - columns?: number（默认3列）
Events:
  - 无（内部使用 uni.previewImage 处理预览）
特性:
  - grid 布局，正方形缩略图
  - 点击图片调用 uni.previewImage 全屏浏览
  - 支持 number 指示器
复用页面: detail.vue, album.vue
```

### 3. VideoCard.vue — 视频封面卡片
```
路径: src/components/VideoCard.vue
用途: 视频封面展示
Props:
  - video: DeceasedVideo 对象
Events:
  - @tap 点击播放
特性:
  - 封面图（或渐变色占位）+ 播放图标叠加
  - 标题文字
  - 圆角卡片样式
复用页面: detail.vue, video.vue
```

### 4. MessageItem.vue — 单条留言展示
```
路径: src/components/MessageItem.vue
用途: 留言列表中的单条留言
Props:
  - message: Message 对象
特性:
  - 留言人姓名 + 时间
  - 留言内容文本
  - 底部分隔线
复用页面: detail.vue, message.vue
```

### 5. FlowerItem.vue — 单条献花记录
```
路径: src/components/FlowerItem.vue
用途: 献花列表中的单条记录
Props:
  - flower: Flower 对象
特性:
  - 鲜花 emoji 图标（根据 flowerType 映射）
  - 献花人姓名
  - 圆角胶囊标签样式
复用页面: detail.vue, flower.vue
```

### 6. NavBar.vue — 自定义导航栏
```
路径: src/components/NavBar.vue
用途: 首页和纪念页的自定义导航栏
Props:
  - title: string
  - statusBarHeight: number
  - showBack?: boolean（默认 false）
Events:
  - @back 返回按钮点击
特性:
  - 固定顶部，毛玻璃/渐变背景
  - 居中标题
  - 可选返回按钮
  - 自动适配状态栏高度
复用页面: index.vue, detail.vue
```

## 组件开发优先级
1. **NavBar** — 首页和详情页都依赖
2. **MemorialHeader** — 详情页核心视觉
3. **MessageItem** — 详情页和留言页都用到
4. **FlowerItem** — 详情页和献花页都用到
5. **AlbumGrid** — 详情页和相册页
6. **VideoCard** — 详情页和视频页

## 当前状态
目前 6 个组件尚未创建，相关逻辑直接写在页面中。Phase C 的目标是：
- 将重复的 UI 逻辑抽取为独立组件
- 保证各页面间视觉一致性
- 减少页面代码量，提高可维护性

## 组件开发注意事项
- 所有组件使用 `<script setup lang="ts">` 语法
- Props 使用 defineProps + TypeScript 接口
- Events 使用 defineEmits
- 样式使用 scoped，尺寸单位使用 rpx
- 复用 uni-icons 图标库
- 不引入新的第三方依赖
