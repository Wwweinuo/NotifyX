import request from './request'

// 联系人接口
export interface Contact {
  id?: number
  name: string
  relationship: string
  birthdayType: 'SOLAR' | 'LUNAR'
  birthdayDate?: string  // MM-DD 格式
  lunarMonth?: number
  lunarDay?: number
  isLeapMonth?: boolean
  enabled: boolean
  notifyEmail: string
  remark?: string
  createdAt?: string
  updatedAt?: string
}

export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

// 获取联系人列表（分页）
export function getContacts(params: { page: number; size: number; keyword?: string }) {
  return request.get('/contacts', { params })
}

// 创建联系人
export function createContact(data: Contact) {
  return request.post('/contacts', data)
}

// 更新联系人
export function updateContact(id: number, data: Contact) {
  return request.put(`/contacts/${id}`, data)
}

// 删除联系人
export function deleteContact(id: number) {
  return request.delete(`/contacts/${id}`)
}
