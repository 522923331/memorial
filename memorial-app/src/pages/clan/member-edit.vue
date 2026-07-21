<template>
  <view class="page">
    <view class="form">
      <view class="form-item">
        <text class="label">姓名 <text class="req">*</text></text>
        <input class="input" v-model="form.name" placeholder="成员姓名" maxlength="50" />
      </view>
      <view class="form-item">
        <text class="label">性别</text>
        <view class="gender-row">
          <view :class="['opt', form.gender === '0' && 'on']" @tap="form.gender = '0'">男</view>
          <view :class="['opt', form.gender === '1' && 'on']" @tap="form.gender = '1'">女</view>
          <view :class="['opt', form.gender === '2' && 'on']" @tap="form.gender = '2'">未知</view>
        </view>
      </view>
      <view class="form-item row">
        <text class="label">是否在世</text>
        <switch :checked="form.isAlive === '0'" color="#2c3e50" @change="onAliveChange" />
      </view>
      <view class="form-item">
        <text class="label">出生日期</text>
        <picker mode="date" :value="form.birthDate || ''" @change="(e: any) => (form.birthDate = e.detail.value)">
          <view class="picker">{{ form.birthDate || '请选择' }}</view>
        </picker>
      </view>
      <view v-if="form.isAlive === '1'" class="form-item">
        <text class="label">逝世日期</text>
        <picker mode="date" :value="form.deathDate || ''" @change="(e: any) => (form.deathDate = e.detail.value)">
          <view class="picker">{{ form.deathDate || '请选择' }}</view>
        </picker>
      </view>
      <view class="form-item">
        <text class="label">字号/辈分</text>
        <input class="input" v-model="form.title" placeholder="选填" maxlength="50" />
      </view>
      <view class="form-item">
        <text class="label">简介</text>
        <textarea class="textarea" v-model="form.bio" placeholder="选填" maxlength="500" />
      </view>
      <view class="form-item">
        <text class="label">肖像</text>
        <view class="avatar-picker" @tap="chooseAvatar">
          <image class="avatar" :src="form.avatar || '/static/images/default-avatar.png'" mode="aspectFill" />
          <text class="tip">{{ form.avatar ? '更换' : '选择' }}</text>
        </view>
      </view>

      <block v-if="isEdit">
        <view class="section-title">家族关系</view>
        <view class="form-item">
          <text class="label">父亲</text>
          <picker :range="parentOptions" range-key="label" :value="fatherIndex" @change="onFatherChange">
            <view class="picker">{{ fatherLabel }}</view>
          </picker>
        </view>
        <view class="form-item">
          <text class="label">母亲</text>
          <picker :range="parentOptions" range-key="label" :value="motherIndex" @change="onMotherChange">
            <view class="picker">{{ motherLabel }}</view>
          </picker>
        </view>
        <view class="form-item">
          <text class="label">配偶</text>
          <view v-if="spouses.length" class="spouse-list">
            <view v-for="sp in spouses" :key="sp.relationId" class="spouse-item">
              <text class="spouse-name">{{ sp.name }}</text>
              <text class="del-link" @tap="removeSpouse(sp)">删除</text>
            </view>
          </view>
          <picker
            v-if="spouseOptions.length"
            :range="spouseOptions"
            range-key="label"
            @change="addSpouse"
          >
            <view class="picker add">+ 添加配偶</view>
          </picker>
          <text v-else class="no-opt">无可添加的配偶</text>
        </view>
        <view class="form-item">
          <text class="label">关联纪念馆（仅已故可关联）</text>
          <picker
            v-if="memorialOptions.length"
            :range="memorialOptions"
            range-key="label"
            :value="memorialIndex"
            @change="onMemorialChange"
          >
            <view class="picker">{{ memorialLabel }}</view>
          </picker>
          <text v-else class="no-opt">暂无可关联的纪念馆</text>
        </view>
      </block>
      <view v-else class="hint">
        <uni-icons type="info" size="14" color="#999" />
        <text class="hint-text">添加后可在编辑页设置家族关系</text>
      </view>
    </view>

    <button class="submit-btn" :loading="submitting" @tap="submit">
      {{ isEdit ? '保存' : '添加成员' }}
    </button>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import {
  getClanMembers,
  addClanMember,
  updateClanMember,
  getMemberRelations,
  setMemberParents,
  addClanRelation,
  deleteClanRelation,
} from '@/api/clan'
import { getMyMemorials, uploadFile } from '@/api/family'
import type { ClanMember, ClanRelation } from '@/types/clan'
import type { Deceased } from '@/types/memorial'

