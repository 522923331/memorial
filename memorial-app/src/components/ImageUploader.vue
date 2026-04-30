<template>
  <view class="image-uploader" @tap="chooseImage">
    <image
      v-if="modelValue"
      class="preview"
      :src="modelValue"
      mode="aspectFill"
    />
    <view v-else class="placeholder">
      <uni-icons type="camera" size="32" color="#ccc" />
      <text class="placeholder-text">上传图片</text>
    </view>
    <view v-if="modelValue" class="remove-btn" @tap.stop="$emit('update:modelValue', '')">
      <uni-icons type="clear" size="20" color="#fff" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { uploadFile } from '@/api/family'

defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

async function chooseImage() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    success: async (res) => {
      let filePath = res.tempFilePaths[0]
      // #ifdef MP-WEIXIN
      try {
        filePath = await new Promise<string>((resolve) => {
          uni.cropImage({
            src: filePath,
            cropScale: '1:1',
            success: (cropRes: any) => resolve(cropRes.tempFilePath),
            fail: () => resolve(filePath),
          })
        })
      } catch {
        // cropImage not supported, use original
      }
      // #endif
      uni.showLoading({ title: '上传中...' })
      try {
        const uploadRes = await uploadFile(filePath)
        emit('update:modelValue', uploadRes.data)
      } catch {
        uni.showToast({ title: '上传失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    },
  })
}
</script>

<style scoped>
.image-uploader {
  width: 200rpx;
  height: 200rpx;
  border-radius: 12rpx;
  overflow: hidden;
  position: relative;
  background: #f5f5f5;
  border: 2rpx dashed #ddd;
}

.preview {
  width: 100%;
  height: 100%;
}

.placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.placeholder-text {
  font-size: 24rpx;
  color: #ccc;
  margin-top: 8rpx;
}

.remove-btn {
  position: absolute;
  top: 0;
  right: 0;
  width: 44rpx;
  height: 44rpx;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 0 0 0 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
