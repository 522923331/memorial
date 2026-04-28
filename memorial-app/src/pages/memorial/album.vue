<template>
  <view class="page">
    <view v-if="albums.length === 0" class="empty-tip">
      <text>暂无相册</text>
    </view>
    <view v-else class="album-grid">
      <image
        v-for="(img, idx) in albums"
        :key="img.albumId"
        class="album-img"
        :src="img.imageUrl"
        mode="aspectFill"
        @tap="previewImage(idx)"
      />
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useMemorialStore } from '@/stores/memorial'

const memorialStore = useMemorialStore()
const albums = computed(() => memorialStore.albums)

function previewImage(index: number) {
  const urls = albums.value.map((a) => a.imageUrl)
  uni.previewImage({ current: index, urls, indicator: 'number' })
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20rpx;
}

.album-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12rpx;
}

.album-img {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 8rpx;
  background: #e8e8e8;
}

.empty-tip {
  text-align: center;
  padding: 100rpx 0;
  color: #999;
  font-size: 28rpx;
}
</style>