const clanId = ref<number>()
const memberId = ref<number>()
const isEdit = ref(false)
const submitting = ref(false)

const form = reactive<Partial<ClanMember>>({
  name: '',
  gender: '0',
  isAlive: '1',
  birthDate: '',
  deathDate: '',
  title: '',
  bio: '',
  avatar: '',
  deceasedId: null,
})

const allMembers = ref<ClanMember[]>([])
const myMemorials = ref<Deceased[]>([])
const spouses = ref<{ relationId: number; toMemberId: number; name: string }[]>([])
const fatherId = ref<number | null>(null)
const motherId = ref<number | null>(null)

onLoad(async (options) => {
  clanId.value = Number(options?.clanId)
  if (options?.memberId) {
    isEdit.value = true
    memberId.value = Number(options.memberId)
    uni.setNavigationBarTitle({ title: '编辑成员' })
    await loadEditData()
  }
})

async function loadEditData() {
  const [memRes, relRes, myRes] = await Promise.all([
    getClanMembers(clanId.value!),
    getMemberRelations(memberId.value!),
    getMyMemorials(),
  ])
  allMembers.value = memRes.data || []
  myMemorials.value = myRes.data || []
  const relations: ClanRelation[] = relRes.data || []

  const me = allMembers.value.find((m) => m.memberId === memberId.value)
  if (me) Object.assign(form, me)

  const fRel = relations.find((r) => r.relationType === 1 && r.fromMemberId === memberId.value)
  const mRel = relations.find((r) => r.relationType === 2 && r.fromMemberId === memberId.value)
  fatherId.value = fRel ? fRel.toMemberId : null
  motherId.value = mRel ? mRel.toMemberId : null

  spouses.value = relations
    .filter((r) => r.relationType === 3 && (r.fromMemberId === memberId.value || r.toMemberId === memberId.value))
    .map((r) => {
      const otherId = r.fromMemberId === memberId.value ? r.toMemberId : r.fromMemberId
      const other = allMembers.value.find((m) => m.memberId === otherId)
      return { relationId: r.relationId, toMemberId: otherId, name: other?.name || '未命名' }
    })
}

function memberLabel(m: ClanMember) {
  return `${m.name || '未命名'}${m.title ? '·' + m.title : ''}（第${m.generation || 1}代）`
}

const parentOptions = computed(() => {
  const arr: { memberId: number; label: string }[] = [{ memberId: 0, label: '无' }]
  allMembers.value
    .filter((m) => m.memberId !== memberId.value)
    .forEach((m) => arr.push({ memberId: m.memberId, label: memberLabel(m) }))
  return arr
})

const fatherLabel = computed(() => {
  const f = allMembers.value.find((m) => m.memberId === fatherId.value)
  return f ? memberLabel(f) : '无'
})
const motherLabel = computed(() => {
  const m = allMembers.value.find((m) => m.memberId === motherId.value)
  return m ? memberLabel(m) : '无'
})
const fatherIndex = computed(() => {
  const i = parentOptions.value.findIndex((o) => o.memberId === fatherId.value)
  return i < 0 ? 0 : i
})
const motherIndex = computed(() => {
  const i = parentOptions.value.findIndex((o) => o.memberId === motherId.value)
  return i < 0 ? 0 : i
})

function onFatherChange(e: any) {
  fatherId.value = parentOptions.value[e.detail.value].memberId || null
}
function onMotherChange(e: any) {
  motherId.value = parentOptions.value[e.detail.value].memberId || null
}

const spouseOptions = computed(() => {
  const existIds = new Set(spouses.value.map((s) => s.toMemberId))
  return allMembers.value
    .filter((m) => m.memberId !== memberId.value && !existIds.has(m.memberId))
    .map((m) => ({ memberId: m.memberId, label: memberLabel(m) }))
})

async function addSpouse(e: any) {
  const opt = spouseOptions.value[e.detail.value]
  if (!opt || !memberId.value) return
  await addClanRelation({
    clanId: clanId.value,
    fromMemberId: memberId.value,
    toMemberId: opt.memberId,
    relationType: 3,
  })
  uni.showToast({ title: '已添加', icon: 'success' })
  await loadEditData()
}

