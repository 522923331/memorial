<template>
  <view class="page">
    <!-- 鲜花选择 -->
    <view class="section">
      <text class="section-title">选择鲜花</text>
      <view class="flower-grid">
        <view
          v-for="item in flowerTypes"
          :key="item.type"
          class="flower-card"
          :class="{ active: selectedType === item.type }"
          @tap="selectedType = item.type"
        >
          <text class="flower-icon">{{ item.icon }}</text>
          <text class="flower-name">{{ item.name }}</text>
        </view>
      </view>
    </view>

    <!-- 留言（选填） -->
    <view class="section">
      <text class="section-title">寄语（选填）</text>
      <input
        class="author-input"
        v-model="authorName"
        placeholder="您的姓名"
      />
    </view>

    <!-- 献花按钮 -->
    <view class="submit-section">
      <button class="submit-btn" @tap="handleSubmit">献花致敬</button>
    </view>

    <!-- 献花记录 -->
    <view class="section">
      <text class="section-title">献花记录</text>
      <view v-if="flowers.length === 0" class="empty-tip">
        <text>暂无献花记录</text>
      </view>
      <view v-else class="flower-list">
        <view v-for="fl in flowers" :key="fl.flowerId" class="flower-record">
          <text class="flower-emoji">{{ getFlowerEmoji(fl.flowerType) }}</text>
          <text class="flower-who">{{ fl.authorName || '匿名' }}</text>
          <text class="flower-when">{{ formatTime(fl.createTime) }}</text>
        </view>
      </view>
    </view>

    <!-- 献花动画 -->
    <view v-if="showAnimation" class="animation-mask" @tap="showAnimation = false">
      <view class="animation-content">
        <text class="animation-flower">{{ getFlowerEmoji(selectedType) }}</text>
        <text class="animation-text">献花成功</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useMemorialStore } from '@/stores/memorial'
import { useUserStore } from '@/stores/user'

const memorialStore = useMemorialStore()
const userStore = useUserStore()

const flowerTypes = [
  { type: 1, name: '菊花', icon: '🌼' },
  { type: 2, name: '百合', icon: '🤍' },
  { type: 3, name: '康乃馨', icon: '🌸' },
  { type: 4, name: '玫瑰', icon: '🌹' },
]

const selectedType = ref(1)
const authorName = ref('')
const showAnimation = ref(false)
const flowers = computed(() => memorialStore.flowers)

onLoad((options) => {
  if (options?.deceasedId) {
    memorialStore.loadFlowers()
  }
  authorName.value = userStore.nickName || ''
})

async function handleSubmit() {
  try {
    await memorialStore.sendFlower(selectedType.value, authorName.value.trim() || '匿名')
    showAnimation.value = true
    setTimeout(() => {
      showAnimation.value = false
    }, 2000)
  } catch {
    // 错误由 request 拦截器处理
  }
}

function getFlowerEmoji(type: number): string {
  const map: Record<number, string> = { 1: '🌼', 2: '🤍', 3: '🌸', 4: '🌹' }
  return map[type] || '🌸'
}

function formatTime(timeStr: string): string {
  if (!timeStr) return ''
  const d = new Date(timeStr)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}

.section {
  background: #fff;
  margin: 20rpx;
  padding: 30rpx;
  border-radius: 16rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 24rpx;
  display: block;
}

.flower-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
}

.flower-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx 0;
  border-radius: 16rpx;
  border: 2rpx solid #eee;
  transition: all 0.2s;
}

.flower-card.active {
  border-color: #2c3e50;
  background: rgba(44, 62, 80, 0.05);
}

.flower-icon {
  font-size: 56rpx;
  margin-bottom: 8rpx;
}

.flower-name {
  font-size: 24rpx;
  color: #666;
}

.author-input {
  font-size: 28rpx;
  padding: 16rpx 20rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
}

.submit-section {
  padding: 0 30rpx;
  margin-top: 10rpx;
}

.submit-btn {
  background: linear-gradient(135deg, #2c3e50, #3a5a7c);
  color: #fff;
  font-size: 32rpx;
  padding: 24rpx;
  border-radius: 48rpx;
  border: none;
  text-align: center;
}

.flower-list {
  display: flex;
  flex-direction: column;
}

.flower-record {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}

.flower-record:last-child {
  border-bottom: none;
}

.flower-emoji {
  font-size: 32rpx;
}

.flower-who {
  font-size: 28rpx;
  color: #333;
  flex: 1;
}

.flower-when {
  font-size: 24rpx;
  color: #ccc;
}

.empty-tip {
  text-align: center;
  padding: 30rpx 0;
  color: #999;
  font-size: 26rpx;
}

.animation-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0,0,0,0.4);
  z-index: 999;
}

.animation-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.animation-flower {
  font-size: 120rpx;
}

.animation-text {
  font-size: 32rpx;
  color: #fff;
  font-weight: 600;
}
</style>
