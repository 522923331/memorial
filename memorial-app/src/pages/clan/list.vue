<template>
  <view class="page">
    <!-- 空状态 -->
    <view v-if="!loading && clans.length === 0" class="empty-state">
      <image class="empty-icon" src="/static/images/default-clan.png" mode="aspectFit" />
      <text class="empty-text">您尚未创建族谱</text>
      <button class="create-btn" @tap="goCreate">创建族谱</button>
    </view>

    <!-- 族谱列表 -->
    <view v-else class="clan-list">
      <view v-for="item in clans" :key="item.clanId" class="clan-card">
        <view class="card-main" @tap="goDetail(item)">
          <image
            class="card-cover"
            :src="item.coverImage || '/static/images/default-clan.png'"
            mode="aspectFill"
          />
          <view class="card-info">
            <view class="name-row">
              <text class="card-name">{{ item.clanName }}</text>
              <text v-if="item.isPublic === '1'" class="card-tag">私密</text>
            </view>
            <text class="card-meta">{{ item.memberCount }}人 · {{ item.generationCount }}代</text>
          </view>
        </view>
        <view class="card-actions">
          <view class="action-btn" @tap="goEdit(item)">
            <uni-icons type="compose" size="18" color="#666" />
            <text class="action-text">编辑</text>
          </view>
          <view class="action-btn" @tap="goMembers(item)">
            <uni-icons type="staff" size="18" color="#666" />
            <text class="action-text">成员</text>
          </view>
          <view class="action-btn" @tap="goQrcode(item)">
            <uni-icons type="qr_code" size="18" color="#666" />
            <text class="action-text">二维码</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 创建按钮 -->
    <view v-if="clans.length > 0" class="fab" @tap="goCreate">
      <uni-icons type="plus" size="28" color="#fff" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getMyClans } from '@/api/clan'
import type { Clan } from '@/types/clan'

const clans = ref<Clan[]>([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await getMyClans()
    clans.value = res.data || []
  } finally {
    loading.value = false
  }
}

onShow(() => {
  load()
})

function goCreate() {
  uni.navigateTo({ url: '/pages/clan/edit' })
}

function goDetail(item: Clan) {
  uni.navigateTo({ url: `/pages/clan/detail?clanId=${item.clanId}` })
}

function goEdit(item: Clan) {
  uni.navigateTo({ url: `/pages/clan/edit?clanId=${item.clanId}` })
}

function goMembers(item: Clan) {
  uni.navigateTo({
    url: `/pages/clan/members?clanId=${item.clanId}&name=${encodeURIComponent(item.clanName)}`,
  })
}

function goQrcode(item: Clan) {
  uni.navigateTo({
    url: `/pages/clan/qrcode?clanId=${item.clanId}&name=${encodeURIComponent(item.clanName)}&url=${encodeURIComponent(item.qrcodeUrl)}`,
  })
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 120rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 160rpx 60rpx 0;
}

.empty-icon {
  width: 200rpx;
  height: 200rpx;
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
  margin-bottom: 40rpx;
}

.create-btn {
  background: linear-gradient(135deg, #2c3e50, #3a5a7c);
  color: #fff;
  font-size: 30rpx;
  padding: 20rpx 60rpx;
  border-radius: 48rpx;
  border: none;
}

.clan-list {
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.clan-card {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.card-main {
  display: flex;
  padding: 24rpx;
  align-items: center;
}

.card-cover {
  width: 120rpx;
  height: 120rpx;
  border-radius: 12rpx;
  background: #e8e8e8;
  flex-shrink: 0;
}

.card-info {
  flex: 1;
  margin-left: 24rpx;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.card-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.card-tag {
  font-size: 20rpx;
  color: #e74c3c;
  border: 1rpx solid #e74c3c;
  border-radius: 6rpx;
  padding: 0 8rpx;
}

.card-meta {
  font-size: 24rpx;
  color: #999;
  margin-top: 10rpx;
  display: block;
}

.card-actions {
  display: flex;
  border-top: 1rpx solid #f0f0f0;
}

.action-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx 0;
  gap: 4rpx;
}

.action-btn:active {
  background: #f5f5f5;
}

.action-text {
  font-size: 22rpx;
  color: #666;
}

.fab {
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
