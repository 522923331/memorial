import request from '@/utils/request'

// 查询族谱列表
export function listClan(query) {
  return request({
    url: '/memorial/clan/list',
    method: 'get',
    params: query
  })
}

// 查询族谱详情（含成员列表）
export function getClan(clanId) {
  return request({
    url: '/memorial/clan/' + clanId,
    method: 'get'
  })
}

// 查询族谱成员列表
export function listClanMembers(clanId) {
  return request({
    url: '/memorial/clan/members/' + clanId,
    method: 'get'
  })
}

// 新增族谱成员
export function addClanMember(data) {
  return request({
    url: '/memorial/clan/member',
    method: 'post',
    data: data
  })
}

// 修改族谱成员
export function updateClanMember(data) {
  return request({
    url: '/memorial/clan/member',
    method: 'put',
    data: data
  })
}

// 删除族谱成员
export function delClanMember(memberIds) {
  return request({
    url: '/memorial/clan/member/' + memberIds,
    method: 'delete'
  })
}

// 修改族谱（下架/恢复等）
export function updateClan(data) {
  return request({
    url: '/memorial/clan',
    method: 'put',
    data: data
  })
}

// 删除族谱
export function delClan(clanIds) {
  return request({
    url: '/memorial/clan/' + clanIds,
    method: 'delete'
  })
}
