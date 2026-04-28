import request from '@/utils/request'

// 查询机构列表
export function listOrganization(query) {
  return request({
    url: '/memorial/organization/list',
    method: 'get',
    params: query
  })
}

// 查询机构详细
export function getOrganization(orgId) {
  return request({
    url: '/memorial/organization/' + orgId,
    method: 'get'
  })
}

// 新增机构
export function addOrganization(data) {
  return request({
    url: '/memorial/organization',
    method: 'post',
    data: data
  })
}

// 修改机构
export function updateOrganization(data) {
  return request({
    url: '/memorial/organization',
    method: 'put',
    data: data
  })
}

// 删除机构
export function delOrganization(orgIds) {
  return request({
    url: '/memorial/organization/' + orgIds,
    method: 'delete'
  })
}
