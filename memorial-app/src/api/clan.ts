import { get, post, put, del } from './request'
import type { Clan, ClanMember, ClanRelation } from '@/types/clan'

// ========== 公开接口（/api/clan/**，无需登录） ==========

/** 扫码进入族谱（聚合族谱信息 + 世系树） */
export function getClanByQrcode(code: string) {
  return get(`/api/clan/qrcode/${code}`)
}

/** 浏览公开族谱 */
export function getPublicClan(clanId: number) {
  return get(`/api/clan/${clanId}`)
}

/** 搜索公开族谱 */
export function searchPublicClan(keyword: string) {
  return get<Clan[]>('/api/clan/search', { keyword })
}

/** 按逝者ID查关联族谱（纪念馆->族谱入口） */
export function getClanByDeceased(deceasedId: number) {
  return get(`/api/clan/by-deceased/${deceasedId}`)
}

// ========== 家属接口（/api/family/clan/**，需token） ==========

/** 我的族谱列表 */
export function getMyClans() {
  return get<Clan[]>('/api/family/clans')
}

/** 族谱详情（家属视角，含完整世系树） */
export function getFamilyClan(clanId: number) {
  return get(`/api/family/clan/${clanId}`)
}

/** 创建族谱 */
export function createClan(data: Partial<Clan>) {
  return post('/api/family/clan', data)
}

/** 编辑族谱 */
export function updateClan(data: Partial<Clan>) {
  return put('/api/family/clan', data)
}

/** 删除族谱 */
export function deleteClan(clanId: number) {
  return del(`/api/family/clan/${clanId}`)
}

/** 重新生成族谱二维码 */
export function regenerateClanQrcode(clanId: number) {
  return post(`/api/family/clan/qrcode/${clanId}`)
}

/** 族谱成员列表 */
export function getClanMembers(clanId: number) {
  return get<ClanMember[]>(`/api/family/clan/${clanId}/members`)
}

/** 新增成员 */
export function addClanMember(data: Partial<ClanMember>) {
  return post('/api/family/clan/member', data)
}

/** 编辑成员 */
export function updateClanMember(data: Partial<ClanMember>) {
  return put('/api/family/clan/member', data)
}

/** 删除成员 */
export function deleteClanMember(memberId: number) {
  return del(`/api/family/clan/member/${memberId}`)
}

/** 设置某成员的生父生母（null 表示清除） */
export function setMemberParents(memberId: number, fatherId: number | null, motherId: number | null) {
  return put(`/api/family/clan/member/${memberId}/parents`, { fatherId, motherId })
}

/** 查询某成员的全部关系（父母/配偶等） */
export function getMemberRelations(memberId: number) {
  return get<ClanRelation[]>(`/api/family/clan/member/${memberId}/relations`)
}

/** 新增关系（配偶/养继父母等） */
export function addClanRelation(data: Partial<ClanRelation>) {
  return post('/api/family/clan/relation', data)
}

/** 删除关系 */
export function deleteClanRelation(relationId: number) {
  return del(`/api/family/clan/relation/${relationId}`)
}

/** 获取族谱世系树 */
export function getClanTree(clanId: number) {
  return get(`/api/family/clan/${clanId}/tree`)
}
