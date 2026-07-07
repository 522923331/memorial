<template>
  <view class="page">
    <!-- 基本信息 -->
    <view class="section">
      <text class="section-title">基本信息</text>

      <view class="form-item">
        <text class="form-label"><text class="required">*</text>姓名</text>
        <input class="form-input" v-model="form.name" placeholder="请输入逝者姓名" />
      </view>

      <view class="form-item" @tap="openGenderPicker">
        <text class="form-label"><text class="required">*</text>性别</text>
        <view class="form-picker">
          <text :class="form.gender ? '' : 'placeholder'">{{ genderLabel }}</text>
          <uni-icons type="right" size="16" color="#ccc" />
        </view>
      </view>

      <view class="form-item">
        <text class="form-label"><text class="required">*</text>出生日期</text>
        <picker mode="date" :value="form.birthDate" :start="dateStart" :end="dateEnd" @change="onBirthDateChange">
          <view class="form-picker">
            <text :class="form.birthDate ? '' : 'placeholder'">{{ form.birthDate || '请选择' }}</text>
            <uni-icons type="right" size="16" color="#ccc" />
          </view>
        </picker>
      </view>

      <view class="form-item">
        <text class="form-label"><text class="required">*</text>逝世日期</text>
        <picker mode="date" :value="form.deathDate" :start="dateStart" :end="dateEnd" @change="onDeathDateChange">
          <view class="form-picker">
            <text :class="form.deathDate ? '' : 'placeholder'">{{ form.deathDate || '请选择' }}</text>
            <uni-icons type="right" size="16" color="#ccc" />
          </view>
        </picker>
      </view>

      <view v-if="ageAtDeath !== null" class="form-item">
        <text class="form-label">享年</text>
        <text class="form-value">{{ ageAtDeath }} 岁</text>
      </view>
    </view>

    <!-- 墓位信息 -->
    <view class="section">
      <text class="section-title">墓位信息（选填）</text>

      <view class="form-item">
        <text class="form-label">墓区</text>
        <input class="form-input" v-model="form.cemeteryArea" placeholder="请输入墓区" />
      </view>

      <view class="form-item" @tap="chooseCemeteryLocation">
        <text class="form-label">地图定位</text>
        <view class="form-picker">
          <text :class="hasCemeteryLocation ? '' : 'placeholder'">
            {{ hasCemeteryLocation ? '已定位，点击修改' : '点击选择墓区位置' }}
          </text>
          <uni-icons type="location" size="16" color="#2c3e50" />
        </view>
      </view>

      <view v-if="hasCemeteryLocation" class="form-item vertical">
        <map
          class="cemetery-map"
          :latitude="form.cemeteryLatitude!"
          :longitude="form.cemeteryLongitude!"
          :markers="cemeteryMarkers"
          :scale="15"
          :show-location="false"
        />
        <view class="clear-location-btn" @tap.stop="clearCemeteryLocation">
          <uni-icons type="clear" size="14" color="#fff" />
          <text>清除定位</text>
        </view>
      </view>

      <view class="form-item">
        <text class="form-label">墓位号</text>
        <input class="form-input" v-model="form.cemeteryNumber" placeholder="请输入墓位号" />
      </view>

      <view class="form-item vertical">
        <text class="form-label">墓区照片</text>
        <ImageUploader v-model="form.cemeteryPhoto" />
      </view>
    </view>

    <!-- 纪念信息 -->
    <view class="section">
      <text class="section-title">纪念信息</text>

      <view class="form-item vertical">
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

      <view class="form-item vertical">
        <view class="label-row">
          <text class="form-label">立碑者</text>
          <text class="char-count">{{ (form.monumentEraser || '').length }}/2000</text>
        </view>
        <textarea
          class="form-textarea"
          v-model="form.monumentEraser"
          maxlength="2000"
          placeholder="选填，记录立碑者信息"
        />
      </view>
    </view>

    <!-- 隐私设置 -->
    <view class="section">
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

    <!-- 性别选择弹窗 -->
    <view v-if="showGenderPicker" class="modal-mask" @tap="showGenderPicker = false">
      <view class="modal-content" @tap.stop>
        <text class="modal-title">选择性别</text>
        <view class="sex-options">
          <view class="sex-option" :class="{ active: tempGender === '0' }" @tap="tempGender = '0'">
            <text>男</text>
          </view>
          <view class="sex-option" :class="{ active: tempGender === '1' }" @tap="tempGender = '1'">
            <text>女</text>
          </view>
          <view class="sex-option" :class="{ active: tempGender === '2' }" @tap="tempGender = '2'">
            <text>未知</text>
          </view>
        </view>
        <view class="modal-btns">
          <button class="modal-btn cancel" @tap="showGenderPicker = false">取消</button>
          <button class="modal-btn confirm" @tap="confirmGender">确定</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getFamilyMemorial, createMemorial, updateMemorial } from '@/api/family'
import ImageUploader from '@/components/ImageUploader.vue'

const isEdit = ref(false)
const deceasedId = ref(0)
const editorCtx = ref<any>(null)
const bioContent = ref('')

const showGenderPicker = ref(false)
const tempGender = ref('0')

const dateStart = '1900-01-01'
const dateEnd = `${new Date().getFullYear()}-12-31`

