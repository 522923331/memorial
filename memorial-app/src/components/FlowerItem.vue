<template>
  <view class="flower-item">
    <text class="flower-emoji">{{ getFlowerEmoji(flower.flowerType) }}</text>
    <text class="flower-name">{{ flower.visitorName || '匿名' }}</text>
    <text v-if="flower.message" class="flower-msg">{{ flower.message }}</text>
  </view>
</template>

<script setup lang="ts">
import type { Flower } from '@/types/memorial'

defineProps<{
  flower: Flower
}>()

function getFlowerEmoji(type: number): string {
  const map: Record<number, string> = { 1: '🌼', 2: '🤍', 3: '🌸', 4: '🌹' }
  return map[type] || '🌸'
}
</script>

<style scoped>
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

.flower-msg {
  font-size: 22rpx;
  color: #999;
  max-width: 200rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
