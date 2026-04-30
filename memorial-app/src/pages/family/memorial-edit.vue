<template>
  <view class="page">
    <!-- 基本信息 -->
    <view class="section">
      <text class="section-title">基本信息</text>

      <view class="form-item">
        <text class="form-label"><text class="required">*</text>姓名</text>
        <input class="form-input" v-model="form.name" placeholder="请输入逝者姓名" />
      </view>

      <view class="form-item">
        <text class="form-label"><text class="required">*</text>性别</text>
        <picker :range="genderOptions" :value="genderIndex" @change="onGenderChange">
          <view class="form-picker">
            <text :class="form.gender ? '' : 'placeholder'">{{ form.gender ? (form.gender === '0' ? '男' : '女') : '请选择' }}</text>
            <uni-icons type="right" size="16" color="#ccc" />
          </view>
        </picker>
      </view>

      <view class="form-item">
        <text class="form-label"><text class="required">*</text>出生日期</text>
        <picker mode="date" :value="form.birthDate" @change="onBirthDateChange">
          <view class="form-picker">
            <text :class="form.birthDate ? '' : 'placeholder'">{{ form.birthDate || '请选择' }}</text>
            <uni-icons type="right" size="16" color="#ccc" />
          </view>
        </picker>
      </view>

      <view class="form-item">
        <text class="form-label"><text class="required">*</text>逝世日期</text>
        <picker mode="date" :value="form.deathDate" @change="onDeathDateChange">
          <view class="form-picker">
            <text :class="form.deathDate ? '' : 'placeholder'">{{ form.deathDate || '请选择' }}</text>
            <uni-icons type="right" size="16" color="#ccc" />
          </view>
        </picker>
      </view>
    </view>

    <!-- 墓位信息 -->
    <view class="section">
      <text class="section-title">墓位信息（选填）</text>

      <view class="form-item">
        <text class="form-label">墓区</text>
        <input class="form-input" v-model="form.cemeteryArea" placeholder="请输入墓区" />
      </view>

      <view class="form-item">
        <text class="form-label">墓位号</text>
        <input class="form-input" v-model="form.cemeteryNumber" placeholder="请输入墓位号" />
      </view>
    </view>

    <!-- 纪念信息 -->
    <view class="section">
      <text class="section-title">纪念信息</text>

      <view class="form-item">
        <text class="form-label">封面图片</text>
        <ImageUploader v-model="form.coverImage" />
      </view>

      <view class="form-item vertical">
        <text class="form-label">生平简介</text>
        <view class="bio-toolbar">
          <view class="toolbar-btn" @tap="formatBio('bold')">
            <text class="toolbar-icon bold">B</text>
          </view>
          <view class="toolbar-btn" @tap="formatBio('italic')">
            <text class="toolbar-icon italic">I</text>
          </view>
          <view class="toolbar-btn" @tap="formatBio('insertLineBreak')">
            <text class="toolbar-icon">↵</text>
          </view>
        </view>
        <editor
          id="bioEditor"
          class="bio-editor"
          placeholder="请输入生平简介"
          @input="onBioInput"
          @ready="onEditorReady"
        />
      </view>
    </view>

    <!-- 隐私设置（编辑模式） -->
    <view v-if="isEdit" class="section">
      <text class="section-title">隐私设置</text>

      <view class="form-item">
        <text class="form-label">公开纪念馆</text>
        <switch :checked="form.isPublic === '0'" @change="onPublicChange" color="#2c3e50" />
      </view>

      <view class="form-item">
        <text class="form-label">允许留言</text>
        <switch :checked="form.allowMessage === '0'" @change="onAllowMessageChange" color="#2c3e50" />
      </view>

      <view class="form-item">
        <text class="form-label">留言需审核</text>
        <switch :checked="form.messageAudit === '1'" @change="onMessageAuditChange" color="#2c3e50" />
      </view>
    </view>

    <!-- 提交按钮 -->
    <view class="submit-section">
      <button class="submit-btn" @tap="handleSubmit">{{ isEdit ? '保存修改' : '创建纪念馆' }}</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getFamilyMemorial, createMemorial, updateMemorial } from '@/api/family'
import ImageUploader from '@/components/ImageUploader.vue'

const isEdit = ref(false)
const deceasedId = ref(0)
const genderOptions = ['男', '女']
const genderIndex = ref(0)
const editorCtx = ref<any>(null)
const bioContent = ref('')

const form = ref({
  name: '',
  gender: '',
  birthDate: '',
  deathDate: '',
  cemeteryArea: '',
  cemeteryNumber: '',
  bio: '',
  coverImage: '',
  isPublic: '0',
  allowMessage: '0',
  messageAudit: '1',
})

