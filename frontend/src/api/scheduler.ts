import request from './request'

export interface SchedulerConfig {
  id?: number
  taskName: string
  cronExpression: string
  description: string
  enabled: boolean
  lastExecutionTime?: string
  nextExecutionTime?: string
  updatedAt?: string
}

// 获取定时任务配置列表
export function getSchedulerConfigs() {
  return request.get('/scheduler-config')
}

// 更新定时任务配置
export function updateSchedulerConfig(id: number, data: Partial<SchedulerConfig>) {
  return request.put(`/scheduler-config/${id}`, data)
}