const form = ref({
  name: '',
  gender: '',
  birthDate: '',
  deathDate: '',
  cemeteryArea: '',
  cemeteryNumber: '',
  cemeteryLatitude: null as number | null,
  cemeteryLongitude: null as number | null,
  cemeteryPhoto: '',
  bio: '',
  monumentEraser: '',
  coverImage: '',
  isPublic: '0',
  allowMessage: '0',
  messageAudit: '1',
})

const genderLabel = computed(() => {
  const g = form.value.gender
  if (g === '0') return '男'
  if (g === '1') return '女'
  if (g === '2') return '未知'
  return '请选择'
})

const hasCemeteryLocation = computed(
  () => form.value.cemeteryLatitude != null && form.value.cemeteryLongitude != null,
)

const cemeteryMarkers = computed(() => {
  if (!hasCemeteryLocation.value) return []
  return [
    {
      id: 1,
      latitude: form.value.cemeteryLatitude,
      longitude: form.value.cemeteryLongitude,
      width: 30,
      height: 30,
    },
  ]
})

const ageAtDeath = computed(() => {
  if (!form.value.birthDate || !form.value.deathDate) return null
  const birth = new Date(form.value.birthDate)
  const death = new Date(form.value.deathDate)
  if (isNaN(birth.getTime()) || isNaN(death.getTime())) return null
  let age = death.getFullYear() - birth.getFullYear()
  const m = death.getMonth() - birth.getMonth()
  if (m < 0 || (m === 0 && death.getDate() < birth.getDate())) {
    age--
  }
  return age >= 0 ? age : null
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
        cemeteryLatitude: d.cemeteryLatitude ?? null,
        cemeteryLongitude: d.cemeteryLongitude ?? null,
        cemeteryPhoto: d.cemeteryPhoto || '',
        bio: d.bio || '',
        monumentEraser: d.monumentEraser || '',
        coverImage: d.coverImage || '',
        isPublic: d.isPublic ?? '0',
        allowMessage: d.allowMessage ?? '0',
        messageAudit: d.messageAudit ?? '1',
      }
    } finally {
      uni.hideLoading()
    }
  }
})

function openGenderPicker() {
  tempGender.value = form.value.gender || '0'
  showGenderPicker.value = true
}

function confirmGender() {
  form.value.gender = tempGender.value
  showGenderPicker.value = false
}

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

function onBirthDateChange(e: any) {
  form.value.birthDate = e.detail.value
}

function onDeathDateChange(e: any) {
  form.value.deathDate = e.detail.value
}

function chooseCemeteryLocation() {
  uni.chooseLocation({
    success: (res) => {
      form.value.cemeteryLatitude = res.latitude
      form.value.cemeteryLongitude = res.longitude
      if (res.name || res.address) {
        form.value.cemeteryArea = res.name || res.address
      }
    },
    fail: (err) => {
      // H5 无 key 或用户取消时，静默处理
      console.log('chooseLocation fail', err)
    },
  })
}

function clearCemeteryLocation() {
  form.value.cemeteryLatitude = null
  form.value.cemeteryLongitude = null
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

function readEditorContent(): Promise<string> {
  return new Promise((resolve) => {
    if (!editorCtx.value) {
      resolve(bioContent.value)
      return
    }
    try {
      editorCtx.value.getContents({
        success: (res: any) => resolve(res.html || ''),
        fail: () => resolve(bioContent.value),
      })
    } catch {
      resolve(bioContent.value)
    }
  })
}

async function handleSubmit() {
  if (!validate()) return

  // 同步读取 editor 内容，避免 @input 不可靠导致 bio 未保存
  const html = await readEditorContent()
  if (html) {
    form.value.bio = html
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

.label-row {
  display: flex;
  width: 100%;
  justify-content: space-between;
  align-items: center;
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

.form-value {
  flex: 1;
  font-size: 28rpx;
  color: #2c3e50;
  text-align: right;
  font-weight: 600;
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
  padding: 16rpx;
  height: 160rpx;
  margin-top: 12rpx;
  background: #f9f9f9;
  border-radius: 12rpx;
  box-sizing: border-box;
}

.char-count {
  font-size: 24rpx;
  color: #999;
}

.cemetery-map {
  width: 100%;
  height: 320rpx;
  border-radius: 12rpx;
  margin-top: 12rpx;
}

.clear-location-btn {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 22rpx;
  padding: 6rpx 16rpx;
  border-radius: 24rpx;
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
  border-radius: 20rpx;
  padding: 40rpx;
  width: 600rpx;
}

.modal-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 30rpx;
  display: block;
  text-align: center;
}

.sex-options {
  display: flex;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.sex-option {
  flex: 1;
  text-align: center;
  padding: 20rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #666;
}

.sex-option.active {
  background: #2c3e50;
  color: #fff;
}

.modal-btns {
  display: flex;
  gap: 20rpx;
  margin-top: 20rpx;
}

.modal-btn {
  flex: 1;
  font-size: 28rpx;
  padding: 20rpx;
  border-radius: 36rpx;
  border: none;
  text-align: center;
}

.modal-btn.cancel {
  background: #f5f5f5;
  color: #666;
}

.modal-btn.confirm {
  background: #2c3e50;
  color: #fff;
}
</style>
