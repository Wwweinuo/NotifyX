<template>
  <div class="scheduler-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">定时任务配置</h2>
    </div>

    <div v-loading="loading">
      <el-card v-for="item in configList" :key="item.id" class="scheduler-card">
        <el-form label-width="120px" label-position="right">
          <el-form-item label="任务名称">
            <span class="readonly-text">{{ item.taskName }}</span>
          </el-form-item>
          <el-form-item label="任务描述">
            <span class="readonly-text">{{ item.description }}</span>
          </el-form-item>
          <el-form-item label="Cron 表达式">
            <el-input v-model="item.cronExpression" placeholder="请输入 Cron 表达式" style="width: 300px" />
          </el-form-item>
          <el-form-item label="快捷时间">
            <div class="quick-time-area">
              <el-button-group>
                <el-button
                  v-for="preset in timePresets"
                  :key="preset.label"
                  :type="item.cronExpression === preset.cron ? 'primary' : 'default'"
                  size="small"
                  @click="item.cronExpression = preset.cron"
                >
                  {{ preset.label }}
                </el-button>
              </el-button-group>
              <div class="custom-time-picker">
                <el-time-picker
                  v-model="item._customTime"
                  placeholder="自定义时间"
                  format="HH:mm"
                  size="small"
                  style="width: 140px"
                  @change="handleCustomTimeChange(item)"
                />
              </div>
            </div>
          </el-form-item>
          <el-form-item label="启用状态">
            <el-switch v-model="item.enabled" />
          </el-form-item>
          <el-form-item label="上次执行时间">
            <span class="readonly-text">{{ item.lastExecutionTime || '暂无' }}</span>
          </el-form-item>
          <el-form-item label="下次执行时间">
            <span class="readonly-text">{{ item.nextExecutionTime || '暂无' }}</span>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="item._saving" @click="handleSave(item)">保存</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSchedulerConfigs, updateSchedulerConfig } from '@/api/scheduler'
import type { SchedulerConfig } from '@/api/scheduler'

interface SchedulerConfigItem extends SchedulerConfig {
  _customTime?: Date
  _saving?: boolean
}

// 快捷时间预设
const timePresets = [
  { label: '每天 6:00', cron: '0 0 6 * * ?' },
  { label: '每天 8:00', cron: '0 0 8 * * ?' },
  { label: '每天 10:00', cron: '0 0 10 * * ?' },
  { label: '每天 12:00', cron: '0 0 12 * * ?' },
  { label: '每天 20:00', cron: '0 0 20 * * ?' },
]

const loading = ref(false)
const configList = ref<SchedulerConfigItem[]>([])

// 加载配置列表
async function loadData() {
  loading.value = true
  try {
    const res: any = await getSchedulerConfigs()
    const data = res.data || []
    configList.value = data.map((item: SchedulerConfig) => ({
      ...item,
      _customTime: undefined,
      _saving: false,
    }))
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

// 自定义时间选择器变更
function handleCustomTimeChange(item: SchedulerConfigItem) {
  if (item._customTime) {
    const date = new Date(item._customTime)
    const hours = date.getHours()
    const minutes = date.getMinutes()
    item.cronExpression = `0 ${minutes} ${hours} * * ?`
  }
}

// 保存配置
async function handleSave(item: SchedulerConfigItem) {
  item._saving = true
  try {
    await updateSchedulerConfig(item.id!, {
      cronExpression: item.cronExpression,
      enabled: item.enabled,
    })
    ElMessage.success('配置保存成功')
    loadData()
  } catch {
    // 错误已在拦截器中处理
  } finally {
    item._saving = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.scheduler-page {
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

.scheduler-card {
  margin-bottom: 20px;
}

.readonly-text {
  color: #606266;
  font-size: 14px;
}

.quick-time-area {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.custom-time-picker {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
