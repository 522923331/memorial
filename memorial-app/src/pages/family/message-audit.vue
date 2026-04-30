<template>
  <view class="page">
    <!-- 纪念馆名称 -->
    <view class="header">
      <text class="header-name">{{ memorialName }}</text>
      <text class="header-count">待审核 {{ messages.length }} 条</text>
    </view>

    <!-- 批量操作栏 -->
    <view v-if="messages.length > 0" class="batch-bar">
      <view class="batch-left">
        <view class="select-all" @tap="toggleSelectAll">
          <view class="checkbox" :class="{ checked: isAllSelected }">
            <uni-icons v-if="isAllSelected" type="checkmarkempty" size="14" color="#fff" />
          </view>
          <text class="select-text">全选</text>
        </view>
        <text class="selected-count">已选 {{ selectedIds.size }} 条</text>
      </view>
      <view class="batch-actions">
        <button
          class="batch-btn approve"
          :disabled="selectedIds.size === 0"
          @tap="handleBatchApprove"
        >批量通过</button>
        <button
          class="batch-btn reject"
          :disabled="selectedIds.size === 0"
          @tap="handleBatchReject"
        >批量拒绝</button>
      </view>
    </view>

    <!-- 留言列表 -->
    <view v-if="messages.length === 0" class="empty-tip">
      <text>无待审核留言</text>
    </view>
    <view v-else class="message-list">
      <view
        v-for="msg in messages"
        :key="msg.messageId"
        class="message-card"
        :class="{ selected: selectedIds.has(msg.messageId) }"
        @tap="toggleSelect(msg.messageId)"
      >
        <view class="card-checkbox">
          <view class="checkbox" :class="{ checked: selectedIds.has(msg.messageId) }">
            <uni-icons v-if="selectedIds.has(msg.messageId)" type="checkmarkempty" size="14" color="#fff" />
          </view>
        </view>
        <view class="card-content">
          <view class="card-header">
            <text class="card-name">{{ msg.visitorName || '匿名' }}</text>
            <text v-if="msg.relation" class="card-relation">{{ msg.relation }}</text>
            <text class="card-time">{{ formatDate(msg.createTime) }}</text>
          </view>
          <text class="card-text">{{ msg.content }}</text>
          <view class="card-actions">
            <button class="action-btn approve" @tap.stop="handleApprove(msg.messageId)">通过</button>
            <button class="action-btn reject" @tap.stop="handleReject(msg.messageId)">拒绝</button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useFamilyStore } from '@/stores/family'
import { auditMessage, batchAuditMessages } from '@/api/family'

const familyStore = useFamilyStore()
const messages = computed(() => familyStore.pendingMessages)
const deceasedId = ref(0)
const memorialName = ref('')
const selectedIds = ref<Set<number>>(new Set())

const isAllSelected = computed(() =>
  messages.value.length > 0 && selectedIds.value.size === messages.value.length,
)

onLoad(async (options) => {
  if (options?.deceasedId) {
    deceasedId.value = Number(options.deceasedId)
    memorialName.value = decodeURIComponent(options.name || '')
    await familyStore.loadPendingMessages(deceasedId.value)
  }
})

function toggleSelect(id: number) {
  const newSet = new Set(selectedIds.value)
  if (newSet.has(id)) {
    newSet.delete(id)
  } else {
    newSet.add(id)
  }
  selectedIds.value = newSet
}

function toggleSelectAll() {
  if (isAllSelected.value) {
    selectedIds.value = new Set()
  } else {
    selectedIds.value = new Set(messages.value.map((m) => m.messageId))
  }
}

async function handleApprove(messageId: number) {
  uni.showLoading({ title: '处理中...' })
  try {
    await familyStore.approveMessage(messageId)
    selectedIds.value = new Set([...selectedIds.value].filter((id) => id !== messageId))
    uni.showToast({ title: '已通过', icon: 'success' })
  } finally {
    uni.hideLoading()
  }
}

async function handleReject(messageId: number) {
  uni.showLoading({ title: '处理中...' })
  try {
    await familyStore.rejectMessage(messageId)
    selectedIds.value = new Set([...selectedIds.value].filter((id) => id !== messageId))
    uni.showToast({ title: '已拒绝', icon: 'success' })
  } finally {
    uni.hideLoading()
  }
}

async function handleBatchApprove() {
  const ids = [...selectedIds.value]
  if (ids.length === 0) return
  uni.showModal({
    title: '提示',
    content: `确定通过选中的 ${ids.length} 条留言？`,
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '处理中...' })
        try {
          await familyStore.batchApprove(ids)
          selectedIds.value = new Set()
          uni.showToast({ title: '已批量通过', icon: 'success' })
        } finally {
          uni.hideLoading()
        }
      }
    },
  })
}

async function handleBatchReject() {
  const ids = [...selectedIds.value]
  if (ids.length === 0) return
  uni.showModal({
    title: '提示',
    content: `确定拒绝选中的 ${ids.length} 条留言？`,
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '处理中...' })
        try {
          await familyStore.batchReject(ids)
          selectedIds.value = new Set()
          uni.showToast({ title: '已批量拒绝', icon: 'success' })
        } finally {
          uni.hideLoading()
        }
      }
    },
  })
}

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}月${d.getDate()}日 ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
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

.batch-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 30rpx;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;
}

.batch-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.select-all {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.select-text {
  font-size: 26rpx;
  color: #333;
}

.selected-count {
  font-size: 24rpx;
  color: #999;
}

.batch-actions {
  display: flex;
  gap: 16rpx;
}

.batch-btn {
  font-size: 24rpx;
  padding: 8rpx 20rpx;
  border-radius: 8rpx;
  border: none;
  line-height: 1.4;
}

.batch-btn.approve {
  background: #e8f5e9;
  color: #2e7d32;
}

.batch-btn.reject {
  background: #fce4ec;
  color: #c62828;
}

.batch-btn[disabled] {
  opacity: 0.5;
}

.checkbox {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  border: 2rpx solid #ccc;
  display: flex;
  align-items: center;
  justify-content: center;
}

.checkbox.checked {
  background: #2c3e50;
  border-color: #2c3e50;
}

.empty-tip {
  text-align: center;
  padding: 100rpx 0;
  color: #999;
  font-size: 28rpx;
}

.message-list {
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.message-card {
  display: flex;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  gap: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.message-card.selected {
  background: #f0f4f8;
}

.card-checkbox {
  padding-top: 4rpx;
}

.card-content {
  flex: 1;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 12rpx;
}

.card-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
}

.card-relation {
  font-size: 22rpx;
  color: #2c3e50;
  background: rgba(44, 62, 80, 0.08);
  padding: 2rpx 12rpx;
  border-radius: 8rpx;
}

.card-time {
  font-size: 22rpx;
  color: #999;
  margin-left: auto;
}

.card-text {
  font-size: 28rpx;
  color: #555;
  line-height: 1.6;
  display: block;
  margin-bottom: 16rpx;
}

.card-actions {
  display: flex;
  gap: 16rpx;
  justify-content: flex-end;
}

.action-btn {
  font-size: 24rpx;
  padding: 8rpx 24rpx;
  border-radius: 8rpx;
  border: none;
  line-height: 1.4;
}

.action-btn.approve {
  background: #2c3e50;
  color: #fff;
}

.action-btn.reject {
  background: #fff;
  color: #dd524d;
  border: 1rpx solid #dd524d;
}
</style>
