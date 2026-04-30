<template>
  <view class="page">
    <!-- 纪念馆名称 -->
    <view class="header">
      <text class="header-name">{{ memorialName }}</text>
      <text class="header-count">共 {{ albums.length }} 张照片</text>
    </view>

    <!-- 操作栏 -->
    <view class="toolbar">
      <button class="upload-btn" @tap="handleUpload">
        <uni-icons type="plusempty" size="18" color="#2c3e50" />
        <text>上传照片</text>
      </button>
    </view>

    <!-- 照片网格 -->
    <view v-if="albums.length === 0" class="empty-tip">
      <text>暂无照片，点击上方按钮上传</text>
    </view>
    <view v-else class="photo-grid">
      <view v-for="item in albums" :key="item.albumId" class="photo-item">
        <image
          class="photo-img"
          :src="item.imageUrl"
          mode="aspectFill"
          @tap="previewImage(item.imageUrl)"
        />
        <view class="photo-delete" @tap="handleDelete(item)">
          <uni-icons type="clear" size="22" color="#fff" />
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useFamilyStore } from '@/stores/family'
import { uploadAlbumPhoto, deleteAlbumPhoto } from '@/api/family'
import { getFamilyMemorial } from '@/api/family'
import type { DeceasedAlbum } from '@/types/memorial'

const familyStore = useFamilyStore()
const albums = ref<DeceasedAlbum[]>([])
const deceasedId = ref(0)
const memorialName = ref('')

onLoad(async (options) => {
  if (options?.deceasedId) {
    deceasedId.value = Number(options.deceasedId)
    memorialName.value = decodeURIComponent(options.name || '')
    await loadAlbums()
  }
})

async function loadAlbums() {
  try {
    const res = await getFamilyMemorial(deceasedId.value)
    albums.value = (res as any).albums || []
  } catch {
    // error handled by interceptor
  }
}

function handleUpload() {
  uni.chooseImage({
    count: 9,
    sizeType: ['compressed'],
    success: async (res) => {
      uni.showLoading({ title: '上传中...' })
      try {
        for (const filePath of res.tempFilePaths) {
          await uploadAlbumPhoto(deceasedId.value, filePath)
        }
        await loadAlbums()
        uni.showToast({ title: '上传成功', icon: 'success' })
      } catch {
        uni.showToast({ title: '部分上传失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    },
  })
}

function handleDelete(item: DeceasedAlbum) {
  uni.showModal({
    title: '提示',
    content: '确定删除该照片？',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '删除中...' })
        try {
          await deleteAlbumPhoto(item.albumId)
          albums.value = albums.value.filter((a) => a.albumId !== item.albumId)
          uni.showToast({ title: '已删除', icon: 'success' })
        } catch {
          uni.showToast({ title: '删除失败', icon: 'none' })
        } finally {
          uni.hideLoading()
        }
      }
    },
  })
}

function previewImage(url: string) {
  const urls = albums.value.map((a) => a.imageUrl)
  const current = url
  uni.previewImage({ urls, current })
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}

.header {
  background: #fff;
  padding: 24rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.header-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  display: block;
}

.header-count {
  font-size: 24rpx;
  color: #999;
  margin-top: 4rpx;
  display: block;
}

.toolbar {
  padding: 20rpx 30rpx;
}

.upload-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  background: #fff;
  border: 2rpx solid #2c3e50;
  color: #2c3e50;
  font-size: 28rpx;
  padding: 16rpx;
  border-radius: 12rpx;
  line-height: 1;
}

.empty-tip {
  text-align: center;
  padding: 100rpx 0;
  color: #999;
  font-size: 28rpx;
}

.photo-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8rpx;
  padding: 0 20rpx;
}

.photo-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 8rpx;
  overflow: hidden;
}

.photo-img {
  width: 100%;
  height: 100%;
}

.photo-delete {
  position: absolute;
  top: 4rpx;
  right: 4rpx;
  width: 44rpx;
  height: 44rpx;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
