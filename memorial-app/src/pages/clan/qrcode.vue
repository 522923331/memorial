<template>
  <view class="page">
    <view class="qrcode-card">
      <text class="title">{{ name }}</text>
      <text class="subtitle">族谱二维码</text>
      <image
        v-if="qrcodeUrl"
        class="qrcode"
        :src="qrcodeUrl"
        mode="aspectFit"
        @tap="preview"
      />
      <view v-else class="qrcode-empty">
        <text>暂无二维码</text>
      </view>
      <view class="actions">
        <button class="btn" @tap="save">保存到相册</button>
        <button class="btn outline" :loading="regenerating" @tap="regenerate">重新生成</button>
      </view>
      <text class="tip">扫描二维码可访问族谱世系</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { regenerateClanQrcode } from '@/api/clan'

const clanId = ref<number>()
const name = ref('')
const qrcodeUrl = ref('')
const regenerating = ref(false)

onLoad((options) => {
  clanId.value = Number(options?.clanId)
  name.value = decodeURIComponent(options?.name || '')
  qrcodeUrl.value = decodeURIComponent(options?.url || '')
})

function preview() {
  if (qrcodeUrl.value) {
    uni.previewImage({ urls: [qrcodeUrl.value] })
  }
}

function save() {
  if (!qrcodeUrl.value) {
    uni.showToast({ title: '暂无二维码', icon: 'none' })
    return
  }
  uni.downloadFile({
    url: qrcodeUrl.value,
    success: (r) => {
      if (r.tempFilePath) {
        uni.saveImageToPhotosAlbum({
          filePath: r.tempFilePath,
          success: () => uni.showToast({ title: '已保存到相册', icon: 'success' }),
          fail: () => uni.showToast({ title: '保存失败，请授权相册权限', icon: 'none' }),
        })
      }
    },
    fail: () => uni.showToast({ title: '下载失败', icon: 'none' }),
  })
}

async function regenerate() {
  if (!clanId.value) return
  regenerating.value = true
  try {
    const res: any = await regenerateClanQrcode(clanId.value)
    qrcodeUrl.value = res.qrcodeUrl || ''
    uni.showToast({ title: '已生成新二维码', icon: 'success' })
  } finally {
    regenerating.value = false
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 40rpx 30rpx;
}

.qrcode-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 50rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.06);
}

.title {
  font-size: 36rpx;
  font-weight: 600;
  color: #333;
}

.subtitle {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
  margin-bottom: 30rpx;
}

.qrcode {
  width: 460rpx;
  height: 460rpx;
  background: #f8f8f8;
  border-radius: 12rpx;
}

.qrcode-empty {
  width: 460rpx;
  height: 460rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f8f8;
  border-radius: 12rpx;
  color: #999;
  font-size: 26rpx;
}

.actions {
  display: flex;
  gap: 20rpx;
  margin-top: 40rpx;
  width: 100%;
}

.btn {
  flex: 1;
  background: linear-gradient(135deg, #2c3e50, #3a5a7c);
  color: #fff;
  font-size: 28rpx;
  padding: 20rpx;
  border-radius: 48rpx;
  border: none;
}

.btn.outline {
  background: #fff;
  color: #2c3e50;
  border: 2rpx solid #2c3e50;
}

.tip {
  font-size: 22rpx;
  color: #999;
  margin-top: 24rpx;
}
</style>
