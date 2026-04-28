import { getToken } from '@/utils/auth'
import type { AjaxResult } from '@/types/api'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || ''
const TIMEOUT = 15000

interface RequestConfig {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: any
  header?: Record<string, string>
  needToken?: boolean
  loading?: boolean
}

function request<T = any>(config: RequestConfig): Promise<AjaxResult<T>> {
  const {
    url,
    method = 'GET',
    data,
    header = {},
    needToken = true,
    loading = false,
  } = config

  if (needToken) {
    const token = getToken()
    if (token) {
      header['Authorization'] = 'Bearer ' + token
    }
  }

  if (loading) {
    uni.showLoading({ title: '加载中...', mask: true })
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
      method,
      data,
      header: { 'Content-Type': 'application/json', ...header },
      timeout: TIMEOUT,
      success: (res) => {
        if (loading) uni.hideLoading()
        const result = res.data as AjaxResult<T>
        if (result.code === 200) {
          resolve(result)
        } else if (result.code === 401) {
          uni.removeStorageSync('memorial_token')
          uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
          setTimeout(() => {
            uni.navigateTo({ url: '/pages/login/index' })
          }, 1500)
          reject(new Error('unauthorized'))
        } else {
          uni.showToast({ title: result.msg || '请求失败', icon: 'none' })
          reject(new Error(result.msg))
        }
      },
      fail: (err) => {
        if (loading) uni.hideLoading()
        uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
        reject(err)
      },
    })
  })
}

export function get<T = any>(url: string, data?: any, needToken = true): Promise<AjaxResult<T>> {
  return request<T>({ url, method: 'GET', data, needToken })
}

export function post<T = any>(url: string, data?: any, needToken = true): Promise<AjaxResult<T>> {
  return request<T>({ url, method: 'POST', data, needToken })
}

export function put<T = any>(url: string, data?: any, needToken = true): Promise<AjaxResult<T>> {
  return request<T>({ url, method: 'PUT', data, needToken })
}

export function del<T = any>(url: string, data?: any, needToken = true): Promise<AjaxResult<T>> {
  return request<T>({ url, method: 'DELETE', data, needToken })
}

export function upload<T = any>(
  url: string,
  filePath: string,
  name = 'file',
  formData?: Record<string, any>,
): Promise<AjaxResult<T>> {
  const token = getToken()
  const header: Record<string, string> = {}
  if (token) {
    header['Authorization'] = 'Bearer ' + token
  }

  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: BASE_URL + url,
      filePath,
      name,
      formData,
      header,
      success: (res) => {
        const result = JSON.parse(res.data) as AjaxResult<T>
        if (result.code === 200) {
          resolve(result)
        } else {
          uni.showToast({ title: result.msg || '上传失败', icon: 'none' })
          reject(new Error(result.msg))
        }
      },
      fail: (err) => {
        uni.showToast({ title: '上传失败', icon: 'none' })
        reject(err)
      },
    })
  })
}
