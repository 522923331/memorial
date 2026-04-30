<template>
  <view class="page">
    <!-- 纪念馆信息 -->
    <view class="memorial-header">
      <image
        class="memorial-cover"
        :src="coverImage || '/static/images/default-avatar.png'"
        mode="aspectFill"
      />
      <text class="memorial-name">{{ memorialName }}</text>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-cards">
      <view class="stat-card">
        <uni-icons type="eye" size="28" color="#2c3e50" />
        <text class="stat-number">{{ totalVisit }}</text>
        <text class="stat-label">访问量</text>
      </view>
      <view class="stat-card">
        <uni-icons type="chat" size="28" color="#2c3e50" />
        <text class="stat-number">{{ totalMessage }}</text>
        <text class="stat-label">留言数</text>
      </view>
      <view class="stat-card">
        <uni-icons type="flower" size="28" color="#2c3e50" />
        <text class="stat-number">{{ totalFlower }}</text>
        <text class="stat-label">献花数</text>
      </view>
    </view>

    <!-- 每日趋势 -->
    <view class="trend-section">
      <text class="section-title">近7日趋势</text>
      <view v-if="recentDays.length === 0" class="trend-empty">
        <text>暂无访问数据</text>
      </view>
      <view v-else class="trend-list">
        <view v-for="day in recentDays" :key="day.date" class="trend-row">
          <text class="trend-date">{{ day.date }}</text>
          <view class="trend-bars">
            <view class="bar-wrap">
              <view class="bar visit" :style="{ width: day.visitWidth }" />
              <text class="bar-label">{{ day.visitCount }}</text>
            </view>
            <view class="bar-wrap">
              <view class="bar message" :style="{ width: day.messageWidth }" />
              <text class="bar-label">{{ day.messageCount }}</text>
            </view>
            <view class="bar-wrap">
              <view class="bar flower" :style="{ width: day.flowerWidth }" />
              <text class="bar-label">{{ day.flowerCount }}</text>
            </view>
          </view>
        </view>
      </view>
      <!-- 图例 -->
      <view v-if="recentDays.length > 0" class="legend">
        <view class="legend-item">
          <view class="legend-dot visit" />
          <text class="legend-text">访问</text>
        </view>
        <view class="legend-item">
          <view class="legend-dot message" />
          <text class="legend-text">留言</text>
        </view>
        <view class="legend-item">
          <view class="legend-dot flower" />
          <text class="legend-text">献花</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getMemorialStatistics } from '@/api/family'

const memorialName = ref('')
const coverImage = ref('')
const totalVisit = ref(0)
const totalMessage = ref(0)
const totalFlower = ref(0)
const dailyStats = ref<any[]>([])

const recentDays = computed(() => {
  const days = dailyStats.value.slice(0, 7).reverse()
  const maxVisit = Math.max(...days.map((d: any) => d.visitCount || 0), 1)
  const maxMessage = Math.max(...days.map((d: any) => d.messageCount || 0), 1)
  const maxFlower = Math.max(...days.map((d: any) => d.flowerCount || 0), 1)
  return days.map((d: any) => {
    const dateStr = d.visitDate
      ? new Date(d.visitDate).toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' })
      : ''
    return {
      date: dateStr,
      visitCount: d.visitCount || 0,
      messageCount: d.messageCount || 0,
      flowerCount: d.flowerCount || 0,
      visitWidth: ((d.visitCount || 0) / maxVisit * 100) + '%',
      messageWidth: ((d.messageCount || 0) / maxMessage * 100) + '%',
      flowerWidth: ((d.flowerCount || 0) / maxFlower * 100) + '%',
    }
  })
})

onLoad(async (options) => {
  if (options?.deceasedId) {
    const deceasedId = Number(options.deceasedId)
    memorialName.value = decodeURIComponent(options.name || '')
    coverImage.value = options.coverImage ? decodeURIComponent(options.coverImage) : ''
    uni.showLoading({ title: '加载中...' })
    try {
      const res = await getMemorialStatistics(deceasedId)
      const data = res as any
      totalVisit.value = data.totalVisit || 0
      totalMessage.value = data.totalMessage || 0
      totalFlower.value = data.totalFlower || 0
      dailyStats.value = data.dailyStats || []
    } finally {
      uni.hideLoading()
    }
  }
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}

.memorial-header {
  display: flex;
  align-items: center;
  background: #fff;
  padding: 30rpx;
  gap: 24rpx;
}

.memorial-cover {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: #e8e8e8;
  flex-shrink: 0;
}

.memorial-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.stats-cards {
  display: flex;
  padding: 24rpx 20rpx;
  gap: 16rpx;
}

.stat-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.stat-number {
  font-size: 40rpx;
  font-weight: 700;
  color: #2c3e50;
  margin-top: 12rpx;
}

.stat-label {
  font-size: 24rpx;
  color: #999;
  margin-top: 4rpx;
}

.trend-section {
  margin: 0 20rpx 20rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  display: block;
  margin-bottom: 20rpx;
}

.trend-empty {
  text-align: center;
  padding: 60rpx 0;
  color: #999;
  font-size: 28rpx;
}

.trend-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.trend-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.trend-date {
  font-size: 24rpx;
  color: #999;
  width: 80rpx;
  flex-shrink: 0;
  text-align: right;
}

.trend-bars {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.bar-wrap {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.bar {
  height: 20rpx;
  border-radius: 4rpx;
  min-width: 4rpx;
  transition: width 0.3s;
}

.bar.visit {
  background: #2c3e50;
}

.bar.message {
  background: #3498db;
}

.bar.flower {
  background: #e74c3c;
}

.bar-label {
  font-size: 20rpx;
  color: #999;
  min-width: 36rpx;
}

.legend {
  display: flex;
  gap: 24rpx;
  margin-top: 20rpx;
  justify-content: center;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.legend-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 4rpx;
}

.legend-dot.visit {
  background: #2c3e50;
}

.legend-dot.message {
  background: #3498db;
}

.legend-dot.flower {
  background: #e74c3c;
}

.legend-text {
  font-size: 22rpx;
  color: #666;
}
</style>
