import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import Layout from '@/layout/Layout.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
        meta: { title: '仪表盘' },
      },
      {
        path: 'contacts',
        name: 'Contacts',
        component: () => import('@/views/contact/ContactList.vue'),
        meta: { title: '联系人管理' },
      },
      {
        path: 'holidays',
        name: 'Holidays',
        component: () => import('@/views/holiday/HolidayList.vue'),
        meta: { title: '节日管理' },
      },
      {
        path: 'email-config',
        name: 'EmailConfig',
        component: () => import('@/views/email/EmailConfig.vue'),
        meta: { title: '邮件配置' },
      },
      {
        path: 'scheduler-config',
        name: 'SchedulerConfig',
        component: () => import('@/views/scheduler/SchedulerConfig.vue'),
        meta: { title: '定时任务配置' },
      },
      {
        path: 'reminders',
        name: 'Reminders',
        component: () => import('@/views/reminder/ReminderList.vue'),
        meta: { title: '提醒记录' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
