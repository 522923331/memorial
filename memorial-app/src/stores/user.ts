import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, setToken as saveToken, removeToken } from '@/utils/auth'
import { setStorage, getStorage, removeStorage, STORAGE_KEYS } from '@/utils/storage'
import { wxMiniLogin, phoneLogin, getUserInfo, logout as logoutApi, updateProfile as updateProfileApi } from '@/api/auth'
import { getMyMemorials } from '@/api/family'
import type { UserInfo } from '@/types/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const userInfo = ref<UserInfo | null>(getStorage<UserInfo | null>(STORAGE_KEYS.USER_INFO, null))
  const isLoggedIn = computed(() => !!token.value)
  const isFamilyMember = ref(false)

  const nickName = computed(() => userInfo.value?.nickName || '')
  const avatar = computed(() => userInfo.value?.avatar || '')
  const phone = computed(() => userInfo.value?.phonenumber || '')

  function setToken(val: string) {
    token.value = val
    saveToken(val)
  }

  async function wxLogin() {
    return new Promise<void>((resolve, reject) => {
      uni.login({
        provider: 'weixin',
        success: async (loginRes) => {
          try {
            const res = await wxMiniLogin(loginRes.code)
            setToken(res.data.token)
            await fetchUserInfo()
            await checkFamilyStatus()
            resolve()
          } catch (err) {
            reject(err)
          }
        },
        fail: (err) => {
          uni.showToast({ title: '微信登录失败', icon: 'none' })
          reject(err)
        },
      })
    })
  }

  async function loginByPhone(phoneNum: string, smsCode: string) {
    const res = await phoneLogin(phoneNum, smsCode)
    setToken(res.data.token)
    await fetchUserInfo()
    await checkFamilyStatus()
  }

  async function fetchUserInfo() {
    try {
      const res = await getUserInfo()
      userInfo.value = res.data
      setStorage(STORAGE_KEYS.USER_INFO, res.data)
    } catch {
      // token 无效时会被 request 拦截器处理
    }
  }

  async function checkFamilyStatus() {
    if (!isLoggedIn.value) {
      isFamilyMember.value = false
      return
    }
    try {
      const res = await getMyMemorials()
      isFamilyMember.value = (res.data?.length ?? 0) > 0
    } catch {
      isFamilyMember.value = false
    }
  }

  async function logout() {
    try {
      await logoutApi()
    } catch {
      // 忽略退出接口错误
    }
    token.value = ''
    userInfo.value = null
    isFamilyMember.value = false
    removeToken()
    removeStorage(STORAGE_KEYS.USER_INFO)
    uni.reLaunch({ url: '/pages/index/index' })
  }

  async function updateProfile(data: { nickName?: string; avatar?: string; sex?: string }) {
    const res = await updateProfileApi(data)
    const d = (res as any)
    if (userInfo.value) {
      userInfo.value = {
        ...userInfo.value,
        nickName: d.nickName ?? userInfo.value.nickName,
        avatar: d.avatar ?? userInfo.value.avatar,
        sex: d.sex ?? userInfo.value.sex,
      }
      setStorage(STORAGE_KEYS.USER_INFO, userInfo.value)
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isFamilyMember,
    nickName,
    avatar,
    phone,
    setToken,
    wxLogin,
    loginByPhone,
    fetchUserInfo,
    checkFamilyStatus,
    updateProfile,
    logout,
  }
})
