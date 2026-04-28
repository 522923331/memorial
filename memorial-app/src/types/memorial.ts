export interface Deceased {
  deceasedId: number
  orgId: number
  familyUserId: number
  name: string
  gender: string
  birthDate: string
  deathDate: string
  cemeteryArea: string
  cemeteryNumber: string
  bio: string
  coverImage: string
  qrcodeCode: string
  qrcodeUrl: string
  isPublic: string
  allowMessage: string
  messageAudit: string
  status: string
  delFlag: string
  orgName: string
  totalVisit: number
  messageCount: number
  flowerCount: number
  createTime: string
}

export interface DeceasedAlbum {
  albumId: number
  deceasedId: number
  imageUrl: string
  thumbnailUrl: string
  description: string
  sortOrder: number
  createTime: string
}

export interface DeceasedVideo {
  videoId: number
  deceasedId: number
  title: string
  videoUrl: string
  coverUrl: string
  description: string
  sortOrder: number
  createTime: string
}

export interface Message {
  messageId: number
  deceasedId: number
  visitorName: string
  visitorPhone: string
  relation: string
  content: string
  isAudited: string
  ipAddress: string
  deceasedName: string
  createTime: string
}

export interface Flower {
  flowerId: number
  deceasedId: number
  visitorName: string
  flowerType: number
  message: string
  ipAddress: string
  deceasedName: string
  createTime: string
}

export interface MemorialPageData {
  deceased: Deceased
  albums: DeceasedAlbum[]
  videos: DeceasedVideo[]
  messages: Message[]
  flowers: Flower[]
  totalVisit: number
  messageCount: number
  flowerCount: number
}

export interface RecentVisit {
  deceasedId: number
  name: string
  coverImage: string
  qrcodeCode: string
  visitTime: number
}
