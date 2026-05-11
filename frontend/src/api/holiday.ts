import request from './request'

export interface Holiday {
  id?: number
  name: string
  type: 'FIXED_SOLAR' | 'FIXED_LUNAR' | 'DYNAMIC_RULE'
  fixedDate?: string
  lunarMonth?: number
  lunarDay?: number
  dynamicRule?: string
  dynamicMonth?: number
  dynamicWeekOrdinal?: number
  dynamicDayOfWeek?: number
  enabled: boolean
  notifyEmail?: string
  createdAt?: string
  updatedAt?: string
}

// 获取节日列表
export function getHolidays() {
  return request.get('/holidays')
}

// 创建节日
export function createHoliday(data: Holiday) {
  return request.post('/holidays', data)
}

// 更新节日
export function updateHoliday(id: number, data: Holiday) {
  return request.put(`/holidays/${id}`, data)
}

// 删除节日
export function deleteHoliday(id: number) {
  return request.delete(`/holidays/${id}`)
}
