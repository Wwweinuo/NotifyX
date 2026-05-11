import request from './request'

// 获取近期提醒预览
export function getUpcomingReminders() {
  return request.get('/dashboard/upcoming')
}

// 获取仪表盘统计数据
export function getDashboardStats() {
  return request.get('/dashboard/stats')
}
