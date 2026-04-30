<template>
  <view class="page">
    <!-- 空状态 -->
    <view v-if="!loading && memorials.length === 0" class="empty-state">
      <image class="empty-icon" src="/static/images/empty.png" mode="aspectFit" />
      <text class="empty-text">您尚未创建纪念馆</text>
      <button class="create-btn" @tap="goCreate">创建纪念馆</button>
    </view>

    <!-- 纪念馆列表 -->
    <view v-else class="memorial-list">
      <view v-for="item in memorials" :key="item.deceasedId" class="memorial-card">
        <view class="card-main" @tap="goDetail(item)">
          <image
            class="card-cover"
            :src="item.coverImage || '/static/images/default-avatar.png'"
            mode="aspectFill"
          />
          <view class="card-info">
            <text class="card-name">{{ item.name }}</text>
            <text class="card-date">{{ item.birthDate }} - {{ item.deathDate }}</text>
            <text v-if="item.orgName" class="card-org">{{ item.orgName }}</text>
          </view>
        </view>
        <view class="card-actions">
          <view class="action-btn" @tap="goEdit(item)">
            <uni-icons type="compose" size="18" color="#666" />
            <text class="action-text">编辑</text>
          </view>
          <view class="action-btn" @tap="goAlbum(item)">
            <uni-icons type="image" size="18" color="#666" />
            <text class="action-text">相册</text>
          </view>
          <view class="action-btn" @tap="goVideo(item)">
            <uni-icons type="videocam" size="18" color="#666" />
            <text class="action-text">视频</text>
          </view>
          <view class="action-btn" @tap="goMessage(item)">
            <uni-icons type="chat" size="18" color="#666" />
            <text class="action-text">留言</text>
          </view>
          <view class="action-btn" @tap="goQrcode(item)">
            <uni-icons type="qr_code" size="18" color="#666" />
            <text class="action-text">二维码</text>
          </view>
          <view class="action-btn" @tap="goStats(item)">
            <uni-icons type="bars" size="18" color="#666" />
            <text class="action-text">统计</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 创建按钮 -->
    <view v-if="memorials.length > 0" class="fab" @tap="goCreate">
      <uni-icons type="plus" size="28" color="#fff" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useFamilyStore } from '@/stores/family'

const familyStore = useFamilyStore()
const memorials = computed(() => familyStore.memorials)
const loading = computed(() => familyStore.loading)

onShow(() => {
  familyStore.loadMyMemorials()
})

function goCreate() {
  uni.navigateTo({ url: '/pages/family/memorial-edit' })
}

function goDetail(item: any) {
  uni.navigateTo({ url: `/pages/memorial/detail?code=${item.qrcodeCode}` })
}

function goEdit(item: any) {
  uni.navigateTo({ url: `/pages/family/memorial-edit?deceasedId=${item.deceasedId}` })
}

function goAlbum(item: any) {
  uni.navigateTo({ url: `/pages/family/album-manage?deceasedId=${item.deceasedId}&name=${encodeURIComponent(item.name)}` })
}

function goVideo(item: any) {
  uni.navigateTo({ url: `/pages/family/video-manage?deceasedId=${item.deceasedId}&name=${encodeURIComponent(item.name)}` })
}

function goMessage(item: any) {
  uni.navigateTo({ url: `/pages/family/message-audit?deceasedId=${item.deceasedId}&name=${encodeURIComponent(item.name)}` })
}

function goQrcode(item: any) {
  uni.navigateTo({ url: `/pages/family/qrcode?deceasedId=${item.deceasedId}&name=${encodeURIComponent(item.name)}` })
}

function goStats(item: any) {
  uni.navigateTo({ url: `/pages/family/statistics?deceasedId=${item.deceasedId}&name=${encodeURIComponent(item.name)}` })
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

.memorial-list {
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.memorial-card {
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

.card-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  display: block;
}

.card-date {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
  display: block;
}

.card-org {
  font-size: 24rpx;
  color: #666;
  margin-top: 4rpx;
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
