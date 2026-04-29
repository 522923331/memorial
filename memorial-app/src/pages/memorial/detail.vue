<template>
  <view class="page">
    <!-- 顶部逝者信息 -->
    <MemorialHeader
      :deceased="deceased"
      :total-visit="totalVisit"
      :message-count="messageCount"
      :flower-count="flowerCount"
      :show-back="true"
      @back="goBack"
    />

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
      <AlbumGrid :albums="albums" :columns="3" />
    </view>

    <!-- 视频 -->
    <view v-if="videos.length > 0" class="section">
      <view class="section-header">
        <text class="section-title">视频</text>
        <text class="section-count">{{ videos.length }}个</text>
      </view>
      <view class="video-list">
        <VideoCard
          v-for="video in videos"
          :key="video.videoId"
          :video="video"
          @tap="goVideo"
        />
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
        <MessageItem
          v-for="msg in messages.slice(0, 5)"
          :key="msg.messageId"
          :message="msg"
        />
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
        <FlowerItem
          v-for="fl in flowers.slice(0, 10)"
          :key="fl.flowerId"
          :flower="fl"
        />
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
import MemorialHeader from '@/components/MemorialHeader.vue'
import AlbumGrid from '@/components/AlbumGrid.vue'
import VideoCard from '@/components/VideoCard.vue'
import MessageItem from '@/components/MessageItem.vue'
import FlowerItem from '@/components/FlowerItem.vue'

const memorialStore = useMemorialStore()

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
  // #ifdef H5
  if (navigator.clipboard) {
    navigator.clipboard.writeText(window.location.href)
    uni.showToast({ title: '链接已复制', icon: 'success' })
  }
  // #endif
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
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

.video-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.video-list > * {
  width: calc(50% - 8rpx);
}

.message-list {
  display: flex;
  flex-direction: column;
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
