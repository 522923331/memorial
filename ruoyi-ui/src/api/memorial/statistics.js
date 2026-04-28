import request from '@/utils/request'

// 查询统计列表
export function listStatistics(query) {
  return request({
    url: '/memorial/statistics/list',
    method: 'get',
    params: query
  })
}

// 根据逝者查询统计详细
export function getStatisticsByDeceased(deceasedId) {
  return request({
    url: '/memorial/statistics/deceased/' + deceasedId,
    method: 'get'
  })
}

// 删除统计
export function delStatistics(statIds) {
  return request({
    url: '/memorial/statistics/' + statIds,
    method: 'delete'
  })
}
