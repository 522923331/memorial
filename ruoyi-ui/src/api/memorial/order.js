import request from '@/utils/request'

// 查询订单列表
export function listOrder(query) {
  return request({
    url: '/memorial/order/list',
    method: 'get',
    params: query
  })
}

// 查询订单详细
export function getOrder(orderId) {
  return request({
    url: '/memorial/order/' + orderId,
    method: 'get'
  })
}

// 新增订单
export function addOrder(data) {
  return request({
    url: '/memorial/order',
    method: 'post',
    data: data
  })
}

// 修改订单
export function updateOrder(data) {
  return request({
    url: '/memorial/order',
    method: 'put',
    data: data
  })
}

// 删除订单
export function delOrder(orderIds) {
  return request({
    url: '/memorial/order/' + orderIds,
    method: 'delete'
  })
}
