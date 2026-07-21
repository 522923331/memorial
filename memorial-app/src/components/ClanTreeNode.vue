<template>
  <view class="clan-node">
    <view class="node-row">
      <!-- 本人 -->
      <view class="person" @tap="onTapMember(member)">
        <image
          class="avatar"
          :src="member.avatar || '/static/images/default-avatar.png'"
          mode="aspectFill"
        />
        <view class="person-info">
          <view class="name-row">
            <text class="name">{{ member.name || '未命名' }}</text>
            <text v-if="member.gender" class="gender">{{ genderText }}</text>
            <text v-if="member.isAlive === '0'" class="alive-tag">在世</text>
          </view>
          <text v-if="member.title" class="sub">字 {{ member.title }}</text>
          <text v-if="dates" class="sub">{{ dates }}</text>
        </view>
        <view
          v-if="member.hasMemorial && member.deceasedQrcodeCode"
          class="memorial-btn"
          @tap.stop="goMemorial(member)"
        >
          <uni-icons type="home" size="13" color="#fff" />
          <text class="memorial-text">纪念馆</text>
        </view>
      </view>
      <!-- 配偶 -->
      <view v-if="member.spouses && member.spouses.length" class="spouses">
        <view
          v-for="(sp, i) in member.spouses"
          :key="i"
          class="person spouse"
          @tap="onTapMember(sp)"
        >
          <image
            class="avatar sm"
            :src="sp.avatar || '/static/images/default-avatar.png'"
            mode="aspectFill"
          />
          <view class="person-info">
            <text class="name sm">{{ sp.name || '未命名' }}</text>
          </view>
          <text class="spouse-tag">配偶</text>
          <view
            v-if="sp.hasMemorial && sp.deceasedQrcodeCode"
            class="memorial-btn sm"
            @tap.stop="goMemorial(sp)"
          >
            <text class="memorial-text">馆</text>
          </view>
        </view>
      </view>
    </view>
    <!-- 子代递归 -->
    <view v-if="member.children && member.children.length" class="children">
      <ClanTreeNode
        v-for="child in member.children"
        :key="child.memberId"
        :member="child"
        @tap-member="(m) => emit('tapMember', m)"
        @go-memorial="(m) => emit('goMemorial', m)"
      />
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ClanMember } from '@/types/clan'

defineOptions({ name: 'ClanTreeNode' })

const props = defineProps<{ member: ClanMember }>()
const emit = defineEmits<{
  (e: 'tapMember', m: ClanMember): void
  (e: 'goMemorial', m: ClanMember): void
}>()

const genderText = computed(() => {
  if (props.member.gender === '0') return '男'
  if (props.member.gender === '1') return '女'
  return ''
})

const dates = computed(() => {
  const b = props.member.birthDate
  const d = props.member.deathDate
  if (!b && !d) return ''
  return `${b || '?'} - ${d || '今'}`
})

function onTapMember(m: ClanMember) {
  emit('tapMember', m)
}
function goMemorial(m: ClanMember) {
  emit('goMemorial', m)
}
</script>

<style scoped>
.clan-node {
  padding-left: 0;
}

.node-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12rpx;
  padding: 16rpx 0;
}

.person {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 12rpx;
  padding: 12rpx 16rpx;
  gap: 12rpx;
  box-shadow: 0 1rpx 4rpx rgba(0, 0, 0, 0.05);
}

.person:active {
  background: #f5f5f5;
}

.avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: #e8e8e8;
  flex-shrink: 0;
}

.avatar.sm {
  width: 56rpx;
  height: 56rpx;
}

.person-info {
  display: flex;
  flex-direction: column;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.name {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
}

.name.sm {
  font-size: 26rpx;
}

.gender {
  font-size: 22rpx;
  color: #999;
}

.alive-tag {
  font-size: 18rpx;
  color: #27ae60;
  border: 1rpx solid #27ae60;
  border-radius: 6rpx;
  padding: 0 6rpx;
}

.sub {
  font-size: 22rpx;
  color: #999;
  margin-top: 4rpx;
}

.memorial-btn {
  display: flex;
  align-items: center;
  gap: 4rpx;
  background: linear-gradient(135deg, #2c3e50, #3a5a7c);
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
}

.memorial-btn.sm {
  padding: 2rpx 10rpx;
}

.memorial-text {
  font-size: 20rpx;
  color: #fff;
}

.spouses {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.spouse {
  background: #f8f8f8;
}

.spouse-tag {
  font-size: 20rpx;
  color: #e74c3c;
}

.children {
  margin-left: 40rpx;
  border-left: 2rpx solid #e0e0e0;
  padding-left: 20rpx;
  margin-top: 4rpx;
}
</style>
