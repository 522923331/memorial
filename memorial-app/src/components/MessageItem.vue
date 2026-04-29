<template>
  <view class="message-item">
    <view class="message-header">
      <text class="message-author">{{ message.visitorName || '匿名' }}</text>
      <text v-if="message.relation" class="message-relation">{{ message.relation }}</text>
      <text class="message-time">{{ formatTime(message.createTime) }}</text>
    </view>
    <text class="message-content">{{ message.content }}</text>
  </view>
</template>

<script setup lang="ts">
import type { Message } from '@/types/memorial'

defineProps<{
  message: Message
}>()

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
.message-item {
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}

.message-item:last-child {
  border-bottom: none;
}

.message-header {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
  gap: 12rpx;
}

.message-author {
  font-size: 26rpx;
  color: #2c3e50;
  font-weight: 500;
}

.message-relation {
  font-size: 22rpx;
  color: #999;
  background: #f5f5f5;
  padding: 2rpx 12rpx;
  border-radius: 8rpx;
}

.message-time {
  font-size: 22rpx;
  color: #ccc;
  margin-left: auto;
}

.message-content {
  font-size: 28rpx;
  color: #555;
  line-height: 1.6;
}
</style>
