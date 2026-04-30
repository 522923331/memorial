import { get, post, put, del, upload } from './request'
import type { AjaxResult } from '@/types/api'
import type { Deceased, DeceasedAlbum, DeceasedVideo, Message } from '@/types/memorial'

/** 获取我的纪念馆列表 */
export function getMyMemorials() {
  return get<Deceased[]>('/api/family/memorials')
}

/** 获取纪念馆详情（家属视角） */
export function getFamilyMemorial(deceasedId: number) {
  return get<Deceased>(`/api/family/memorial/${deceasedId}`)
}

/** 创建纪念馆 */
export function createMemorial(data: Partial<Deceased>) {
  return post<Deceased>('/api/family/memorial', data)
}

/** 更新纪念馆 */
export function updateMemorial(data: Partial<Deceased>) {
  return put<void>('/api/family/memorial', data)
}

/** 通用文件上传 */
export function uploadFile(filePath: string, formData?: Record<string, string>) {
  return upload<string>('/api/family/upload', filePath, 'file', formData)
}

/** 上传相册照片 */
export function uploadAlbumPhoto(deceasedId: number, filePath: string, formData?: Record<string, string>) {
  return upload<DeceasedAlbum>('/api/family/album/upload', filePath, 'file', {
    deceasedId: String(deceasedId),
    ...formData,
  })
}

/** 删除相册照片 */
export function deleteAlbumPhoto(albumId: number) {
  return del<void>(`/api/family/album/${albumId}`)
}

/** 更新相册描述 */
export function updateAlbum(data: Partial<DeceasedAlbum>) {
  return put<void>('/api/family/album', data)
}

/** 上传视频 */
export function uploadFamilyVideo(deceasedId: number, filePath: string, formData?: Record<string, string>) {
  return upload<DeceasedVideo>('/api/family/video/upload', filePath, 'file', {
    deceasedId: String(deceasedId),
    ...formData,
  })
}

/** 删除视频 */
export function deleteFamilyVideo(videoId: number) {
  return del<void>(`/api/family/video/${videoId}`)
}

/** 更新视频信息 */
export function updateFamilyVideo(data: Partial<DeceasedVideo>) {
  return put<void>('/api/family/video', data)
}

/** 获取待审核留言 */
export function getPendingMessages(deceasedId: number) {
  return get<Message[]>(`/api/family/messages/pending/${deceasedId}`)
}

/** 审核单条留言 */
export function auditMessage(messageId: number, isAudited: string) {
  return put<void>('/api/family/message/audit', { messageId, isAudited })
}

/** 批量审核留言 */
export function batchAuditMessages(status: string, messageIds: number[]) {
  return put<void>(`/api/family/message/batchAudit?status=${status}&messageIds=${messageIds.join(',')}`)
}

/** 重新生成二维码 */
export function regenerateQrcode(deceasedId: number) {
  return post<{ qrcodeUrl: string; qrcodeCode: string }>(`/api/family/memorial/qrcode/${deceasedId}`)
}
