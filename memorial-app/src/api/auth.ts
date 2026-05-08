import { get, post, put } from './request'
import type { AjaxResult } from '@/types/api'
import type { LoginResult, UserInfo } from '@/types/user'

/** 微信小程序登录 */
export function wxMiniLogin(code: string) {
  return post<LoginResult>('/api/wxLogin', { code }, false)
}

/** 发送短信验证码 */
export function sendSmsCode(phone: string) {
  return post<{ code: string }>('/api/sms/send', { phone }, false)
}

/** 手机号登录 */
export function phoneLogin(phone: string, smsCode: string) {
  return post<LoginResult>('/api/phoneLogin', { phone, code: smsCode }, false)
}

/** 获取当前用户信息 */
export function getUserInfo() {
  return get<UserInfo>('/api/userInfo')
}

/** 绑定手机号 */
export function bindPhone(phone: string, smsCode: string) {
  return post<void>('/api/bindPhone', { phone, code: smsCode })
}

/** 退出登录 */
export function logout() {
  return post<void>('/logout')
}

/** 更新个人信息 */
export function updateProfile(data: { nickName?: string; avatar?: string; sex?: string }) {
  return put('/api/family/profile', data)
}
