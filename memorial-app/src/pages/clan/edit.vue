<template>
  <view class="page">
    <view class="form">
      <view class="form-item">
        <text class="label">族谱名 <text class="required">*</text></text>
        <input class="input" v-model="form.clanName" placeholder="如：张氏族谱" maxlength="100" />
      </view>
      <view class="form-item">
        <text class="label">姓氏</text>
        <input class="input" v-model="form.surname" placeholder="如：张" maxlength="20" />
      </view>
      <view class="form-item">
        <text class="label">封面</text>
        <view class="cover-picker" @tap="chooseCover">
          <image
            class="cover"
            :src="form.coverImage || '/static/images/default-avatar.png'"
            mode="aspectFill"
          />
          <text class="cover-tip">{{ form.coverImage ? '点击更换' : '点击选择封面' }}</text>
        </view>
      </view>
      <view class="form-item">
        <text class="label">简介</text>
        <textarea class="textarea" v-model="form.description" placeholder="族谱简介（选填）" maxlength="500" />
      </view>
      <view class="form-item row">
        <view class="row-label">
          <text class="label">公开族谱</text>
          <text class="row-tip">{{ form.isPublic === '0' ? '访客可浏览' : '仅族长可见' }}</text>
        </view>
        <switch :checked="form.isPublic === '0'" @change="onPublicChange" color="#2c3e50" />
      </view>
      <view class="form-item row">
        <view class="row-label">
          <text class="label">在世成员肖像对访客可见</text>
          <text class="row-tip">{{ form.showAliveAvatar === '0' ? '显示' : '隐藏' }}</text>
        </view>
        <switch :checked="form.showAliveAvatar === '0'" @change="onAvatarChange" color="#2c3e50" />
      </view>
    </view>

    <button class="submit-btn" :loading="submitting" @tap="submit">
      {{ isEdit ? '保存' : '创建族谱' }}
    </button>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { createClan, updateClan, getFamilyClan } from '@/api/clan'
import { uploadFile } from '@/api/family'
import type { Clan } from '@/types/clan'

const isEdit = ref(false)
const submitting = ref(false)
const clanId = ref<number>()
const form = reactive<Partial<Clan>>({
  clanName: '',
  surname: '',
  description: '',
  coverImage: '',
  isPublic: '0',
  showAliveAvatar: '0',
})

onLoad(async (options) => {
  if (options?.clanId) {
    isEdit.value = true
    clanId.value = Number(options.clanId)
    uni.setNavigationBarTitle({ title: '编辑族谱' })
    try {
      const res: any = await getFamilyClan(clanId.value)
      const clan: Clan = res.clan
      if (clan) {
        Object.assign(form, clan)
      }
    } catch {
      /* ignore */
    }
  }
})

function onPublicChange(e: any) {
  form.isPublic = e.detail.value ? '0' : '1'
}

function onAvatarChange(e: any) {
  form.showAliveAvatar = e.detail.value ? '0' : '1'
}

function chooseCover() {
  uni.chooseImage({
    count: 1,
    success: async (r) => {
      const path = r.tempFilePaths[0]
      uni.showLoading({ title: '上传中', mask: true })
      try {
        const res = await uploadFile(path)
        form.coverImage = res.data
      } finally {
        uni.hideLoading()
      }
    },
  })
}

async function submit() {
  if (!form.clanName?.trim()) {
    uni.showToast({ title: '请输入族谱名', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    if (isEdit.value && clanId.value) {
      await updateClan({ ...form, clanId: clanId.value })
      uni.showToast({ title: '保存成功', icon: 'success' })
    } else {
      await createClan(form)
      uni.showToast({ title: '创建成功', icon: 'success' })
    }
    setTimeout(() => uni.navigateBack(), 1000)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20rpx;
  padding-bottom: calc(160rpx + env(safe-area-inset-bottom));
}

.form {
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx 24rpx;
}

.form-item {
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}

.form-item:last-child {
  border-bottom: none;
}

.form-item.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.label {
  font-size: 28rpx;
  color: #333;
  display: block;
  margin-bottom: 16rpx;
}

.form-item.row .label {
  margin-bottom: 0;
}

.required {
  color: #e74c3c;
}

.row-label {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.row-tip {
  font-size: 22rpx;
  color: #999;
}

.input {
  font-size: 28rpx;
  padding: 12rpx 0;
  color: #333;
}

.textarea {
  width: 100%;
  font-size: 28rpx;
  padding: 12rpx 0;
  min-height: 120rpx;
  color: #333;
  box-sizing: border-box;
}

.cover-picker {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.cover {
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  background: #e8e8e8;
}

.cover-tip {
  font-size: 26rpx;
  color: #2c3e50;
}

.submit-btn {
  position: fixed;
  left: 30rpx;
  right: 30rpx;
  bottom: calc(40rpx + env(safe-area-inset-bottom));
  background: linear-gradient(135deg, #2c3e50, #3a5a7c);
  color: #fff;
  font-size: 32rpx;
  padding: 24rpx;
  border-radius: 48rpx;
  border: none;
}
</style>
