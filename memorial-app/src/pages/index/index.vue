<template>
  <view class="page">
    <!-- 自定义导航栏 -->
    <NavBar title="云上纪念" :show-back="false" />

    <!-- 搜索栏 -->
    <view class="search-section">
      <view class="search-bar">
        <uni-icons type="search" size="18" color="#999" />
        <input
          class="search-input"
          v-model="keyword"
          placeholder="搜索逝者姓名"
          confirm-type="search"
          @confirm="handleSearch"
          @input="onSearchInput"
        />
        <view v-if="keyword" class="search-clear" @tap="clearSearch">
          <uni-icons type="clear" size="16" color="#ccc" />
        </view>
      </view>

      <!-- 搜索结果 -->
      <view v-if="searchResults.length > 0" class="search-results">
        <view
          v-for="item in searchResults"
          :key="item.deceasedId"
          class="search-item"
          @tap="goToMemorial(item)"
        >
          <image
            class="search-avatar"
            :src="item.coverImage || '/static/images/default-avatar.png'"
            mode="aspectFill"
          />
          <view class="search-info">
            <text class="search-name">{{ item.name }}</text>
            <text class="search-date">{{ item.birthDate }} - {{ item.deathDate }}</text>
          </view>
          <uni-icons type="right" size="16" color="#ccc" />
        </view>
      </view>
    </view>

    <!-- 最近访问 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">最近访问</text>
        <text v-if="recentVisits.length > 0" class="section-more" @tap="clearRecentVisits">清空</text>
      </view>

      <view v-if="recentVisits.length === 0" class="empty-state">
        <image class="empty-icon" src="/static/images/empty.png" mode="aspectFit" />
        <text class="empty-text">暂无访问记录</text>
        <text class="empty-hint">搜索或扫码访问纪念页</text>
      </view>

      <view v-else class="recent-list">
        <view
          v-for="item in recentVisits"
          :key="item.deceasedId"
          class="recent-item"
          @tap="goToMemorialByCode(item.qrcodeCode)"
        >
          <image
            class="recent-avatar"
            :src="item.coverImage || '/static/images/default-avatar.png'"
            mode="aspectFill"
          />
          <view class="recent-info">
            <text class="recent-name">{{ item.name }}</text>
            <text class="recent-time">{{ formatTime(item.visitTime) }}</text>
          </view>
          <uni-icons type="right" size="16" color="#ccc" />
        </view>
      </view>
    </view>

    <!-- 扫码按钮 -->
    <view class="scan-btn" @tap="handleScan">
      <uni-icons type="scan" size="24" color="#fff" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { searchDeceased } from '@/api/memorial'
import { useMemorialStore } from '@/stores/memorial'
import { parseQrCode } from '@/utils/qrcode'
import NavBar from '@/components/NavBar.vue'
import type { Deceased } from '@/types/memorial'

const memorialStore = useMemorialStore()

const keyword = ref('')
const searchResults = ref<Deceased[]>([])
const recentVisits = computed(() => memorialStore.recentVisits)
let searchTimer: ReturnType<typeof setTimeout> | null = null

onShow(() => {
  // 每次显示时刷新最近访问
})

function onSearchInput() {
  if (searchTimer) clearTimeout(searchTimer)
  if (!keyword.value.trim()) {
    searchResults.value = []
    return
  }
  searchTimer = setTimeout(() => {
    handleSearch()
  }, 500)
}

async function handleSearch() {
  if (!keyword.value.trim()) {
    searchResults.value = []
    return
  }
  try {
    const res = await searchDeceased(keyword.value.trim())
    searchResults.value = res.data || []
  } catch {
    searchResults.value = []
  }
}

function clearSearch() {
  keyword.value = ''
  searchResults.value = []
}

function goToMemorial(item: Deceased) {
  if (item.qrcodeCode) {
    uni.navigateTo({ url: `/pages/memorial/detail?code=${item.qrcodeCode}` })
  }
  clearSearch()
}

function goToMemorialByCode(code: string) {
  uni.navigateTo({ url: `/pages/memorial/detail?code=${code}` })
}

function handleScan() {
  // #ifdef MP-WEIXIN
  uni.scanCode({
    scanType: ['qrCode'],
    success: (res) => {
      const code = parseQrCode(res.result)
      if (code) {
        uni.navigateTo({ url: `/pages/memorial/detail?code=${code}` })
      } else {
        uni.showToast({ title: '无法识别该二维码', icon: 'none' })
      }
    },
    fail: () => {
      uni.showToast({ title: '扫码取消', icon: 'none' })
    },
  })
  // #endif
  // #ifdef H5
  uni.showToast({ title: '请在小程序中使用扫码功能', icon: 'none' })
  // #endif
}

function clearRecentVisits() {
  uni.showModal({
    title: '提示',
    content: '确定清空最近访问记录？',
    success: (res) => {
      if (res.confirm) {
        memorialStore.recentVisits = []
      }
    },
  })
}

function formatTime(timestamp: number): string {
  const now = Date.now()
  const diff = now - timestamp
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  const d = new Date(timestamp)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.search-section {
  padding: 20rpx 30rpx 0;
}

.search-bar {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 36rpx;
  padding: 16rpx 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.search-input {
  flex: 1;
  margin-left: 12rpx;
  font-size: 28rpx;
  height: 40rpx;
}

.search-clear {
  padding: 4rpx;
}

.search-results {
  margin-top: 16rpx;
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.search-item {
  display: flex;
  align-items: center;
  padding: 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.search-item:last-child {
  border-bottom: none;
}

.search-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: #e8e8e8;
}

.search-info {
  flex: 1;
  margin-left: 20rpx;
}

.search-name {
  font-size: 30rpx;
  color: #333;
  font-weight: 500;
}

.search-date {
  font-size: 24rpx;
  color: #999;
  margin-top: 4rpx;
  display: block;
}

.section {
  margin: 30rpx;
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

.section-more {
  font-size: 24rpx;
  color: #999;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 0;
}

.empty-icon {
  width: 200rpx;
  height: 200rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

.empty-hint {
  font-size: 24rpx;
  color: #ccc;
  margin-top: 8rpx;
}

.recent-list {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.recent-item {
  display: flex;
  align-items: center;
  padding: 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.recent-item:last-child {
  border-bottom: none;
}

.recent-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: #e8e8e8;
}

.recent-info {
  flex: 1;
  margin-left: 20rpx;
}

.recent-name {
  font-size: 30rpx;
  color: #333;
  font-weight: 500;
}

.recent-time {
  font-size: 24rpx;
  color: #999;
  margin-top: 4rpx;
  display: block;
}

.scan-btn {
  position: fixed;
  right: 40rpx;
  bottom: 180rpx;
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #2c3e50, #3a5a7c);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6rpx 20rpx rgba(44, 62, 80, 0.4);
  z-index: 50;
}
</style>
