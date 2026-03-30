import request from '@/utils/request'

export interface StatsData {
  date: string
  newUserCount: number
  lostUserCount: number
  totalUserCount: number
  messageCount: number
  replyCount: number
}

// 获取粉丝统计数据
export function getUserStats(params: any) {
  return request({
    url: '/admin/stats/users',
    method: 'get',
    params
  })
}

// 获取消息统计数据
export function getMessageStats(params: any) {
  return request({
    url: '/admin/stats/messages',
    method: 'get',
    params
  })
}

// 获取规则触发统计
export function getRuleStats() {
  return request({
    url: '/admin/stats/rules',
    method: 'get'
  })
}

// 获取概览数据
export function getOverview() {
  return request({
    url: '/admin/stats/overview',
    method: 'get'
  })
}
