<template>
  <view class="page">
    <!-- 用户信息头部 -->
    <view class="user-header">
      <view class="user-card" @tap="goLogin">
        <image
          class="user-avatar"
          :src="userStore.avatar || '/static/images/default-avatar.png'"
          mode="aspectFill"
        />
        <view class="user-info">
          <text class="user-name">{{ userStore.isLoggedIn ? userStore.nickName : '点击登录' }}</text>
          <text v-if="userStore.phone" class="user-phone">{{ userStore.phone }}</text>
        </view>
        <uni-icons type="right" size="18" color="#ccc" />
      </view>
    </view>

    <!-- 菜单列表 -->
    <view class="menu-section">
      <view class="menu-item" @tap="goProfile">
        <uni-icons type="person" size="22" color="#666" />
        <text class="menu-text">个人信息</text>
        <uni-icons type="right" size="16" color="#ccc" />
      </view>
      <view class="menu-item" @tap="goAbout">
        <uni-icons type="info" size="22" color="#666" />
        <text class="menu-text">关于我们</text>
        <uni-icons type="right" size="16" color="#ccc" />
      </view>
    </view>

    <!-- 退出登录 -->
    <view v-if="userStore.isLoggedIn" class="logout-section">
      <button class="logout-btn" @tap="handleLogout">退出登录</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

function goLogin() {
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/login/index' })
  }
}

function goProfile() {
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  uni.navigateTo({ url: '/pages/mine/profile' })
}

function goAbout() {
  uni.showModal({
    title: '云上纪念',
    content: '线上纪念馆平台，缅怀至亲，铭记于心。v1.0.0',
    showCancel: false,
  })
}

function handleLogout() {
  uni.showModal({
    title: '提示',
    content: '确定退出登录？',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
      }
    },
  })
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}

.user-header {
  background: linear-gradient(135deg, #2c3e50, #3a5a7c);
  padding: 60rpx 30rpx 40rpx;
}

.user-card {
  display: flex;
  align-items: center;
}

.user-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255,255,255,0.4);
}

.user-info {
  flex: 1;
  margin-left: 24rpx;
}

.user-name {
  font-size: 36rpx;
  font-weight: 600;
  color: #fff;
}

.user-phone {
  font-size: 26rpx;
  color: rgba(255,255,255,0.7);
  margin-top: 8rpx;
  display: block;
}

.menu-section {
  background: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-text {
  flex: 1;
  font-size: 30rpx;
  color: #333;
  margin-left: 16rpx;
}

.logout-section {
  padding: 40rpx 30rpx;
}

.logout-btn {
  background: #fff;
  color: #dd524d;
  font-size: 30rpx;
  padding: 24rpx;
  border-radius: 48rpx;
  border: none;
  text-align: center;
}
</style>
