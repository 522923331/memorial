<template>
  <view class="page">
    <!-- 顶部逝者信息 -->
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-bg">
        <image
          class="header-cover"
          :src="deceased?.coverImage || '/static/images/default-avatar.png'"
          mode="aspectFill"
        />
        <view class="header-overlay" />
      </view>
      <view class="header-content">
        <view class="nav-back" @tap="goBack">
          <uni-icons type="left" size="20" color="#fff" />
        </view>
        <view class="deceased-info">
          <image
            class="deceased-avatar"
            :src="deceased?.coverImage || '/static/images/default-avatar.png'"
            mode="aspectFill"
          />
          <text class="deceased-name">{{ deceased?.name }}</text>
          <text class="deceased-dates">
            {{ deceased?.birthDate || '?' }} — {{ deceased?.deathDate || '?' }}
          </text>
          <text v-if="deceased?.orgName" class="deceased-org">{{ deceased.orgName }}</text>
        </view>
      </view>
    </view>

    <!-- 统计栏 -->
    <view class="stats-bar">
      <view class="stat-item">
        <text class="stat-num">{{ totalVisit }}</text>
        <text class="stat-label">访问</text>
      </view>
      <view class="stat-divider" />
      <view class="stat-item">
        <text class="stat-num">{{ messageCount }}</text>
        <text class="stat-label">留言</text>
      </view>
      <view class="stat-divider" />
      <view class="stat-item">
        <text class="stat-num">{{ flowerCount }}</text>
        <text class="stat-label">献花</text>
      </view>
    </view>

    <!-- 纪念文章 -->
    <view v-if="deceased?.bio" class="section">
      <view class="section-title">纪念文</view>
      <view class="bio-content" :class="{ 'bio-collapsed': bioCollapsed }">
        <rich-text :nodes="deceased.bio" />
      </view>
      <view v-if="bioCollapsed" class="bio-expand" @tap="bioCollapsed = false">
        <text>展开全部</text>
        <uni-icons type="down" size="14" color="#2c3e50" />
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-bar">
      <view class="action-btn" @tap="goMessage">
        <uni-icons type="chat" size="22" color="#2c3e50" />
        <text class="action-text">留言寄语</text>
      </view>
      <view class="action-btn" @tap="goFlower">
        <uni-icons type="star" size="22" color="#2c3e50" />
        <text class="action-text">献花致敬</text>
      </view>
      <view class="action-btn" @tap="handleShare">
        <uni-icons type="redo" size="22" color="#2c3e50" />
        <text class="action-text">分享</text>
      </view>
    </view>

    <!-- 相册 -->
    <view v-if="albums.length > 0" class="section">
      <view class="section-header">
        <text class="section-title">相册</text>
        <text class="section-count">{{ albums.length }}张</text>
      </view>
      <scroll-view scroll-x class="album-scroll">
        <view class="album-grid">
          <image
            v-for="(img, idx) in albums"
            :key="img.albumId"
            class="album-thumb"
            :src="img.imageUrl"
            mode="aspectFill"
            @tap="previewImage(idx)"
          />
        </view>
      </scroll-view>
    </view>

    <!-- 视频 -->
    <view v-if="videos.length > 0" class="section">
      <view class="section-header">
        <text class="section-title">视频</text>
        <text class="section-count">{{ videos.length }}个</text>
      </view>
      <view class="video-list">
        <view
          v-for="video in videos"
          :key="video.videoId"
          class="video-card"
          @tap="goVideo"
        >
          <view class="video-cover">
            <uni-icons type="videocam" size="30" color="#fff" />
          </view>
          <text class="video-title">{{ video.title || '纪念视频' }}</text>
        </view>
      </view>
    </view>

    <!-- 留言 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">留言寄语</text>
        <text class="section-count">{{ messageCount }}条</text>
      </view>

      <view v-if="messages.length === 0" class="empty-tip">
        <text>暂无留言，成为第一个留言的人</text>
      </view>

      <view v-else class="message-list">
        <view v-for="msg in messages.slice(0, 5)" :key="msg.messageId" class="message-item">
          <view class="message-header">
            <text class="message-author">{{ msg.visitorName || '匿名' }}</text>
            <text class="message-time">{{ formatTime(msg.createTime) }}</text>
          </view>
          <text class="message-content">{{ msg.content }}</text>
        </view>
        <view v-if="messages.length > 5" class="more-link" @tap="goMessage">
          <text>查看全部留言</text>
        </view>
      </view>
    </view>

    <!-- 献花 -->
    <view v-if="flowers.length > 0" class="section">
      <view class="section-header">
        <text class="section-title">献花致敬</text>
        <text class="section-count">{{ flowerCount }}次</text>
      </view>
      <view class="flower-list">
        <view v-for="fl in flowers.slice(0, 10)" :key="fl.flowerId" class="flower-item">
          <text class="flower-emoji">{{ getFlowerEmoji(fl.flowerType) }}</text>
          <text class="flower-name">{{ fl.visitorName || '匿名' }}</text>
        </view>
      </view>
    </view>

    <!-- 底部留白 -->
    <view style="height: 40rpx" />

    <!-- 加载中 -->
    <view v-if="loading" class="loading-mask">
      <uni-load-more status="loading" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useMemorialStore } from '@/stores/memorial'

const memorialStore = useMemorialStore()

const sysInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(sysInfo.statusBarHeight || 0)
const bioCollapsed = ref(true)
const code = ref('')

