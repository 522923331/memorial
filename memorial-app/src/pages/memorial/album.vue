<template>
  <view class="page">
    <NavBar title="相册" :show-back="true" @back="goBack" />
    <view class="album-section">
      <AlbumGrid :albums="albums" :columns="3" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useMemorialStore } from '@/stores/memorial'
import NavBar from '@/components/NavBar.vue'
import AlbumGrid from '@/components/AlbumGrid.vue'

const memorialStore = useMemorialStore()
const albums = computed(() => memorialStore.albums)

function goBack() {
  uni.navigateBack({ delta: 1 })
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}

.album-section {
  padding: 20rpx;
}
</style>
