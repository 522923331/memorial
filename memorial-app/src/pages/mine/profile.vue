<template>
  <view class="page">
    <view class="profile-section">
      <!-- 头像 -->
      <view class="avatar-row" @tap="handleAvatarChange">
        <image
          class="avatar"
          :src="userStore.avatar || '/static/images/default-avatar.png'"
          mode="aspectFill"
        />
        <view class="avatar-edit-hint">
          <uni-icons type="camera" size="16" color="#fff" />
        </view>
      </view>

      <!-- 昵称 -->
      <view class="info-item" @tap="showNickNameInput = true">
        <text class="info-label">昵称</text>
        <text class="info-value">{{ userStore.nickName || '-' }}</text>
        <uni-icons type="right" size="16" color="#ccc" />
      </view>

      <!-- 性别 -->
      <view class="info-item" @tap="showSexPicker = true">
        <text class="info-label">性别</text>
        <text class="info-value">{{ sexLabel }}</text>
        <uni-icons type="right" size="16" color="#ccc" />
      </view>

      <!-- 手机号 -->
      <view class="info-item">
        <text class="info-label">手机号</text>
        <text class="info-value">{{ userStore.phone || '未绑定' }}</text>
        <button v-if="!userStore.phone" class="bind-btn" size="mini" @tap="showBindPhone = true">绑定</button>
      </view>
    </view>

    <!-- 昵称修改弹窗 -->
    <view v-if="showNickNameInput" class="modal-mask" @tap="showNickNameInput = false">
      <view class="modal-content" @tap.stop>
        <text class="modal-title">修改昵称</text>
        <input class="modal-input" v-model="editNickName" maxlength="20" placeholder="请输入昵称" />
        <view class="modal-btns">
          <button class="modal-btn cancel" @tap="showNickNameInput = false">取消</button>
          <button class="modal-btn confirm" @tap="handleUpdateNickName">确定</button>
        </view>
      </view>
    </view>

    <!-- 性别选择弹窗 -->
    <view v-if="showSexPicker" class="modal-mask" @tap="showSexPicker = false">
      <view class="modal-content" @tap.stop>
        <text class="modal-title">选择性别</text>
        <view class="sex-options">
          <view class="sex-option" :class="{ active: editSex === '0' }" @tap="editSex = '0'">
            <text>男</text>
          </view>
          <view class="sex-option" :class="{ active: editSex === '1' }" @tap="editSex = '1'">
            <text>女</text>
          </view>
          <view class="sex-option" :class="{ active: editSex === '2' }" @tap="editSex = '2'">
            <text>未知</text>
          </view>
        </view>
        <view class="modal-btns">
          <button class="modal-btn cancel" @tap="showSexPicker = false">取消</button>
          <button class="modal-btn confirm" @tap="handleUpdateSex">确定</button>
        </view>
      </view>
    </view>

    <!-- 绑定手机号弹窗 -->
    <view v-if="showBindPhone" class="modal-mask" @tap="showBindPhone = false">
      <view class="modal-content" @tap.stop>
        <text class="modal-title">绑定手机号</text>
        <input class="modal-input" v-model="bindPhoneNum" type="number" maxlength="11" placeholder="请输入手机号" />
        <view class="code-group">
          <input class="modal-input code-input" v-model="bindCode" type="number" maxlength="6" placeholder="验证码" />
          <button
            class="code-btn"
            :disabled="bindCountdown > 0"
            @tap="handleSendBindCode"
          >{{ bindCountdown > 0 ? bindCountdown + 's' : '获取验证码' }}</button>
        </view>
        <view class="modal-btns">
          <button class="modal-btn cancel" @tap="showBindPhone = false">取消</button>
          <button class="modal-btn confirm" @tap="handleBindPhone">确定</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { bindPhone, sendSmsCode as sendSmsCodeApi } from '@/api/auth'
import { uploadFile } from '@/api/family'

const userStore = useUserStore()

const showNickNameInput = ref(false)
const editNickName = ref('')
const showSexPicker = ref(false)
const editSex = ref('0')
const showBindPhone = ref(false)
const bindPhoneNum = ref('')
const bindCode = ref('')
const bindCountdown = ref(0)
let bindCountdownTimer: ReturnType<typeof setInterval> | null = null

const sexLabel = computed(() => {
  const sex = userStore.userInfo?.sex
  if (sex === '0') return '男'
  if (sex === '1') return '女'
  if (sex === '2') return '未知'
  return '未设置'
})

function handleAvatarChange() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    success: async (res) => {
      const filePath = res.tempFilePaths[0]
      uni.showLoading({ title: '上传中...' })
      try {
        const uploadRes = await uploadFile(filePath)
        await userStore.updateProfile({ avatar: uploadRes.data })
        uni.showToast({ title: '头像已更新', icon: 'success' })
      } catch {
        uni.showToast({ title: '上传失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    },
  })
}

// 昵称编辑
;(() => {
  const init = () => { editNickName.value = userStore.nickName }
  // 初始化
  init()
})()

function handleUpdateNickName() {
  if (!editNickName.value.trim()) {
    uni.showToast({ title: '请输入昵称', icon: 'none' })
    return
  }
  uni.showLoading({ title: '保存中...' })
  userStore.updateProfile({ nickName: editNickName.value.trim() })
    .then(() => {
      showNickNameInput.value = false
      uni.showToast({ title: '昵称已更新', icon: 'success' })
    })
    .finally(() => uni.hideLoading())
}

function handleUpdateSex() {
  uni.showLoading({ title: '保存中...' })
  userStore.updateProfile({ sex: editSex.value })
    .then(() => {
      showSexPicker.value = false
      uni.showToast({ title: '性别已更新', icon: 'success' })
    })
    .finally(() => uni.hideLoading())
}

async function handleSendBindCode() {
  if (!bindPhoneNum.value || bindPhoneNum.value.length !== 11) {
    uni.showToast({ title: '请输入正确手机号', icon: 'none' })
    return
  }
  try {
    const res = await sendSmsCodeApi(bindPhoneNum.value)
    uni.showToast({ title: '验证码已发送', icon: 'success' })
    if (res.data?.code) {
      bindCode.value = res.data.code
    }
    bindCountdown.value = 60
    bindCountdownTimer = setInterval(() => {
      bindCountdown.value--
      if (bindCountdown.value <= 0 && bindCountdownTimer) {
        clearInterval(bindCountdownTimer)
        bindCountdownTimer = null
      }
    }, 1000)
  } catch {
    // 错误由 request 拦截器处理
  }
}

async function handleBindPhone() {
  if (!bindPhoneNum.value || bindPhoneNum.value.length !== 11) {
    uni.showToast({ title: '请输入正确手机号', icon: 'none' })
    return
  }
  try {
    await bindPhone(bindPhoneNum.value, bindCode.value)
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
  position: relative;
}

.avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: #e8e8e8;
}

.avatar-edit-hint {
  position: absolute;
  bottom: 30rpx;
  right: calc(50% - 80rpx);
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
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

.code-group {
  display: flex;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.code-input {
  flex: 1;
  margin-bottom: 0;
}

.code-btn {
  font-size: 24rpx;
  color: #2c3e50;
  background: #f5f5f5;
  padding: 20rpx 16rpx;
  border-radius: 12rpx;
  border: none;
  white-space: nowrap;
}

.code-btn[disabled] {
  color: #ccc;
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

.sex-options {
  display: flex;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.sex-option {
  flex: 1;
  text-align: center;
  padding: 20rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #666;
}

.sex-option.active {
  background: #2c3e50;
  color: #fff;
}
</style>
