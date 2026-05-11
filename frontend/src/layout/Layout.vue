<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="layout-aside">
      <div class="logo-section">
        <h1 class="logo-title">NotifyX</h1>
        <p class="logo-subtitle">提醒系统</p>
      </div>
      <el-menu
        :default-active="activeMenu"
        background-color="#1d1e1f"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/contacts">
          <el-icon><User /></el-icon>
          <span>联系人管理</span>
        </el-menu-item>
        <el-menu-item index="/holidays">
          <el-icon><Calendar /></el-icon>
          <span>节日管理</span>
        </el-menu-item>
        <el-menu-item index="/email-config">
          <el-icon><Message /></el-icon>
          <span>邮件配置</span>
        </el-menu-item>
        <el-menu-item index="/scheduler-config">
          <el-icon><Timer /></el-icon>
          <span>定时任务</span>
        </el-menu-item>
        <el-menu-item index="/reminders">
          <el-icon><Bell /></el-icon>
          <span>提醒记录</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container class="layout-main">
      <el-header class="layout-header">
        <span class="header-title">{{ currentTitle }}</span>
      </el-header>
      <el-main class="layout-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  Odometer,
  User,
  Calendar,
  Message,
  Timer,
  Bell,
} from '@element-plus/icons-vue'

const route = useRoute()

const activeMenu = computed(() => route.path)

const currentTitle = computed(() => {
  return (route.meta?.title as string) || 'NotifyX'
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.layout-aside {
  background-color: #1d1e1f;
  overflow-y: auto;
}

.logo-section {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #333;
}

.logo-title {
  color: #fff;
  font-size: 24px;
  font-weight: 700;
  margin: 0;
}

.logo-subtitle {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}

.el-menu {
  border-right: none;
}

.layout-header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  height: 56px;
  padding: 0 20px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.layout-content {
  background-color: #f5f7fa;
  padding: 20px;
}
</style>
