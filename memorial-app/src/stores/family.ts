import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  getMyMemorials,
  getFamilyMemorial,
  createMemorial,
  updateMemorial,
  uploadAlbumPhoto,
  deleteAlbumPhoto,
  uploadFamilyVideo,
  deleteFamilyVideo,
  updateFamilyVideo,
  getPendingMessages,
  auditMessage,
  batchAuditMessages,
} from '@/api/family'
import type { Deceased, DeceasedAlbum, DeceasedVideo, Message } from '@/types/memorial'

export const useFamilyStore = defineStore('family', () => {
  const memorials = ref<Deceased[]>([])
  const currentMemorial = ref<Deceased | null>(null)
  const albums = ref<DeceasedAlbum[]>([])
  const videos = ref<DeceasedVideo[]>([])
  const pendingMessages = ref<Message[]>([])
  const loading = ref(false)

  async function loadMyMemorials() {
    loading.value = true
    try {
      const res = await getMyMemorials()
      memorials.value = res.data || []
    } finally {
      loading.value = false
    }
  }

  async function loadMemorialDetail(deceasedId: number) {
    loading.value = true
    try {
      const res = await getFamilyMemorial(deceasedId)
      currentMemorial.value = res.data
      albums.value = (res as any).albums || []
      videos.value = (res as any).videos || []
    } finally {
      loading.value = false
    }
  }

  async function createMemorialForm(data: Partial<Deceased>) {
    const res = await createMemorial(data)
    await loadMyMemorials()
    return res.data
  }

  async function updateMemorialForm(data: Partial<Deceased>) {
    await updateMemorial(data)
    await loadMyMemorials()
  }

  async function uploadPhoto(deceasedId: number, filePath: string) {
    const res = await uploadAlbumPhoto(deceasedId, filePath)
    if (currentMemorial.value?.deceasedId === deceasedId) {
      albums.value.push(res.data)
    }
    return res.data
  }

  async function removePhoto(albumId: number) {
    await deleteAlbumPhoto(albumId)
    albums.value = albums.value.filter((a) => a.albumId !== albumId)
  }

  async function uploadVideo(deceasedId: number, filePath: string, formData?: Record<string, string>) {
    const res = await uploadFamilyVideo(deceasedId, filePath, formData)
    if (currentMemorial.value?.deceasedId === deceasedId) {
      videos.value.push(res.data)
    }
    return res.data
  }

  async function removeVideo(videoId: number) {
    await deleteFamilyVideo(videoId)
    videos.value = videos.value.filter((v) => v.videoId !== videoId)
  }

  async function editVideo(data: Partial<DeceasedVideo>) {
    await updateFamilyVideo(data)
    const idx = videos.value.findIndex((v) => v.videoId === data.videoId)
    if (idx !== -1 && data.videoId) {
      videos.value[idx] = { ...videos.value[idx], ...data } as DeceasedVideo
    }
  }

  async function loadPendingMessages(deceasedId: number) {
    loading.value = true
    try {
      const res = await getPendingMessages(deceasedId)
      pendingMessages.value = res.data || []
    } finally {
      loading.value = false
    }
  }

  async function approveMessage(messageId: number) {
    await auditMessage(messageId, '1')
    pendingMessages.value = pendingMessages.value.filter((m) => m.messageId !== messageId)
  }

  async function rejectMessage(messageId: number) {
    await auditMessage(messageId, '2')
    pendingMessages.value = pendingMessages.value.filter((m) => m.messageId !== messageId)
  }

  async function batchApprove(messageIds: number[]) {
    await batchAuditMessages('1', messageIds)
    const idSet = new Set(messageIds)
    pendingMessages.value = pendingMessages.value.filter((m) => !idSet.has(m.messageId))
  }

  async function batchReject(messageIds: number[]) {
    await batchAuditMessages('2', messageIds)
    const idSet = new Set(messageIds)
    pendingMessages.value = pendingMessages.value.filter((m) => !idSet.has(m.messageId))
  }

  function clearFamily() {
    memorials.value = []
    currentMemorial.value = null
    albums.value = []
    videos.value = []
    pendingMessages.value = []
  }

  return {
    memorials,
    currentMemorial,
    albums,
    videos,
    pendingMessages,
    loading,
    loadMyMemorials,
    loadMemorialDetail,
    createMemorialForm,
    updateMemorialForm,
    uploadPhoto,
    removePhoto,
    uploadVideo,
    removeVideo,
    editVideo,
    loadPendingMessages,
    approveMessage,
    rejectMessage,
    batchApprove,
    batchReject,
    clearFamily,
  }
})