async function removeSpouse(sp: { relationId: number }) {
  await deleteClanRelation(sp.relationId)
  uni.showToast({ title: '已删除', icon: 'success' })
  await loadEditData()
}

const memorialOptions = computed(() =>
  myMemorials.value.map((d) => ({ deceasedId: d.deceasedId, label: d.name })),
)
const memorialLabel = computed(() => {
  const d = myMemorials.value.find((m) => m.deceasedId === form.deceasedId)
  return d ? d.name : '未关联'
})
const memorialIndex = computed(() => {
  const i = memorialOptions.value.findIndex((o) => o.deceasedId === form.deceasedId)
  return i < 0 ? 0 : i
})
function onMemorialChange(e: any) {
  const opt = memorialOptions.value[e.detail.value]
  if (opt) form.deceasedId = opt.deceasedId
}

function onAliveChange(e: any) {
  form.isAlive = e.detail.value ? '0' : '1'
  if (form.isAlive === '0') {
    form.deathDate = ''
    form.deceasedId = null
  }
}

function chooseAvatar() {
  uni.chooseImage({
    count: 1,
    success: async (r) => {
      const path = r.tempFilePaths[0]
      uni.showLoading({ title: '上传中', mask: true })
      try {
        const res = await uploadFile(path)
        form.avatar = res.data
      } finally {
        uni.hideLoading()
      }
    },
  })
}

async function submit() {
  if (!form.name?.trim()) {
    uni.showToast({ title: '请输入姓名', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    if (isEdit.value && memberId.value) {
      await updateClanMember({ ...form, memberId: memberId.value, clanId: clanId.value })
      await setMemberParents(memberId.value, fatherId.value, motherId.value)
    } else {
      await addClanMember({ ...form, clanId: clanId.value })
    }
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1000)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20rpx;
  padding-bottom: calc(160rpx + env(safe-area-inset-bottom));
}

.form {
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx 24rpx;
}

.form-item {
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}

.form-item:last-child {
  border-bottom: none;
}

.form-item.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.label {
  font-size: 28rpx;
  color: #333;
  display: block;
  margin-bottom: 16rpx;
}

.form-item.row .label {
  margin-bottom: 0;
}

.req {
  color: #e74c3c;
}

.input {
  font-size: 28rpx;
  padding: 12rpx 0;
  color: #333;
}

.textarea {
  width: 100%;
  font-size: 28rpx;
  padding: 12rpx 0;
  min-height: 120rpx;
  color: #333;
  box-sizing: border-box;
}

.gender-row {
  display: flex;
  gap: 16rpx;
}

.opt {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  border: 2rpx solid #e0e0e0;
  border-radius: 12rpx;
  font-size: 26rpx;
  color: #666;
}

.opt.on {
  border-color: #2c3e50;
  color: #2c3e50;
  background: rgba(44, 62, 80, 0.05);
}

.picker {
  font-size: 28rpx;
  padding: 12rpx 0;
  color: #333;
}

.picker.add {
  color: #2c3e50;
}

.avatar-picker {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.avatar {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  background: #e8e8e8;
}

.tip {
  font-size: 26rpx;
  color: #2c3e50;
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #2c3e50;
  margin-top: 24rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid #f0f0f0;
}

.spouse-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  margin-bottom: 12rpx;
}

.spouse-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8f8f8;
  padding: 16rpx 20rpx;
  border-radius: 10rpx;
}

.spouse-name {
  font-size: 26rpx;
  color: #333;
}

.del-link {
  font-size: 24rpx;
  color: #e74c3c;
}

.no-opt {
  font-size: 24rpx;
  color: #bbb;
}

.hint {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 20rpx;
}

.hint-text {
  font-size: 24rpx;
  color: #999;
}

.submit-btn {
  position: fixed;
  left: 30rpx;
  right: 30rpx;
  bottom: calc(40rpx + env(safe-area-inset-bottom));
  background: linear-gradient(135deg, #2c3e50, #3a5a7c);
  color: #fff;
  font-size: 32rpx;
  padding: 24rpx;
  border-radius: 48rpx;
  border: none;
}
</style>
