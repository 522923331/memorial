import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getMemorialByCode, getMessages, getFlowers, submitMessage, submitFlower } from '@/api/memorial'
import { setStorage, getStorage, STORAGE_KEYS } from '@/utils/storage'
import type {
  Deceased,
  DeceasedAlbum,
  DeceasedVideo,
  Message,
  Flower,
  RecentVisit,
} from '@/types/memorial'

export const useMemorialStore = defineStore('memorial', () => {
  const currentDeceased = ref<Deceased | null>(null)
  const albums = ref<DeceasedAlbum[]>([])
  const videos = ref<DeceasedVideo[]>([])
  const messages = ref<Message[]>([])
  const flowers = ref<Flower[]>([])
  const totalVisit = ref(0)
  const messageCount = ref(0)
  const flowerCount = ref(0)
  const loading = ref(false)
  const recentVisits = ref<RecentVisit[]>(
    getStorage<RecentVisit[]>(STORAGE_KEYS.RECENT_VISITS, []),
  )

  async function loadMemorialPage(code: string) {
    loading.value = true
    try {
      const res = await getMemorialByCode(code)
      const data = res.data
      currentDeceased.value = data.deceased
      albums.value = data.albums || []
      videos.value = data.videos || []
      messages.value = data.messages || []
      flowers.value = data.flowers || []
      totalVisit.value = data.totalVisit || 0
      messageCount.value = data.messageCount || 0
      flowerCount.value = data.flowerCount || 0
      addRecentVisit(data.deceased)
    } finally {
      loading.value = false
    }
  }

  async function loadMessages() {
    if (!currentDeceased.value) return
    const res = await getMessages(currentDeceased.value.deceasedId)
    messages.value = res.data || []
  }

  async function loadFlowers() {
    if (!currentDeceased.value) return
    const res = await getFlowers(currentDeceased.value.deceasedId)
    flowers.value = res.data || []
  }

  async function sendMessage(content: string, visitorName: string) {
    if (!currentDeceased.value) return
    await submitMessage({
      deceasedId: currentDeceased.value.deceasedId,
      content,
      visitorName,
    })
    await loadMessages()
    messageCount.value++
  }

  async function sendFlower(flowerType: number, visitorName: string) {
    if (!currentDeceased.value) return
    await submitFlower({
      deceasedId: currentDeceased.value.deceasedId,
      flowerType,
      visitorName,
    })
    await loadFlowers()
    flowerCount.value++
  }

  function addRecentVisit(deceased: Deceased) {
    const visit: RecentVisit = {
      deceasedId: deceased.deceasedId,
      name: deceased.name,
      coverImage: deceased.coverImage,
      qrcodeCode: deceased.qrcodeCode,
      visitTime: Date.now(),
    }
    // 去重，已存在的移到最前面
    const list = recentVisits.value.filter((v) => v.deceasedId !== deceased.deceasedId)
    list.unshift(visit)
    // 最多保留 20 条
    recentVisits.value = list.slice(0, 20)
    setStorage(STORAGE_KEYS.RECENT_VISITS, recentVisits.value)
  }

  function clearMemorial() {
    currentDeceased.value = null
    albums.value = []
    videos.value = []
    messages.value = []
    flowers.value = []
    totalVisit.value = 0
    messageCount.value = 0
    flowerCount.value = 0
  }

  return {
    currentDeceased,
    albums,
    videos,
    messages,
    flowers,
    totalVisit,
    messageCount,
    flowerCount,
    loading,
    recentVisits,
    loadMemorialPage,
    loadMessages,
    loadFlowers,
    sendMessage,
    sendFlower,
    addRecentVisit,
    clearMemorial,
  }
})
