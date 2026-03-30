import request from '@/utils/request'

export interface ReplyRule {
  id?: number
  ruleName: string
  ruleType: string
  priority?: number
  matchMode?: string
  matchContent?: string
  messageType?: string
  eventType?: string
  replyType: string
  replyContent?: string
  mediaId?: string
  articleData?: string
  musicData?: string
  isEnabled?: boolean
  hitCount?: number
  effectiveDate?: string
  expireDate?: string
  createTime?: string
  updateTime?: string
}

// 获取回复规则列表
export function getReplyRules(params: any) {
  return request({
    url: '/admin/rules/page',
    method: 'get',
    params
  })
}

// 获取回复规则详情
export function getReplyRule(id: number) {
  return request({
    url: `/admin/rules/${id}`,
    method: 'get'
  })
}

// 创建回复规则
export function createReplyRule(data: ReplyRule) {
  return request({
    url: '/admin/rules',
    method: 'post',
    data
  })
}

// 更新回复规则
export function updateReplyRule(id: number, data: ReplyRule) {
  return request({
    url: `/admin/rules/${id}`,
    method: 'put',
    data
  })
}

// 删除回复规则
export function deleteReplyRule(id: number) {
  return request({
    url: `/admin/rules/${id}`,
    method: 'delete'
  })
}

// 启用/禁用回复规则
export function setRuleEnabled(id: number, enabled: boolean) {
  return request({
    url: `/admin/rules/${id}/enabled`,
    method: 'put',
    params: { enabled }
  })
}

// 测试规则匹配
export function testMatch(params: any) {
  return request({
    url: '/admin/rules/test-match',
    method: 'post',
    params
  })
}
