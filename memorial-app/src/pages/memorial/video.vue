<template>
  <view class="page">
    <view v-if="videos.length === 0" class="empty-tip">
      <text>暂无视频</text>
    </view>
    <view v-else class="video-list">
      <view v-for="video in videos" :key="video.videoId" class="video-item">
        <video
          class="video-player"
          :src="video.videoUrl"
          :poster="video.coverUrl"
          controls
          object-fit="contain"
        />
        <text class="video-title">{{ video.title || '纪念视频' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useMemorialStore } from '@/stores/memorial'

const memorialStore = useMemorialStore()
const videos = computed(() => memorialStore.videos)
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20rpx;
}

.video-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.video-item {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
}

.video-player {
  width: 100%;
  height: 420rpx;
}

.video-title {
  font-size: 28rpx;
  color: #333;
  padding: 16rpx 20rpx;
  display: block;
}

.empty-tip {
  text-align: center;
  padding: 100rpx 0;
  color: #999;
  font-size: 28rpx;
}
</style>
