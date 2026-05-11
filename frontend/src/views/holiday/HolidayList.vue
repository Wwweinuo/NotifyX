<template>
  <div class="holiday-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">节日管理</h2>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增节日</el-button>
      </div>
    </div>

    <!-- 表格区域 -->
    <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="name" label="节日名称" min-width="120" />
      <el-table-column label="类型" min-width="100">
        <template #default="{ row }">
          <el-tag :type="getTypeTagColor(row.type)" size="small">
            {{ getTypeLabel(row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="日期信息" min-width="150">
        <template #default="{ row }">
          <span v-if="row.type === 'FIXED_SOLAR'">{{ row.fixedDate }}</span>
          <span v-else-if="row.type === 'FIXED_LUNAR'">{{ formatLunarDate(row.lunarMonth, row.lunarDay) }}</span>
          <span v-else>{{ row.dynamicRule }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="notifyEmail" label="接收邮箱" min-width="180" />
      <el-table-column label="状态" min-width="80">
        <template #default="{ row }">
          <el-switch
            v-model="row.enabled"
            @change="handleToggleEnabled(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" link :icon="Delete" :disabled="isBuiltIn(row)" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑节日' : '新增节日'"
      width="600px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
        label-position="right"
      >
        <el-form-item label="节日名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入节日名称" />
        </el-form-item>
        <el-form-item label="节日类型" prop="type">
          <el-radio-group v-model="form.type" @change="handleTypeChange">
            <el-radio value="FIXED_SOLAR">固定公历</el-radio>
            <el-radio value="FIXED_LUNAR">固定农历</el-radio>
            <el-radio value="DYNAMIC_RULE">动态规则</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 固定公历 -->
        <el-form-item v-if="form.type === 'FIXED_SOLAR'" label="公历日期" prop="fixedDate">
          <el-date-picker
            v-model="solarDateValue"
            type="date"
            placeholder="选择日期"
            format="MM-DD"
            value-format="MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <!-- 固定农历 -->
        <template v-if="form.type === 'FIXED_LUNAR'">
          <el-form-item label="农历月份" prop="lunarMonth">
            <el-select v-model="form.lunarMonth" placeholder="请选择月份" style="width: 100%">
              <el-option
                v-for="item in lunarMonthOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="农历日" prop="lunarDay">
            <el-select v-model="form.lunarDay" placeholder="请选择日" style="width: 100%">
              <el-option
                v-for="item in lunarDayOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </template>

        <!-- 动态规则 -->
        <template v-if="form.type === 'DYNAMIC_RULE'">
          <el-form-item label="月份" prop="dynamicMonth">
            <el-select v-model="form.dynamicMonth" placeholder="请选择月份" style="width: 100%">
              <el-option v-for="m in 12" :key="m" :label="`${m}月`" :value="m" />
            </el-select>
          </el-form-item>
          <el-form-item label="第几个星期" prop="dynamicWeekOrdinal">
            <el-select v-model="form.dynamicWeekOrdinal" placeholder="请选择" style="width: 100%">
              <el-option v-for="w in 5" :key="w" :label="`第${w}个`" :value="w" />
            </el-select>
          </el-form-item>
          <el-form-item label="星期几" prop="dynamicDayOfWeek">
            <el-select v-model="form.dynamicDayOfWeek" placeholder="请选择" style="width: 100%">
              <el-option v-for="(item, index) in weekDayOptions" :key="index" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="规则描述">
            <el-input :model-value="dynamicRuleDescription" readonly />
          </el-form-item>
        </template>

        <el-form-item label="接收邮箱" prop="notifyEmail">
          <el-input v-model="form.notifyEmail" placeholder="请输入接收邮箱（可选，不填使用默认）" />
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getHolidays, createHoliday, updateHoliday, deleteHoliday } from '@/api/holiday'
import type { Holiday } from '@/api/holiday'

// 农历月份名称
const lunarMonthNames = ['正月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '腊月']

// 农历日名称
const lunarDayNames = [
  '初一', '初二', '初三', '初四', '初五', '初六', '初七', '初八', '初九', '初十',
  '十一', '十二', '十三', '十四', '十五', '十六', '十七', '十八', '十九', '二十',
  '廿一', '廿二', '廿三', '廿四', '廿五', '廿六', '廿七', '廿八', '廿九', '三十',
]

// 星期选项
const weekDayOptions = [
  { label: '星期一', value: 1 },
  { label: '星期二', value: 2 },
  { label: '星期三', value: 3 },
  { label: '星期四', value: 4 },
  { label: '星期五', value: 5 },
  { label: '星期六', value: 6 },
  { label: '星期日', value: 7 },
]

// 格式化农历日期显示
function formatLunarDate(month?: number, day?: number): string {
  if (!month || !day) return ''
  return `${lunarMonthNames[month - 1]}${lunarDayNames[day - 1]}`
}

// 类型标签
function getTypeLabel(type: string): string {
  const map: Record<string, string> = {
    FIXED_SOLAR: '公历节日',
    FIXED_LUNAR: '农历节日',
    DYNAMIC_RULE: '动态节日',
  }
  return map[type] || type
}

function getTypeTagColor(type: string): string {
  const map: Record<string, string> = {
    FIXED_SOLAR: 'primary',
    FIXED_LUNAR: 'warning',
    DYNAMIC_RULE: 'success',
  }
  return map[type] || 'info'
}

// 判断是否为内置节日（有 id 且为系统初始数据）
function isBuiltIn(row: Holiday): boolean {
  return !!(row.id && row.id <= 20)
}

// 农历月份选项
const lunarMonthOptions = lunarMonthNames.map((label, index) => ({
  label,
  value: index + 1,
}))

// 农历日选项
const lunarDayOptions = lunarDayNames.map((label, index) => ({
  label,
  value: index + 1,
}))

// 列表相关
const tableData = ref<Holiday[]>([])
const loading = ref(false)

// 弹窗相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const editingId = ref<number | undefined>()

// 公历日期值
const solarDateValue = computed({
  get: () => form.fixedDate || '',
  set: (val: string) => {
    form.fixedDate = val
  },
})

// 动态规则描述
const dynamicRuleDescription = computed(() => {
  if (!form.dynamicMonth || !form.dynamicWeekOrdinal || !form.dynamicDayOfWeek) return ''
  const weekDay = weekDayOptions.find(w => w.value === form.dynamicDayOfWeek)?.label || ''
  return `${form.dynamicMonth}月第${form.dynamicWeekOrdinal}个${weekDay}`
})

// 表单数据
const form = reactive<Holiday>({
  name: '',
  type: 'FIXED_SOLAR',
  fixedDate: '',
  lunarMonth: undefined,
  lunarDay: undefined,
  dynamicRule: '',
  dynamicMonth: undefined,
  dynamicWeekOrdinal: undefined,
  dynamicDayOfWeek: undefined,
  enabled: true,
  notifyEmail: '',
})

// 表单验证规则
const formRules = reactive<FormRules>({
  name: [{ required: true, message: '请输入节日名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择节日类型', trigger: 'change' }],
  fixedDate: [{ required: true, message: '请选择公历日期', trigger: 'change' }],
  lunarMonth: [{ required: true, message: '请选择农历月份', trigger: 'change' }],
  lunarDay: [{ required: true, message: '请选择农历日', trigger: 'change' }],
  dynamicMonth: [{ required: true, message: '请选择月份', trigger: 'change' }],
  dynamicWeekOrdinal: [{ required: true, message: '请选择第几个星期', trigger: 'change' }],
  dynamicDayOfWeek: [{ required: true, message: '请选择星期几', trigger: 'change' }],
})

// 加载列表数据
async function loadData() {
  loading.value = true
  try {
    const res: any = await getHolidays()
    tableData.value = res.data || []
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

// 新增
function handleAdd() {
  isEdit.value = false
  editingId.value = undefined
  dialogVisible.value = true
}

// 编辑
function handleEdit(row: Holiday) {
  isEdit.value = true
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    type: row.type,
    fixedDate: row.fixedDate || '',
    lunarMonth: row.lunarMonth,
    lunarDay: row.lunarDay,
    dynamicRule: row.dynamicRule || '',
    dynamicMonth: row.dynamicMonth,
    dynamicWeekOrdinal: row.dynamicWeekOrdinal,
    dynamicDayOfWeek: row.dynamicDayOfWeek,
    enabled: row.enabled,
    notifyEmail: row.notifyEmail || '',
  })
  dialogVisible.value = true
}

// 切换类型时清除其他类型数据
function handleTypeChange() {
  form.fixedDate = ''
  form.lunarMonth = undefined
  form.lunarDay = undefined
  form.dynamicRule = ''
  form.dynamicMonth = undefined
  form.dynamicWeekOrdinal = undefined
  form.dynamicDayOfWeek = undefined
}

// 切换启用状态
async function handleToggleEnabled(row: Holiday) {
  try {
    await updateHoliday(row.id!, { ...row })
    ElMessage.success(row.enabled ? '已启用' : '已禁用')
  } catch {
    row.enabled = !row.enabled
  }
}

// 删除
function handleDelete(row: Holiday) {
  ElMessageBox.confirm(`确定要删除节日"${row.name}"吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await deleteHoliday(row.id!)
      ElMessage.success('删除成功')
      loadData()
    } catch {
      // 错误已在拦截器中处理
    }
  }).catch(() => {
    // 用户取消
  })
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const submitData: Holiday = {
        name: form.name,
        type: form.type,
        fixedDate: form.type === 'FIXED_SOLAR' ? form.fixedDate : undefined,
        lunarMonth: form.type === 'FIXED_LUNAR' ? form.lunarMonth : undefined,
        lunarDay: form.type === 'FIXED_LUNAR' ? form.lunarDay : undefined,
        dynamicRule: form.type === 'DYNAMIC_RULE' ? dynamicRuleDescription.value : undefined,
        dynamicMonth: form.type === 'DYNAMIC_RULE' ? form.dynamicMonth : undefined,
        dynamicWeekOrdinal: form.type === 'DYNAMIC_RULE' ? form.dynamicWeekOrdinal : undefined,
        dynamicDayOfWeek: form.type === 'DYNAMIC_RULE' ? form.dynamicDayOfWeek : undefined,
        enabled: form.enabled,
        notifyEmail: form.notifyEmail || undefined,
      }
      if (isEdit.value && editingId.value) {
        await updateHoliday(editingId.value, submitData)
        ElMessage.success('更新成功')
      } else {
        await createHoliday(submitData)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadData()
    } catch {
      // 错误已在拦截器中处理
    } finally {
      submitLoading.value = false
    }
  })
}

// 重置表单
function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, {
    name: '',
    type: 'FIXED_SOLAR',
    fixedDate: '',
    lunarMonth: undefined,
    lunarDay: undefined,
    dynamicRule: '',
    dynamicMonth: undefined,
    dynamicWeekOrdinal: undefined,
    dynamicDayOfWeek: undefined,
    enabled: true,
    notifyEmail: '',
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.holiday-page {
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
</style>