onLoad(async (options) => {
  if (options?.deceasedId) {
    isEdit.value = true
    deceasedId.value = Number(options.deceasedId)
    uni.showLoading({ title: '加载中...' })
    try {
      const res = await getFamilyMemorial(deceasedId.value)
      const d = res.data
      form.value = {
        name: d.name || '',
        gender: d.gender || '',
        birthDate: d.birthDate || '',
        deathDate: d.deathDate || '',
        cemeteryArea: d.cemeteryArea || '',
        cemeteryNumber: d.cemeteryNumber || '',
        bio: d.bio || '',
        coverImage: d.coverImage || '',
        isPublic: d.isPublic ?? '0',
        allowMessage: d.allowMessage ?? '0',
        messageAudit: d.messageAudit ?? '1',
      }
      genderIndex.value = d.gender === '1' ? 1 : 0
    } finally {
      uni.hideLoading()
    }
  }
})

function onEditorReady() {
  uni.createSelectorQuery()
    .select('#bioEditor')
    .context((res: any) => {
      editorCtx.value = res.context
      if (form.value.bio) {
        editorCtx.value.setContents({ html: form.value.bio })
      }
    })
    .exec()
}

function formatBio(name: string) {
  editorCtx.value?.format(name)
}

function onBioInput(e: any) {
  bioContent.value = e.detail.html
}

function onGenderChange(e: any) {
  genderIndex.value = e.detail.value
  form.value.gender = e.detail.value === 0 ? '0' : '1'
}

function onBirthDateChange(e: any) {
  form.value.birthDate = e.detail.value
}

function onDeathDateChange(e: any) {
  form.value.deathDate = e.detail.value
}

function onPublicChange(e: any) {
  form.value.isPublic = e.detail.value ? '0' : '1'
}

function onAllowMessageChange(e: any) {
  form.value.allowMessage = e.detail.value ? '0' : '1'
}

function onMessageAuditChange(e: any) {
  form.value.messageAudit = e.detail.value ? '1' : '0'
}

function validate(): boolean {
  if (!form.value.name.trim()) {
    uni.showToast({ title: '请输入姓名', icon: 'none' })
    return false
  }
  if (!form.value.gender) {
    uni.showToast({ title: '请选择性别', icon: 'none' })
    return false
  }
  if (!form.value.birthDate) {
    uni.showToast({ title: '请选择出生日期', icon: 'none' })
    return false
  }
  if (!form.value.deathDate) {
    uni.showToast({ title: '请选择逝世日期', icon: 'none' })
    return false
  }
  if (form.value.birthDate > form.value.deathDate) {
    uni.showToast({ title: '出生日期不能晚于逝世日期', icon: 'none' })
    return false
  }
  return true
}

async function handleSubmit() {
  if (!validate()) return

  if (bioContent.value) {
    form.value.bio = bioContent.value
  }

  uni.showLoading({ title: isEdit.value ? '保存中...' : '创建中...' })
  try {
    if (isEdit.value) {
      await updateMemorial({
        deceasedId: deceasedId.value,
        ...form.value,
      })
      uni.showToast({ title: '保存成功', icon: 'success' })
    } else {
      await createMemorial(form.value)
      uni.showToast({ title: '创建成功', icon: 'success' })
    }
    setTimeout(() => {
      uni.navigateBack({ delta: 1 })
    }, 1500)
  } catch {
    // 错误由 request 拦截器处理
  } finally {
    uni.hideLoading()
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 140rpx;
}

.section {
  background: #fff;
  margin: 20rpx;
  padding: 30rpx;
  border-radius: 16rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 24rpx;
  display: block;
}

.form-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}

.form-item:last-child {
  border-bottom: none;
}

.form-item.vertical {
  flex-direction: column;
  align-items: flex-start;
}

.form-label {
  font-size: 28rpx;
  color: #333;
  flex-shrink: 0;
  width: 180rpx;
}

.required {
  color: #dd524d;
  margin-right: 4rpx;
}

.form-input {
  flex: 1;
  font-size: 28rpx;
  text-align: right;
  padding: 8rpx 0;
}

.form-picker {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 28rpx;
  color: #333;
}

.placeholder {
  color: #ccc;
}

.form-textarea {
  width: 100%;
  font-size: 28rpx;
  padding: 16rpx 0;
  height: 200rpx;
  margin-top: 12rpx;
}

.bio-toolbar {
  display: flex;
  gap: 16rpx;
  margin-top: 12rpx;
  padding: 12rpx 0;
}

.toolbar-btn {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 8rpx;
}

.toolbar-btn:active {
  background: #e0e0e0;
}

.toolbar-icon {
  font-size: 28rpx;
  font-weight: 700;
  color: #333;
}

.toolbar-icon.italic {
  font-style: italic;
}

.bio-editor {
  width: 100%;
  min-height: 300rpx;
  font-size: 28rpx;
  padding: 16rpx 0;
  line-height: 1.8;
}

.submit-section {
  padding: 0 30rpx;
  margin-top: 30rpx;
}

.submit-btn {
  background: linear-gradient(135deg, #2c3e50, #3a5a7c);
  color: #fff;
  font-size: 32rpx;
  padding: 24rpx;
  border-radius: 48rpx;
  border: none;
  text-align: center;
}
</style>
