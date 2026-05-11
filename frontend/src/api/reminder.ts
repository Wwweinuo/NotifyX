import request from './request'

export interface ReminderRecord {
  id?: number
  reminderType: 'BIRTHDAY' | 'HOLIDAY'
  targetName: string
  targetDate: string
  emailTo: string
  emailSubject: string
  emailContent: string
  status: 'SUCCESS' | 'FAILED' | 'PENDING'
  failureReason?: string
  retryCount: number
  sentAt?: string
  createdAt?: string
}

// 获取提醒记录列表（分页）
export function getReminders(params: { page: number; size: number; status?: string }) {
  return request.get('/reminders', { params })
}

// 手动触发提醒
export function triggerReminders() {
  return request.post('/reminders/trigger')
}
