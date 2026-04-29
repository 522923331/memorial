<template>
  <view class="video-card" @tap="$emit('tap')">
    <view class="video-cover">
      <image
        v-if="video.coverUrl"
        class="cover-img"
        :src="video.coverUrl"
        mode="aspectFill"
      />
      <view class="play-icon">
        <uni-icons type="videocam" size="30" color="#fff" />
      </view>
    </view>
    <text class="video-title">{{ video.title || '纪念视频' }}</text>
  </view>
</template>

<script setup lang="ts">
import type { DeceasedVideo } from '@/types/memorial'

defineProps<{
  video: DeceasedVideo
}>()

defineEmits<{
  (e: 'tap'): void
}>()
</script>

<style scoped>
.video-card {
  border-radius: 12rpx;
  overflow: hidden;
  background: #fff;
}

.video-cover {
  position: relative;
  width: 100%;
  height: 240rpx;
  background: linear-gradient(135deg, #2c3e50, #3a5a7c);
}

.cover-img {
  width: 100%;
  height: 100%;
}

.play-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 80rpx;
  height: 80rpx;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-title {
  font-size: 26rpx;
  color: #333;
  padding: 12rpx 16rpx;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
