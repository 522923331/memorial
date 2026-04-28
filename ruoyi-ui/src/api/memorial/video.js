import request from '@/utils/request'

// 查询视频列表
export function listVideo(query) {
  return request({
    url: '/memorial/video/list',
    method: 'get',
    params: query
  })
}

// 查询视频详细
export function getVideo(videoId) {
  return request({
    url: '/memorial/video/' + videoId,
    method: 'get'
  })
}

// 新增视频
export function addVideo(data) {
  return request({
    url: '/memorial/video',
    method: 'post',
    data: data
  })
}

// 修改视频
export function updateVideo(data) {
  return request({
    url: '/memorial/video',
    method: 'put',
    data: data
  })
}

// 删除视频
export function delVideo(videoIds) {
  return request({
    url: '/memorial/video/' + videoIds,
    method: 'delete'
  })
}