const deceased = computed(() => memorialStore.currentDeceased)
const albums = computed(() => memorialStore.albums)
const videos = computed(() => memorialStore.videos)
const messages = computed(() => memorialStore.messages)
const flowers = computed(() => memorialStore.flowers)
const totalVisit = computed(() => memorialStore.totalVisit)
const messageCount = computed(() => memorialStore.messageCount)
const flowerCount = computed(() => memorialStore.flowerCount)
const loading = computed(() => memorialStore.loading)

onLoad((options) => {
  if (options?.code) {
    code.value = options.code
    memorialStore.loadMemorialPage(options.code)
  }
})

function goBack() {
  uni.navigateBack({ delta: 1 })
}

function previewImage(index: number) {
  const urls = albums.value.map((a) => a.imageUrl)
  uni.previewImage({
    current: index,
    urls,
    indicator: 'number',
  })
}

function goMessage() {
  uni.navigateTo({ url: `/pages/memorial/message?deceasedId=${deceased.value?.deceasedId}` })
}

function goFlower() {
  uni.navigateTo({ url: `/pages/memorial/flower?deceasedId=${deceased.value?.deceasedId}` })
}

function goVideo() {
  uni.navigateTo({ url: `/pages/memorial/video?deceasedId=${deceased.value?.deceasedId}` })
}

function handleShare() {
  // #ifdef MP-WEIXIN
  // 小程序分享由 onShareAppMessage 处理
  // #endif
  // #ifdef H5
  if (navigator.clipboard) {
    navigator.clipboard.writeText(window.location.href)
    uni.showToast({ title: '链接已复制', icon: 'success' })
  }
  // #endif
}

function getFlowerEmoji(type: number): string {
  const map: Record<number, string> = { 1: '🌼', 2: '百合', 3: '康乃馨', 4: '🌹' }
  return map[type] || '🌸'
}

function formatTime(timeStr: string): string {
  if (!timeStr) return ''
  const d = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  if (diff < 86400000) {
    return `${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`
  }
  if (diff < 604800000) {
    return `${Math.floor(diff / 86400000)}天前`
  }
  return `${d.getMonth() + 1}月${d.getDate()}日`
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}

.header {
  position: relative;
  height: 520rpx;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.header-cover {
  width: 100%;
  height: 100%;
}

.header-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, rgba(44,62,80,0.3) 0%, rgba(44,62,80,0.8) 100%);
}

.header-content {
  position: relative;
  z-index: 2;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.nav-back {
  padding: 20rpx;
  width: 80rpx;
}

.deceased-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  padding-bottom: 40rpx;
}

.deceased-avatar {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  border: 6rpx solid rgba(255,255,255,0.6);
  margin-bottom: 16rpx;
}

.deceased-name {
  font-size: 40rpx;
  font-weight: 700;
  color: #fff;
}

.deceased-dates {
  font-size: 26rpx;
  color: rgba(255,255,255,0.8);
  margin-top: 8rpx;
}

.deceased-org {
  font-size: 24rpx;
  color: rgba(255,255,255,0.6);
  margin-top: 4rpx;
}

.stats-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  padding: 30rpx 0;
  margin-bottom: 20rpx;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-num {
  font-size: 36rpx;
  font-weight: 700;
  color: #2c3e50;
}

.stat-label {
  font-size: 24rpx;
  color: #999;
  margin-top: 4rpx;
}

.stat-divider {
  width: 1rpx;
  height: 60rpx;
  background: #eee;
}

.action-bar {
  display: flex;
  background: #fff;
  padding: 24rpx 0;
  margin-bottom: 20rpx;
}

.action-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.action-text {
  font-size: 24rpx;
  color: #666;
}

.section {
  background: #fff;
  margin-bottom: 20rpx;
  padding: 30rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.section-count {
  font-size: 24rpx;
  color: #999;
}

.bio-content {
  font-size: 28rpx;
  color: #555;
  line-height: 1.8;
}

.bio-collapsed {
  max-height: 260rpx;
  overflow: hidden;
  position: relative;
}

.bio-collapsed::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 80rpx;
  background: linear-gradient(transparent, #fff);
}

.bio-expand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
  padding-top: 10rpx;
  color: #2c3e50;
  font-size: 26rpx;
}

.album-scroll {
  white-space: nowrap;
}

.album-grid {
  display: inline-flex;
  gap: 16rpx;
}

.album-thumb {
  width: 200rpx;
  height: 200rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.video-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.video-card {
  width: calc(50% - 8rpx);
}

.video-cover {
  width: 100%;
  height: 240rpx;
  background: linear-gradient(135deg, #2c3e50, #3a5a7c);
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-title {
  font-size: 26rpx;
  color: #333;
  margin-top: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-list {
  display: flex;
  flex-direction: column;
}

.message-item {
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}

.message-item:last-child {
  border-bottom: none;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.message-author {
  font-size: 26rpx;
  color: #2c3e50;
  font-weight: 500;
}

.message-time {
  font-size: 22rpx;
  color: #ccc;
}

.message-content {
  font-size: 28rpx;
  color: #555;
  line-height: 1.6;
}

.more-link {
  text-align: center;
  padding: 20rpx 0;
  color: #2c3e50;
  font-size: 26rpx;
}

.empty-tip {
  text-align: center;
  padding: 30rpx 0;
  color: #999;
  font-size: 26rpx;
}

.flower-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.flower-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
  background: #f9f9f9;
  padding: 8rpx 16rpx;
  border-radius: 24rpx;
}

.flower-emoji {
  font-size: 28rpx;
}

.flower-name {
  font-size: 24rpx;
  color: #666;
}

.loading-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255,255,255,0.8);
  z-index: 999;
}
</style>
