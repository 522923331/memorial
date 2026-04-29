<template>
  <view class="page">
    <NavBar title="留言寄语" :show-back="true" @back="goBack" />

    <!-- 留言列表 -->
    <view class="message-list">
      <view v-if="messages.length === 0" class="empty-tip">
        <text>暂无留言，成为第一个留言的人</text>
      </view>
      <MessageItem
        v-for="msg in messages"
        :key="msg.messageId"
        :message="msg"
      />
    </view>

    <!-- 发表留言 -->
    <view class="input-bar">
      <view class="input-form">
        <input
          class="author-input"
          v-model="authorName"
          placeholder="您的姓名"
        />
        <textarea
          class="content-input"
          v-model="content"
          placeholder="写下您的思念..."
          maxlength="500"
          :auto-height="false"
        />
        <view class="input-footer">
          <text class="char-count">{{ content.length }}/500</text>
          <button
            class="submit-btn"
            :disabled="!content.trim()"
            @tap="handleSubmit"
          >提交</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useMemorialStore } from '@/stores/memorial'
import { useUserStore } from '@/stores/user'
import NavBar from '@/components/NavBar.vue'
import MessageItem from '@/components/MessageItem.vue'

const memorialStore = useMemorialStore()
const userStore = useUserStore()

const deceasedId = ref(0)
const authorName = ref('')
const content = ref('')
const messages = computed(() => memorialStore.messages)

onLoad((options) => {
  if (options?.deceasedId) {
    deceasedId.value = Number(options.deceasedId)
  }
  authorName.value = userStore.nickName || ''
  memorialStore.loadMessages()
})

function goBack() {
  uni.navigateBack({ delta: 1 })
}

async function handleSubmit() {
  if (!content.value.trim()) return
  try {
    await memorialStore.sendMessage(content.value.trim(), authorName.value.trim() || '匿名')
    uni.showToast({ title: '留言已提交', icon: 'success' })
    content.value = ''
  } catch {
    // 错误由 request 拦截器处理
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 400rpx;
}

.message-list {
  padding: 20rpx 30rpx;
}

.empty-tip {
  text-align: center;
  padding: 60rpx 0;
  color: #999;
  font-size: 28rpx;
}

.input-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -2rpx 12rpx rgba(0,0,0,0.06);
}

.input-form {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.author-input {
  font-size: 28rpx;
  padding: 16rpx 20rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
}

.content-input {
  font-size: 28rpx;
  padding: 16rpx 20rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  height: 160rpx;
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.char-count {
  font-size: 24rpx;
  color: #ccc;
}

.submit-btn {
  background: #2c3e50;
  color: #fff;
  font-size: 28rpx;
  padding: 16rpx 40rpx;
  border-radius: 36rpx;
  border: none;
  line-height: 1;
}

.submit-btn[disabled] {
  opacity: 0.5;
}
</style>
