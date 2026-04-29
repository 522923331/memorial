<template>
  <view class="page">
    <view class="login-header">
      <image class="logo" src="/static/images/logo.png" mode="aspectFit" />
      <text class="app-name">云上纪念</text>
      <text class="app-desc">缅怀至亲，铭记于心</text>
    </view>

    <view class="login-body">
      <!-- #ifdef MP-WEIXIN -->
      <button class="wx-login-btn" open-type="getPhoneNumber" @getphonenumber="onWxLogin">
        微信一键登录
      </button>
      <button class="wx-login-btn plain" @tap="onWxLoginSimple">
        微信快捷登录
      </button>
      <!-- #endif -->

      <!-- #ifdef H5 -->
      <view class="phone-login">
        <view class="input-group">
          <input
            class="phone-input"
            v-model="phone"
            type="number"
            maxlength="11"
            placeholder="请输入手机号"
          />
        </view>
        <view class="input-group code-group">
          <input
            class="code-input"
            v-model="smsCode"
            type="number"
            maxlength="6"
            placeholder="验证码"
          />
          <button
            class="code-btn"
            :disabled="countdown > 0"
            @tap="sendSmsCode"
          >{{ countdown > 0 ? countdown + 's' : '获取验证码' }}</button>
        </view>
        <button class="login-btn" @tap="onPhoneLogin">登录</button>
      </view>
      <!-- #endif -->
    </view>

    <view class="login-footer">
      <text class="privacy-link">登录即表示同意</text>
      <text class="privacy-link highlight">《用户协议》</text>
      <text class="privacy-link">和</text>
      <text class="privacy-link highlight">《隐私政策》</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { sendSmsCode as sendSmsCodeApi } from '@/api/auth'

const userStore = useUserStore()

const phone = ref('')
const smsCode = ref('')
const countdown = ref(0)
let countdownTimer: ReturnType<typeof setInterval> | null = null

// #ifdef MP-WEIXIN
async function onWxLogin() {
  try {
    await userStore.wxLogin()
    uni.navigateBack()
  } catch {
    // 错误由 store 和 request 处理
  }
}

async function onWxLoginSimple() {
  try {
    await userStore.wxLogin()
    uni.navigateBack()
  } catch {
    // 错误由 store 和 request 处理
  }
}
// #endif

// #ifdef H5
async function onPhoneLogin() {
  if (!phone.value || phone.value.length !== 11) {
    uni.showToast({ title: '请输入正确手机号', icon: 'none' })
    return
  }
  if (!smsCode.value) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }
  try {
    await userStore.loginByPhone(phone.value, smsCode.value)
    uni.navigateBack()
  } catch {
    // 错误由 store 和 request 处理
  }
}

async function sendSmsCode() {
  if (!phone.value || phone.value.length !== 11) {
    uni.showToast({ title: '请输入正确手机号', icon: 'none' })
    return
  }
  try {
    const res = await sendSmsCodeApi(phone.value)
    uni.showToast({ title: '验证码已发送', icon: 'success' })
    // P0阶段：后端直接返回验证码，自动填入方便测试
    if (res.data?.code) {
      smsCode.value = res.data.code
    }
    countdown.value = 60
    countdownTimer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0 && countdownTimer) {
        clearInterval(countdownTimer)
        countdownTimer = null
      }
    }, 1000)
  } catch {
    // 错误由 request 拦截器处理
  }
}
// #endif
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 60rpx;
}

.login-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 200rpx;
  margin-bottom: 100rpx;
}

.logo {
  width: 160rpx;
  height: 160rpx;
  margin-bottom: 24rpx;
}

.app-name {
  font-size: 44rpx;
  font-weight: 700;
  color: #2c3e50;
}

.app-desc {
  font-size: 28rpx;
  color: #999;
  margin-top: 12rpx;
}

.login-body {
  width: 100%;
}

.wx-login-btn {
  background: #07c160;
  color: #fff;
  font-size: 32rpx;
  padding: 24rpx;
  border-radius: 48rpx;
  border: none;
  text-align: center;
  margin-bottom: 24rpx;
}

.wx-login-btn.plain {
  background: #fff;
  color: #07c160;
  border: 2rpx solid #07c160;
}

.phone-login {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.input-group {
  display: flex;
  align-items: center;
}

.phone-input,
.code-input {
  flex: 1;
  font-size: 30rpx;
  padding: 24rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
}

.code-group {
  display: flex;
  gap: 16rpx;
}

.code-btn {
  font-size: 26rpx;
  color: #2c3e50;
  background: #f5f5f5;
  padding: 24rpx 20rpx;
  border-radius: 12rpx;
  border: none;
  white-space: nowrap;
}

.code-btn[disabled] {
  color: #ccc;
}

.login-btn {
  background: #2c3e50;
  color: #fff;
  font-size: 32rpx;
  padding: 24rpx;
  border-radius: 48rpx;
  border: none;
  text-align: center;
  margin-top: 16rpx;
}

.login-footer {
  position: fixed;
  bottom: 80rpx;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
}

.privacy-link {
  font-size: 24rpx;
  color: #999;
}

.privacy-link.highlight {
  color: #2c3e50;
}
</style>
