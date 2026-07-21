export interface Clan {
  clanId: number
  clanName: string
  surname: string
  familyUserId: number
  orgId: number
  rootMemberId?: number | null
  coverImage: string
  description?: string
  isPublic: string
  showAliveAvatar: string
  qrcodeCode: string
  qrcodeUrl: string
  memberCount: number
  generationCount: number
  status: string
  delFlag: string
  createTime: string
}

export interface ClanMember {
  memberId: number
  clanId: number
  name: string
  gender: string
  birthDate?: string | null
  deathDate?: string | null
  isAlive: string
  birthPlace?: string
  title?: string
  generation?: number
  avatar?: string
  bio?: string
  deceasedId?: number | null
  sortOrder?: number
  /** 是否已建纪念馆（buildTree 时填充） */
  hasMemorial?: boolean
  /** 关联逝者的二维码编码（hasMemorial 时填充，跳纪念馆用） */
  deceasedQrcodeCode?: string
  /** 配偶（浅拷贝，不递归子代） */
  spouses?: ClanMember[]
  /** 子代（递归） */
  children?: ClanMember[]
  createTime?: string
}

export interface ClanRelation {
  relationId: number
  clanId: number
  fromMemberId: number
  toMemberId: number
  relationType: number
  relationOrder: number
  extra?: string
  createTime: string
}

/** 族谱关系类型 */
export const CLAN_RELATION_TYPE = {
  FATHER: 1,
  MOTHER: 2,
  SPOUSE: 3,
  ADOPTIVE_FATHER: 4,
  ADOPTIVE_MOTHER: 5,
  STEPFATHER: 6,
  STEPMOTHER: 7,
} as const

/** 族谱详情聚合（扫码/浏览/家属详情接口返回） */
export interface ClanTreeData {
  clan: Clan
  root: ClanMember | null
}
