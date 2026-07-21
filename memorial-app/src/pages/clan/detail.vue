<template>
  <view class="page">
    <view v-if="clan" class="header">
      <image
        class="cover"
        :src="clan.coverImage || '/static/images/default-avatar.png'"
        mode="aspectFill"
      />
      <view class="header-info">
        <view class="name-row">
          <text class="clan-name">{{ clan.clanName }}</text>
          <text v-if="clan.isPublic === '1'" class="private-tag">私密</text>
        </view>
        <text class="clan-meta">{{ clan.memberCount }}人 · {{ clan.generationCount }}代</text>
        <text v-if="clan.description" class="clan-desc">{{ clan.description }}</text>
      </view>
    </view>

    <view v-if="isOwner" class="owner-bar">
      <view class="owner-btn" @tap="goEdit">
        <uni-icons type="compose" size="16" color="#2c3e50" />
        <text>编辑</text>
      </view>
      <view class="owner-btn" @tap="goMembers">
        <uni-icons type="staff" size="16" color="#2c3e50" />
        <text>成员</text>
      </view>
      <view class="owner-btn" @tap="goQrcode">
        <uni-icons type="qr_code" size="16" color="#2c3e50" />
        <text>二维码</text>
      </view>
    </view>

    <view class="tree-section">
      <view v-if="!root" class="tree-empty">
        <text>{{ isOwner ? '暂无成员，请到成员管理添加' : '该族谱暂无成员' }}</text>
      </view>
      <ClanTreeNode
        v-else
        :member="root"
        @tap-member="onTapMember"
        @go-memorial="goMemorial"
      />
    </view>

    <view v-if="isOwner" class="tip-bar">
      <uni-icons type="info" size="14" color="#999" />
      <text class="tip-text">点击成员可编辑</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getClanByQrcode, getPublicClan } from '@/api/clan'
import { useUserStore } from '@/stores/user'
import type { Clan, ClanMember } from '@/types/clan'
import ClanTreeNode from '@/components/ClanTreeNode.vue'

const userStore = useUserStore()
const clan = ref<Clan | null>(null)
const root = ref<ClanMember | null>(null)

const isOwner = computed(() => {
  if (!clan.value) return false
  const uid = userStore.userInfo?.userId
  return !!uid && clan.value.familyUserId === uid
})

onLoad(async (options) => {
  if (options?.code) {
    await loadByQrcode(options.code)
  } else if (options?.clanId) {
    await loadById(Number(options.clanId))
  }
})

async function loadByQrcode(code: string) {
  const res: any = await getClanByQrcode(code)
  clan.value = res.clan
  root.value = res.root
  if (clan.value) {
    uni.setNavigationBarTitle({ title: clan.value.clanName })
  }
}

async function loadById(clanId: number) {
  const res: any = await getPublicClan(clanId)
  clan.value = res.clan
  root.value = res.root
  if (clan.value) {
    uni.setNavigationBarTitle({ title: clan.value.clanName })
  }
}

function onTapMember(m: ClanMember) {
  if (!isOwner.value || !m.memberId || !clan.value) return
  uni.navigateTo({
    url: `/pages/clan/member-edit?clanId=${clan.value.clanId}&memberId=${m.memberId}`,
  })
}

function goMemorial(m: ClanMember) {
  if (m.deceasedQrcodeCode) {
    uni.navigateTo({ url: `/pages/memorial/detail?code=${m.deceasedQrcodeCode}` })
  }
}

function goEdit() {
  if (clan.value) {
    uni.navigateTo({ url: `/pages/clan/edit?clanId=${clan.value.clanId}` })
  }
}

function goMembers() {
  if (clan.value) {
    uni.navigateTo({
      url: `/pages/clan/members?clanId=${clan.value.clanId}&name=${encodeURIComponent(clan.value.clanName)}`,
    })
  }
}

function goQrcode() {
  if (clan.value) {
    uni.navigateTo({
      url: `/pages/clan/qrcode?clanId=${clan.value.clanId}&name=${encodeURIComponent(clan.value.clanName)}&url=${encodeURIComponent(clan.value.qrcodeUrl)}`,
    })
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: calc(60rpx + env(safe-area-inset-bottom));
}

.header {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #2c3e50, #3a5a7c);
  padding: 40rpx 30rpx;
  gap: 24rpx;
}

.cover {
  width: 140rpx;
  height: 140rpx;
  border-radius: 16rpx;
  border: 2rpx solid rgba(255, 255, 255, 0.3);
  flex-shrink: 0;
}

.header-info {
  flex: 1;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.clan-name {
  font-size: 36rpx;
  font-weight: 600;
  color: #fff;
}

.private-tag {
  font-size: 20rpx;
  color: #fff;
  background: rgba(231, 76, 60, 0.9);
  padding: 2rpx 12rpx;
  border-radius: 6rpx;
}

.clan-meta {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 12rpx;
  display: block;
}

.clan-desc {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 8rpx;
  display: block;
}

.owner-bar {
  display: flex;
  background: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
  padding: 16rpx 0;
}

.owner-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
  font-size: 24rpx;
  color: #2c3e50;
}

.owner-btn:active {
  background: #f5f5f5;
}

.tree-section {
  background: #fff;
  margin: 0 20rpx;
  border-radius: 16rpx;
  padding: 24rpx;
}

.tree-empty {
  text-align: center;
  padding: 80rpx 0;
  color: #999;
  font-size: 26rpx;
}

.tip-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  margin-top: 20rpx;
}

.tip-text {
  font-size: 22rpx;
  color: #999;
}
</style>
