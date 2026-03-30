import request from '@/utils/request'

export interface Tag {
  id?: number
  name: string
  description?: string
  color?: string
  sortOrder?: number
}

// 获取标签列表
export function listTags() {
  return request({
    url: '/admin/tags/list',
    method: 'get'
  })
}

// 创建标签
export function createTag(data: Tag) {
  return request({
    url: '/admin/tags',
    method: 'post',
    data
  })
}

// 更新标签
export function updateTag(id: number, data: Tag) {
  return request({
    url: `/admin/tags/${id}`,
    method: 'put',
    data
  })
}

// 删除标签
export function deleteTag(id: number) {
  return request({
    url: `/admin/tags/${id}`,
    method: 'delete'
  })
}
