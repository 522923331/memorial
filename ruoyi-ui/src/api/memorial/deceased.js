import request from '@/utils/request'

// 查询逝者列表
export function listDeceased(query) {
  return request({
    url: '/memorial/deceased/list',
    method: 'get',
    params: query
  })
}

// 查询逝者详细
export function getDeceased(deceasedId) {
  return request({
    url: '/memorial/deceased/' + deceasedId,
    method: 'get'
  })
}

// 新增逝者
export function addDeceased(data) {
  return request({
    url: '/memorial/deceased',
    method: 'post',
    data: data
  })
}

// 修改逝者
export function updateDeceased(data) {
  return request({
    url: '/memorial/deceased',
    method: 'put',
    data: data
  })
}

// 删除逝者
export function delDeceased(deceasedIds) {
  return request({
    url: '/memorial/deceased/' + deceasedIds,
    method: 'delete'
  })
}

// 上传相册照片
export function uploadAlbum(data) {
  return request({
    url: '/memorial/deceased/album/upload',
    method: 'post',
    data: data
  })
}

// 删除相册照片
export function delAlbum(albumIds) {
  return request({
    url: '/memorial/deceased/album/' + albumIds,
    method: 'delete'
  })
}

// 根据家属查询逝者列表
export function listByFamily(familyUserId) {
  return request({
    url: '/memorial/deceased/listByFamily/' + familyUserId,
    method: 'get'
  })
}
