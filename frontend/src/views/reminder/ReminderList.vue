<template>
  <div class="reminder-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">提醒记录</h2>
      <div class="header-actions">
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 140px" @change="handleFilterChange">
          <el-option label="全部" value="" />
          <el-option label="成功" value="SUCCESS" />
          <el-option label="失败" value="FAILED" />
          <el-option label="待发送" value="PENDING" />
        </el-select>
        <el-button type="warning" :loading="triggerLoading" @click="handleTrigger">立即检查提醒</el-button>
      </div>
    </div>

    <!-- 表格区域 -->
    <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
      <el-table-column label="类型" min-width="80">
        <template #default="{ row }">
          <el-tag :type="row.reminderType === 'BIRTHDAY' ? 'primary' : 'success'" size="small">
            {{ row.reminderType === 'BIRTHDAY' ? '生日' : '节日' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="targetName" label="提醒对象" min-width="100" />
      <el-table-column prop="targetDate" label="目标日期" min-width="100" />
      <el-table-column prop="emailTo" label="接收邮箱" min-width="180" />
      <el-table-column prop="emailSubject" label="邮件主题" min-width="180" show-overflow-tooltip />
      <el-table-column label="状态" min-width="80">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)" size="small">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sentAt" label="发送时间" min-width="160" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleViewDetail(row)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 查看详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="提醒详情"
      width="600px"
    >
      <template v-if="currentDetail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="类型">
            <el-tag :type="currentDetail.reminderType === 'BIRTHDAY' ? 'primary' : 'success'" size="small">
              {{ currentDetail.reminderType === 'BIRTHDAY' ? '生日' : '节日' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提醒对象">{{ currentDetail.targetName }}</el-descriptions-item>
          <el-descriptions-item label="目标日期">{{ currentDetail.targetDate }}</el-descriptions-item>
          <el-descriptions-item label="接收邮箱">{{ currentDetail.emailTo }}</el-descriptions-item>
          <el-descriptions-item label="邮件主题">{{ currentDetail.emailSubject }}</el-descriptions-item>
          <el-descriptions-item label="邮件内容">
            <div class="email-content">{{ currentDetail.emailContent }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(currentDetail.status)" size="small">
              {{ getStatusLabel(currentDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentDetail.status === 'FAILED'" label="失败原因">
            <span class="failure-reason">{{ currentDetail.failureReason }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="重试次数">{{ currentDetail.retryCount }}</el-descriptions-item>
          <el-descriptions-item label="发送时间">{{ currentDetail.sentAt || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentDetail.createdAt }}</el-descriptions-item>
        </el-descriptions>
      </template>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getReminders, triggerReminders } from '@/api/reminder'
import type { ReminderRecord } from '@/api/reminder'

// 状态标签
function getStatusTagType(status: string): string {
  const map: Record<string, string> = {
    SUCCESS: 'success',
    FAILED: 'danger',
    PENDING: 'warning',
  }
  return map[status] || 'info'
}

function getStatusLabel(status: string): string {
  const map: Record<string, string> = {
    SUCCESS: '成功',
    FAILED: '失败',
    PENDING: '待发送',
  }
  return map[status] || status
}

// 列表相关
const tableData = ref<ReminderRecord[]>([])
const loading = ref(false)
const triggerLoading = ref(false)
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 详情弹窗
const detailDialogVisible = ref(false)
const currentDetail = ref<ReminderRecord | null>(null)

// 加载列表数据
async function loadData() {
  loading.value = true
  try {
    const params: { page: number; size: number; status?: string } = {
      page: currentPage.value - 1,
      size: pageSize.value,
    }
    if (statusFilter.value) {
      params.status = statusFilter.value
    }
    const res: any = await getReminders(params)
    const data = res.data
    tableData.value = data.content || []
    total.value = data.totalElements || 0
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

// 状态筛选变更
function handleFilterChange() {
  currentPage.value = 1
  loadData()
}

// 分页
function handleSizeChange() {
  currentPage.value = 1
  loadData()
}

function handleCurrentChange() {
  loadData()
}

// 手动触发提醒
async function handleTrigger() {
  triggerLoading.value = true
  try {
    await triggerReminders()
    ElMessage.success('提醒检查已触发')
    loadData()
  } catch {
    // 错误已在拦截器中处理
  } finally {
    triggerLoading.value = false
  }
}

// 查看详情
function handleViewDetail(row: ReminderRecord) {
  currentDetail.value = row
  detailDialogVisible.value = true
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.reminder-page {
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

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.email-content {
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 200px;
  overflow-y: auto;
}

.failure-reason {
  color: #f56c6c;
}
</style>
