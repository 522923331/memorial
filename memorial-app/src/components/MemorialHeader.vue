<template>
  <view class="memorial-header" :style="{ paddingTop: statusBarHeight + 'px' }">
    <view class="header-bg">
      <image
        class="header-cover"
        :src="deceased?.coverImage || '/static/images/default-avatar.png'"
        mode="aspectFill"
      />
      <view class="header-overlay" />
    </view>
    <view class="header-content">
      <view v-if="showBack" class="nav-back" @tap="$emit('back')">
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
          {{ formatDisplayDate(deceased?.birthDate) }} — {{ formatDisplayDate(deceased?.deathDate) }}
        </text>
        <text v-if="deceased?.orgName" class="deceased-org">{{ deceased.orgName }}</text>
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
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { Deceased } from '@/types/memorial'

const props = withDefaults(defineProps<{
  deceased: Deceased | null
  totalVisit?: number
  messageCount?: number
  flowerCount?: number
  showBack?: boolean
}>(), {
  totalVisit: 0,
  messageCount: 0,
  flowerCount: 0,
  showBack: true,
})

defineEmits<{
  (e: 'back'): void
}>()

const sysInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(sysInfo.statusBarHeight || 0)

function formatDisplayDate(dateStr?: string): string {
  if (!dateStr) return '?'
  // 适配 "1945-03-15" 或 "2023-11-20T00:00:00" 格式
  return dateStr.substring(0, 10)
}
</script>

<style scoped>
.memorial-header {
  position: relative;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  height: 520rpx;
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
  height: 520rpx;
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
  position: relative;
  z-index: 2;
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
</style>
