import request from '@/utils/request'

export interface WechatUser {
  id?: number
  openId: string
  nickname?: string
  gender?: number
  country?: string
  province?: string
  city?: string
  headImgUrl?: string
  subscribe?: number
  subscribeTime?: string
  remark?: string
  tagIds?: string
}

// 获取用户列表
export function pageUsers(params: any) {
  return request({
    url: '/admin/users/page',
    method: 'get',
    params
  })
}

// 获取用户详情
export function getUserDetail(id: number) {
  return request({
    url: `/admin/users/${id}`,
    method: 'get'
  })
}

// 更新用户备注
export function updateRemark(id: number, remark: string) {
  return request({
    url: `/admin/users/${id}/remark`,
    method: 'put',
    params: { remark }
  })
}

// 批量打标签
export function batchTag(userIds: number[], tagId: number) {
  return request({
    url: '/admin/users/batch-tag',
    method: 'put',
    data: userIds,
    params: { tagId }
  })
}

// 移除标签
export function removeTag(userId: number, tagId: number) {
  return request({
    url: `/admin/users/${userId}/remove-tag/${tagId}`,
    method: 'put'
  })
}
