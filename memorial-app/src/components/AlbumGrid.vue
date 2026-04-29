<template>
  <view v-if="albums.length === 0" class="empty-tip">
    <text>暂无相册</text>
  </view>
  <view v-else class="album-grid" :style="{ gridTemplateColumns: `repeat(${columns}, 1fr)` }">
    <image
      v-for="(img, idx) in albums"
      :key="img.albumId"
      class="album-img"
      :src="img.imageUrl"
      mode="aspectFill"
      @tap="previewImage(idx)"
    />
  </view>
</template>

<script setup lang="ts">
import type { DeceasedAlbum } from '@/types/memorial'

const props = withDefaults(defineProps<{
  albums: DeceasedAlbum[]
  columns?: number
}>(), {
  columns: 3,
})

function previewImage(index: number) {
  const urls = props.albums.map((a) => a.imageUrl)
  uni.previewImage({
    current: index,
    urls,
    indicator: 'number',
  })
}
</script>

<style scoped>
.album-grid {
  display: grid;
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
  padding: 60rpx 0;
  color: #999;
  font-size: 28rpx;
}
</style>
