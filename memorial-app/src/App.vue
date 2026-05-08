<script setup lang="ts">
import { onLaunch, onShow } from '@dcloudio/uni-app'
import { useMemorialStore } from '@/stores/memorial'

onLaunch((options) => {
  // 处理通过扫码启动小程序的场景
  // 小程序码跳转路径格式: pages/memorial/detail?code=xxx
  if (options?.query?.code) {
    uni.navigateTo({
      url: `/pages/memorial/detail?code=${options.query.code}`,
    })
  }
})

onShow(() => {
  console.log('App Show')
})
</script>

<style>
page {
  background-color: #f5f5f5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  font-size: 28rpx;
  color: #333;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
