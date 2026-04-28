import request from '@/utils/request'

// 查询献花记录列表
export function listFlower(query) {
  return request({
    url: '/memorial/flower/list',
    method: 'get',
    params: query
  })
}

// 查询献花记录详细
export function getFlower(flowerId) {
  return request({
    url: '/memorial/flower/' + flowerId,
    method: 'get'
  })
}

// 删除献花记录
export function delFlower(flowerIds) {
  return request({
    url: '/memorial/flower/' + flowerIds,
    method: 'delete'
  })
}
