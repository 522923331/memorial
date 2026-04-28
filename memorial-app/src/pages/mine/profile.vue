<template>
  <view class="page">
    <view class="profile-section">
      <view class="avatar-row">
        <image
          class="avatar"
          :src="userStore.avatar || '/static/images/default-avatar.png'"
          mode="aspectFill"
        />
      </view>

      <view class="info-item">
        <text class="info-label">昵称</text>
        <text class="info-value">{{ userStore.nickName || '-' }}</text>
      </view>
      <view class="info-item">
        <text class="info-label">手机号</text>
        <text class="info-value">{{ userStore.phone || '未绑定' }}</text>
        <button v-if="!userStore.phone" class="bind-btn" size="mini" @tap="showBindPhone = true">绑定</button>
      </view>
    </view>

    <!-- 绑定手机号弹窗 -->
    <view v-if="showBindPhone" class="modal-mask" @tap="showBindPhone = false">
      <view class="modal-content" @tap.stop>
        <text class="modal-title">绑定手机号</text>
        <input class="modal-input" v-model="bindPhone" type="number" maxlength="11" placeholder="请输入手机号" />
        <input class="modal-input" v-model="bindCode" type="number" maxlength="6" placeholder="验证码" />
        <view class="modal-btns">
          <button class="modal-btn cancel" @tap="showBindPhone = false">取消</button>
          <button class="modal-btn confirm" @tap="handleBindPhone">确定</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { bindPhone } from '@/api/auth'

const userStore = useUserStore()

const showBindPhone = ref(false)
const bindPhone = ref('')
const bindCode = ref('')

async function handleBindPhone() {
  if (!bindPhone.value || bindPhone.value.length !== 11) {
    uni.showToast({ title: '请输入正确手机号', icon: 'none' })
    return
  }
  try {
    await bindPhone(bindPhone.value, bindCode.value)
    uni.showToast({ title: '绑定成功', icon: 'success' })
    showBindPhone.value = false
    await userStore.fetchUserInfo()
  } catch {
    // 错误由 request 处理
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}

.profile-section {
  background: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
  padding: 30rpx;
}

.avatar-row {
  display: flex;
  justify-content: center;
  padding: 20rpx 0 30rpx;
}

.avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: #e8e8e8;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 28rpx;
  color: #999;
  width: 140rpx;
}

.info-value {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.bind-btn {
  font-size: 24rpx;
  color: #2c3e50;
  background: #f5f5f5;
  border-radius: 24rpx;
  padding: 8rpx 20rpx;
  border: none;
}

.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.modal-content {
  background: #fff;
  border-radius: 20rpx;
  padding: 40rpx;
  width: 600rpx;
}

.modal-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 30rpx;
  display: block;
  text-align: center;
}

.modal-input {
  font-size: 28rpx;
  padding: 20rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
}

.modal-btns {
  display: flex;
  gap: 20rpx;
  margin-top: 20rpx;
}

.modal-btn {
  flex: 1;
  font-size: 28rpx;
  padding: 20rpx;
  border-radius: 36rpx;
  border: none;
  text-align: center;
}

.modal-btn.cancel {
  background: #f5f5f5;
  color: #666;
}

.modal-btn.confirm {
  background: #2c3e50;
  color: #fff;
}
</style>
