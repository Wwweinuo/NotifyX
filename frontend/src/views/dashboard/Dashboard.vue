<template>
  <div class="dashboard-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">仪表盘</h2>
    </div>

    <!-- 统计卡片区 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="联系人总数" :value="stats.contactCount">
            <template #prefix>
              <el-icon class="stat-icon" color="#409eff"><User /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="节日总数" :value="stats.holidayCount">
            <template #prefix>
              <el-icon class="stat-icon" color="#67c23a"><Calendar /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="本月已发送提醒" :value="stats.monthSentCount">
            <template #prefix>
              <el-icon class="stat-icon" color="#e6a23c"><Bell /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- 近期提醒预览 -->
    <el-card class="upcoming-card">
      <template #header>
        <span class="card-title">近7天提醒预览</span>
      </template>
      <div v-loading="loading">
        <el-timeline v-if="upcomingList.length > 0">
          <el-timeline-item
            v-for="(item, index) in upcomingList"
            :key="index"
            :timestamp="item.date"
            placement="top"
          >
            <div class="timeline-content">
              <span class="event-name">{{ item.name }}</span>
              <el-tag :type="item.type === 'BIRTHDAY' ? 'primary' : 'success'" size="small" style="margin-left: 8px;">
                {{ item.type === 'BIRTHDAY' ? '生日' : '节日' }}
              </el-tag>
            </div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="近期没有提醒事件" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { User, Calendar, Bell } from '@element-plus/icons-vue'
import { getUpcomingReminders, getDashboardStats } from '@/api/dashboard'

interface UpcomingItem {
  date: string
  name: string
  type: 'BIRTHDAY' | 'HOLIDAY'
}

const loading = ref(false)
const stats = reactive({
  contactCount: 0,
  holidayCount: 0,
  monthSentCount: 0,
})
const upcomingList = ref<UpcomingItem[]>([])

// 加载统计数据
async function loadStats() {
  try {
    const res: any = await getDashboardStats()
    if (res.data) {
      stats.contactCount = res.data.contactCount || 0
      stats.holidayCount = res.data.holidayCount || 0
      stats.monthSentCount = res.data.monthSentCount || 0
    }
  } catch {
    // 错误已在拦截器中处理
  }
}

// 加载近期提醒
async function loadUpcoming() {
  loading.value = true
  try {
    const res: any = await getUpcomingReminders()
    upcomingList.value = res.data || []
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStats()
  loadUpcoming()
})
</script>

<style scoped>
.dashboard-page {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  text-align: center;
}

.stat-icon {
  font-size: 20px;
  margin-right: 4px;
}

.upcoming-card {
  margin-top: 0;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.timeline-content {
  display: flex;
  align-items: center;
}

.event-name {
  font-size: 14px;
  color: #303133;
}
</style>
