import request from './request'

export interface EmailConfig {
  id?: number
  smtpHost: string
  smtpPort: number
  username: string
  password: string
  fromName: string
  defaultToEmail: string
  enabled: boolean
}

// 获取邮件配置
export function getEmailConfig() {
  return request.get('/email-config')
}

// 更新邮件配置
export function updateEmailConfig(data: EmailConfig) {
  return request.put('/email-config', data)
}

// 测试发送邮件
export function testSendEmail(testTo: string) {
  return request.post('/email-config/test', { testTo })
}
