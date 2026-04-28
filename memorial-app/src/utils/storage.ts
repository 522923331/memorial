export function getStorage<T>(key: string, fallback: T): T {
  const value = uni.getStorageSync(key)
  return value !== '' && value !== undefined ? value : fallback
}

export function setStorage<T>(key: string, value: T): void {
  uni.setStorageSync(key, value)
}

export function removeStorage(key: string): void {
  uni.removeStorageSync(key)
}

export const STORAGE_KEYS = {
  TOKEN: 'memorial_token',
  USER_INFO: 'memorial_user_info',
  RECENT_VISITS: 'memorial_recent_visits',
} as const
