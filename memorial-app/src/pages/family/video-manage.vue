<template>
  <view class="page">
    <!-- 纪念馆名称 -->
    <view class="header">
      <text class="header-name">{{ memorialName }}</text>
      <text class="header-count">共 {{ videos.length }} 个视频</text>
    </view>

    <!-- 操作栏 -->
    <view class="toolbar">
      <button class="upload-btn" @tap="handleUpload">
        <uni-icons type="plusempty" size="18" color="#2c3e50" />
        <text>上传视频</text>
      </button>
    </view>

    <!-- 视频列表 -->
    <view v-if="videos.length === 0" class="empty-tip">
      <text>暂无视频，点击上方按钮上传</text>
    </view>
    <view v-else class="video-list">
      <view v-for="item in videos" :key="item.videoId" class="video-card">
        <video
          class="video-player"
          :src="item.videoUrl"
          :poster="item.coverUrl"
          controls
          object-fit="contain"
        />
        <view class="video-info">
          <text class="video-title">{{ item.title || '纪念视频' }}</text>
          <view class="video-actions">
            <view class="action-btn" @tap="showEditModal(item)">
              <uni-icons type="compose" size="18" color="#666" />
              <text class="action-text">编辑</text>
            </view>
            <view class="action-btn delete" @tap="handleDelete(item)">
              <uni-icons type="trash" size="18" color="#dd524d" />
              <text class="action-text del-text">删除</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 编辑弹窗 -->
    <view v-if="editModalVisible" class="modal-mask" @tap="editModalVisible = false">
      <view class="modal-content" @tap.stop>
        <text class="modal-title">编辑视频信息</text>
        <view class="modal-form">
          <text class="modal-label">标题</text>
          <input class="modal-input" v-model="editForm.title" placeholder="视频标题" />
          <text class="modal-label">描述</text>
          <textarea
            class="modal-textarea"
            v-model="editForm.description"
            placeholder="视频描述（选填）"
            maxlength="200"
          />
        </view>
        <view class="modal-actions">
          <button class="modal-cancel" @tap="editModalVisible = false">取消</button>
          <button class="modal-confirm" @tap="handleEditSave">保存</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { uploadFamilyVideo, deleteFamilyVideo, updateFamilyVideo, getFamilyMemorial } from '@/api/family'
import type { DeceasedVideo } from '@/types/memorial'

const videos = ref<DeceasedVideo[]>([])
const deceasedId = ref(0)
const memorialName = ref('')
const editModalVisible = ref(false)
const editForm = ref({ videoId: 0, title: '', description: '' })

onLoad(async (options) => {
  if (options?.deceasedId) {
    deceasedId.value = Number(options.deceasedId)
    memorialName.value = decodeURIComponent(options.name || '')
    await loadVideos()
  }
})

async function loadVideos() {
  try {
    const res = await getFamilyMemorial(deceasedId.value)
    videos.value = (res as any).videos || []
  } catch {
    // error handled by interceptor
  }
}

function handleUpload() {
  uni.chooseVideo({
    maxDuration: 60,
    compressed: true,
    success: async (res) => {
      uni.showLoading({ title: '上传中...' })
      try {
        await uploadFamilyVideo(deceasedId.value, res.tempFilePath, {
          title: '纪念视频',
        })
        await loadVideos()
        uni.showToast({ title: '上传成功', icon: 'success' })
      } catch {
        uni.showToast({ title: '上传失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    },
  })
}

function handleDelete(item: DeceasedVideo) {
  uni.showModal({
    title: '提示',
    content: '确定删除该视频？',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '删除中...' })
        try {
          await deleteFamilyVideo(item.videoId)
          videos.value = videos.value.filter((v) => v.videoId !== item.videoId)
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

function showEditModal(item: DeceasedVideo) {
  editForm.value = {
    videoId: item.videoId,
    title: item.title || '',
    description: item.description || '',
  }
  editModalVisible.value = true
}

async function handleEditSave() {
  uni.showLoading({ title: '保存中...' })
  try {
    await updateFamilyVideo(editForm.value)
    const idx = videos.value.findIndex((v) => v.videoId === editForm.value.videoId)
    if (idx !== -1) {
      videos.value[idx] = {
        ...videos.value[idx],
        title: editForm.value.title,
        description: editForm.value.description,
      }
    }
    editModalVisible.value = false
    uni.showToast({ title: '保存成功', icon: 'success' })
  } catch {
    uni.showToast({ title: '保存失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
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

.video-list {
  padding: 0 20rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.video-card {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
}

.video-player {
  width: 100%;
  height: 420rpx;
}

.video-info {
  padding: 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.video-title {
  font-size: 28rpx;
  color: #333;
  flex: 1;
}

.video-actions {
  display: flex;
  gap: 24rpx;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.action-text {
  font-size: 24rpx;
  color: #666;
}

.del-text {
  color: #dd524d;
}

.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.modal-content {
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx;
  width: 600rpx;
}

.modal-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  display: block;
  margin-bottom: 30rpx;
}

.modal-label {
  font-size: 26rpx;
  color: #666;
  display: block;
  margin-bottom: 8rpx;
  margin-top: 20rpx;
}

.modal-input {
  font-size: 28rpx;
  padding: 16rpx 20rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
}

.modal-textarea {
  font-size: 28rpx;
  padding: 16rpx 20rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  height: 160rpx;
  width: 100%;
}

.modal-actions {
  display: flex;
  gap: 20rpx;
  margin-top: 30rpx;
}

.modal-cancel {
  flex: 1;
  background: #f5f5f5;
  color: #666;
  font-size: 28rpx;
  padding: 20rpx;
  border-radius: 12rpx;
  border: none;
  text-align: center;
}

.modal-confirm {
  flex: 1;
  background: #2c3e50;
  color: #fff;
  font-size: 28rpx;
  padding: 20rpx;
  border-radius: 12rpx;
  border: none;
  text-align: center;
}
</style>
