<template>
  <view
    class="nav-bar"
    :style="{ paddingTop: statusBarHeight + 'px', background: bgColor }"
  >
    <view class="nav-bar-content">
      <view v-if="showBack" class="nav-back" @tap="handleBack">
        <uni-icons type="left" size="20" :color="titleColor" />
      </view>
      <text class="nav-title" :style="{ color: titleColor }">{{ title }}</text>
      <view class="nav-slot">
        <slot name="right" />
      </view>
    </view>
  </view>
  <!-- 占位，防止内容被导航栏遮挡 -->
  <view :style="{ height: (statusBarHeight + 44) + 'px' }" />
</template>

<script setup lang="ts">
import { ref } from 'vue'

withDefaults(defineProps<{
  title: string
  showBack?: boolean
  bgColor?: string
  titleColor?: string
}>(), {
  showBack: false,
  bgColor: 'linear-gradient(135deg, #2c3e50, #3a5a7c)',
  titleColor: '#fff',
})

const emit = defineEmits<{
  (e: 'back'): void
}>()

const sysInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(sysInfo.statusBarHeight || 0)

function handleBack() {
  emit('back')
}
</script>

<style scoped>
.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
}

.nav-bar-content {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.nav-back {
  position: absolute;
  left: 0;
  padding: 0 20rpx;
  height: 44px;
  display: flex;
  align-items: center;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
}

.nav-slot {
  position: absolute;
  right: 0;
  padding: 0 20rpx;
  height: 44px;
  display: flex;
  align-items: center;
}
</style>
