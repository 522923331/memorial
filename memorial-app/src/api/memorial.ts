import { get, post } from './request'
import type { AjaxResult } from '@/types/api'
import type { MemorialPageData, Message, Flower, Deceased } from '@/types/memorial'

/** 通过二维码编码获取纪念页数据 */
export function getMemorialByCode(code: string) {
  return get<MemorialPageData>(`/api/qrcode/${code}`, undefined, false)
}

/** 搜索逝者 */
export function searchDeceased(keyword: string) {
  return get<Deceased[]>('/api/search', { keyword }, false)
}

/** 获取逝者的已审核留言 */
export function getMessages(deceasedId: number) {
  return get<Message[]>(`/api/messages/${deceasedId}`, undefined, false)
}

/** 获取逝者的献花记录 */
export function getFlowers(deceasedId: number) {
  return get<Flower[]>(`/api/flowers/${deceasedId}`, undefined, false)
}

/** 提交留言 */
export function submitMessage(data: { deceasedId: number; content: string; visitorName: string }) {
  return post<void>('/api/message', data, false)
}

/** 提交献花 */
export function submitFlower(data: { deceasedId: number; flowerType: number; visitorName: string }) {
  return post<void>('/api/flower', data, false)
}
