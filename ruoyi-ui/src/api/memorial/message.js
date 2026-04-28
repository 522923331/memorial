import request from '@/utils/request'

// 查询留言列表
export function listMessage(query) {
  return request({
    url: '/memorial/message/list',
    method: 'get',
    params: query
  })
}

// 查询留言详细
export function getMessage(messageId) {
  return request({
    url: '/memorial/message/' + messageId,
    method: 'get'
  })
}

// 审核留言
export function auditMessage(data) {
  return request({
    url: '/memorial/message/audit',
    method: 'put',
    data: data
  })
}

// 批量审核留言
export function batchAuditMessage(data) {
  return request({
    url: '/memorial/message/batchAudit',
    method: 'put',
    data: data
  })
}

// 删除留言
export function delMessage(messageIds) {
  return request({
    url: '/memorial/message/' + messageIds,
    method: 'delete'
  })
}
