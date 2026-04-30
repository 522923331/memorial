<template>
  <view class="page">
    <!-- 纪念馆信息 -->
    <view class="memorial-header">
      <image
        class="memorial-cover"
        :src="deceased.coverImage || '/static/images/default-avatar.png'"
        mode="aspectFill"
      />
      <view class="memorial-info">
        <text class="memorial-name">{{ memorialName }}</text>
        <text class="memorial-date">{{ deceased.birthDate }} - {{ deceased.deathDate }}</text>
      </view>
    </view>

    <!-- 二维码区域 -->
    <view class="qrcode-section">
      <view v-if="qrcodeUrl" class="qrcode-wrapper">
        <image
          class="qrcode-image"
          :src="fullQrcodeUrl"
          mode="aspectFit"
          @longpress="handleLongPress"
        />
        <text class="qrcode-hint">长按保存二维码图片</text>
      </view>
      <view v-else class="qrcode-empty">
        <text>暂无二维码</text>
      </view>

      <view class="qrcode-code">
        <text class="code-label">编码：{{ deceased.qrcodeCode }}</text>
        <view class="copy-btn" @tap="copyCode">
          <uni-icons type="compose" size="14" color="#2c3e50" />
          <text>复制</text>
        </view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="actions">
      <button class="action-btn save" @tap="handleSaveToAlbum">保存到相册</button>
      <button class="action-btn regenerate" @tap="handleRegenerate">重新生成二维码</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getFamilyMemorial, regenerateQrcode } from '@/api/family'
import type { Deceased } from '@/types/memorial'

const deceased = ref<Partial<Deceased>>({})
const memorialName = ref('')
const deceasedId = ref(0)

const qrcodeUrl = computed(() => deceased.value.qrcodeUrl || '')
const fullQrcodeUrl = computed(() => {
  if (!qrcodeUrl.value) return ''
  // 如果已经是完整URL则直接返回，否则拼接基础路径
  if (qrcodeUrl.value.startsWith('http')) return qrcodeUrl.value
  const baseUrl = import.meta.env.VITE_API_BASE_URL || ''
  return baseUrl + qrcodeUrl.value
})

onLoad(async (options) => {
  if (options?.deceasedId) {
    deceasedId.value = Number(options.deceasedId)
    memorialName.value = decodeURIComponent(options.name || '')
    uni.showLoading({ title: '加载中...' })
    try {
      const res = await getFamilyMemorial(deceasedId.value)
      deceased.value = res.data
    } finally {
      uni.hideLoading()
    }
  }
})

function copyCode() {
  uni.setClipboardData({
    data: deceased.value.qrcodeCode || '',
    success: () => {
      uni.showToast({ title: '已复制', icon: 'success' })
    },
  })
}

function handleSaveToAlbum() {
  if (!fullQrcodeUrl.value) {
    uni.showToast({ title: '暂无二维码', icon: 'none' })
    return
  }

  // #ifdef H5
  uni.showToast({ title: '请长按图片保存', icon: 'none' })
  return
  // #endif

  // #ifndef H5
  uni.downloadFile({
    url: fullQrcodeUrl.value,
    success: (res) => {
      if (res.statusCode === 200) {
        uni.saveImageToPhotosAlbum({
          filePath: res.tempFilePath,
          success: () => {
            uni.showToast({ title: '已保存到相册', icon: 'success' })
          },
          fail: () => {
            uni.showToast({ title: '保存失败，请检查相册权限', icon: 'none' })
          },
        })
      }
    },
    fail: () => {
      uni.showToast({ title: '下载失败', icon: 'none' })
    },
  })
  // #endif
}

function handleLongPress() {
  // H5端长按可通过浏览器自带功能保存
}

async function handleRegenerate() {
  uni.showModal({
    title: '提示',
    content: '重新生成后，旧二维码将失效，确定继续？',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '生成中...' })
        try {
          const result = await regenerateQrcode(deceasedId.value)
          deceased.value.qrcodeUrl = result.data.qrcodeUrl
          deceased.value.qrcodeCode = result.data.qrcodeCode
          uni.showToast({ title: '已重新生成', icon: 'success' })
        } catch {
          uni.showToast({ title: '生成失败', icon: 'none' })
        } finally {
          uni.hideLoading()
        }
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

.memorial-header {
  display: flex;
  align-items: center;
  background: #fff;
  padding: 30rpx;
  gap: 24rpx;
}

.memorial-cover {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: #e8e8e8;
  flex-shrink: 0;
}

.memorial-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  display: block;
}

.memorial-date {
  font-size: 24rpx;
  color: #999;
  margin-top: 4rpx;
  display: block;
}

.qrcode-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 30rpx;
  background: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
}

.qrcode-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.qrcode-image {
  width: 400rpx;
  height: 400rpx;
  border: 2rpx solid #eee;
  border-radius: 12rpx;
}

.qrcode-hint {
  font-size: 24rpx;
  color: #999;
  margin-top: 16rpx;
}

.qrcode-empty {
  padding: 80rpx 0;
  color: #999;
  font-size: 28rpx;
}

.qrcode-code {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 30rpx;
  padding: 16rpx 24rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
}

.code-label {
  font-size: 26rpx;
  color: #666;
  font-family: monospace;
}

.copy-btn {
  display: flex;
  align-items: center;
  gap: 4rpx;
  font-size: 24rpx;
  color: #2c3e50;
}

.actions {
  padding: 30rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.action-btn {
  font-size: 30rpx;
  padding: 24rpx;
  border-radius: 48rpx;
  border: none;
  text-align: center;
}

.action-btn.save {
  background: linear-gradient(135deg, #2c3e50, #3a5a7c);
  color: #fff;
}

.action-btn.regenerate {
  background: #fff;
  color: #666;
  border: 1rpx solid #ddd;
}
</style>
