<template>
  <view class="page">
    <view v-if="members.length" class="member-list">
      <view v-for="m in members" :key="m.memberId" class="member-card" @tap="goEdit(m)">
        <image
          class="avatar"
          :src="m.avatar || '/static/images/default-avatar.png'"
          mode="aspectFill"
        />
        <view class="info">
          <view class="name-row">
            <text class="name">{{ m.name || '未命名' }}</text>
            <text v-if="m.gender" class="gender">{{ m.gender === '0' ? '男' : m.gender === '1' ? '女' : '' }}</text>
            <text v-if="m.isAlive === '0'" class="alive">在世</text>
            <text v-if="m.deceasedId" class="memorial-tag">已关联纪念馆</text>
          </view>
          <text class="meta">
            第 {{ m.generation }} 代
            <text v-if="m.birthDate || m.deathDate"> · {{ m.birthDate || '?' }} - {{ m.deathDate || '今' }}</text>
          </text>
        </view>
        <view class="del-btn" @tap.stop="onDelete(m)">
          <uni-icons type="trash" size="18" color="#e74c3c" />
        </view>
      </view>
    </view>

    <view v-if="!loading && members.length === 0" class="empty">
      <text class="empty-text">暂无成员</text>
      <text class="empty-tip">点击右下角按钮添加始祖</text>
    </view>

    <view class="fab" @tap="goAdd">
      <uni-icons type="plus" size="28" color="#fff" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getClanMembers, deleteClanMember } from '@/api/clan'
import type { ClanMember } from '@/types/clan'

const clanId = ref<number>()
const members = ref<ClanMember[]>([])
const loading = ref(false)

onLoad((options) => {
  clanId.value = Number(options?.clanId)
})

onShow(() => {
  if (clanId.value) load()
})

async function load() {
  loading.value = true
  try {
    const res = await getClanMembers(clanId.value!)
    members.value = res.data || []
  } finally {
    loading.value = false
  }
}

function goAdd() {
  uni.navigateTo({ url: `/pages/clan/member-edit?clanId=${clanId.value}` })
}

function goEdit(m: ClanMember) {
  uni.navigateTo({
    url: `/pages/clan/member-edit?clanId=${clanId.value}&memberId=${m.memberId}`,
  })
}

function onDelete(m: ClanMember) {
  uni.showModal({
    title: '提示',
    content: `确定删除成员「${m.name || '未命名'}」？其相关关系也会一并删除`,
    success: async (r) => {
      if (r.confirm) {
        await deleteClanMember(m.memberId)
        uni.showToast({ title: '已删除', icon: 'success' })
        load()
      }
    },
  })
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20rpx;
  padding-bottom: 160rpx;
}

.member-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.member-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
  gap: 20rpx;
  box-shadow: 0 1rpx 6rpx rgba(0, 0, 0, 0.05);
}

.member-card:active {
  background: #fafafa;
}

.avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  background: #e8e8e8;
  flex-shrink: 0;
}

.info {
  flex: 1;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 10rpx;
  flex-wrap: wrap;
}

.name {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
}

.gender {
  font-size: 22rpx;
  color: #999;
}

.alive {
  font-size: 18rpx;
  color: #27ae60;
  border: 1rpx solid #27ae60;
  border-radius: 6rpx;
  padding: 0 6rpx;
}

.memorial-tag {
  font-size: 18rpx;
  color: #2c3e50;
  border: 1rpx solid #2c3e50;
  border-radius: 6rpx;
  padding: 0 6rpx;
}

.meta {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
  display: block;
}

.del-btn {
  padding: 16rpx;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;
  gap: 12rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

.empty-tip {
  font-size: 24rpx;
  color: #bbb;
}

.fab {
  position: fixed;
  right: 40rpx;
  bottom: 180rpx;
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #2c3e50, #3a5a7c);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6rpx 20rpx rgba(44, 62, 80, 0.4);
  z-index: 50;
}
</style>
